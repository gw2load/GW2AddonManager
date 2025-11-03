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
package com.gw2tb.manager.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.toSize
import com.gw2tb.manager.ui.theme.indication.IndicationAlpha
import com.gw2tb.manager.ui.theme.indication.IndicationAnimation
import com.gw2tb.manager.ui.theme.indication.getIndicationEndRadius
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@Stable
fun glimmer(
    radius: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified,
    alpha: IndicationAlpha? = null
): IndicationNodeFactory =
    GlimmerNodeFactory(radius, color, alpha)

@Stable
fun glimmer(
    radius: Dp = Dp.Unspecified,
    color: ColorProducer,
    alpha: IndicationAlpha? = null
): IndicationNodeFactory =
    GlimmerNodeFactory(radius, color, alpha)

object GlimmerDefaults {

    fun glimmerColor(contentColor: Color, lightTheme: Boolean): Color {
        val contentLuminance = contentColor.luminance()
        // If we are on a colored surface (typically indicated by low luminance content), the
        // ripple color should be white.
        return if (!lightTheme && contentLuminance < 0.5) {
            Color.White
            // Otherwise use contentColor
        } else {
            contentColor
        }
    }

    fun glimmerAlpha(contentColor: Color, lightTheme: Boolean): IndicationAlpha {
        return when {
            lightTheme -> {
                if (contentColor.luminance() > 0.5) {
                    LightThemeHighContrastIndicationAlpha
                } else {
                    LightThemeLowContrastIndicationAlpha
                }
            }
            else -> {
                DarkThemeIndicationAlpha
            }
        }
    }

}

val LocalGlimmerConfiguration: ProvidableCompositionLocal<GlimmerConfiguration?> =
    compositionLocalOf { GlimmerConfiguration() }

@Immutable
class GlimmerConfiguration(
    val color: Color = Color.Unspecified,
    val indicationAlpha: IndicationAlpha? = null
) {
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is GlimmerConfiguration -> false
        else -> this.color == other.color && this.indicationAlpha == other.indicationAlpha
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + indicationAlpha.hashCode()
        return result
    }

    override fun toString(): String =
        "GlimmerConfiguration(color=$color, indicationAlpha=$indicationAlpha)"

}

@Stable
private class GlimmerNodeFactory(
    private val radius: Dp,
    private val color: ColorProducer,
    private val alpha: IndicationAlpha?
) : IndicationNodeFactory {

    constructor(radius: Dp, color: Color, alpha: IndicationAlpha?) : this(radius, ColorProducer { color }, alpha)

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        DelegatingThemeAwareIndicationNode(interactionSource, radius, color, alpha)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is GlimmerNodeFactory -> false
        else -> radius == other.radius && color == other.color
    }

    override fun hashCode(): Int = color.hashCode()

}

private class DelegatingThemeAwareIndicationNode(
    private val interactionSource: InteractionSource,
    private val radius: Dp,
    private val color: ColorProducer,
    private val alpha: IndicationAlpha?
) : DelegatingNode(), CompositionLocalConsumerModifierNode, ObserverModifierNode {

    private var indicationNode: DelegatableNode? = null

    override fun onAttach() {
        updateConfiguration()
    }

    override fun onObservedReadsChanged() {
        updateConfiguration()
    }

    private fun updateConfiguration() {
        observeReads {
            val configuration = currentValueOf(LocalGlimmerConfiguration)
            if (configuration == null) {
                removeIndication()
            } else {
                if (indicationNode == null) attachNewIndication()
            }
        }
    }

    private fun attachNewIndication() {
        val calculateColor = ColorProducer {
            val userDefinedColor = color()
            if (userDefinedColor.isSpecified) {
                userDefinedColor
            } else {
                // If this is null, the ripple will be removed, so this should always be non-null in
                // normal use
                val indicationConfiguration = currentValueOf(LocalGlimmerConfiguration)
                if (indicationConfiguration?.color?.isSpecified == true) {
                    indicationConfiguration.color
                } else {
                    GlimmerDefaults.glimmerColor(
                        contentColor = currentValueOf(LocalContentColor),
                        lightTheme = true
                    )
                }
            }
        }
        val calculatedIndicationAlpha = {
            if (alpha != null) {
                alpha
            } else {
                // If this is null, the ripple will be removed, so this should always be non-null in
                // normal use
                val indicationConfiguration = currentValueOf(LocalGlimmerConfiguration)
                indicationConfiguration?.indicationAlpha
                    ?: GlimmerDefaults.glimmerAlpha(
                        contentColor = currentValueOf(LocalContentColor),
                        lightTheme = true
                    )
            }
        }

        indicationNode =
            delegate(
                createIndicationModifierNode(
                    interactionSource,
                    radius,
                    calculateColor,
                    calculatedIndicationAlpha
                )
            )
    }

    private fun removeIndication() {
        indicationNode?.let { undelegate(it) }
        indicationNode = null
    }

}

private val LightThemeHighContrastIndicationAlpha =
    IndicationAlpha(
        pressedAlpha = 0.24f,
        focusedAlpha = 0.24f,
        draggedAlpha = 0.16f,
        hoveredAlpha = 0.08f,
    )

/**
 * Alpha levels for low luminance content in a light theme.
 *
 * This content will typically be placed on grayscale surfaces, so the contrast here can be lower
 * without sacrificing accessibility and legibility.
 *
 * These levels are typically used for body text on the main surface (white in light theme, grey in
 * dark theme) and text / iconography in surface colored tabs / bottom navigation / etc.
 */
private val LightThemeLowContrastIndicationAlpha =
    IndicationAlpha(
        pressedAlpha = 0.12f,
        focusedAlpha = 0.12f,
        draggedAlpha = 0.08f,
        hoveredAlpha = 0.04f,
    )

/** Alpha levels for all content in a dark theme. */
private val DarkThemeIndicationAlpha =
    IndicationAlpha(
        pressedAlpha = 0.10f,
        focusedAlpha = 0.12f,
        draggedAlpha = 0.08f,
        hoveredAlpha = 0.04f,
    )

private fun createIndicationModifierNode(
    interactionSource: InteractionSource,
    radius: Dp,
    color: ColorProducer,
    indicationAlpha: () -> IndicationAlpha
) : DelegatableNode =
    IndicationNode(interactionSource, radius, color, indicationAlpha)

private class IndicationNode(
    private val interactionSource: InteractionSource,
    private val radius: Dp,
    private val color: ColorProducer,
    private val indicationAlpha: () -> IndicationAlpha
) : Modifier.Node(), CompositionLocalConsumerModifierNode, DrawModifierNode, LayoutAwareModifierNode {

    private val indications = mutableMapOf<PressInteraction.Press, IndicationAnimation>()

    private var stateLayer: StateLayer? = null

    private var targetRadius: Float = 0f

    private var indicationSize: Size = Size.Zero

    val indicationColor: Color
        get() = color()

    // Track interactions that were emitted before we have been placed - we need to wait until we
    // have a valid size in order to set the radius and size correctly.
    private var hasValidSize = false
    private val pendingInteractions = mutableListOf<PressInteraction>()

    override fun onRemeasured(size: IntSize) {
        hasValidSize = true
        val density = requireDensity()
        indicationSize = size.toSize()
        targetRadius =
            with(density) {
                if (radius.isUnspecified) {
                    // Explicitly calculate the radius instead of using RippleDrawable.RADIUS_AUTO
                    // on
                    // Android since the latest spec does not match with the existing radius
                    // calculation
                    // in the framework.
                    getIndicationEndRadius(indicationSize)
                } else {
                    radius.toPx()
                }
            }
        // Flush any pending interactions that were waiting for measurement
        pendingInteractions.forEach { handlePressInteraction(it) }
        pendingInteractions.clear()
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction -> {
                        if (hasValidSize) {
                            handlePressInteraction(interaction)
                        } else {
                            // Handle these later when we have a valid size
                            pendingInteractions += interaction
                        }
                    }
                    else -> updateStateLayer(interaction, this)
                }
            }
        }
    }

    override fun onDetach() {
        indications.clear()
    }

    private fun handlePressInteraction(pressInteraction: PressInteraction) {
        when (pressInteraction) {
            is PressInteraction.Press -> addIndication(pressInteraction, targetRadius)
            is PressInteraction.Release -> removeIndication(pressInteraction.press)
            is PressInteraction.Cancel -> removeIndication(pressInteraction.press)
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        stateLayer?.run { drawStateLayer(targetRadius, indicationColor) }
        drawIndications()
    }

    fun DrawScope.drawIndications() {
        val alpha = indicationAlpha().pressedAlpha
        if (alpha != 0f) {
            indications.forEach { (_, indication) -> with(indication) { draw(indicationColor.copy(alpha = alpha)) } }
        }
    }

    fun addIndication(interaction: PressInteraction.Press, targetRadius: Float) {
        // Finish existing indications
        indications.forEach { (_, indication) -> indication.finish() }
        val indicationAnimation = IndicationAnimation(radius = targetRadius)
        indications[interaction] = indicationAnimation

        coroutineScope.launch {
            try {
                indicationAnimation.animate()
            } finally {
                indications.remove(interaction)
                invalidateDraw()
            }
        }

        invalidateDraw()
    }

    fun removeIndication(interaction: PressInteraction.Press) {
        indications[interaction]?.finish()
    }

    private fun updateStateLayer(interaction: Interaction, scope: CoroutineScope) {
        val stateLayer =
            stateLayer
                ?: StateLayer(indicationAlpha).also { instance ->
                    // Invalidate when adding the state layer so we can start drawing it
                    invalidateDraw()
                    stateLayer = instance
                }
        stateLayer.handleInteraction(interaction, scope)
    }

}

private class StateLayer(private val indicationAlpha: () -> IndicationAlpha) {
    private val animatedAlpha = Animatable(0F)

    private val interactions: MutableList<Interaction> = mutableListOf()
    private var currentInteraction: Interaction? = null

    fun handleInteraction(interaction: Interaction, scope: CoroutineScope) {
        when (interaction) {
            is HoverInteraction.Enter -> {
                interactions.add(interaction)
            }
            is HoverInteraction.Exit -> {
                interactions.remove(interaction.enter)
            }
            is FocusInteraction.Focus -> {
                interactions.add(interaction)
            }
            is FocusInteraction.Unfocus -> {
                interactions.remove(interaction.focus)
            }
            is DragInteraction.Start -> {
                interactions.add(interaction)
            }
            is DragInteraction.Stop -> {
                interactions.remove(interaction.start)
            }
            is DragInteraction.Cancel -> {
                interactions.remove(interaction.start)
            }
            else -> return
        }

        // The most recent interaction is the one we want to show
        val newInteraction = interactions.lastOrNull()

        if (currentInteraction != newInteraction) {
            if (newInteraction != null) {
                val rippleAlpha = indicationAlpha()
                val targetAlpha =
                    when (newInteraction) {
                        is HoverInteraction.Enter -> rippleAlpha.hoveredAlpha
                        is FocusInteraction.Focus -> rippleAlpha.focusedAlpha
                        is DragInteraction.Start -> rippleAlpha.draggedAlpha
                        else -> 0f
                    }
                val incomingAnimationSpec = incomingStateLayerAnimationSpecFor(newInteraction)

                scope.launch { animatedAlpha.animateTo(targetAlpha, incomingAnimationSpec) }
            } else {
                val outgoingAnimationSpec = outgoingStateLayerAnimationSpecFor(currentInteraction)

                scope.launch { animatedAlpha.animateTo(0f, outgoingAnimationSpec) }
            }
            currentInteraction = newInteraction
        }
    }

    fun DrawScope.drawStateLayer(radius: Float, color: Color) {
        val alpha = animatedAlpha.value

        if (alpha > 0f) {
            val modulatedColor = color.copy(alpha = alpha)
            clipRect { drawCircle(modulatedColor, radius) }
        }
    }
}

/**
 * @return the [AnimationSpec] used when transitioning to [interaction], either from a previous
 *   state, or no state.
 */
private fun incomingStateLayerAnimationSpecFor(interaction: Interaction): AnimationSpec<Float> {
    return when (interaction) {
        is HoverInteraction.Enter -> DefaultTweenSpec
        is FocusInteraction.Focus -> TweenSpec(durationMillis = 45, easing = LinearEasing)
        is DragInteraction.Start -> TweenSpec(durationMillis = 45, easing = LinearEasing)
        else -> DefaultTweenSpec
    }
}

/** @return the [AnimationSpec] used when transitioning away from [interaction], to no state. */
private fun outgoingStateLayerAnimationSpecFor(interaction: Interaction?): AnimationSpec<Float> {
    return when (interaction) {
        is HoverInteraction.Enter -> DefaultTweenSpec
        is FocusInteraction.Focus -> DefaultTweenSpec
        is DragInteraction.Start -> TweenSpec(durationMillis = 150, easing = LinearEasing)
        else -> DefaultTweenSpec
    }
}

/** Default / fallback [AnimationSpec]. */
private val DefaultTweenSpec = TweenSpec<Float>(durationMillis = 15, easing = LinearEasing)
