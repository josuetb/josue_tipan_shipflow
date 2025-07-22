package com.pucetec.josue_tipan_shipflow.services.impl

import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageRegisterPayload
import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageStatusUpdatePayload
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageRegisterResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageFullDetailResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageSimpleResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.StatusUpdateResult
import com.pucetec.josue_tipan_shipflow.exceptions.*
import com.pucetec.josue_tipan_shipflow.mappers.PackageEventMapper
import com.pucetec.josue_tipan_shipflow.mappers.PackageMapper
import com.pucetec.josue_tipan_shipflow.models.entities.Status
import com.pucetec.josue_tipan_shipflow.repositories.PackageEventRepository
import com.pucetec.josue_tipan_shipflow.repositories.PackageRepository
import com.pucetec.josue_tipan_shipflow.services.PackageService
import com.pucetec.josue_tipan_shipflow.services.StatusValidationService
import com.pucetec.josue_tipan_shipflow.services.TrackingService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PackageServiceImpl(
    private val packageRepository: PackageRepository,
    private val packageEventRepository: PackageEventRepository,
    private val trackingService: TrackingService,
    private val statusValidationService: StatusValidationService,
    private val packageMapper: PackageMapper,
    private val packageEventMapper: PackageEventMapper
) : PackageService {

    override fun createPackage(request: PackageRegisterPayload): PackageRegisterResponse {
        if (request.cityFrom.equals(request.cityTo, ignoreCase = true)) {
            throw InvalidCityException("Origin city cannot be the same as destination city")
        }

        if (request.description.length > 50) {
            throw DescriptionTooLongException("Description exceeds maximum length of 50 characters. Current length: ${request.description.length}")
        }

        val trackingId = trackingService.generateTrackingId()

        val packageEntity = packageMapper.toEntity(request, trackingId)

        val savedPackage = packageRepository.save(packageEntity)

        val initialEvent = packageEventMapper.toEntity(
            PackageStatusUpdatePayload(Status.PENDING, "Package registered and pending processing"),
            savedPackage
        )
        packageEventRepository.save(initialEvent)

        return packageMapper.toCreateResponse(savedPackage)
    }

    @Transactional(readOnly = true)
    override fun getAllPackages(): List<PackageSimpleResponse> {
        return packageRepository.findAll().map { packageMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getPackageByTrackingId(trackingId: String): PackageSimpleResponse {
        val packageEntity = packageRepository.findByTrackingId(trackingId)
            .orElseThrow { PackageNotFoundException("Package with tracking ID '$trackingId' not found") }

        return packageMapper.toResponse(packageEntity)
    }

    @Transactional(readOnly = true)
    override fun getPackageWithHistory(trackingId: String): PackageFullDetailResponse {
        val packageEntity = packageRepository.findByTrackingId(trackingId)
            .orElseThrow { PackageNotFoundException("Package with tracking ID '$trackingId' not found") }

        return packageMapper.toDetailResponse(packageEntity, packageEventMapper)
    }

    override fun updatePackageStatus(trackingId: String, request: PackageStatusUpdatePayload): StatusUpdateResult {
        val packageEntity = packageRepository.findByTrackingId(trackingId)
            .orElseThrow { PackageNotFoundException("Package with tracking ID '$trackingId' not found") }

        if (!statusValidationService.isValidTransition(packageEntity.currentStatus, request.status)) {
            throw InvalidStatusTransitionException(
                "Invalid status transition from ${packageEntity.currentStatus} to ${request.status}"
            )
        }

        if (request.status == Status.DELIVERED) {
            val hasBeenInTransit = packageEntity.events.any { it.status == Status.IN_TRANSIT }
            if (!hasBeenInTransit) {
                throw BusinessRuleException("Package can only be marked as DELIVERED if it has been IN_TRANSIT previously")
            }
        }

        packageEntity.currentStatus = request.status
        packageRepository.save(packageEntity)

        val statusEvent = packageEventMapper.toEntity(request, packageEntity)
        packageEventRepository.save(statusEvent)

        return StatusUpdateResult(
            message = "Package status updated successfully",
            trackingId = trackingId,
            newStatus = request.status.name,
            updatedAt = LocalDateTime.now()
        )
    }
} 