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