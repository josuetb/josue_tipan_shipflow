package com.pucetec.josue_tipan_shipflow.models.entities

import jakarta.persistence.*

@Entity
@Table(name = "events")
data class PackageEvent(
    @Enumerated(EnumType.STRING)
    val status: Status,

    @Column(name = "comment", length = 255)
    val comment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    val packageEntity: Package
) : BaseEntity() 