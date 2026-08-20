package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SwipeableActivityDashboard(
    newsArticles: List<NewsArticle>,
    onOpenNewsArticle: (NewsArticle) -> Unit,
    onRefreshNews: () -> Unit,
    isNewsRefreshing: Boolean,
    onOpenSparkAi: () -> Unit,
    onJumpToReels: () -> Unit,
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_dashboard")
            .border(1.dp, GupSupLine, RoundedCornerShape(20.dp))
            .background(GupSupSurface, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (pagerState.currentPage == 0) GupSupRed else GupSupBlue, CircleShape)
                )
                Text(
                    text = when (pagerState.currentPage) {
                        0 -> "AI NEWS & CURRENT AFFAIRS RADAR / 01"
                        1 -> "GEMINI SPARK AI DIRECTOR / 02"
                        2 -> "COMMUNITY TRENDING PULSE / 03"
                        else -> "ENDLESS REELS STREAM / 04"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = GupSupBlue
                )
            }

            // Dot Indicator
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 16.dp else 6.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (pagerState.currentPage == index) GupSupRed else GupSupLine
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp)
        ) { page ->
            when (page) {
                0 -> AiNewsDashboardCard(
                    articles = newsArticles,
                    isRefreshing = isNewsRefreshing,
                    onRefresh = onRefreshNews,
                    onOpenArticle = onOpenNewsArticle
                )
                1 -> SparkAiDirectorCard(onOpenSparkAi = onOpenSparkAi)
                2 -> CommunityPulseCard(onJumpToReels = onJumpToReels)
                3 -> LiveReelsRadarCard(onJumpToReels = onJumpToReels)
            }
        }
    }
}

@Composable
private fun AiNewsDashboardCard(
    articles: List<NewsArticle>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenArticle: (NewsArticle) -> Unit
) {
    val topArticle = articles.firstOrNull() ?: NewsArticle(
        id = "sample_top",
        headline = "AI News Radar Connecting Top Newsrooms",
        source = "The Indian Express Hindi",
        category = "Breaking",
        summary = "Live AI current affairs curated from The Indian Express Hindi, The Lallantop, News Pinch, and The Hindu.",
        publishedTime = "Live"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(GupSupCardBg)
            .border(1.dp, GupSupLine.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable { onOpenArticle(topArticle) }
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row: Source badge, category tag, AI live indicator, refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Source badge with branding colors
                Box(
                    modifier = Modifier
                        .background(parseColorHex(topArticle.sourceColorHex).copy(alpha = 0.16f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = topArticle.source.uppercase(),
                        color = parseColorHex(topArticle.sourceColorHex),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .background(GupSupRed.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AI MAINTAINED ✨",
                        color = GupSupRed,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = topArticle.publishedTime,
                    fontSize = 10.sp,
                    color = GupSupMuted
                )

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("refresh_ai_news_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh News",
                        tint = GupSupText,
                        modifier = Modifier
                            .size(15.dp)
                            .then(if (isRefreshing) Modifier.rotate(angle) else Modifier)
                    )
                }
            }
        }

        // Headline
        Text(
            text = topArticle.headline,
            style = MaterialTheme.typography.titleMedium,
            color = GupSupText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 17.sp,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // Bottom row: Sources strip & Read CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ IE Hindi · Lallantop · News Pinch · The Hindu",
                fontSize = 9.sp,
                color = GupSupMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GupSupText)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Read AI Deep Dive →",
                    color = GupSupBg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SparkAiDirectorCard(onOpenSparkAi: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(GupSupBlue.copy(alpha = 0.08f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GupSupRed,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "GEMINI SPARK AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = GupSupRed
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "GENERATE\nCREATIVE HOOKS",
                style = MaterialTheme.typography.headlineMedium,
                color = GupSupText,
                lineHeight = 22.sp
            )
            Text(
                text = "Instant captions & reel pacing ideas",
                style = MaterialTheme.typography.bodyMedium,
                color = GupSupMuted,
                fontSize = 11.sp
            )
        }

        Button(
            onClick = onOpenSparkAi,
            colors = ButtonDefaults.buttonColors(
                containerColor = GupSupText,
                contentColor = GupSupBg
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.testTag("launch_spark_ai_btn")
        ) {
            Text(text = "Launch ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CommunityPulseCard(onJumpToReels: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            OverlineTag(text = "COMMUNITY PULSE", color = GupSupTeal)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TRENDING\n#35mmFilm",
                style = MaterialTheme.typography.headlineMedium,
                color = GupSupText,
                lineHeight = 22.sp
            )
            Text(
                text = "+89 new comments · 24 live cuts",
                style = MaterialTheme.typography.bodyMedium,
                color = GupSupMuted,
                fontSize = 11.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GupSupAmber)
                    .clickable { onJumpToReels() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = GupSupText,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "EXPLORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupText,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveReelsRadarCard(onJumpToReels: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(GupSupPurple.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            OverlineTag(text = "ENDLESS REELS", color = GupSupPurple)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NON-STOP\nSCROLL CUTS",
                style = MaterialTheme.typography.headlineMedium,
                color = GupSupText,
                lineHeight = 22.sp
            )
            Text(
                text = "Endless 24fps motion stream & audio sync",
                style = MaterialTheme.typography.bodyMedium,
                color = GupSupMuted,
                fontSize = 11.sp
            )
        }

        IconButton(
            onClick = onJumpToReels,
            modifier = Modifier
                .size(46.dp)
                .background(GupSupPurple, CircleShape)
                .testTag("jump_to_reel_btn")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Watch Endless Reels",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
