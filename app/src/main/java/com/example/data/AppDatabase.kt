package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String,
    val subscriptionTier: String = "Free", // "Free", "Starter", "Growth", "VIP"
    val isLoggedIn: Boolean = false,
    val isPremium: Boolean = false,
    val joinDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // e.g., "Amazon Seller", "TikTok Shop", "Walmart Marketplace", "Etsy", "Shopify", "Product Research", "Supplier List", "Digital Products", "Marketing and Ads"
    val title: String,
    val content: String,
    val isCompleted: Boolean = false,
    val lastAccessed: Long = 0L,
    val isPremium: Boolean = false,
    val tierNeeded: String = "Free", // "Free", "Starter", "Growth", "VIP"
    val durationMin: Int = 10,
    val isUploadedByAdmin: Boolean = false
)

@Entity(tableName = "resources")
data class Resource(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Checklist", "Template", "Supplier Sheet", "Guide"
    val description: String,
    val fileType: String, // "PDF", "XLSX", "Google Doc", "Sheet"
    val fileSize: String, // "128 KB", "1.5 MB", etc.
    val contentStub: String, // Complete interactive viewable instructions inside the app
    val downloadCount: Int = 0,
    val isUploadedByAdmin: Boolean = false
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val authorName: String,
    val authorTier: String = "Free", // e.g. "Free", "Starter", "Growth", "VIP", "Admin"
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    val category: String = "General", // "General", "Q&A", "Wins", "Support"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_replies")
data class CommunityReply(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val authorName: String,
    val authorTier: String = "Free",
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 2. DAOs
// ==========================================

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET subscriptionTier = :tier, isPremium = :isPremium WHERE email = :email")
    suspend fun updateSubscription(email: String, tier: String, isPremium: Boolean)

    @Query("UPDATE user_profile SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY id ASC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: Int): Lesson?

    @Query("SELECT * FROM lessons WHERE isCompleted = 1")
    fun getCompletedLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE lastAccessed > 0 ORDER BY lastAccessed DESC LIMIT 1")
    fun getLastAccessedLesson(): Flow<Lesson?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<Lesson>)

    @Update
    suspend fun updateLesson(lesson: Lesson)

    @Query("UPDATE lessons SET isCompleted = :completed, lastAccessed = :time WHERE id = :id")
    suspend fun updateProgress(id: Int, completed: Boolean, time: Long)

    @Query("UPDATE lessons SET lastAccessed = :time WHERE id = :id")
    suspend fun updateLastAccessed(id: Int, time: Long)

    @Query("UPDATE lessons SET isCompleted = 0")
    suspend fun resetAllProgress()
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources ORDER BY id DESC")
    fun getAllResources(): Flow<List<Resource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: Resource)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<Resource>)

    @Query("UPDATE resources SET downloadCount = downloadCount + 1 WHERE id = :id")
    suspend fun incrementDownloadCount(id: Int)
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Query("SELECT * FROM community_posts WHERE category = :category ORDER BY timestamp DESC")
    fun getPostsByCategory(category: String): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)

    @Query("UPDATE community_posts SET likesCount = likesCount + 1 WHERE id = :postId")
    suspend fun likePost(postId: Int)

    @Query("UPDATE community_posts SET repliesCount = repliesCount + 1 WHERE id = :postId")
    suspend fun incrementRepliesCount(postId: Int)
}

@Dao
interface CommunityReplyDao {
    @Query("SELECT * FROM community_replies WHERE postId = :postId ORDER BY timestamp ASC")
    fun getRepliesForPost(postId: Int): Flow<List<CommunityReply>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: CommunityReply)
}

// ==========================================
// 3. Database
// ==========================================

@Database(
    entities = [
        UserProfile::class,
        Lesson::class,
        Resource::class,
        CommunityPost::class,
        CommunityReply::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun lessonDao(): LessonDao
    abstract fun resourceDao(): ResourceDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun communityReplyDao(): CommunityReplyDao
}
