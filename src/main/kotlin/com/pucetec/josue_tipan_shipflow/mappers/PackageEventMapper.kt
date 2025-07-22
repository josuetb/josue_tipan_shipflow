package com.pucetec.josue_tipan_shipflow.mappers

import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageStatusUpdatePayload
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageEventHistoryPayload
import com.pucetec.josue_tipan_shipflow.models.entities.Package
import com.pucetec.josue_tipan_shipflow.models.entities.PackageEvent
import org.springframework.stereotype.Component

@Component
class PackageEventMapper {

    fun toEntity(request: PackageStatusUpdatePayload, packageEntity: Package): PackageEvent {
        return PackageEvent(
            status = request.status,
            comment = request.comment,
            packageEntity = packageEntity
        )
    }

    fun toResponse(entity: PackageEvent): PackageEventHistoryPayload {
        return PackageEventHistoryPayload(
            id = entity.id,
            status = entity.status,
            comment = entity.comment,
            createdAt = entity.createdAt
        )
    }
} 