package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelsScreen(
    reels: List<PostEntity>,
    onLikeReel: (String) -> Unit,
    onBookmarkReel: (String) -> Unit,
    onOpenComments: (PostEntity) -> Unit,
    onOpenSparkAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayReels = if (reels.isEmpty()) {
        listOf(
            PostEntity(
                id = "r_fallback",
                username = "noah",
                userHandle = "noah_afterhours",
                caption = "THE AFTERGLOW. Rough cut 24fps motion test.",
                mediaType = "reel",
                likes = 389,
                frameTitle = "THE\nAFTERGLOW",
                mediaColorHex = "#E85D2A",
                filterName = "Sunset Rust",
                createdAt = "4h ago"
            )
        )
    } else reels

    val endlessPageCount = if (displayReels.isNotEmpty()) 10000 else 1
    val initialPage = if (displayReels.isNotEmpty()) (5000 / displayReels.size) * displayReels.size else 0
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { endlessPageCount }
    )
    var isMuted by remember { mutableStateOf(false) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterNames = listOf("Original", "Editorial Noir", "Sunset Rust", "Slate Crisp", "Neon Night")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GupSupBg)
            .testTag("reels_screen")
    ) {
        // Vertical Endless Pager for Reels
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("endless_reels_pager")
        ) { virtualIndex ->
            val actualIndex = virtualIndex % displayReels.size
            val reel = displayReels[actualIndex]
            ReelStage(
                reel = reel,
                index = actualIndex,
                virtualIndex = virtualIndex,
                isMuted = isMuted,
                currentFilter = filterNames[selectedFilterIndex],
                onToggleMute = { isMuted = !isMuted },
                onCycleFilter = {
                    selectedFilterIndex = (selectedFilterIndex + 1) % filterNames.size
                },
                onLike = { onLikeReel(reel.id) },
                onBookmark = { onBookmarkReel(reel.id) },
                onOpenComments = { onOpenComments(reel) },
                onOpenSparkAi = onOpenSparkAi
            )
        }

        // Floating Top Header with Schoolbell font
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).background(GupSupRed, CircleShape))
                Column {
                    Text(
                        text = "prakash gupsup",
                        style = SchoolbellHeadingStyle,
                        color = GupSupRed,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "ENDLESS 24FPS STREAM // LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupText,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .size(38.dp)
                    .background(GupSupSurface.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, GupSupLine, CircleShape)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Toggle Mute",
                    tint = GupSupText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ReelStage(
    reel: PostEntity,
    index: Int,
    virtualIndex: Int = index,
    isMuted: Boolean,
    currentFilter: String,
    onToggleMute: () -> Unit,
    onCycleFilter: () -> Unit,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenSparkAi: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }

    val reelColor = parseColorHex(reel.mediaColorHex, getPaletteColor(index))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 82.dp, start = 14.dp, end = 14.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(reelColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onLike()
                        showDoubleTapHeart = true
                        coroutineScope.launch {
                            delay(800)
                            showDoubleTapHeart = false
                        }
                    },
                    onTap = {
                        isPlaying = !isPlaying
                    }
                )
            }
    ) {
        FrameGridOverlay()

        // Equalizer Sound Wave simulation
        if (isPlaying && !isMuted) {
            AudioWaveEqualizer(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            )
        }

        // Center Play / Pause Indicator
        AnimatedVisibility(
            visible = !isPlaying,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Double Tap Floating Heart
        AnimatedVisibility(
            visible = showDoubleTapHeart,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = GupSupRed,
                modifier = Modifier.size(100.dp)
            )
        }

        // Bottom Left Reel Metadata & Copy
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OverlineTag(
                    text = "NOW PLAYING / 0${index + 1}",
                    color = GupSupText
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GupSupText.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentFilter.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = GupSupText
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = reel.frameTitle.ifBlank { "THE\nAFTERGLOW" },
                style = MaterialTheme.typography.displayLarge,
                color = GupSupText,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GupSupAvatar(name = reel.username, colorIndex = reel.avatarColorIndex, size = 24.dp)
                Text(
                    text = "@${reel.username}",
                    style = MaterialTheme.typography.titleMedium,
                    color = GupSupText,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = reel.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = GupSupMuted,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio track ticker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GupSupSurface.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = GupSupRed, modifier = Modifier.size(14.dp))
                Text(
                    text = "Original Sound · 24fps Analog Bleed",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = GupSupText
                )
            }
        }

        // Rich Right-Side Reel Action Rail
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLike,
                    modifier = Modifier
                        .testTag("reel-like-${reel.id}")
                        .size(46.dp)
                        .background(GupSupSurface.copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, GupSupLine, CircleShape)
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Reel",
                        tint = if (reel.isLiked) GupSupRed else GupSupText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = reel.likes.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = GupSupText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Comment Controls Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onOpenComments,
                    modifier = Modifier
                        .testTag("reel-comments-${reel.id}")
                        .size(46.dp)
                        .background(GupSupSurface.copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, GupSupLine, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Reel Comments",
                        tint = GupSupText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = reel.commentsCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = GupSupText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Spark AI Remix / Idea Generator Action
            IconButton(
                onClick = onOpenSparkAi,
                modifier = Modifier
                    .testTag("reel-spark-ai-${reel.id}")
                    .size(46.dp)
                    .background(GupSupRed, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Spark Remix",
                    tint = GupSupBg,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Filter Preset Cycler
            IconButton(
                onClick = onCycleFilter,
                modifier = Modifier
                    .testTag("reel-filter-${reel.id}")
                    .size(42.dp)
                    .background(GupSupSurface.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, GupSupLine, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterVintage,
                    contentDescription = "Filter",
                    tint = GupSupBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bookmark / Save Action
            IconButton(
                onClick = onBookmark,
                modifier = Modifier
                    .size(42.dp)
                    .background(GupSupSurface.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, GupSupLine, CircleShape)
            ) {
                Icon(
                    imageVector = if (reel.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save Reel",
                    tint = if (reel.isBookmarked) GupSupRed else GupSupText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AudioWaveEqualizer(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(3.dp).height(h1.dp).background(Color.White, CircleShape))
        Box(modifier = Modifier.width(3.dp).height(h2.dp).background(Color.White, CircleShape))
        Box(modifier = Modifier.width(3.dp).height(h3.dp).background(Color.White, CircleShape))
    }
}
