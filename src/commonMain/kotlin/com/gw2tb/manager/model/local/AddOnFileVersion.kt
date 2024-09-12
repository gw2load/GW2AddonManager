package com.gw2tb.manager.model.local

import androidx.compose.runtime.Immutable

@Immutable
data class AddOnFileVersion(
    private val major: UShort,
    private val minor: UShort,
    private val patch: UShort,
    private val fix: UShort
) : Comparable<AddOnFileVersion> {

    override fun compareTo(other: AddOnFileVersion): Int = when {
        this === other -> 0
        this.major > other.major -> 1
        this.major < other.major -> -1
        this.minor > other.minor -> 1
        this.minor < other.minor -> -1
        this.patch > other.patch -> 1
        this.patch < other.patch -> -1
        this.fix > other.fix -> 1
        this.fix < other.fix -> -1
        else -> 0
    }

    override fun toString(): String = "$major.$minor.$patch.$fix"

}