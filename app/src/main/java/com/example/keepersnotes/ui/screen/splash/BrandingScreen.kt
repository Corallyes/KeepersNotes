package com.example.keepersnotes.ui.screen.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keepersnotes.R
import com.example.keepersnotes.ui.theme.DarkBackground
import com.example.keepersnotes.ui.theme.DividerDark
import com.example.keepersnotes.ui.theme.LightInk
import com.example.keepersnotes.ui.theme.SecondaryLightInk
import kotlinx.coroutines.delay

@Composable
fun BrandingScreen(
    onNavigateToHome: () -> Unit
) {
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showDivider by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue = if (showLogo) 0.25f else 0f,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "logoAlpha"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "titleAlpha"
    )

    val dividerAlpha by animateFloatAsState(
        targetValue = if (showDivider) 0.6f else 0f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing),
        label = "dividerAlpha"
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = if (showSubtitle) 0.8f else 0f,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "subtitleAlpha"
    )

    LaunchedEffect(Unit) {
        showLogo = true
        delay(150)
        showTitle = true
        delay(100)
        showDivider = true
        delay(100)
        showSubtitle = true
        delay(650)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        AgedPaperTexture()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            // Watermark logo
            Image(
                painter = painterResource(id = R.drawable.logo_transparent),
                contentDescription = "守密人笔记",
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .graphicsLayer { alpha = logoAlpha },
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Typewriter title
            Text(
                text = "守密人笔记",
                color = LightInk,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = titleAlpha }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Thin decorative divider
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .graphicsLayer { alpha = dividerAlpha },
                thickness = 0.5.dp,
                color = DividerDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Handwritten subtitle
            Text(
                text = "记录秘密，保存真相",
                color = SecondaryLightInk,
                fontSize = 15.sp,
                fontFamily = FontFamily.Cursive,
                fontStyle = FontStyle.Italic,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = subtitleAlpha }
            )
        }
    }
}
