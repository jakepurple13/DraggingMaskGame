package com.programmersbox.common

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun App() {
    M3MaterialThemeSetup(isSystemInDarkTheme()) {
        var offset by remember { mutableStateOf(Offset.Zero) }
        val offsetAnimation = remember(offset) { Animatable(offset, Offset.VectorConverter) }
        val size by remember { mutableStateOf(250f) }
        val scope = rememberCoroutineScope()
        var center by remember { mutableStateOf(IntOffset.Zero) }
        var canvasSize by remember { mutableStateOf(Size.Zero) }
        var itemOffset by remember { mutableStateOf(Offset.Zero) }
        val foundText by remember {
            derivedStateOf {
                itemOffset.x in offset.x..(offset.x + size) &&
                        itemOffset.y in offset.y..(offset.y + size)
            }
        }
        var showFound by remember(itemOffset) { mutableStateOf(false) }
        var timer by timer(!showFound, foundText)

        val readableTimer by remember {
            derivedStateOf { LocalTime.fromMillisecondOfDay(timer.roundToInt()).toString() }
        }

        LaunchedEffect(canvasSize) {
            val x = Random.nextInt(0, canvasSize.width.roundToInt())
            val y = Random.nextInt(0, canvasSize.height.roundToInt())
            itemOffset = Offset(x.toFloat(), y.toFloat())
        }

        LaunchedEffect(offset, foundText) {
            if (foundText) {
                delay(1000)
                println("FOUND IT!")
                showFound = true
            }
        }

        Surface {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Masking") },
                        actions = {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        offsetAnimation.animateTo(center.toOffset()) {
                                            offset = value.copy(
                                                x = value.x - size / 2,
                                                y = value.y - size / 2
                                            )
                                        }
                                    }
                                }
                            ) { Text("Reset Position") }
                        }
                    )
                },
                bottomBar = {
                    BottomAppBar(
                        actions = { Text(readableTimer) },
                        floatingActionButton = {
                            OutlinedButton(
                                onClick = {
                                    val x = Random.nextInt(0, canvasSize.width.roundToInt())
                                    val y = Random.nextInt(0, canvasSize.height.roundToInt())
                                    itemOffset = Offset(x.toFloat(), y.toFloat())
                                    showFound = false
                                    timer = 0.0
                                },
                                enabled = showFound
                            ) { Text("Found!") }
                        }
                    )
                }
            ) { p ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(p)
                        .background(MaterialTheme.colorScheme.onSurface)
                ) {
                    val data = updateTransitionData(foundText)
                    Text(
                        "Here!",
                        modifier = Modifier
                            .offset { itemOffset.round() }
                            .scale(data.scale),
                        color = data.color,
                    )
                }

                ShowBehind(
                    offset = { offset },
                    offsetChange = { offset += it },
                    sourceDrawing = ShowBehindDefaults.defaultSourceDrawing(size, offset),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(p)
                        .onGloballyPositioned {
                            center = it.size.center
                            canvasSize = it.size.toSize()
                        }
                )
            }
        }
    }
}

@Composable
private fun updateTransitionData(foundText: Boolean): TransitionData {
    val transition = updateTransition(foundText)
    val color = transition.animateColor { state ->
        if (state) Color(0xFF2ecc71)
        else MaterialTheme.colorScheme.surface
    }
    val size = transition.animateFloat { state -> if (state) 1f else .75f }
    return remember(transition) { TransitionData(color, size) }
}

private class TransitionData(
    color: State<Color>,
    scale: State<Float>
) {
    val color by color
    val scale by scale
}

@Composable
internal fun timer(runTimer: Boolean, foundText: Boolean): MutableState<Double> {
    val counter = remember { mutableStateOf(0.0) }

    LaunchedEffect(runTimer, foundText) {
        while (runTimer) {
            delay(1)
            counter.value += if (foundText) .5 else 1.0
        }
    }

    return counter
}