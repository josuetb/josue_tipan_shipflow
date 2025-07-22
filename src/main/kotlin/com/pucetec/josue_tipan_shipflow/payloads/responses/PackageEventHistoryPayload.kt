package com.pucetec.josue_tipan_shipflow.payloads.responses

import com.pucetec.josue_tipan_shipflow.models.entities.Status
import java.time.LocalDateTime

data class PackageEventHistoryPayload(
    val id: Long,
    val status: Status,
    val comment: String?,
    val createdAt: LocalDateTime
) 