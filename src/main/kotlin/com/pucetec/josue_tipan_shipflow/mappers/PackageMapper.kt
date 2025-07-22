package com.pucetec.josue_tipan_shipflow.mappers

import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageRegisterPayload
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageRegisterResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageFullDetailResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageSimpleResponse
import com.pucetec.josue_tipan_shipflow.models.entities.Package
import com.pucetec.josue_tipan_shipflow.models.entities.Status
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PackageMapper {

    fun toEntity(request: PackageRegisterPayload, trackingId: String): Package {
        return Package(
            trackingId = trackingId,
            type = request.type,
            weight = request.weight,
            description = request.description,
            cityFrom = request.cityFrom,
            cityTo = request.cityTo,
            estimatedDeliveryDate = LocalDateTime.now().plusDays(5)
        )
    }

    fun toResponse(entity: Package): PackageSimpleResponse {
        return PackageSimpleResponse(
            id = entity.id,
            trackingId = entity.trackingId,
            type = entity.type,
            description = entity.description,
            weight = entity.weight,
            currentStatus = entity.currentStatus,
            cityFrom = entity.cityFrom,
            cityTo = entity.cityTo,
            createdAt = entity.createdAt,
            estimatedDeliveryDate = entity.estimatedDeliveryDate
        )
    }

    fun toCreateResponse(entity: Package): PackageRegisterResponse {
        return PackageRegisterResponse(
            message = "Package created successfully",
            trackingId = entity.trackingId,
            packageInfo = toResponse(entity)
        )
    }

    fun toDetailResponse(entity: Package, eventMapper: PackageEventMapper): PackageFullDetailResponse {
        return PackageFullDetailResponse(
            packageInfo = toResponse(entity),
            statusHistory = entity.events.map { eventMapper.toResponse(it) }
        )
    }
} 