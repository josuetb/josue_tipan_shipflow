package com.pucetec.josue_tipan_shipflow.services.impl

import org.springframework.boot.test.mock.mockito.MockBean
import com.pucetec.josue_tipan_shipflow.models.entities.Type
import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageRegisterPayload
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageRegisterResponse
import com.pucetec.josue_tipan_shipflow.repositories.PackageRepository
import com.pucetec.josue_tipan_shipflow.repositories.PackageEventRepository
import com.pucetec.josue_tipan_shipflow.services.TrackingService
import com.pucetec.josue_tipan_shipflow.services.StatusValidationService
import com.pucetec.josue_tipan_shipflow.mappers.PackageMapper
import com.pucetec.josue_tipan_shipflow.mappers.PackageEventMapper
import com.pucetec.josue_tipan_shipflow.services.impl.PackageServiceImpl
import com.pucetec.josue_tipan_shipflow.exceptions.InvalidCityException
import com.pucetec.josue_tipan_shipflow.exceptions.DescriptionTooLongException
import com.pucetec.josue_tipan_shipflow.exceptions.PackageNotFoundException
import com.pucetec.josue_tipan_shipflow.exceptions.InvalidStatusTransitionException
import com.pucetec.josue_tipan_shipflow.exceptions.BusinessRuleException
import com.pucetec.josue_tipan_shipflow.models.entities.Status
import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageStatusUpdatePayload
import com.pucetec.josue_tipan_shipflow.payloads.responses.StatusUpdateResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

class PackageServiceImplTest {

    private val packageRepository: PackageRepository = mock()
    private val packageEventRepository: PackageEventRepository = mock()
    private val trackingService: TrackingService = mock()
    private val statusValidationService: StatusValidationService = mock()
    private val packageMapper: PackageMapper = mock()
    private val packageEventMapper: PackageEventMapper = mock()

    private val service = PackageServiceImpl(
        packageRepository,
        packageEventRepository,
        trackingService,
        statusValidationService,
        packageMapper,
        packageEventMapper
    )

    @Test
    fun `should register package successfully`() {
        val payload = PackageRegisterPayload(Type.DOCUMENT, 1.0f, "desc", "Quito", "Guayaquil")
        val trackingId = "PKG123"
        val entity = mock<com.pucetec.josue_tipan_shipflow.models.entities.Package>()
        val response = mock<PackageRegisterResponse>()
        val eventEntity = mock<com.pucetec.josue_tipan_shipflow.models.entities.PackageEvent>()

        whenever(trackingService.generateTrackingId()).thenReturn(trackingId)
        whenever(packageMapper.toEntity(payload, trackingId)).thenReturn(entity)
        whenever(packageRepository.save(entity)).thenReturn(entity)
        whenever(packageMapper.toCreateResponse(entity)).thenReturn(response)
        whenever(packageEventMapper.toEntity(any(), any())).thenReturn(eventEntity)

        val result = service.createPackage(payload)

        assertEquals(response, result)
        verify(packageEventRepository).save(any())
    }

    @Test
    fun `should get all packages`() {
        val entity = mock<com.pucetec.josue_tipan_shipflow.models.entities.Package>()
        val response = mock<com.pucetec.josue_tipan_shipflow.payloads.responses.PackageSimpleResponse>()
        whenever(packageRepository.findAll()).thenReturn(listOf(entity))
        whenever(packageMapper.toResponse(entity)).thenReturn(response)
        val result = service.getAllPackages()
        assertEquals(listOf(response), result)
    }

    @Test
    fun `should get package by trackingId`() {
        val trackingId = "PKG123"
        val entity = mock<com.pucetec.josue_tipan_shipflow.models.entities.Package>()
        val response = mock<com.pucetec.josue_tipan_shipflow.payloads.responses.PackageSimpleResponse>()
        whenever(packageRepository.findByTrackingId(trackingId)).thenReturn(java.util.Optional.of(entity))
        whenever(packageMapper.toResponse(entity)).thenReturn(response)
        val result = service.getPackageByTrackingId(trackingId)
        assertEquals(response, result)
    }

    @Test
    fun `should throw error if package not found by trackingId`() {
        val trackingId = "PKG999"
        whenever(packageRepository.findByTrackingId(trackingId)).thenReturn(java.util.Optional.empty())
        assertThrows<PackageNotFoundException> {
            service.getPackageByTrackingId(trackingId)
        }
    }

    @Test
    fun `should get package with history`() {
        val trackingId = "PKG123"
        val entity = mock<com.pucetec.josue_tipan_shipflow.models.entities.Package>()
        val response = mock<com.pucetec.josue_tipan_shipflow.payloads.responses.PackageFullDetailResponse>()
        whenever(packageRepository.findByTrackingId(trackingId)).thenReturn(java.util.Optional.of(entity))
        whenever(packageMapper.toDetailResponse(entity, packageEventMapper)).thenReturn(response)
        val result = service.getPackageWithHistory(trackingId)
        assertEquals(response, result)
    }

    @Test
    fun `should throw error if package with history not found`() {
        val trackingId = "PKG999"
        whenever(packageRepository.findByTrackingId(trackingId)).thenReturn(java.util.Optional.empty())
        assertThrows<PackageNotFoundException> {
            service.getPackageWithHistory(trackingId)
        }
    }

    @Test
    fun `should update package status successfully`() {
        val trackingId = "PKG123"
        val request = PackageStatusUpdatePayload(Status.IN_TRANSIT, "En tránsito")
        val entity = mock<com.pucetec.josue_tipan_shipflow.models.entities.Package>()
        val eventEntity = mock<com.pucetec.josue_tipan_shipflow.models.entities.PackageEvent>()
        val resultResponse = StatusUpdateResult("OK", trackingId, "IN_TRANSIT", java.time.LocalDateTime.now())

        whenever(packageRepository.findByTrackingId(trackingId)).thenReturn(java.util.Optional.of(entity))
        whenever(statusValidationService.isValidTransition(any(), eq(request.status))).thenReturn(true)
        whenever(packageEventMapper.toEntity(request, entity)).thenReturn(eventEntity)
        whenever(packageRepository.save(entity)).thenReturn(entity)
        whenever(packageEventRepository.save(eventEntity)).thenReturn(eventEntity)

        // Simulate previous events for DELIVERED if necessary
        whenever(entity.events).thenReturn(mutableListOf())
        whenever(entity.currentStatus).thenReturn(Status.PENDING)

        val result = service.updatePackageStatus(trackingId, request)
        assertEquals("IN_TRANSIT", result.newStatus)
    }

    @Test
    fun `should throw error if package not found when updating status`() {
        val trackingId = "PKG999"
        val request = PackageStatusUpdatePayload(Status.IN_TRANSIT, "En tránsito")
        whenever(packageRepository.findByTrackingId(trackingId)).thenReturn(java.util.Optional.empty())
        assertThrows<PackageNotFoundException> {
            service.updatePackageStatus(trackingId, request)
        }
    }

    @Test
    fun `should throw error if status transition is invalid`() {
        val trackingId = "PKG123"
        val request = PackageStatusUpdatePayload(Status.IN_TRANSIT, "En tránsito")
        val entity = mock<com.pucetec.josue_tipan_shipflow.models.entities.Package>()
        whenever(packageRepository.findByTrackingId(trackingId)).thenReturn(java.util.Optional.of(entity))
        whenever(statusValidationService.isValidTransition(any(), eq(request.status))).thenReturn(false)
        assertThrows<InvalidStatusTransitionException> {
            service.updatePackageStatus(trackingId, request)
        }
    }

    @Test
    fun `should throw error if marked as DELIVERED without being IN_TRANSIT`() {
        val trackingId = "PKG123"
        val request = PackageStatusUpdatePayload(Status.DELIVERED, "Entregado")
        val entity = mock<com.pucetec.josue_tipan_shipflow.models.entities.Package>()
        whenever(packageRepository.findByTrackingId(trackingId)).thenReturn(java.util.Optional.of(entity))
        whenever(statusValidationService.isValidTransition(any(), eq(request.status))).thenReturn(true)
        whenever(entity.events).thenReturn(mutableListOf())
        whenever(entity.currentStatus).thenReturn(Status.PENDING)
        assertThrows<BusinessRuleException> {
            service.updatePackageStatus(trackingId, request)
        }
    }

    @Test
    fun `should throw error if cities are the same`() {
        val payload = PackageRegisterPayload(Type.DOCUMENT, 1.0f, "desc", "Quito", "Quito")
        assertThrows<InvalidCityException> {
            service.createPackage(payload)
        }
    }

    @Test
    fun `should throw error if description is too long`() {
        val payload = PackageRegisterPayload(Type.DOCUMENT, 1.0f, "a".repeat(51), "Quito", "Guayaquil")
        assertThrows<DescriptionTooLongException> {
            service.createPackage(payload)
        }
    }
} 