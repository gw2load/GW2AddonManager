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
package com.gw2tb.manager.util

fun <T : Any> Maybe(value: T?): Maybe<T> =
    if (value != null) Maybe.Some(value) else Maybe.None()

sealed interface Maybe<T : Any> {

    val orNull: T? get() = if (this is Some<T>) value else null

    class Some<T : Any>(val value: T) : Maybe<T>

    class None<T : Any> : Maybe<T>

}
