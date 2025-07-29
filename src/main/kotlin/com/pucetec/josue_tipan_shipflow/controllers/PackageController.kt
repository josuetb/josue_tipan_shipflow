package com.pucetec.josue_tipan_shipflow.controllers

import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageRegisterPayload
import com.pucetec.josue_tipan_shipflow.payloads.requests.PackageStatusUpdatePayload
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageRegisterResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageFullDetailResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.PackageSimpleResponse
import com.pucetec.josue_tipan_shipflow.payloads.responses.StatusUpdateResult
import com.pucetec.josue_tipan_shipflow.services.PackageService
import com.pucetec.josue_tipan_shipflow.routes.Routes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.BASE_URL + Routes.PACKAGES)
class PackageController(
    private val packageService: PackageService
) {
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): Map<String, String> =
        mapOf("error" to (ex.message ?: "Bad request"))

    @PostMapping
    fun createPackage(@RequestBody request: PackageRegisterPayload): ResponseEntity<PackageRegisterResponse> {
        val response = packageService.createPackage(request)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @GetMapping
    fun getAllPackages(): ResponseEntity<List<PackageSimpleResponse>> {
        val packages = packageService.getAllPackages()
        return ResponseEntity(packages, HttpStatus.OK)
    }

    @GetMapping(Routes.TRACKING_ID)
    fun getPackageByTrackingId(@PathVariable trackingId: String): ResponseEntity<PackageSimpleResponse> {
        val packageResponse = packageService.getPackageByTrackingId(trackingId)
        return ResponseEntity(packageResponse, HttpStatus.OK)
    }

    @GetMapping(Routes.PACKAGE_HISTORY)
    fun getPackageWithHistory(@PathVariable trackingId: String): ResponseEntity<PackageFullDetailResponse> {
        val packageDetail = packageService.getPackageWithHistory(trackingId)
        return ResponseEntity(packageDetail, HttpStatus.OK)
    }

    @PutMapping(Routes.PACKAGE_STATUS)
    fun updatePackageStatus(
        @PathVariable trackingId: String,
        @RequestBody request: PackageStatusUpdatePayload
    ): ResponseEntity<StatusUpdateResult> {
        val response = packageService.updatePackageStatus(trackingId, request)
        return ResponseEntity(response, HttpStatus.OK)
    }
} 