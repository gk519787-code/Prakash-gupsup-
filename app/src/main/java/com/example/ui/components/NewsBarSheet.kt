package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.data.util.FastDataProcessor
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsBarSheet(
    visible: Boolean,
    articles: List<NewsArticle>,
    isRefreshing: Boolean,
    selectedSource: String,
    onDismiss: () -> Unit,
    onOpenArticle: (NewsArticle) -> Unit,
    onRefreshNews: () -> Unit,
    onSelectSource: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    if (!visible) return

    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newsSearchQuery by remember { mutableStateOf("") }
    var activeAudioArticleId by remember { mutableStateOf<String?>(null) }
    var filteredArticles by remember { mutableStateOf(articles) }

    val sources = listOf("All", "The Indian Express Hindi", "Lallantop News", "News Pinch", "The Hindu")
    val coroutineScope = rememberCoroutineScope()

    // Trigger fast data processing algorithm on query or source change
    LaunchedEffect(articles, newsSearchQuery, selectedSource) {
        filteredArticles = FastDataProcessor.instance.searchNews(
            articles = articles,
            query = newsSearchQuery,
            sourceFilter = selectedSource
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = GupSupBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GupSupLine)
            )
        },
        modifier = Modifier.testTag("news_bar_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 18.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GupSupRed, GupSupAmber))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = "News",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(GupSupRed, CircleShape)
                            )
                            Text(
                                text = "CURRENT AFFAIRS RADAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = GupSupBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "prakash gupsup News Bar",
                            style = SchoolbellHeadingStyle,
                            color = GupSupText,
                            fontSize = 20.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshNews,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GupSupSurface, CircleShape)
                            .border(1.dp, GupSupLine, CircleShape)
                            .testTag("news_bar_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isRefreshing) GupSupRed else GupSupText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GupSupSurface, CircleShape)
                            .border(1.dp, GupSupLine, CircleShape)
                            .testTag("close_news_bar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = GupSupText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Real-Time Search Field
            OutlinedTextField(
                value = newsSearchQuery,
                onValueChange = { newsSearchQuery = it },
                placeholder = {
                    Text(
                        text = "Search breaking news, Hindi/Eng topics...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GupSupDim
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GupSupDim,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (newsSearchQuery.isNotBlank()) {
                        IconButton(onClick = { newsSearchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = GupSupDim)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("news_bar_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GupSupBlue,
                    unfocusedBorderColor = GupSupLine,
                    focusedContainerColor = GupSupSurface,
                    unfocusedContainerColor = GupSupSurface,
                    focusedTextColor = GupSupText,
                    unfocusedTextColor = GupSupText
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-Source Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sources) { source ->
                    val isSelected = selectedSource == source
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) GupSupText else GupSupSurface)
                            .border(1.dp, if (isSelected) GupSupText else GupSupLine, RoundedCornerShape(20.dp))
                            .clickable { onSelectSource(source) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("news_source_chip_$source")
                    ) {
                        Text(
                            text = source,
                            color = if (isSelected) GupSupBg else GupSupText,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Breaking Alert Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GupSupRed.copy(alpha = 0.09f))
                    .border(1.dp, GupSupRed.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(GupSupRed, CircleShape)
                    )
                    Text(
                        text = "LIVE AI NEWSSTREAM: 4 NEWSROOMS CONNECTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = if (isRefreshing) "Syncing..." else "24/7 Live",
                    fontSize = 10.sp,
                    color = GupSupDim,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feed of News Articles
            if (filteredArticles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = GupSupDim, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No news found for '$newsSearchQuery'",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GupSupMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("news_bar_articles_list"),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredArticles, key = { it.id }) { article ->
                        val isAudioActive = activeAudioArticleId == article.id
                        NewsBarCard(
                            article = article,
                            isAudioPlaying = isAudioActive,
                            onToggleAudio = {
                                activeAudioArticleId = if (isAudioActive) null else article.id
                            },
                            onClick = { onOpenArticle(article) },
                            onToggleLike = { onToggleLike(article.id) },
                            onToggleBookmark = { onToggleBookmark(article.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsBarCard(
    article: NewsArticle,
    isAudioPlaying: Boolean,
    onToggleAudio: () -> Unit,
    onClick: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GupSupLine, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("news_card_${article.id}"),
        colors = CardDefaults.cardColors(containerColor = GupSupSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Source & Timestamp & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(parseColorHex(article.sourceColorHex).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = article.source,
                            color = parseColorHex(article.sourceColorHex),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = "• ${article.publishedTime}",
                        fontSize = 10.sp,
                        color = GupSupMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (article.isBookmarked) GupSupAmber else GupSupDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleLike,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (article.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (article.isLiked) GupSupRed else GupSupDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Headline
            Text(
                text = article.headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GupSupText,
                lineHeight = 20.sp
            )

            // Summary
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodySmall,
                color = GupSupMuted,
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Audio Reader & Deep Dive Action Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Summary Button
                Button(
                    onClick = onToggleAudio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAudioPlaying) GupSupRed else GupSupCardBg,
                        contentColor = if (isAudioPlaying) Color.White else GupSupText
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Audio",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAudioPlaying) "Playing 45s Brief..." else "Listen Brief 🎧",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Deep Dive link
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "AI Deep Dive",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GupSupRed
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Deep Dive",
                        tint = GupSupRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
