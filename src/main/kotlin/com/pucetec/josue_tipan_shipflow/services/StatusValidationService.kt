package com.pucetec.josue_tipan_shipflow.services

import com.pucetec.josue_tipan_shipflow.models.entities.Status

interface StatusValidationService {
    fun isValidTransition(currentStatus: Status, newStatus: Status): Boolean
} 