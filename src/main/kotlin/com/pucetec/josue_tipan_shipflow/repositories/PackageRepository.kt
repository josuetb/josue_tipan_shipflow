package com.pucetec.josue_tipan_shipflow.repositories

import com.pucetec.josue_tipan_shipflow.models.entities.Package
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PackageRepository : JpaRepository<Package, Long> {
    fun findByTrackingId(trackingId: String): Optional<Package>
    fun existsByTrackingId(trackingId: String): Boolean
} 