package com.pucetec.josue_tipan_shipflow.payloads.requests

import com.pucetec.josue_tipan_shipflow.models.entities.Type

data class PackageRegisterPayload(
    val type: Type,
    val weight: Float,
    val description: String,
    val cityFrom: String,
    val cityTo: String
) 