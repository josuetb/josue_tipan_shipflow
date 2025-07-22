package com.pucetec.josue_tipan_shipflow.payloads.responses

import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageEventHistoryPayload

data class PackageFullDetailResponse(
    val packageInfo: PackageSimpleResponse,
    val statusHistory: List<PackageEventHistoryPayload>
) 