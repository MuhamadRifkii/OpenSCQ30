package com.oppzippy.openscq30.ui.theme

import androidx.activity.BackEventCompat
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection

private const val PREDICTIVE_BACK_DURATION_MS = 280

private val SlideSpringSpec = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 480f,
    visibilityThreshold = IntOffset.VisibilityThreshold,
)

private val PredictiveSlideSpec = tween<IntOffset>(
    durationMillis = PREDICTIVE_BACK_DURATION_MS,
    easing = LinearEasing,
)

private val PredictiveFadeSpec = tween<Float>(
    durationMillis = PREDICTIVE_BACK_DURATION_MS,
    easing = LinearEasing,
)

@Composable
fun isRtlLayout(): Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl

/**
 * Returns the sign for where the *outgoing* screen exits on Back:
 * -1 => exits left (slides Right -> Left on back; opens Left -> Right)
 * +1 => exits right (slides Left -> Right on back; opens Right -> Left)
 */
fun popExitSignForDirection(
    direction: BackTransitionDirection,
    swipeEdge: Int? = null,
    isRtl: Boolean = false,
): Int = when (direction) {
    BackTransitionDirection.LeftToRightOpen -> -1
    BackTransitionDirection.RightToLeftOpen -> 1
    BackTransitionDirection.FollowSwipeEdge -> when (swipeEdge) {
        BackEventCompat.EDGE_LEFT -> 1
        BackEventCompat.EDGE_RIGHT -> -1
        else -> if (isRtl) -1 else 1
    }
}

fun appEnterTransition(
    enabled: Boolean,
    direction: BackTransitionDirection,
    isRtl: Boolean,
): EnterTransition {
    if (!enabled) return EnterTransition.None
    val popExitSign = popExitSignForDirection(direction, null, isRtl)
    return slideInHorizontally(SlideSpringSpec) { width -> width * popExitSign }
}

fun appExitTransition(
    enabled: Boolean,
    direction: BackTransitionDirection,
    isRtl: Boolean,
): ExitTransition {
    if (!enabled) return ExitTransition.None
    val popExitSign = popExitSignForDirection(direction, null, isRtl)
    return slideOutHorizontally(SlideSpringSpec) { width -> -(width / 4) * popExitSign }
}

fun appPopEnterTransition(
    enabled: Boolean,
    direction: BackTransitionDirection,
    swipeEdge: Int? = null,
    isRtl: Boolean = false,
): EnterTransition {
    if (!enabled) return EnterTransition.None
    val popExitSign = popExitSignForDirection(direction, swipeEdge, isRtl)
    return slideInHorizontally(SlideSpringSpec) { width -> -(width / 4) * popExitSign }
}

fun appPopExitTransition(
    enabled: Boolean,
    direction: BackTransitionDirection,
    swipeEdge: Int? = null,
    isRtl: Boolean = false,
): ExitTransition {
    if (!enabled) return ExitTransition.None
    val popExitSign = popExitSignForDirection(direction, swipeEdge, isRtl)
    return slideOutHorizontally(SlideSpringSpec) { width -> width * popExitSign }
}

fun appPredictivePopEnterTransition(
    slideEnabled: Boolean,
    predictiveEnabled: Boolean,
    direction: BackTransitionDirection,
    swipeEdge: Int,
    isRtl: Boolean,
): EnterTransition {
    if (!predictiveEnabled) return EnterTransition.None
    if (!slideEnabled) return fadeIn(PredictiveFadeSpec)
    val popExitSign = popExitSignForDirection(direction, swipeEdge, isRtl)
    return slideInHorizontally(PredictiveSlideSpec) { width -> -(width / 4) * popExitSign }
}

fun appPredictivePopExitTransition(
    slideEnabled: Boolean,
    predictiveEnabled: Boolean,
    direction: BackTransitionDirection,
    swipeEdge: Int,
    isRtl: Boolean,
): ExitTransition {
    if (!predictiveEnabled) return ExitTransition.None
    if (!slideEnabled) return fadeOut(PredictiveFadeSpec)
    val popExitSign = popExitSignForDirection(direction, swipeEdge, isRtl)
    return slideOutHorizontally(PredictiveSlideSpec) { width -> width * popExitSign }
}

fun AnimatedContentTransitionScope<Boolean>.rootTransitionSpec(
    enabled: Boolean,
    direction: BackTransitionDirection,
    isRtl: Boolean,
): ContentTransform {
    if (!enabled) {
        return EnterTransition.None togetherWith ExitTransition.None
    }
    return (
        if (targetState) {
            appEnterTransition(true, direction, isRtl) togetherWith appExitTransition(true, direction, isRtl)
        } else {
            appPopEnterTransition(true, direction, null, isRtl) togetherWith
                appPopExitTransition(true, direction, null, isRtl)
        }
    ).using(SizeTransform(clip = false))
}
