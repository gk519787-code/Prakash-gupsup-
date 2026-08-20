package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val username: String,
    val userHandle: String,
    val avatarColorIndex: Int = 0,
    val caption: String,
    val mediaType: String, // "photo", "reel", "story", "text"
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val commentsCount: Int = 0,
    val createdAt: String,
    val frameTitle: String = "",
    val mediaColorHex: String = "#1557C0",
    val filterName: String = "Original",
    val location: String = "Studio / Tokyo Light",
    val tags: String = "#film #cinematic #gupsup",
    val sparkAiCritique: String = "Strong negative space balance with high-contrast framing."
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val username: String,
    val avatarColorIndex: Int = 0,
    val text: String,
    val timeAgo: String,
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val isSparkAi: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatPartnerId: String, // "mara", "noah", "iris", "june", "spark"
    val chatPartnerName: String,
    val senderName: String,
    val text: String,
    val timeAgo: String,
    val isFromUser: Boolean = false,
    val isSparkAi: Boolean = false
)

data class Story(
    val id: String,
    val username: String,
    val avatarColorIndex: Int,
    val headline: String,
    val subhead: String,
    val isLive: Boolean = false,
    val isSeen: Boolean = false,
    val colorHex: String = "#1557C0"
)

data class NewsArticle(
    val id: String,
    val headline: String,
    val source: String, // "The Indian Express Hindi", "The Lallantop News", "News Pinch", "The Hindu"
    val category: String, // "Breaking", "National", "Politics", "World & Tech", "Cinema"
    val summary: String,
    val keyPoints: List<String> = emptyList(),
    val publishedTime: String = "Just now",
    val sourceColorHex: String = "#E85D2A",
    val aiDeepDive: String = "",
    val readTimeMinutes: Int = 2,
    val isTrending: Boolean = false,
    val isLiked: Boolean = false,
    val likesCount: Int = 124,
    val isBookmarked: Boolean = false
)

data class ActivityCard(
    val id: String,
    val tag: String,
    val title: String,
    val subtitle: String,
    val highlightText: String,
    val statLabel: String,
    val statValue: String,
    val actionType: ActivityActionType,
    val bgHex: String
)

enum class ActivityActionType {
    NEWS_RADAR,
    SPARK_AI,
    CREATE_REEL,
    VIEW_TRENDING
}
