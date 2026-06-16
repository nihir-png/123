@file:OptIn(ExperimentalFoundationApi::class)
package com.designer.alarmclock.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.designer.alarmclock.R
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.Inter
import com.designer.alarmclock.ui.theme.Urbanist
import kotlinx.coroutines.launch

// ── Exact colors from the Figma onboarding frames ──
private val ScreenBackground = Color(0xFFF7F7F9) // page background (#f7f7f9)
private val TitleColor = Color(0xFF1A1A1A)        // title text (#1a1a1a)
private val SubtitleColor = Color(0xFF7B7B86)     // subtitle text (#7b7b86)
private val DotInactiveColor = Color(0xFFE1E1E7)  // inactive page dot (#e1e1e7)

// Golden gradient used by the active page dot and the button.
// Figma: from rgb(255,210,51)=#FFD233 to rgb(255,184,0)=#FFB800, nearly top→bottom.
private val GoldenGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFD233), Color(0xFFFFB800))
)

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.onboarding_1,
            title = "Smart alarms for better mornings",
            description = "Wake up gently with beautiful reminders, repeats, snooze, and focused routines."
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_2,
            title = "Every timezone in one calm place",
            description = "Track world clocks, countdowns, and stopwatch laps with the same clean rhythm."
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_3,
            title = "Start every day right on time",
            description = "A polished alarm experience designed to feel effortless from the very first tap."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground) // Figma page background (#f7f7f9)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = pages[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Image fills the top of the screen edge-to-edge.
                //    Figma: image container is 390 wide and ~580 tall, anchored to the
                //    very top (y = 0). The text block below starts at y = 587, i.e.
                //    580 (image) + 7 (gap) — see the spacer just after this box.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(580.dp)
                ) {
                    Image(
                        painter = painterResource(id = page.imageRes),
                        contentDescription = page.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )

                    // Gradient fade at the bottom of the image: transparent -> #F7F7F9,
                    // height 180dp, bottom-aligned (Figma node 1:1702 et al.).
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x00F7F7F9), // fully transparent
                                        ScreenBackground   // solid page background
                                    )
                                )
                            )
                    )
                }

                // 2. Gap between image bottom (y=580) and text block (y=587) = 7dp.
                Spacer(modifier = Modifier.height(7.dp))

                // 3. Text section and Action button section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title text — Urbanist Bold 26 / line-height 34 / tracking 0.25
                    Text(
                        text = page.title,
                        style = TextStyle(
                            fontFamily = Urbanist,
                            fontWeight = FontWeight.Bold,
                            color = TitleColor,
                            fontSize = 26.sp,
                            lineHeight = 34.sp,
                            letterSpacing = 0.25.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(304.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle text — Inter Regular 15 / line-height 21 / tracking -0.1594
                    Text(
                        text = page.description,
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            color = SubtitleColor,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            letterSpacing = (-0.1594).sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(31.dp))

                    // Page Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 28.dp else 8.dp,
                                label = "indicatorWidth"
                            )
                            val backgroundBrush = if (isSelected) {
                                GoldenGradient
                            } else {
                                SolidColor(DotInactiveColor)
                            }

                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(backgroundBrush)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Button
                    val isLastPage = pagerState.currentPage == pages.size - 1
                    Button(
                        onClick = {
                            if (isLastPage) {
                                onFinished()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        // Button: full width, height 50, fully rounded pill (radius 25),
                        // golden gradient background (Figma node 1:1700 et al.).
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(GoldenGradient, shape = RoundedCornerShape(25.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Button label — Urbanist SemiBold 16 / line-height 24 /
                            // tracking -0.3125, black.
                            Text(
                                text = if (isLastPage) "Get Started" else "Next",
                                style = TextStyle(
                                    fontFamily = Urbanist,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = Color.Black,
                                    letterSpacing = (-0.3125).sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(name = "Onboarding Light Theme", showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    AlarmClockTheme(darkTheme = false) {
        OnboardingScreen(onFinished = {})
    }
}
