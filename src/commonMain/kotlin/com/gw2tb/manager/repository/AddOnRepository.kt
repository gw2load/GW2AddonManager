/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024 Leon Linhart
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU Lesser General Public License as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.gw2tb.manager.repository

import com.gw2tb.manager.model.catalog.AddOnListing
import java.nio.channels.ReadableByteChannel

/**
 * A repository of add-on listings.
 */
interface AddOnRepository {

    /**
     * Returns a list of add-on listings available from the repository.
     *
     * @return  the available add-ons
     */
    suspend fun getAddOnListings(): List<AddOnListing>

    /**
     * Opens a download channel for the given listing.
     *
     * @param listing   the listing to download
     *
     * @return  the channel to read from
     */
    suspend fun download(listing: AddOnListing): ReadableByteChannel

    /** Invalidates the add-on listing cache. */
    fun invalidateCache()

}