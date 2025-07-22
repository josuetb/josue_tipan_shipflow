package com.pucetec.josue_tipan_shipflow.payloads.requests

import com.pucetec.josue_tipan_shipflow.models.entities.Status

data class PackageStatusUpdatePayload(
    val status: Status,
    val comment: String? = null
) 