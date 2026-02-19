/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2026 Leon Linhart
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

import com.gw2tb.manager.exceptions.ManagerException

sealed interface FetchResult<T> {

    companion object {

        fun <T> failed(cause: ManagerException): FetchResult<T> {
            @Suppress("UNCHECKED_CAST")
            return Failure(cause) as FetchResult<T>
        }

    }

    val value: T

    data class Success<T>(override val value: T) : FetchResult<T>, FetchResultWithValue<T> {
        override fun <T> copy(value: T): FetchResultWithValue<T> = Success(value)
    }

    data class Cached<T>(override val value: T) : FetchResult<T>, FetchResultWithValue<T> {
        override fun <T> copy(value: T): FetchResultWithValue<T> = Cached(value)
    }

    data class Stale<T>(
        override val value: T,
        override val cause: ManagerException
    ) : FetchResult<T>, FetchResultWithValue<T>, FetchResultWithException {
        override fun <T> copy(value: T): FetchResultWithValue<T> = Stale(value, cause)
    }

    data class Failure(
        override val cause: ManagerException
    ) : FetchResult<Nothing>, FetchResultWithException {
        override val value: Nothing get() = error("Cannot retrieve value from failed fetch")
    }

}

sealed interface FetchResultWithValue<T> {
    val value: T
    fun <T> copy(value: T): FetchResultWithValue<T>
}

sealed interface FetchResultWithException {
    val cause: ManagerException
}

@Suppress("UNCHECKED_CAST")
fun <S, T> FetchResult<S>.map(transform: (S) -> T): FetchResult<T> = when (this) {
    is FetchResultWithValue<*> -> copy(transform(value))
    is FetchResult.Failure -> this
} as FetchResult<T>

@Suppress("UNCHECKED_CAST")
fun <T> FetchResult<T>.withException(exception: ManagerException): FetchResult<T> = when (this) {
    is FetchResultWithValue<*> -> FetchResult.Stale(value, exception)
    else -> FetchResult.Failure(exception) as FetchResult<T>
}
