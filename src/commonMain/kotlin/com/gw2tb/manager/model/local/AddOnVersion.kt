package com.gw2tb.manager.model.local

import androidx.compose.runtime.Immutable

@Immutable
data class AddOnVersion(
    val fileVersion: AddOnFileVersion,
    private val versionString: String = fileVersion.toString()
) : Comparable<AddOnVersion> {

    override fun compareTo(other: AddOnVersion): Int = fileVersion.compareTo(other.fileVersion)
    override fun toString(): String = versionString

}