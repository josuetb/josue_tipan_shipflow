package com.pucetec.josue_tipan_shipflow.payloads.responses

import java.time.LocalDateTime

data class StatusUpdateResult(
    val message: String,
    val trackingId: String,
    val newStatus: String,
    val updatedAt: LocalDateTime
) 