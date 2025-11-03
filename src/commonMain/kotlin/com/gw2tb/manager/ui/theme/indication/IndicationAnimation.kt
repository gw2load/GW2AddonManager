/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2025 Leon Linhart
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
package com.gw2tb.manager.ui.theme.indication

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class IndicationAnimation(
    private val radius: Float
) {

    private var startRadius: Float? = null

    private val animatedAlpha = Animatable(0f)
    private val animatedRadiusPercent = Animatable(0f)

    private val finishSignalDeferred = CompletableDeferred<Unit>(null)

    private var finishedFadingIn by mutableStateOf(false)
    private var finishRequested by mutableStateOf(false)

    suspend fun animate() {
        fadeIn()
        finishedFadingIn = true
        finishSignalDeferred.await()
        fadeOut()
    }

    private suspend fun fadeIn() {
        coroutineScope {
            launch {
                animatedAlpha.animateTo(
                    1f,
                    tween(durationMillis = FadeInDuration, easing = LinearEasing),
                )
            }
            launch {
                animatedRadiusPercent.animateTo(
                    1f,
                    tween(durationMillis = RadiusDuration, easing = LinearEasing),
                )
            }
        }
    }

    private suspend fun fadeOut() {
        coroutineScope {
            launch {
                animatedAlpha.animateTo(
                    0f,
                    tween(durationMillis = FadeOutDuration, easing = LinearEasing),
                )
            }
        }
    }

    fun finish() {
        finishRequested = true
        finishSignalDeferred.complete(Unit)
    }

    fun DrawScope.draw(color: Color) {
        if (startRadius == null) {
            startRadius = getIndicationStartRadius(size)
        }

        val alpha =
            if (finishRequested && !finishedFadingIn) {
                // If we are still fading-in we should immediately switch to the final alpha.
                1f
            } else {
                animatedAlpha.value
            }

        val radius = lerp(startRadius!!, radius, animatedRadiusPercent.value)
        val modulatedColor = color.copy(alpha = color.alpha * alpha)
        clipRect {
            drawCircle(Brush.radialGradient(listOf(modulatedColor, Color.Transparent), radius = radius), radius, center)
        }
    }

}

internal fun getIndicationStartRadius(size: Size) =
    max(size.width, size.height) * 0.3f

internal fun Density.getIndicationEndRadius(size: Size): Float
    = sqrt(size.width.pow(2) + size.height.pow(2)) / 2F

private const val FadeInDuration = 75
private const val RadiusDuration = 225
private const val FadeOutDuration = 150
