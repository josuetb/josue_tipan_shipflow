package com.pucetec.josue_tipan_shipflow.payloads.responses

import com.pucetec.josue_tipan_shipflow.models.entities.Status
import com.pucetec.josue_tipan_shipflow.models.entities.Type
import java.time.LocalDateTime

data class PackageSimpleResponse(
    val id: Long,
    val trackingId: String,
    val type: Type,
    val description: String,
    val weight: Float,
    val currentStatus: Status,
    val cityFrom: String,
    val cityTo: String,
    val createdAt: LocalDateTime,
    val estimatedDeliveryDate: LocalDateTime
) 