package com.pucetec.josue_tipan_shipflow.models.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "packages")
class Package(
    @Column(name = "tracking_id", unique = true, nullable = false)
    val trackingId: String,

    @Enumerated(EnumType.STRING)
    val type: Type,

    val weight: Float,

    @Column(length = 50)
    val description: String,

    @Column(name = "city_from")
    val cityFrom: String,

    @Column(name = "city_to")
    val cityTo: String,

    @Column(name = "estimated_delivery_date")
    val estimatedDeliveryDate: LocalDateTime
) : BaseEntity() {

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    var currentStatus: Status = Status.PENDING

    @OneToMany(mappedBy = "packageEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val events: MutableList<PackageEvent> = mutableListOf()
} 