package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.data.model.PostEntity
import com.example.data.model.Story
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    posts: List<PostEntity>,
    stories: List<Story>,
    newsArticles: List<NewsArticle>,
    isNewsRefreshing: Boolean,
    selectedNewsSource: String,
    onOpenNewsArticle: (NewsArticle) -> Unit,
    onOpenNewsBar: () -> Unit,
    onRefreshNews: () -> Unit,
    onSelectNewsSource: (String) -> Unit,
    onLikePost: (String) -> Unit,
    onBookmarkPost: (String) -> Unit,
    onOpenComments: (PostEntity) -> Unit,
    onOpenStory: (Int) -> Unit,
    onOpenMessages: () -> Unit,
    onOpenSparkAi: () -> Unit,
    onJumpToReels: () -> Unit,
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GupSupBg)
            .testTag("home_feed_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header with Schoolbell font
        item {
            HomeHeader(
                onMessages = onOpenMessages,
                onOpenSparkAi = onOpenSparkAi,
                onOpenNewsBar = onOpenNewsBar
            )
        }

        // Swipeable Activity Dashboard Carousel (AI News Radar replaces Daily Cut)
        item {
            SwipeableActivityDashboard(
                newsArticles = newsArticles,
                onOpenNewsArticle = onOpenNewsArticle,
                onRefreshNews = onRefreshNews,
                isNewsRefreshing = isNewsRefreshing,
                onOpenSparkAi = onOpenSparkAi,
                onJumpToReels = onJumpToReels,
                onCreatePost = onCreatePost
            )
        }

        // Slideable Stories Rail
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Slideable Stories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GupSupText
                    )
                    Text(
                        text = "⟵ SLIDE TO FLIP ⟶",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupMuted,
                        fontSize = 9.sp
                    )
                }

                StoryRail(
                    stories = stories,
                    onStoryClick = onOpenStory
                )
            }
        }

        // Feed Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Community Frames",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GupSupText
                )
                OverlineTag(
                    text = "${posts.size} NEW FRAMES",
                    color = GupSupDim
                )
            }
        }

        // Feed Posts
        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GupSupRed)
                }
            }
        } else {
            itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
                PostCard(
                    post = post,
                    index = index,
                    onLike = { onLikePost(post.id) },
                    onBookmark = { onBookmarkPost(post.id) },
                    onCommentClick = { onOpenComments(post) },
                    onOpenSparkAi = onOpenSparkAi
                )
            }
        }
    }
}

@Composable
fun HomeHeader(
    onMessages: () -> Unit,
    onOpenSparkAi: () -> Unit,
    onOpenNewsBar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            OverlineTag(text = "SOCIAL AI ENGINE / 01", color = GupSupBlue)
            Text(
                text = "prakash gupsup",
                style = SchoolbellTitleStyle,
                color = GupSupText
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spark Gemini AI button
            IconButton(
                onClick = onOpenSparkAi,
                modifier = Modifier
                    .testTag("header_spark_ai_btn")
                    .size(42.dp)
                    .border(1.dp, GupSupLine, CircleShape)
                    .background(GupSupSurface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Spark AI",
                    tint = GupSupRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Messages Button
            GupSupIconButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Messages",
                testTag = "messages_button",
                onClick = onMessages,
                badgeCount = 2
            )

            // UNIQUE CIRCULAR SHAPED NEWS ICON (Situated to the right side of message icon)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .testTag("news_icon_button")
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF5252),
                                Color(0xFFFF7A00),
                                Color(0xFFFFB300)
                            )
                        )
                    )
                    .padding(2.dp) // Outer ring border effect
            ) {
                IconButton(
                    onClick = onOpenNewsBar,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(GupSupSurface)
                        .testTag("news_radar_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = "Open News Bar",
                            tint = GupSupRed,
                            modifier = Modifier.size(22.dp)
                        )
                        // Live News Indicator Dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GupSupRed)
                                .border(1.5.dp, GupSupSurface, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoryRail(
    stories: List<Story>,
    onStoryClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(stories) { index, story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .testTag("story_item_${story.username}")
                    .clickable { onStoryClick(index) }
            ) {
                GupSupAvatar(
                    name = story.username,
                    colorIndex = story.avatarColorIndex,
                    size = 52.dp,
                    hasActiveStory = !story.isSeen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = story.username,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = if (story.isSeen) GupSupDim else GupSupText
                )
            }
        }
    }
}

@Composable
fun PostCard(
    post: PostEntity,
    index: Int,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onCommentClick: () -> Unit,
    onOpenSparkAi: () -> Unit
) {
    val postColor = parseColorHex(post.mediaColorHex, getPaletteColor(index))
    var isLikeScaleAnimated by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isLikeScaleAnimated) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "like_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}")
            .padding(bottom = 8.dp)
    ) {
        // Author row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GupSupAvatar(
                    name = post.username,
                    colorIndex = post.avatarColorIndex,
                    size = 38.dp
                )
                Column {
                    Text(
                        text = "@${post.username}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GupSupText
                    )
                    Text(
                        text = "${post.createdAt} · ${post.location}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = GupSupDim
                    )
                }
            }

            IconButton(onClick = { /* more options */ }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Options",
                    tint = GupSupMuted
                )
            }
        }

        // Media Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(postColor)
        ) {
            FrameGridOverlay()

            // Frame Number & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "FRAME 0${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GupSupText
                )
                Text(
                    text = post.filterName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GupSupText.copy(alpha = 0.7f)
                )
            }

            // Center Mark
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp)
                    .border(1.dp, GupSupText.copy(alpha = 0.4f), CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (post.mediaType == "reel") Icons.Default.PlayArrow else Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = GupSupText,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Frame Title
            if (post.frameTitle.isNotBlank()) {
                Text(
                    text = post.frameTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = GupSupText,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                )
            }
        }

        // Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .testTag("like-${post.id}")
                    .clickable {
                        isLikeScaleAnimated = true
                        onLike()
                    }
                    .padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) GupSupRed else GupSupText,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scaleAnim)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = post.likes.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = GupSupMuted,
                    fontSize = 13.sp
                )
            }

            // Comment
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .testTag("comment-${post.id}")
                    .clickable(onClick = onCommentClick)
                    .padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = GupSupText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = post.commentsCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = GupSupMuted,
                    fontSize = 13.sp
                )
            }

            // Share / Send
            IconButton(
                onClick = { /* share */ },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Share",
                    tint = GupSupText,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bookmark
            IconButton(
                onClick = onBookmark,
                modifier = Modifier.size(34.dp).testTag("bookmark-${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (post.isBookmarked) GupSupRed else GupSupText,
                    modifier = Modifier.size(23.dp)
                )
            }
        }

        // Caption & Tags
        Column {
            Text(
                text = "${post.username}  ${post.caption}",
                style = MaterialTheme.typography.bodyLarge,
                color = GupSupMuted,
                lineHeight = 21.sp
            )
            if (post.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.tags,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GupSupBlue,
                    fontSize = 12.sp
                )
            }
        }
    }
}
