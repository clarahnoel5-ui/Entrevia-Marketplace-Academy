package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppNotification(
    val id: Int,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AcademyRepository

    // Base UI flows from Room
    val activeProfile: StateFlow<UserProfile?>
    val lessons: StateFlow<List<Lesson>>
    val completedLessons: StateFlow<List<Lesson>>
    val lastAccessedLesson: StateFlow<Lesson?>
    val resources: StateFlow<List<Resource>>
    val posts: StateFlow<List<CommunityPost>>

    // Live filtering and ephemeral states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _viewingLesson = MutableStateFlow<Lesson?>(null)
    val viewingLesson: StateFlow<Lesson?> = _viewingLesson.asStateFlow()

    private val _viewingPost = MutableStateFlow<CommunityPost?>(null)
    val viewingPost: StateFlow<CommunityPost?> = _viewingPost.asStateFlow()

    val viewingPostReplies: StateFlow<List<CommunityReply>> = _viewingPost
        .flatMapLatest { post ->
            if (post != null) {
                repository.getRepliesForPost(post.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Chat Bot Ephemeral States
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Welcome to the Entrevia Academy! 🚀 I am your AI Business Coach. Ask me how to find winning products, compose product descriptions, or draft an LLC legal checklist. How can I serve your business goals today?",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    // Notification State Machine
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    init {
        val database = DatabaseProvider.getDatabase(application)
        repository = AcademyRepository(database)

        // Bind flows from Database
        activeProfile = repository.activeProfile.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        lessons = repository.allLessons.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        completedLessons = repository.completedLessons.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        lastAccessedLesson = repository.lastAccessedLesson.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        resources = repository.allResources.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        posts = repository.allPosts.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        // Run Pre-population and setup default push notifications
        viewModelScope.launch {
            repository.prepopulateIfNeeded()
            setupInitialNotifications()
        }
    }

    // ==========================================
    // Core Navigation & Search Actions
    // ==========================================
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun startViewingLesson(lesson: Lesson) {
        _viewingLesson.value = lesson
        viewModelScope.launch {
            repository.markLessonAccessed(lesson.id)
        }
    }

    fun stopViewingLesson() {
        _viewingLesson.value = null
    }

    fun startViewingPost(post: CommunityPost) {
        _viewingPost.value = post
    }

    fun stopViewingPost() {
        _viewingPost.value = null
    }

    // ==========================================
    // Sells & Account Setup Core Actions
    // ==========================================
    fun handleLogin(email: String, name: String) {
        viewModelScope.launch {
            if (email.isNotBlank() && name.isNotBlank()) {
                repository.createOrLoginUser(email, name)
                sendSystemNotification(
                    "Welcome Aboard!",
                    "Hi $name, you have successfully logged in. Let's grow your online e-commerce business!"
                )
            }
        }
    }

    fun handleLogout() {
        viewModelScope.launch {
            repository.logoutUser()
            stopViewingLesson()
            stopViewingPost()
        }
    }

    fun purchaseSubscription(tier: String) {
        viewModelScope.launch {
            val user = activeProfile.firstOrNull() ?: activeProfile.value
            if (user != null) {
                repository.upgradeSubscription(user.email, tier)
                sendSystemNotification(
                    "Plan Upgraded! 💎",
                    "Congratulations! You are now subscribed to the $tier Plan. Premium templates and custom digital lessons unlocked."
                )
            }
        }
    }

    // ==========================================
    // Course Progression
    // ==========================================
    fun toggleLessonCompletion(lessonId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateLessonProgress(lessonId, !currentStatus)
            
            // Refresh currently viewed lesson state to update checkmark dynamically
            val currentLvl = viewingLesson.value
            if (currentLvl != null && currentLvl.id == lessonId) {
                _viewingLesson.value = currentLvl.copy(isCompleted = !currentStatus)
            }

            if (!currentStatus) {
                sendSystemNotification(
                    "Lesson Completed! 🎓",
                    "Way to go! Keep up the incredible momentum, consistency builds empires."
                )
            }
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            sendSystemNotification(
                "Progress Reset",
                "Your learning milestones and completed checkmarks have been successfully reset."
            )
        }
    }

    // ==========================================
    // Resource Library
    // ==========================================
    fun downloadResourceStub(resourceId: Int, title: String) {
        viewModelScope.launch {
            repository.incrementDownloadCount(resourceId)
            sendSystemNotification(
                "Resource Synced 📥",
                "Successfully exported physical bundle '$title' to your device. Check your offline templates!"
            )
        }
    }

    // ==========================================
    // Community Interactions
    // ==========================================
    fun addCommunityPost(title: String, content: String, category: String) {
        viewModelScope.launch {
            val user = activeProfile.value
            val author = user?.name ?: "Anonymous Founder"
            val tier = user?.subscriptionTier ?: "Free"
            
            repository.createCommunityPost(title, content, author, tier, category)
            sendSystemNotification(
                "Feedback Shared 📣",
                "Your discussion card about $category was published and pinned to the global marketplace community board."
            )
        }
    }

    fun likePost(postId: Int) {
        viewModelScope.launch {
            repository.likePost(postId)
            
            // Sync locally viewed post liked state
            val currentP = _viewingPost.value
            if (currentP != null && currentP.id == postId) {
                _viewingPost.value = currentP.copy(likesCount = currentP.likesCount + 1)
            }
        }
    }

    fun replyToPost(postId: Int, content: String) {
        viewModelScope.launch {
            val user = activeProfile.value
            val author = user?.name ?: "Anonymous Founder"
            val tier = user?.subscriptionTier ?: "Free"

            repository.replyToPost(postId, author, tier, content)
            
            // Sync locally viewed post replies counter
            val currentP = _viewingPost.value
            if (currentP != null && currentP.id == postId) {
                _viewingPost.value = currentP.copy(repliesCount = currentP.repliesCount + 1)
            }
        }
    }

    // ==========================================
    // Admin Creator System
    // ==========================================
    fun uploadCourseLesson(category: String, title: String, content: String, durationMin: Int, tierNeeded: String) {
        viewModelScope.launch {
            val lesson = Lesson(
                category = category,
                title = title,
                content = content,
                isCompleted = false,
                lastAccessed = 0L,
                isPremium = tierNeeded != "Free",
                tierNeeded = tierNeeded,
                durationMin = durationMin,
                isUploadedByAdmin = true
            )
            repository.uploadCustomLesson(lesson)
            sendSystemNotification(
                "Curriculum Expanded 📚",
                "Creator Hub: Successfully pushed lesson '$title' to the live $category directory."
            )
        }
    }

    fun uploadResourceToLibrary(title: String, category: String, description: String, fileType: String, fileSize: String, contentStub: String) {
        viewModelScope.launch {
            val res = Resource(
                title = title,
                category = category,
                description = description,
                fileType = fileType,
                fileSize = fileSize,
                contentStub = contentStub,
                downloadCount = 0,
                isUploadedByAdmin = true
            )
            repository.uploadCustomResource(res)
            sendSystemNotification(
                "Template Integrated 📁",
                "Creator Hub: Uploaded custom checklist '$title' to active student libraries."
            )
        }
    }

    // ==========================================
    // AI Business Coach Assistant Calls
    // ==========================================
    fun sendAiMessage(promptText: String) {
        if (promptText.isBlank()) return

        // 1. Append user's chat message to the queue
        val currentList = _chatMessages.value.toMutableList()
        val userMsg = ChatMessage(text = promptText, isUser = true)
        currentList.add(userMsg)
        _chatMessages.value = currentList

        // 2. Clear state, start network loading indicators
        _chatLoading.value = true

        viewModelScope.launch {
            try {
                // Compile dialogue arrays for Gemini contextual references
                val historyList = chatMessages.value
                    .drop(1) // exclude welcome greeting
                    .dropLast(1) // exclude current active prompt
                    .map { it.text to it.isUser }

                val rawResponseClean = GeminiService.generateResponse(promptText, historyList)

                val responseList = _chatMessages.value.toMutableList()
                responseList.add(ChatMessage(text = rawResponseClean, isUser = false))
                _chatMessages.value = responseList
            } catch (e: Exception) {
                val responseList = _chatMessages.value.toMutableList()
                responseList.add(ChatMessage(text = "I encountered an error analyzing your request: ${e.localizedMessage}. Please review API details in Secrets panel.", isUser = false))
                _chatMessages.value = responseList
            } finally {
                _chatLoading.value = false
            }
        }
    }

    fun resetChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "My advice desk is reset! 🧼 Let us begin a fresh brainstorm. What products or listings shall we target next?",
                isUser = false
            )
        )
        _chatLoading.value = false
    }

    // ==========================================
    // Ephemeral Push Notifications Hub
    // ==========================================
    private fun setupInitialNotifications() {
        _notifications.value = listOf(
            AppNotification(
                id = 1,
                title = "TikTok Shop Action Required! ⚠️",
                content = "General update: TikTok is rolling out individual verification changes. Check your tax information to avoid holding payouts."
            ),
            AppNotification(
                id = 2,
                title = "Alibaba Global Sourcing Trend 🌟",
                content = "Sourcing alert: Demand for custom organic cotton apparel is spiking massively for summer. Double check suppliers now."
            ),
            AppNotification(
                id = 3,
                title = "Amazon FBA Fee Adjustment",
                content = "Seasonal update: Prime fulfillment fee standard sizes updated. Check your product margin calculation sheets!"
            )
        )
    }

    fun sendSystemNotification(title: String, content: String) {
        val current = _notifications.value.toMutableList()
        val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
        current.add(0, AppNotification(id = nextId, title = title, content = content))
        _notifications.value = current
    }

    fun dismissNotification(id: Int) {
        val current = _notifications.value.filter { it.id != id }
        _notifications.value = current
    }

    fun markNotificationsAsRead() {
        val current = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = current
    }
}
