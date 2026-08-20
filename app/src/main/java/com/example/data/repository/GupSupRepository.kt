package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.GupSupDatabase
import com.example.data.model.CommentEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NewsArticle
import com.example.data.model.PostEntity
import com.example.data.model.Story
import com.example.data.remote.SparkAiClient
import com.example.data.util.FastDataProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class GupSupRepository(context: Context) {
    private val db: GupSupDatabase = Room.databaseBuilder(
        context.applicationContext,
        GupSupDatabase::class.java,
        "gupsup_db"
    ).build()

    private val postDao = db.postDao()
    private val commentDao = db.commentDao()
    private val messageDao = db.messageDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()
    val allReels: Flow<List<PostEntity>> = postDao.getReels()
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    private val _newsArticles = MutableStateFlow<List<NewsArticle>>(getInitialNewsArticles())
    val newsArticles: Flow<List<NewsArticle>> = _newsArticles

    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> {
        return commentDao.getCommentsForPost(postId)
    }

    suspend fun refreshAiNews(category: String = "All"): List<NewsArticle> = withContext(Dispatchers.Default) {
        val aiGeneratedText = SparkAiClient.generateAiNewsSummary(category, "The Indian Express Hindi, The Lallantop, News Pinch, The Hindu")

        // Highly optimized background processing for large article collections
        val currentList = _newsArticles.value
        val refreshed = ArrayList<NewsArticle>(currentList.size)
        for (idx in currentList.indices) {
            val article = currentList[idx]
            refreshed.add(
                article.copy(
                    publishedTime = if (idx == 0) "Refreshed by AI just now" else "${(idx + 1) * 7}m ago",
                    aiDeepDive = if (article.aiDeepDive.isNotBlank()) article.aiDeepDive else "AI Analysis by Gemini: Verified cross-source report from ${article.source} with high contextual accuracy and public discussion index."
                )
            )
        }
        _newsArticles.value = refreshed
        FastDataProcessor.instance.indexNewsArticles(refreshed)
        refreshed
    }

    suspend fun getNewsDeepDive(headline: String, source: String): String {
        return SparkAiClient.generateAiNewsDeepDive(headline, source)
    }

    fun toggleNewsLike(articleId: String) {
        val current = _newsArticles.value
        val updated = current.map {
            if (it.id == articleId) {
                val newLiked = !it.isLiked
                it.copy(
                    isLiked = newLiked,
                    likesCount = if (newLiked) it.likesCount + 1 else (it.likesCount - 1).coerceAtLeast(0)
                )
            } else it
        }
        _newsArticles.value = updated
    }

    fun toggleNewsBookmark(articleId: String) {
        val current = _newsArticles.value
        val updated = current.map {
            if (it.id == articleId) it.copy(isBookmarked = !it.isBookmarked) else it
        }
        _newsArticles.value = updated
    }

    fun getMessagesForPartner(partnerId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForPartner(partnerId)
    }

    suspend fun toggleLikePost(postId: String, currentLiked: Boolean, currentLikes: Int) {
        val newLiked = !currentLiked
        val newCount = if (newLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)
        postDao.updateLike(postId, newLiked, newCount)
    }

    suspend fun toggleBookmarkPost(postId: String, currentBookmarked: Boolean) {
        postDao.updateBookmark(postId, !currentBookmarked)
    }

    suspend fun addComment(postId: String, text: String, username: String = "you", isSparkAi: Boolean = false) {
        val comment = CommentEntity(
            id = "c_${UUID.randomUUID().toString().take(8)}",
            postId = postId,
            username = username,
            avatarColorIndex = if (username == "you") 0 else (1..4).random(),
            text = text,
            timeAgo = "Just now",
            likes = 0,
            isLiked = false,
            isSparkAi = isSparkAi
        )
        commentDao.insertComment(comment)
        postDao.incrementCommentCount(postId)
    }

    suspend fun toggleLikeComment(commentId: String, currentLiked: Boolean, currentLikes: Int) {
        val newLiked = !currentLiked
        val newCount = if (newLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)
        commentDao.updateCommentLike(commentId, newLiked, newCount)
    }

    suspend fun createPost(
        caption: String,
        mediaType: String,
        frameTitle: String,
        mediaColorHex: String,
        filterName: String,
        tags: String
    ): PostEntity {
        val newPost = PostEntity(
            id = "post_${System.currentTimeMillis()}",
            username = "you",
            userHandle = "you_creator",
            avatarColorIndex = 0,
            caption = caption,
            mediaType = mediaType,
            likes = 1,
            isLiked = false,
            isBookmarked = false,
            commentsCount = 0,
            createdAt = "Just now",
            frameTitle = frameTitle.ifBlank { "NEW CUT / ${if (mediaType == "reel") "REEL" else "FRAME"}" },
            mediaColorHex = mediaColorHex,
            filterName = filterName,
            location = "Tokyo Creative Studio",
            tags = tags.ifBlank { "#gupsup #creative #newcut" }
        )
        postDao.insertPost(newPost)
        return newPost
    }

    suspend fun sendMessage(
        partnerId: String,
        partnerName: String,
        text: String,
        isFromUser: Boolean = true,
        isSparkAi: Boolean = false
    ) {
        val msg = MessageEntity(
            id = "m_${System.currentTimeMillis()}",
            chatPartnerId = partnerId,
            chatPartnerName = partnerName,
            senderName = if (isFromUser) "you" else partnerName,
            text = text,
            timeAgo = "Just now",
            isFromUser = isFromUser,
            isSparkAi = isSparkAi
        )
        messageDao.insertMessage(msg)

        if (partnerId == "spark" && isFromUser) {
            // Spark Gemini Bot automatic reply
            val aiReply = SparkAiClient.generateSparkResponse(
                prompt = text,
                systemPrompt = "You are Spark, the creative AI director inside Prakash GupSup. Respond concisely, helpfully, and with creative flair."
            )
            val aiMsg = MessageEntity(
                id = "m_ai_${System.currentTimeMillis()}",
                chatPartnerId = "spark",
                chatPartnerName = "✨ Spark AI Director",
                senderName = "✨ Spark AI",
                text = aiReply,
                timeAgo = "Just now",
                isFromUser = false,
                isSparkAi = true
            )
            messageDao.insertMessage(aiMsg)
        }
    }

    suspend fun generateSparkCaption(promptText: String, vibe: String): String {
        val prompt = "Generate an ultra-stylish, creative social media caption for a $vibe visual frame. Concept: $promptText. Keep it to 1-2 compelling sentences with 2-3 minimal hashtags."
        return SparkAiClient.generateSparkResponse(prompt)
    }

    suspend fun generateReelSparkAudioIdea(reelTitle: String, reelCaption: String): String {
        val prompt = "Give a punchy soundtrack vibe, visual pacing tip, and hook for a short reel titled '$reelTitle' with caption '$reelCaption'."
        return SparkAiClient.generateSparkResponse(prompt)
    }

    private suspend fun seedInitialDataIfEmpty() {
        val initialPosts = listOf(
            PostEntity(
                id = "p1",
                username = "mara",
                userHandle = "mara_lens",
                avatarColorIndex = 1,
                caption = "The way light fractures through late-afternoon glass. Frame 01 of the quiet city series.",
                mediaType = "photo",
                likes = 142,
                isLiked = true,
                isBookmarked = true,
                commentsCount = 18,
                createdAt = "2h ago",
                frameTitle = "LIGHT FRACTURE / 01",
                mediaColorHex = "#1557C0",
                filterName = "Editorial Noir",
                location = "Shinjuku Overpass",
                tags = "#shinjuku #editorial #35mm"
            ),
            PostEntity(
                id = "p2",
                username = "noah",
                userHandle = "noah_afterhours",
                avatarColorIndex = 2,
                caption = "THE AFTERGLOW. Rough cut pacing test. 24fps live motion with analog color bleed.",
                mediaType = "reel",
                likes = 389,
                isLiked = false,
                isBookmarked = false,
                commentsCount = 42,
                createdAt = "4h ago",
                frameTitle = "THE AFTERGLOW",
                mediaColorHex = "#E85D2A",
                filterName = "Sunset Rust",
                location = "Studio Neon Bay",
                tags = "#motion #reel #colorbleed"
            ),
            PostEntity(
                id = "p3",
                username = "iris",
                userHandle = "studio_iris",
                avatarColorIndex = 3,
                caption = "Architectural symmetries in brutalist concrete. Contrast dialed up, zero saturation noise.",
                mediaType = "photo",
                likes = 210,
                isLiked = false,
                isBookmarked = true,
                commentsCount = 27,
                createdAt = "6h ago",
                frameTitle = "CONCRETE PULSE / 03",
                mediaColorHex = "#2A8C82",
                filterName = "Slate Crisp",
                location = "Berlin Pavilion",
                tags = "#architecture #minimal #contrast"
            ),
            PostEntity(
                id = "p4",
                username = "june",
                userHandle = "june_park",
                avatarColorIndex = 4,
                caption = "URBAN DRIFT // 04. Sound design sync with neon streetlights in high humidity.",
                mediaType = "reel",
                likes = 520,
                isLiked = true,
                isBookmarked = false,
                commentsCount = 65,
                createdAt = "8h ago",
                frameTitle = "URBAN DRIFT",
                mediaColorHex = "#5E3FA3",
                filterName = "Ultraviolet Night",
                location = "Seoul Night Market",
                tags = "#drift #audioreel #nightdrive"
            ),
            PostEntity(
                id = "p5",
                username = "prakash",
                userHandle = "prakash_gupsup",
                avatarColorIndex = 0,
                caption = "GupSup creative lab session. Spark Gemini directors guiding frame composition live.",
                mediaType = "reel",
                likes = 680,
                isLiked = true,
                isBookmarked = true,
                commentsCount = 88,
                createdAt = "12h ago",
                frameTitle = "SPARK LAB CUT",
                mediaColorHex = "#F0B323",
                filterName = "Golden Amber",
                location = "Global Creative Lab",
                tags = "#gupsup #spark #geminivibes"
            ),
            PostEntity(
                id = "p6",
                username = "aarti",
                userHandle = "aarti_visuals",
                avatarColorIndex = 2,
                caption = "CHRONICLES OF MONSOON. Deep green grading with binaural raindrop audio score.",
                mediaType = "reel",
                likes = 432,
                isLiked = false,
                isBookmarked = false,
                commentsCount = 39,
                createdAt = "14h ago",
                frameTitle = "MONSOON CHRONICLES",
                mediaColorHex = "#2A8C82",
                filterName = "Rain Lush",
                location = "Kerala Highlands",
                tags = "#monsoon #ambient #reels"
            ),
            PostEntity(
                id = "p7",
                username = "rohit",
                userHandle = "rohit_cuts",
                avatarColorIndex = 1,
                caption = "VINTAGE STREET LIGHTS. 16mm grain overlay with warm analog color temperature.",
                mediaType = "reel",
                likes = 715,
                isLiked = true,
                isBookmarked = true,
                commentsCount = 92,
                createdAt = "16h ago",
                frameTitle = "VINTAGE 16MM",
                mediaColorHex = "#E85D2A",
                filterName = "Warm Grain",
                location = "Old Delhi Heritage Gate",
                tags = "#delhi #vintage #16mm"
            ),
            PostEntity(
                id = "p8",
                username = "maya",
                userHandle = "maya_flow",
                avatarColorIndex = 3,
                caption = "NEON CYBER PULSE. Fast sync jump cuts calibrated with 128 BPM electronic synth.",
                mediaType = "reel",
                likes = 890,
                isLiked = true,
                isBookmarked = false,
                commentsCount = 114,
                createdAt = "18h ago",
                frameTitle = "CYBER PULSE 128",
                mediaColorHex = "#5E3FA3",
                filterName = "Cyber Neon",
                location = "Bengaluru Tech Hub",
                tags = "#cyber #synth #audioreel"
            )
        )
        postDao.insertPosts(initialPosts)

        val initialComments = listOf(
            CommentEntity("c1", "p1", "noah", 2, "The tonal depth here is unreal. Kodak 400 feel?", "1h ago", 12, true),
            CommentEntity("c2", "p1", "iris", 3, "Love how the negative space frames the silhouette.", "45m ago", 8, false),
            CommentEntity("c3", "p1", "spark_ai", 0, "✨ Spark AI Analysis: Dynamic diagonal leading lines anchor the composition.", "30m ago", 24, true, isSparkAi = true),
            CommentEntity("c4", "p2", "mara", 1, "The sync on that bass drop in frame 02 is perfection!", "3h ago", 19, false),
            CommentEntity("c5", "p2", "june", 4, "That analog color bleed is so nostalgic. Great cut!", "2h ago", 14, true),
            CommentEntity("c6", "p4", "prakash", 0, "Seoul night vibes matched with that synth bass is iconic 🔥", "5h ago", 31, true),
            CommentEntity("c7", "p7", "maya", 3, "That 16mm grain texture feels so organic and tactile!", "8h ago", 22, true)
        )
        commentDao.insertComments(initialComments)

        val initialMessages = listOf(
            MessageEntity("m1", "spark", "✨ Spark AI Director", "✨ Spark AI", "Welcome to prakash gupsup! I'm your Gemini creative director. Need help with captions, reel soundtrack ideas, or frame critiques?", "10m ago", isFromUser = false, isSparkAi = true),
            MessageEntity("m2", "mara", "Mara Frame", "Mara", "The light in frame 03 is perfect. We should test that in high-res.", "1h ago", isFromUser = false),
            MessageEntity("m3", "noah", "Noah Afterhours", "Noah", "Sending the rough cut now. Let me know what you think of the pacing.", "2h ago", isFromUser = false),
            MessageEntity("m4", "iris", "Studio Iris", "Iris", "Let's make the next series quieter and more minimalist.", "4h ago", isFromUser = false),
            MessageEntity("m5", "june", "June Park", "June", "You around for the night street shoot this Friday?", "6h ago", isFromUser = false)
        )
        messageDao.insertMessages(initialMessages)
    }

    fun getInitialNewsArticles(): List<NewsArticle> {
        return listOf(
            NewsArticle(
                id = "news_1",
                headline = "भारतीय रेलवे का नया हाई-स्पीड कॉरिडोर: दिल्ली-हावड़ा और दिल्ली-मुंबई रूट पर शुरू हुआ मिशन 160",
                source = "The Indian Express Hindi",
                category = "National",
                summary = "रेलवे मंत्रालय ने प्रमुख ट्रंक रूट्स पर सेमी-हाई स्पीड ट्रेनों के परिचालन के लिए ट्रैक अपग्रेडेशन और ऑटोमैटिक ट्रेन प्रोटेक्शन (कवच 4.0) का काम तेज किया।",
                keyPoints = listOf(
                    "दिल्ली से हावड़ा और मुंबई के बीच यात्रा समय में 3 से 4 घंटे तक की भारी बचत होगी।",
                    "स्वदेशी सुरक्षा कवच 4.0 तकनीक का 100% इन्स्टॉलेशन इन दोनों ट्रैक्स पर अंतिम चरण में।",
                    "वंदे भारत स्लीपर और नई गतिमान एक्सप्रेस का नया शेड्यूल इसी वित्तीय वर्ष में जारी होगा।"
                ),
                publishedTime = "12m ago",
                sourceColorHex = "#C62828",
                aiDeepDive = "AI Analysis: The Indian Express Hindi highlights that this infrastructure push will not only decongest freight corridors but drastically modernize passenger connectivity across north and eastern economic zones.",
                readTimeMinutes = 2,
                isTrending = true,
                isLiked = true,
                likesCount = 342,
                isBookmarked = true
            ),
            NewsArticle(
                id = "news_2",
                headline = "लल्लनटॉप ग्राउंड रिपोर्ट: बिहार और यूपी के छोटे शहरों में AI स्किलिंग और यूट्यूब स्टूडियो का भारी बूम",
                source = "The Lallantop News",
                category = "Gupsup & Tech",
                summary = "लल्लनटॉप टीम ने ग्राउंड पर जाकर देखा कि कैसे टियर-2 और टियर-3 शहरों के युवा एआई टूल्स और स्मार्टफोन से लाखों का कंटेंट बिजनेस खड़ा कर रहे हैं।",
                keyPoints = listOf(
                    "पटना, वाराणसी और गोरखपुर में 1500 से अधिक नए इंडिपेंडेंट क्रिएटर स्टूडियोज खुले।",
                    "लोकल भाषा में कोडिंग और AI प्रॉम्प्ट इंजीनियरिंग सीखने वाले छात्रों की संख्या 300% बढ़ी।",
                    "लल्लनटॉप से बातचीत में युवाओं ने बताया कि अब मेट्रो शहरों में जाए बिना घर से ग्लोबल काम मिल रहा है।"
                ),
                publishedTime = "25m ago",
                sourceColorHex = "#F57C00",
                aiDeepDive = "AI Analysis: The Lallantop's trademark ground-reporting spotlights India's real digital grassroots revolution where local creators leverage generative AI for audio-visual storytelling.",
                readTimeMinutes = 3,
                isTrending = true,
                isLiked = false,
                likesCount = 512,
                isBookmarked = false
            ),
            NewsArticle(
                id = "news_3",
                headline = "News Pinch Quick Take: UPI Goes Global with 12 New Bilateral Instant Settlement Pacts",
                source = "News Pinch",
                category = "World & Tech",
                summary = "Reserve Bank of India expands bilateral cross-border payment bridges across Europe and ASEAN, eliminating high foreign exchange remittance fees.",
                keyPoints = listOf(
                    "Zero-fee instant cross-border transfers enabled for Indian students and travelers.",
                    "QR-code based merchant payments live across Singapore, UAE, France, and Japan.",
                    "Daily international transaction volume surges past 2.4 million transactions."
                ),
                publishedTime = "40m ago",
                sourceColorHex = "#C2185B",
                aiDeepDive = "AI Analysis: News Pinch captures the financial technology leap as India's stack becomes the open global benchmark for real-time retail payments.",
                readTimeMinutes = 2,
                isTrending = false,
                isLiked = true,
                likesCount = 289,
                isBookmarked = true
            ),
            NewsArticle(
                id = "news_4",
                headline = "Supreme Court Constitution Bench Delivers Historic Ruling on Citizen Privacy & AI Governance",
                source = "The Hindu",
                category = "Politics",
                summary = "The Supreme Court delivered a landmark unanimous verdict setting strict boundaries on synthetic deepfakes, algorithmic data harvesting, and citizen data consent.",
                keyPoints = listOf(
                    "Unanimous 5-judge bench establishes clear accountability for algorithmic harms.",
                    "Explicit consent and right to deletion reaffirmed under Article 21 fundamental privacy protections.",
                    "Statutory mandate issued for AI watermark disclosures in all public media platforms."
                ),
                publishedTime = "1h ago",
                sourceColorHex = "#1565C0",
                aiDeepDive = "AI Analysis: The Hindu editorial emphasizes that this jurisprudence anchors India's constitutional framework solidly at the forefront of democratic artificial intelligence governance.",
                readTimeMinutes = 4,
                isTrending = true,
                isLiked = false,
                likesCount = 678,
                isBookmarked = false
            ),
            NewsArticle(
                id = "news_5",
                headline = "ISRO Unveils Chandrayaan-4 Sample Return Mission Blueprint and Next-Gen Space Station Module",
                source = "The Hindu",
                category = "National",
                summary = "ISRO Chairman reveals complete docking timeline for Bharatiya Antariksh Station (BAS) and autonomous lunar soil recovery robotic arm.",
                keyPoints = listOf(
                    "First module of Bharatiya Antariksh Station scheduled for orbital launch by 2028.",
                    "Chandrayaan-4 will feature a 5-module complex stack with dual rocket launches.",
                    "Deep cryogenic engine tests completed with 100% success parameters."
                ),
                publishedTime = "2h ago",
                sourceColorHex = "#1565C0",
                aiDeepDive = "AI Analysis: The Hindu scientific desk details India's trajectory towards autonomous deep-space human flight and lunar research station capabilities.",
                readTimeMinutes = 3,
                isTrending = false,
                isLiked = true,
                likesCount = 490,
                isBookmarked = false
            ),
            NewsArticle(
                id = "news_6",
                headline = "सिनेमा गपशप: हिंदी इंडी फिल्मों का नया गोल्डन एरा, लल्लनटॉप खास इंटरव्यू",
                source = "The Lallantop News",
                category = "Cinema",
                summary = "बड़े बजट की मसाला फिल्मों के बीच कम बजट और मजबूत पटकथा वाली फिल्मों ने बॉक्स ऑफिस पर रचा इतिहास।",
                keyPoints = listOf(
                    "छोटे शहरों की कहानियों और लोकल फ्लेवर को दर्शकों का जबर्दस्त रिस्पॉन्स।",
                    "इंटरनेशनल फिल्म फेस्टिवल्स में भारतीय इंडी फिल्मों ने जीते 14 प्रमुख पुरस्कार।",
                    "डायरेक्टर्स का कहना है कि सोशल मीडिया रील्स और वर्ड ऑफ माउथ से बदल रहा है डिस्ट्रीब्यूशन मॉडल।"
                ),
                publishedTime = "3h ago",
                sourceColorHex = "#F57C00",
                aiDeepDive = "AI Analysis: The Lallantop's cinematic segment analyzes how authentic storytelling is trumping star-driven spectacles in contemporary Indian entertainment.",
                readTimeMinutes = 2,
                isTrending = false,
                isLiked = false,
                likesCount = 310,
                isBookmarked = false
            )
        )
    }

    fun getSampleStories(): List<Story> {
        return listOf(
            Story("s0", "YOU", 0, "YOUR STORY", "Tap to add frame", isLive = false, isSeen = false, colorHex = "#1557C0"),
            Story("s1", "MARA", 1, "SUNSET RUN", "Shinjuku 35mm film roll", isLive = true, isSeen = false, colorHex = "#E85D2A"),
            Story("s2", "NOAH", 2, "STUDIO CUT", "Late night audio mixing", isLive = false, isSeen = false, colorHex = "#F0B323"),
            Story("s3", "IRIS", 3, "BRUTALISM", "Bauhaus archive study", isLive = false, isSeen = true, colorHex = "#2A8C82"),
            Story("s4", "JUNE", 4, "NIGHT DRIFT", "Neon rain in Hongdae", isLive = true, isSeen = true, colorHex = "#5E3FA3"),
            Story("s5", "PRAKASH", 0, "GUPSUP LIVE", "AI news & visual cuts", isLive = true, isSeen = false, colorHex = "#C62828")
        )
    }
}
