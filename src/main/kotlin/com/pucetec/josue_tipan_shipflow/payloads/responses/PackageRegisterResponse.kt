package com.pucetec.josue_tipan_shipflow.payloads.responses

data class PackageRegisterResponse(
    val message: String,
    val trackingId: String,
    val packageInfo: PackageSimpleResponse
) 