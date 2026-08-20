package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CommentEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NewsArticle
import com.example.data.model.PostEntity
import com.example.data.model.Story
import com.example.data.repository.GupSupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import com.example.data.util.FastDataProcessor
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GupSupUiState(
    val currentTab: String = "home", // "home", "explore", "reels", "create", "profile"
    val activeStoryIndex: Int? = null,
    val activeCommentPost: PostEntity? = null,
    val activeNewsArticle: NewsArticle? = null,
    val isNewsBarOpen: Boolean = false,
    val isComposerOpen: Boolean = false,
    val isMessagesOpen: Boolean = false,
    val isSparkAiOpen: Boolean = false,
    val activeChatPartnerId: String = "spark",
    val searchQuery: String = "",
    val selectedExploreCategory: String = "All",
    val selectedNewsSource: String = "All",
    val isNewsRefreshing: Boolean = false,
    val statusMessage: String? = null
)

class GupSupViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GupSupRepository(application)

    val posts: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reels: StateFlow<List<PostEntity>> = repository.allReels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newsArticles: StateFlow<List<NewsArticle>> = repository.newsArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getInitialNewsArticles())

    private val _stories = MutableStateFlow<List<Story>>(repository.getSampleStories())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _uiState = MutableStateFlow(GupSupUiState())
    val uiState: StateFlow<GupSupUiState> = _uiState.asStateFlow()

    init {
        // High-efficiency parallel indexing for large feed inputs
        viewModelScope.launch {
            posts.collect { currentPosts ->
                if (currentPosts.isNotEmpty()) {
                    FastDataProcessor.instance.indexPosts(currentPosts)
                }
            }
        }
        viewModelScope.launch {
            newsArticles.collect { currentNews ->
                if (currentNews.isNotEmpty()) {
                    FastDataProcessor.instance.indexNewsArticles(currentNews)
                }
            }
        }
    }

    fun setTab(tab: String) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setNewsBarVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isNewsBarOpen = visible)
    }

    fun openStory(index: Int) {
        _uiState.value = _uiState.value.copy(activeStoryIndex = index)
    }

    fun closeStory() {
        _uiState.value = _uiState.value.copy(activeStoryIndex = null)
    }

    fun openNewsArticle(article: NewsArticle) {
        _uiState.value = _uiState.value.copy(activeNewsArticle = article)
    }

    fun closeNewsArticle() {
        _uiState.value = _uiState.value.copy(activeNewsArticle = null)
    }

    fun setSelectedNewsSource(source: String) {
        _uiState.value = _uiState.value.copy(selectedNewsSource = source)
    }

    fun refreshAiNews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isNewsRefreshing = true)
            try {
                repository.refreshAiNews(_uiState.value.selectedNewsSource)
            } finally {
                _uiState.value = _uiState.value.copy(isNewsRefreshing = false)
            }
        }
    }

    fun toggleNewsLike(articleId: String) {
        repository.toggleNewsLike(articleId)
    }

    fun toggleNewsBookmark(articleId: String) {
        repository.toggleNewsBookmark(articleId)
    }

    suspend fun getNewsDeepDive(headline: String, source: String): String {
        return repository.getNewsDeepDive(headline, source)
    }

    fun openCommentSheet(post: PostEntity) {
        _uiState.value = _uiState.value.copy(activeCommentPost = post)
    }

    fun closeCommentSheet() {
        _uiState.value = _uiState.value.copy(activeCommentPost = null)
    }

    fun setComposerVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isComposerOpen = visible)
    }

    fun setMessagesVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isMessagesOpen = visible)
    }

    fun setSparkAiVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isSparkAiOpen = visible)
    }

    fun setActiveChatPartner(partnerId: String) {
        _uiState.value = _uiState.value.copy(activeChatPartnerId = partnerId)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setExploreCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedExploreCategory = category)
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val post = posts.value.find { it.id == postId } ?: return@launch
            repository.toggleLikePost(postId, post.isLiked, post.likes)
        }
    }

    fun toggleBookmark(postId: String) {
        viewModelScope.launch {
            val post = posts.value.find { it.id == postId } ?: return@launch
            repository.toggleBookmarkPost(postId, post.isBookmarked)
        }
    }

    fun addComment(postId: String, text: String, isSparkAi: Boolean = false) {
        viewModelScope.launch {
            repository.addComment(postId, text, "you", isSparkAi)
        }
    }

    fun toggleCommentLike(commentId: String, currentLiked: Boolean, currentLikes: Int) {
        viewModelScope.launch {
            repository.toggleLikeComment(commentId, currentLiked, currentLikes)
        }
    }

    fun createPost(
        caption: String,
        mediaType: String,
        frameTitle: String = "",
        colorHex: String = "#1557C0",
        filterName: String = "Original",
        tags: String = "#gupsup"
    ) {
        viewModelScope.launch {
            repository.createPost(
                caption = caption,
                mediaType = mediaType,
                frameTitle = frameTitle,
                mediaColorHex = colorHex,
                filterName = filterName,
                tags = tags
            )
            setComposerVisible(false)
            setTab("home")
        }
    }

    fun sendMessage(partnerId: String, partnerName: String, text: String) {
        viewModelScope.launch {
            repository.sendMessage(partnerId, partnerName, text, isFromUser = true)
        }
    }

    suspend fun generateSparkCaption(prompt: String, tone: String): String {
        return repository.generateSparkCaption(prompt, tone)
    }

    suspend fun generateReelAudioIdea(reelTitle: String, reelCaption: String): String {
        return repository.generateReelSparkAudioIdea(reelTitle, reelCaption)
    }

    fun getCommentsForPost(postId: String) = repository.getCommentsForPost(postId)

    fun getMessagesForPartner(partnerId: String) = repository.getMessagesForPartner(partnerId)
}
