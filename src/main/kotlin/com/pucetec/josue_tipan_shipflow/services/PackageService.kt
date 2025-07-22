package com.pucetec.josue_tipan_shipflow.services

import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageRegisterPayload
import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageStatusUpdatePayload
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageRegisterResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageFullDetailResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageSimpleResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.StatusUpdateResult

interface PackageService {
    fun createPackage(request: PackageRegisterPayload): PackageRegisterResponse
    fun getAllPackages(): List<PackageSimpleResponse>
    fun getPackageByTrackingId(trackingId: String): PackageSimpleResponse
    fun getPackageWithHistory(trackingId: String): PackageFullDetailResponse
    fun updatePackageStatus(trackingId: String, request: PackageStatusUpdatePayload): StatusUpdateResult
} 