package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.components.CommentControlsSheet
import com.example.ui.components.GupSupBottomNav
import com.example.ui.components.NewsBarSheet
import com.example.ui.components.NewsDetailModal
import com.example.ui.components.SparkAiModal
import com.example.ui.components.StoryViewerModal
import com.example.ui.screens.*
import com.example.ui.theme.GupSupTheme
import com.example.ui.viewmodel.GupSupViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GupSupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GupSupTheme {
                GupSupApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GupSupApp(viewModel: GupSupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val newsArticles by viewModel.newsArticles.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen Content based on active tab
            when (uiState.currentTab) {
                "home" -> HomeScreen(
                    posts = posts,
                    stories = stories,
                    newsArticles = newsArticles,
                    isNewsRefreshing = uiState.isNewsRefreshing,
                    selectedNewsSource = uiState.selectedNewsSource,
                    onOpenNewsArticle = { viewModel.openNewsArticle(it) },
                    onOpenNewsBar = { viewModel.setNewsBarVisible(true) },
                    onRefreshNews = { viewModel.refreshAiNews() },
                    onSelectNewsSource = { viewModel.setSelectedNewsSource(it) },
                    onLikePost = { viewModel.toggleLike(it) },
                    onBookmarkPost = { viewModel.toggleBookmark(it) },
                    onOpenComments = { viewModel.openCommentSheet(it) },
                    onOpenStory = { viewModel.openStory(it) },
                    onOpenMessages = { viewModel.setMessagesVisible(true) },
                    onOpenSparkAi = { viewModel.setSparkAiVisible(true) },
                    onJumpToReels = { viewModel.setTab("reels") },
                    onCreatePost = { viewModel.setComposerVisible(true) }
                )
                "explore" -> ExploreScreen(
                    posts = posts,
                    searchQuery = uiState.searchQuery,
                    selectedCategory = uiState.selectedExploreCategory,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onCategoryChange = { viewModel.setExploreCategory(it) }
                )
                "reels" -> ReelsScreen(
                    reels = reels,
                    onLikeReel = { viewModel.toggleLike(it) },
                    onBookmarkReel = { viewModel.toggleBookmark(it) },
                    onOpenComments = { viewModel.openCommentSheet(it) },
                    onOpenSparkAi = { viewModel.setSparkAiVisible(true) }
                )
                "create" -> CreateScreen(
                    onOpenComposer = { viewModel.setComposerVisible(true) }
                )
                "profile" -> ProfileScreen(
                    posts = posts
                )
            }

            // Bottom Navigation Bar
            GupSupBottomNav(
                currentTab = uiState.currentTab,
                onTabSelected = { tab ->
                    if (tab == "create") {
                        viewModel.setComposerVisible(true)
                    } else {
                        viewModel.setTab(tab)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // AI News Bar Interactive Hub
    NewsBarSheet(
        visible = uiState.isNewsBarOpen,
        articles = newsArticles,
        isRefreshing = uiState.isNewsRefreshing,
        selectedSource = uiState.selectedNewsSource,
        onDismiss = { viewModel.setNewsBarVisible(false) },
        onOpenArticle = {
            viewModel.setNewsBarVisible(false)
            viewModel.openNewsArticle(it)
        },
        onRefreshNews = { viewModel.refreshAiNews() },
        onSelectSource = { viewModel.setSelectedNewsSource(it) },
        onToggleLike = { viewModel.toggleNewsLike(it) },
        onToggleBookmark = { viewModel.toggleNewsBookmark(it) }
    )

    // AI News Deep Dive Modal
    uiState.activeNewsArticle?.let { article ->
        NewsDetailModal(
            article = article,
            onDismiss = { viewModel.closeNewsArticle() },
            onToggleLike = { viewModel.toggleNewsLike(it) },
            onToggleBookmark = { viewModel.toggleNewsBookmark(it) },
            onFetchDeepDive = { headline, source ->
                viewModel.getNewsDeepDive(headline, source)
            }
        )
    }

    // Story Fullscreen Viewer Modal
    uiState.activeStoryIndex?.let { storyIndex ->
        StoryViewerModal(
            stories = stories,
            initialStoryIndex = storyIndex,
            onDismiss = { viewModel.closeStory() },
            onSendReaction = { _, _ -> viewModel.closeStory() }
        )
    }

    // Comment Controls Bottom Sheet
    uiState.activeCommentPost?.let { post ->
        val postComments by viewModel.getCommentsForPost(post.id).collectAsState(initial = emptyList())
        CommentControlsSheet(
            postId = post.id,
            postTitle = post.frameTitle.ifBlank { "POST 01" },
            comments = postComments,
            onDismiss = { viewModel.closeCommentSheet() },
            onAddComment = { text, isSpark -> viewModel.addComment(post.id, text, isSpark) },
            onLikeComment = { id, liked, count -> viewModel.toggleCommentLike(id, liked, count) },
            onSparkPolishText = { text -> viewModel.generateSparkCaption(text, "Cinematic") }
        )
    }

    // New Frame Composer Modal
    ComposerModal(
        visible = uiState.isComposerOpen,
        onClose = { viewModel.setComposerVisible(false) },
        onSubmit = { caption, mediaType, frameTitle, colorHex, filterName, tags ->
            viewModel.createPost(
                caption = caption,
                mediaType = mediaType,
                frameTitle = frameTitle,
                colorHex = colorHex,
                filterName = filterName,
                tags = tags
            )
        },
        onSparkGenerateCaption = { prompt, tone ->
            viewModel.generateSparkCaption(prompt, tone)
        }
    )

    // Spark AI Director Studio Modal
    if (uiState.isSparkAiOpen) {
        SparkAiModal(
            onDismiss = { viewModel.setSparkAiVisible(false) },
            onApplyGeneratedText = { generatedText ->
                viewModel.setSparkAiVisible(false)
                viewModel.setComposerVisible(true)
            },
            onGenerateCaption = { prompt, tone ->
                viewModel.generateSparkCaption(prompt, tone)
            },
            onGenerateReelAudio = { title, caption ->
                viewModel.generateReelAudioIdea(title, caption)
            }
        )
    }

    // Direct Messages & AI Chat Modal
    MessagesModal(
        visible = uiState.isMessagesOpen,
        messages = messages,
        onClose = { viewModel.setMessagesVisible(false) },
        onSendMessage = { partnerId, partnerName, text ->
            viewModel.sendMessage(partnerId, partnerName, text)
        }
    )
}
