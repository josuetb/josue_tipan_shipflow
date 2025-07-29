package com.pucetec.josue_tipan_shipflow.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.pucetec.josue_tipan_shipflow.models.entities.Type
import com.pucetec.josue_tipan_shipflow.models.entities.Status
import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageRegisterPayload
import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageStatusUpdatePayload
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageRegisterResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageSimpleResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageFullDetailResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.StatusUpdateResult
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageEventHistoryPayload
import com.pucetec.josue_tipan_shipflow.services.PackageService
import com.pucetec.josue_tipan_shipflow.exceptions.InvalidCityException
import com.pucetec.josue_tipan_shipflow.exceptions.PackageNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(PackageController::class)
class PackageControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var packageService: PackageService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should create package and return 201`() {
        val payload = PackageRegisterPayload(
            type = Type.DOCUMENT,
            weight = 1.0f,
            description = "desc",
            cityFrom = "Quito",
            cityTo = "Guayaquil"
        )

        val packageInfo = PackageSimpleResponse(
            id = 1L,
            trackingId = "PKG123",
            type = Type.DOCUMENT,
            description = "desc",
            weight = 1.0f,
            currentStatus = Status.PENDING,
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            createdAt = LocalDateTime.now(),
            estimatedDeliveryDate = LocalDateTime.now().plusDays(5)
        )

        val response = PackageRegisterResponse(
            message = "Package created",
            trackingId = "PKG123",
            packageInfo = packageInfo
        )

        whenever(packageService.createPackage(any())).thenReturn(response)

        mockMvc.perform(
            post("/shipflow/api/packages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.trackingId").value("PKG123"))
    }

    @Test
    fun `should return 400 when cities are the same`() {
        val payload = PackageRegisterPayload(
            type = Type.DOCUMENT,
            weight = 1.0f,
            description = "desc",
            cityFrom = "Quito",
            cityTo = "Quito" // mismas ciudades
        )

        whenever(packageService.createPackage(any())).thenThrow(InvalidCityException("Las ciudades no pueden ser iguales"))

        mockMvc.perform(
            post("/shipflow/api/packages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when payload is invalid`() {
        val invalidPayload = PackageRegisterPayload(
            type = Type.DOCUMENT,
            weight = -5.0f, // peso inválido
            description = "",
            cityFrom = "",
            cityTo = ""
        )

        whenever(packageService.createPackage(any())).thenThrow(IllegalArgumentException("Invalid payload"))

        mockMvc.perform(
            post("/shipflow/api/packages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayload))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should get all packages successfully`() {
        val packages = listOf(
            PackageSimpleResponse(
                id = 1L,
                trackingId = "PKG123",
                type = Type.DOCUMENT,
                description = "desc1",
                weight = 1.0f,
                currentStatus = Status.PENDING,
                cityFrom = "Quito",
                cityTo = "Guayaquil",
                createdAt = LocalDateTime.now(),
                estimatedDeliveryDate = LocalDateTime.now().plusDays(5)
            ),
            PackageSimpleResponse(
                id = 2L,
                trackingId = "PKG456",
                type = Type.FRAGILE,
                description = "desc2",
                weight = 2.5f,
                currentStatus = Status.IN_TRANSIT,
                cityFrom = "Guayaquil",
                cityTo = "Cuenca",
                createdAt = LocalDateTime.now(),
                estimatedDeliveryDate = LocalDateTime.now().plusDays(5)
            )
        )

        whenever(packageService.getAllPackages()).thenReturn(packages)

        mockMvc.perform(get("/shipflow/api/packages"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].trackingId").value("PKG123"))
            .andExpect(jsonPath("$[1].trackingId").value("PKG456"))
            .andExpect(jsonPath("$[0].type").value("DOCUMENT"))
            .andExpect(jsonPath("$[1].type").value("FRAGILE"))
    }

    @Test
    fun `should get package by tracking id successfully`() {
        val trackingId = "PKG123"
        val packageResponse = PackageSimpleResponse(
            id = 1L,
            trackingId = trackingId,
            type = Type.DOCUMENT,
            description = "desc",
            weight = 1.0f,
            currentStatus = Status.PENDING,
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            createdAt = LocalDateTime.now(),
            estimatedDeliveryDate = LocalDateTime.now().plusDays(5)
        )

        whenever(packageService.getPackageByTrackingId(trackingId)).thenReturn(packageResponse)

        mockMvc.perform(get("/shipflow/api/packages/$trackingId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.trackingId").value(trackingId))
            .andExpect(jsonPath("$.type").value("DOCUMENT"))
            .andExpect(jsonPath("$.currentStatus").value("PENDING"))
    }

    @Test
    fun `should return 404 when package not found by tracking id`() {
        val trackingId = "PKG999"

        whenever(packageService.getPackageByTrackingId(trackingId)).thenThrow(PackageNotFoundException("Package not found"))

        mockMvc.perform(get("/shipflow/api/packages/$trackingId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should get package history successfully`() {
        val trackingId = "PKG123"
        val packageInfo = PackageSimpleResponse(
            id = 1L,
            trackingId = trackingId,
            type = Type.DOCUMENT,
            description = "desc",
            weight = 1.0f,
            currentStatus = Status.IN_TRANSIT,
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            createdAt = LocalDateTime.now(),
            estimatedDeliveryDate = LocalDateTime.now().plusDays(5)
        )
        
        val packageHistory = PackageFullDetailResponse(
            packageInfo = packageInfo,
            statusHistory = listOf(
                PackageEventHistoryPayload(
                    id = 1L,
                    status = Status.PENDING,
                    comment = "Package registered",
                    createdAt = LocalDateTime.now()
                ),
                PackageEventHistoryPayload(
                    id = 2L,
                    status = Status.IN_TRANSIT,
                    comment = "Package picked up",
                    createdAt = LocalDateTime.now()
                )
            )
        )

        whenever(packageService.getPackageWithHistory(trackingId)).thenReturn(packageHistory)

        mockMvc.perform(get("/shipflow/api/packages/$trackingId/history"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.packageInfo.trackingId").value(trackingId))
            .andExpect(jsonPath("$.statusHistory").isArray())
            .andExpect(jsonPath("$.statusHistory.length()").value(2))
    }

    @Test
    fun `should return 404 when package history not found`() {
        val trackingId = "PKG999"

        whenever(packageService.getPackageWithHistory(trackingId)).thenThrow(PackageNotFoundException("Package not found"))

        mockMvc.perform(get("/shipflow/api/packages/$trackingId/history"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should update package status successfully`() {
        val trackingId = "PKG123"
        val updatePayload = PackageStatusUpdatePayload(
            status = Status.IN_TRANSIT,
            comment = "Package picked up"
        )

        val updateResult = StatusUpdateResult(
            message = "Package status updated successfully",
            trackingId = trackingId,
            newStatus = "IN_TRANSIT",
            updatedAt = LocalDateTime.now()
        )

        whenever(packageService.updatePackageStatus(eq(trackingId), any())).thenReturn(updateResult)

        mockMvc.perform(
            put("/shipflow/api/packages/$trackingId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.trackingId").value(trackingId))
            .andExpect(jsonPath("$.newStatus").value("IN_TRANSIT"))
            .andExpect(jsonPath("$.message").value("Package status updated successfully"))
    }

    @Test
    fun `should return 400 when status update fails`() {
        val trackingId = "PKG123"
        val updatePayload = PackageStatusUpdatePayload(
            status = Status.DELIVERED,
            comment = "Invalid status transition"
        )

        whenever(packageService.updatePackageStatus(eq(trackingId), any())).thenThrow(IllegalArgumentException("Invalid status transition"))

        mockMvc.perform(
            put("/shipflow/api/packages/$trackingId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should create package with different type successfully`() {
        val payload = PackageRegisterPayload(
            type = Type.FRAGILE,
            weight = 2.5f,
            description = "Vidrio frágil",
            cityFrom = "Guayaquil",
            cityTo = "Cuenca"
        )

        val packageInfo = PackageSimpleResponse(
            id = 2L,
            trackingId = "PKG456",
            type = Type.FRAGILE,
            description = "Vidrio frágil",
            weight = 2.5f,
            currentStatus = Status.PENDING,
            cityFrom = "Guayaquil",
            cityTo = "Cuenca",
            createdAt = LocalDateTime.now(),
            estimatedDeliveryDate = LocalDateTime.now().plusDays(5)
        )

        val response = PackageRegisterResponse(
            message = "Package created successfully",
            trackingId = "PKG456",
            packageInfo = packageInfo
        )

        whenever(packageService.createPackage(any())).thenReturn(response)

        mockMvc.perform(
            post("/shipflow/api/packages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.trackingId").value("PKG456"))
            .andExpect(jsonPath("$.packageInfo.type").value("FRAGILE"))
            .andExpect(jsonPath("$.packageInfo.weight").value(2.5))
    }
}
