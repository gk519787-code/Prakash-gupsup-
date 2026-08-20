package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.NewsArticle
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NewsDetailModal(
    article: NewsArticle?,
    onDismiss: () -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onFetchDeepDive: suspend (String, String) -> String
) {
    if (article == null) return

    val coroutineScope = rememberCoroutineScope()
    var isAudioPlaying by remember { mutableStateOf(false) }
    var deepDiveText by remember { mutableStateOf(article.aiDeepDive) }
    var isLoadingDeepDive by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("news_detail_modal"),
            containerColor = GupSupBg,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GupSupSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .border(1.dp, GupSupLine, CircleShape)
                                .testTag("close_news_modal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GupSupText
                            )
                        }

                        Column {
                            Text(
                                text = "CURRENT AFFAIRS RADAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = GupSupBlue
                            )
                            Text(
                                text = article.source,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = parseColorHex(article.sourceColorHex)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggleBookmark(article.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (article.isBookmarked) GupSupAmber else GupSupText
                            )
                        }
                        IconButton(
                            onClick = { onToggleLike(article.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (article.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (article.isLiked) GupSupRed else GupSupText
                            )
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Source badge & time
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(parseColorHex(article.sourceColorHex))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = article.source.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "${article.publishedTime} · ${article.readTimeMinutes} min read",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GupSupMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Main Headline
                item {
                    Text(
                        text = article.headline,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = GupSupText,
                        lineHeight = 28.sp,
                        fontSize = 20.sp
                    )
                }

                // AI Audio Bullet Reader Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GupSupRed.copy(alpha = 0.08f))
                            .border(1.dp, GupSupRed.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .clickable { isAudioPlaying = !isAudioPlaying }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GupSupRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Listen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isAudioPlaying) "Listening to AI News Brief (Hindi/Eng)..." else "Listen to 45s AI Audio Summary",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = GupSupText
                                )
                                Text(
                                    text = "Automated voice generation by prakash gupsup AI",
                                    fontSize = 10.sp,
                                    color = GupSupMuted
                                )
                            }
                        }
                    }
                }

                // Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GupSupSurface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CORE BRIEF / सारांश",
                                style = MaterialTheme.typography.labelSmall,
                                color = GupSupBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = article.summary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = GupSupText,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Key Takeaways 3-bullets
                if (article.keyPoints.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GupSupCardBg)
                                .border(1.dp, GupSupLine, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "KEY HIGHLIGHTS & GROUND IMPACT",
                                style = MaterialTheme.typography.labelSmall,
                                color = GupSupAmber
                            )

                            article.keyPoints.forEachIndexed { idx, point ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(GupSupAmber.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = GupSupAmber
                                        )
                                    }
                                    Text(
                                        text = point,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GupSupText,
                                        lineHeight = 19.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Gemini AI Deep Dive Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GupSupPurple.copy(alpha = 0.08f))
                            .border(1.dp, GupSupPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GupSupPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "GEMINI AI CONTEXT & ANALYSIS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GupSupPurple
                                )
                            }

                            if (deepDiveText.isBlank()) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isLoadingDeepDive = true
                                            try {
                                                deepDiveText = onFetchDeepDive(article.headline, article.source)
                                            } finally {
                                                isLoadingDeepDive = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GupSupPurple),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Analyze ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLoadingDeepDive) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = GupSupPurple,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Connecting to Gemini 3.5 Flash & newsrooms...",
                                    fontSize = 12.sp,
                                    color = GupSupMuted
                                )
                            }
                        } else {
                            Text(
                                text = deepDiveText.ifBlank {
                                    "Tap 'Analyze ✨' to generate contextual background, fact-check transparency, and cross-source analysis with ${article.source}."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = GupSupText,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Source transparency label
                item {
                    Text(
                        text = "Source: ${article.source} · Verified under AI news syndication protocols for prakash gupsup.",
                        fontSize = 10.sp,
                        color = GupSupMuted,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )
                }
            }
        }
    }
}
