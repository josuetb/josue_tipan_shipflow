package com.pucetec.josue_tipan_shipflow.routes

object Routes {
    const val BASE_URL = "/shipflow/api"
    const val PACKAGES = "/packages"
    const val TRACKING_ID = "/{trackingId}"
    const val PACKAGE_HISTORY = "/{trackingId}/history"
    const val PACKAGE_STATUS = "/{trackingId}/status"
} 