package com.example.keepersnotes.ui.screen.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.R
import com.example.keepersnotes.ui.theme.DarkBackground
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(
    onNavigateToBrand: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.35f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(500)
        onNavigateToBrand()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        AgedPaperTexture()

        Image(
            painter = painterResource(id = R.drawable.logo_transparent),
            contentDescription = "守密人笔记",
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .graphicsLayer { alpha = logoAlpha },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun AgedPaperTexture() {
    val seed = remember { Random.nextInt() }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val random = Random(seed)

        // Base warm paper tone overlay
        drawRect(
            color = Color(0xFF1A1714),
            alpha = 0.6f
        )

        // Paper grain - tiny dots
        repeat(3000) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val alpha = random.nextFloat() * 0.035f
            val warmTint = Color(
                red = 0.85f + random.nextFloat() * 0.15f,
                green = 0.75f + random.nextFloat() * 0.15f,
                blue = 0.60f + random.nextFloat() * 0.15f
            )
            drawCircle(
                color = warmTint,
                radius = random.nextFloat() * 1.2f + 0.2f,
                center = androidx.compose.ui.geometry.Offset(x, y),
                alpha = alpha
            )
        }

        // Age stains - larger warm spots
        repeat(15) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val radius = random.nextFloat() * 80f + 30f
            val alpha = random.nextFloat() * 0.02f + 0.005f
            drawCircle(
                color = Color(0xFFBFA882),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(x, y),
                alpha = alpha
            )
        }

        // Faint horizontal lines like aged paper fibers
        repeat(60) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val length = random.nextFloat() * 50f + 15f
            val alpha = random.nextFloat() * 0.018f
            drawLine(
                color = Color(0xFFD6C6A5),
                start = androidx.compose.ui.geometry.Offset(x, y),
                end = androidx.compose.ui.geometry.Offset(
                    x + length,
                    y + random.nextFloat() * 3f - 1.5f
                ),
                strokeWidth = random.nextFloat() * 0.6f + 0.1f,
                alpha = alpha
            )
        }

        // Edge darkening (vignette)
        val vignetteSize = size.width * 0.8f
        drawCircle(
            color = Color.Black,
            radius = vignetteSize,
            center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2),
            alpha = 0.15f
        )
    }
}
