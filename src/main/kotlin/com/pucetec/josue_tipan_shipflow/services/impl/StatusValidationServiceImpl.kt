package com.pucetec.josue_tipan_shipflow.services.impl

import com.pucetec.josue_tipan_shipflow.models.entities.Status
import com.pucetec.josue_tipan_shipflow.services.StatusValidationService
import org.springframework.stereotype.Service

@Service
class StatusValidationServiceImpl : StatusValidationService {

    private val validTransitions = mapOf(
        Status.PENDING to listOf(Status.IN_TRANSIT),
        Status.IN_TRANSIT to listOf(Status.DELIVERED, Status.ON_HOLD, Status.CANCELLED),
        Status.ON_HOLD to listOf(Status.IN_TRANSIT, Status.CANCELLED),
        Status.DELIVERED to emptyList(),
        Status.CANCELLED to emptyList()
    )

    override fun isValidTransition(currentStatus: Status, newStatus: Status): Boolean {
        return validTransitions[currentStatus]?.contains(newStatus) ?: false
    }

} 