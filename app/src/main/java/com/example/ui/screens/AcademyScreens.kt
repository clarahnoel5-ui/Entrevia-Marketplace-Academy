package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Lesson
import com.example.data.Resource
import com.example.data.CommunityPost
import com.example.data.CommunityReply
import com.example.data.UserProfile
import com.example.ui.AcademyViewModel
import com.example.ui.AppNotification
import com.example.ui.ChatMessage
import com.example.ui.theme.*

// Active screens navigation descriptor mapping to Core Material Icons ONLY (Guaranteed to exist in all environments)
enum class AppScreen(val tabName: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Filled.Home),
    COURSES("Courses", Icons.Filled.PlayArrow, Icons.Filled.PlayArrow),
    AI_COACH("AI Coach", Icons.Filled.Send, Icons.Filled.Send),
    LIBRARY("Library", Icons.Filled.List, Icons.Filled.List),
    COMMUNITY("Community", Icons.Filled.AccountCircle, Icons.Filled.AccountCircle),
    PROFILE("Profile", Icons.Filled.Person, Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntreviaAcademyApp(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val activeUser by viewModel.activeProfile.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    // Linear Soft Gold Gradient for Premium Brand Elements
    val goldGradient = Brush.linearGradient(
        colors = listOf(LuxuryGoldLight, LuxuryGold, LuxuryGoldDark)
    )

    // Handle Login/Signup flow first if no active profile exists
    if (activeUser == null) {
        LoginSignupScreen(
            onLogin = { email, name -> viewModel.handleLogin(email, name) },
            goldGradient = goldGradient
        )
    } else {
        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
        var showNotificationsBlock by remember { mutableStateOf(false) }
        var showAdminPanel by remember { mutableStateOf(false) }
        var showCheckoutScreenByTier by remember { mutableStateOf<String?>(null) }

        val notifications by viewModel.notifications.collectAsStateWithLifecycle()
        val unreadNotificationCount = notifications.size

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Logo",
                                tint = LuxuryGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENTREVIA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 3.sp,
                                color = if (isDark) LuxuryGold else LuxuryNavy
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showNotificationsBlock = true },
                            modifier = Modifier.testTag("notification_bell_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationCount > 0) {
                                        Badge(containerColor = LuxuryGold) {
                                            Text(
                                                text = unreadNotificationCount.toString(),
                                                color = LuxuryNavy,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifications Board",
                                    tint = if (isDark) LuxuryGoldLight else LuxuryNavy
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (showAdminPanel) {
                            IconButton(onClick = { showAdminPanel = false }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (isDark) LuxuryGold else LuxuryNavy
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isDark) LuxuryNavy else LuxuryBeigeAccent
                    )
                )
            },
            bottomBar = {
                if (!showAdminPanel) {
                    NavigationBar(
                        containerColor = if (isDark) LuxuryNavy else Color.White,
                        tonalElevation = 8.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        AppScreen.values().forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentScreen = screen
                                    viewModel.stopViewingLesson()
                                    viewModel.stopViewingPost()
                                },
                                label = {
                                    Text(
                                        text = screen.tabName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.activeIcon,
                                        contentDescription = screen.tabName,
                                        tint = if (isSelected) {
                                            if (isDark) LuxuryGold else LuxuryNavy
                                        } else {
                                            if (isDark) LuxuryGray else Color.Gray
                                        }
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent
                                )
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showAdminPanel) {
                    AdminCreatorScreen(
                        viewModel = viewModel,
                        onClose = { showAdminPanel = false }
                    )
                } else {
                    when (currentScreen) {
                        AppScreen.HOME -> HomeScreen(
                            viewModel = viewModel,
                            user = activeUser!!,
                            goldGradient = goldGradient,
                            onTabNavigate = { currentScreen = it }
                        )
                        AppScreen.COURSES -> CourseSectionScreen(
                            viewModel = viewModel,
                            user = activeUser!!,
                            goldGradient = goldGradient
                        )
                        AppScreen.AI_COACH -> AiAssistantScreen(
                            viewModel = viewModel,
                            user = activeUser!!,
                            goldGradient = goldGradient
                        )
                        AppScreen.LIBRARY -> ResourceLibraryScreen(
                            viewModel = viewModel,
                            user = activeUser!!,
                            goldGradient = goldGradient
                        )
                        AppScreen.COMMUNITY -> CommunitySectionScreen(
                            viewModel = viewModel,
                            user = activeUser!!,
                            goldGradient = goldGradient
                        )
                        AppScreen.PROFILE -> ProfileScreen(
                            viewModel = viewModel,
                            user = activeUser!!,
                            goldGradient = goldGradient,
                            onEnterAdminDesk = { showAdminPanel = true },
                            onTriggerUpgradePay = { showCheckoutScreenByTier = it }
                        )
                    }
                }

                // Notifications Dialog Overlay
                if (showNotificationsBlock) {
                    NotificationsDialog(
                        notificationsList = notifications,
                        onDismiss = { showNotificationsBlock = false },
                        onDismissItem = { viewModel.dismissNotification(it) },
                        onClearAll = { viewModel.markNotificationsAsRead() }
                    )
                }

                // Subscription checkout Dialogue overlay
                showCheckoutScreenByTier?.let { tier ->
                    SimulatedPaymentDialog(
                        tierName = tier,
                        price = when (tier) {
                            "Starter" -> "$2.99"
                            "Growth" -> "$5.99"
                            "VIP" -> "$9.99"
                            else -> "$0.00"
                        },
                        onClose = { showCheckoutScreenByTier = null },
                        onConfirmPay = {
                            viewModel.purchaseSubscription(tier)
                            showCheckoutScreenByTier = null
                        },
                        goldGradient = goldGradient
                    )
                }
            }
        }
    }
}

// ==========================================
// A. LOGIN & SIGN-UP SCREEN
// ==========================================
@Composable
fun LoginSignupScreen(
    onLogin: (String, String) -> Unit,
    goldGradient: Brush
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryNavy)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(goldGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Entrevia",
                    tint = LuxuryNavy,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Entrevia Marketplace",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 1.sp,
                color = LuxuryGoldLight,
                textAlign = TextAlign.Center
            )
            Text(
                text = "ACADEMY",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 5.sp,
                color = LuxuryGold,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            Text(
                text = "Start, learn, and grow your online business with absolute confidence.",
                fontSize = 14.sp,
                color = LuxuryGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Text Inputs
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Rachel Green") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = LuxuryGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = LuxuryNavyLight,
                    focusedLabelColor = LuxuryGold,
                    unfocusedLabelColor = LuxuryGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("your@email.com") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = LuxuryGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = LuxuryNavyLight,
                    focusedLabelColor = LuxuryGold,
                    unfocusedLabelColor = LuxuryGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input")
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && name.isNotBlank()) {
                        onLogin(email.trim(), name.trim())
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("auth_submit_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(goldGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSignUp) "CREATE FREE ACCOUNT" else "SECURE MEMBERSHIP ENTER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = LuxuryNavy,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { isSignUp = !isSignUp }
            ) {
                Text(
                    text = if (isSignUp) "Already a registered member? Enter Workspace" else "New to Entrevia? Start FREE Consultation Profile",
                    color = LuxuryGoldLight,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Perfect for busy parents and shy entrepreneurs. No credit card required to begin learning.",
                fontSize = 11.sp,
                color = LuxuryGray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

// ==========================================
// 1. HOME COMPONENT
// ==========================================
@Composable
fun HomeScreen(
    viewModel: AcademyViewModel,
    user: UserProfile,
    goldGradient: Brush,
    onTabNavigate: (AppScreen) -> Unit
) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val completedLessons by viewModel.completedLessons.collectAsStateWithLifecycle()
    val lastAccessedLesson by viewModel.lastAccessedLesson.collectAsStateWithLifecycle()

    val totalCount = lessons.size
    val completedCount = completedLessons.size
    val progressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val progressPercent = (progressRatio * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) LuxuryNavyCard else Color.White
                ),
                border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_pitch_card")
            ) {
                Box {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Welcome Back, ${user.name}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (isSystemInDarkTheme()) Color.White else LuxuryNavy
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(goldGradient)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${user.subscriptionTier.uppercase()} MEMBER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuxuryNavy
                                )
                            }
                        }

                        Text(
                            text = "“Start, learn, and grow your online business with confidence.”",
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = LuxuryGoldLight,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = LuxuryGoldDark.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "ACADEMY LEVEL", fontSize = 11.sp, color = LuxuryGray)
                                Text(
                                    text = "$progressPercent% Complete",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isSystemInDarkTheme()) Color.White else LuxuryNavy
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "PROGRESS TRACK", fontSize = 11.sp, color = LuxuryGray)
                                Text(
                                    text = "$completedCount / $totalCount Lessons",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = LuxuryGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = progressRatio,
                            trackColor = if (isSystemInDarkTheme()) LuxuryNavyLight else LuxuryBeigeAccent,
                            color = LuxuryGold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // Resume Study Module
        item {
            Column {
                Text(
                    text = "CONTINUE LEARNING",
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) LuxuryGoldLight else LuxuryNavy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (lastAccessedLesson != null) {
                    val lesson = lastAccessedLesson!!
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) LuxuryNavyCard else Color.White),
                        border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTabNavigate(AppScreen.COURSES)
                                viewModel.startViewingLesson(lesson)
                            }
                            .testTag("resume_course_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSystemInDarkTheme()) LuxuryNavyLight else LuxuryBeigeAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = lesson.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LuxuryGold
                                )
                                Text(
                                    text = lesson.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) Color.White else LuxuryNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Estimated time: ${lesson.durationMin} mins left",
                                    fontSize = 12.sp,
                                    color = LuxuryGray
                                )
                            }

                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "Continue",
                                tint = LuxuryGold
                            )
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) LuxuryNavyCard.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ready to start your first class?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color.White else LuxuryNavy
                            )
                            Text(
                                text = "Explore the Amazon, TikTok, or Shopify classes below to begin.",
                                fontSize = 12.sp,
                                color = LuxuryGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            Button(
                                onClick = { onTabNavigate(AppScreen.COURSES) },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "View Curriculum", color = LuxuryNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Access Services Board
        item {
            Column {
                Text(
                    text = "ACADEMY HIGHLIGHTS",
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) LuxuryGoldLight else LuxuryNavy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSystemInDarkTheme()) LuxuryNavyCard else Color.White)
                            .border(1.dp, LuxuryGoldDark.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable { onTabNavigate(AppScreen.AI_COACH) }
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "AI Coach Desk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSystemInDarkTheme()) Color.White else LuxuryNavy
                        )
                        Text(
                            text = "Brainstorm in seconds",
                            fontSize = 11.sp,
                            color = LuxuryGray
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSystemInDarkTheme()) LuxuryNavyCard else Color.White)
                            .border(1.dp, LuxuryGoldDark.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable { onTabNavigate(AppScreen.LIBRARY) }
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Resource Library",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSystemInDarkTheme()) Color.White else LuxuryNavy
                        )
                        Text(
                            text = "Outlines & checklists",
                            fontSize = 11.sp,
                            color = LuxuryGray
                        )
                    }
                }
            }
        }

        // Live Community Wins Ticker
        item {
            Column {
                Text(
                    text = "STUDENT WINS",
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) LuxuryGoldLight else LuxuryNavy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) LuxuryNavyCard else Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Award",
                                tint = LuxuryGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weekly Spotlight win",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = LuxuryGoldDark
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“I've sent my first 50 private label products to Amazon FBA warehouse, and 30 sold out on the first week! The SEO optimization lesson saved my life.”",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = if (isSystemInDarkTheme()) LuxuryTextLight else LuxuryTextDark,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "— Sarah M., Mother of 2 & FBA Seller",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGray
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. COURSE SECTION COMPONENT (CATEGORIES & LESSONS)
// ==========================================
@Composable
fun CourseSectionScreen(
    viewModel: AcademyViewModel,
    user: UserProfile,
    goldGradient: Brush
) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val viewingLesson by viewModel.viewingLesson.collectAsStateWithLifecycle()

    val isDark = isSystemInDarkTheme()

    val coreCategories = listOf(
        "Amazon Seller",
        "TikTok Shop",
        "Walmart Marketplace",
        "Etsy",
        "Shopify",
        "Product Research",
        "Supplier List",
        "Digital Products",
        "Marketing and Ads"
    )

    val filteredLessons = lessons.filter { lesson ->
        val matchesSearch = lesson.title.contains(searchQuery, ignoreCase = true) ||
                lesson.content.contains(searchQuery, ignoreCase = true) ||
                lesson.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == null || lesson.category == selectedCategory
        matchesSearch && matchesCategory
    }

    if (viewingLesson != null) {
        LessonViewerScreen(
            lesson = viewingLesson!!,
            user = user,
            onBack = { viewModel.stopViewingLesson() },
            onToggleComplete = { viewModel.toggleLessonCompletion(viewingLesson!!.id, viewingLesson!!.isCompleted) },
            goldGradient = goldGradient
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search lessons, strategies, topics...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = LuxuryGold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = LuxuryGray)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                    focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                    unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                    focusedContainerColor = if (isDark) LuxuryNavyCard else Color.White,
                    unfocusedContainerColor = if (isDark) LuxuryNavyCard else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("course_search_bar")
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All Topics") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxuryGold,
                            selectedLabelColor = LuxuryNavy,
                            containerColor = if (isDark) LuxuryNavyCard else Color.White,
                            labelColor = if (isDark) Color.White else LuxuryTextDark
                        )
                    )
                }

                items(coreCategories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxuryGold,
                            selectedLabelColor = LuxuryNavy,
                            containerColor = if (isDark) LuxuryNavyCard else Color.White,
                            labelColor = if (isDark) Color.White else LuxuryTextDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredLessons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = null,
                            tint = LuxuryGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No educational modules found",
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else LuxuryNavy
                        )
                        Text(
                            text = "Try clearing search queries or select a different category.",
                            color = LuxuryGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredLessons) { lesson ->
                        LessonRowCard(
                            lesson = lesson,
                            user = user,
                            onLessonClick = { viewModel.startViewingLesson(lesson) },
                            onToggleComplete = { viewModel.toggleLessonCompletion(lesson.id, lesson.isCompleted) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonRowCard(
    lesson: Lesson,
    user: UserProfile,
    onLessonClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val userAccessScore = when (user.subscriptionTier) {
        "VIP" -> 4
        "Growth" -> 3
        "Starter" -> 2
        else -> 1
    }

    val neededAccessScore = when (lesson.tierNeeded) {
        "VIP" -> 4
        "Growth" -> 3
        "Starter" -> 2
        else -> 1
    }

    val isLocked = neededAccessScore > userAccessScore

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (lesson.isCompleted) LuxuryGold.copy(alpha = 0.5f) else if (isDark) LuxuryNavyLight else LuxuryBeigeAccent
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) LuxuryNavyCard else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked) { onLessonClick() }
            .testTag("lesson_card_${lesson.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (!isLocked) onToggleComplete() },
                modifier = Modifier.size(32.dp)
            ) {
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = if (isDark) LuxuryGray else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (lesson.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = LuxuryGold,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(2.dp, if (isDark) LuxuryNavyLight else Color.LightGray, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lesson.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGold
                    )
                    if (lesson.isPremium) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(LuxuryGoldDark.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = lesson.tierNeeded,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = lesson.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isLocked) {
                        if (isDark) Color.White.copy(alpha = 0.5f) else LuxuryNavy.copy(alpha = 0.5f)
                    } else {
                        if (isDark) Color.White else LuxuryNavy
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = LuxuryGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lesson.durationMin} minutes study duration",
                        fontSize = 11.sp,
                        color = LuxuryGray
                    )
                }
            }

            if (!isLocked) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = LuxuryGold
                )
            }
        }
    }
}

// ==========================================
// DETAILED LECTURE VIEWER COMPONENT
// ==========================================
@Composable
fun LessonViewerScreen(
    lesson: Lesson,
    user: UserProfile,
    onBack: () -> Unit,
    onToggleComplete: () -> Unit,
    goldGradient: Brush
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) LuxuryNavy else LuxuryBeige)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) LuxuryGold else LuxuryNavy
                )
            }
            Text(
                text = "LESSON MODULE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = LuxuryGray,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (lesson.isCompleted) LuxuryGold else if (isDark) LuxuryNavyLight else Color.White)
                    .clickable { onToggleComplete() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (lesson.isCompleted) "✓ COMPLETED" else "MARK COMPLETE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (lesson.isCompleted) LuxuryNavy else LuxuryGold
                )
            }
        }

        Divider(color = LuxuryGold.copy(alpha = 0.2f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = lesson.category.uppercase(),
                fontSize = 11.sp,
                color = LuxuryGold,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = lesson.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else LuxuryNavy,
                lineHeight = 30.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = LuxuryGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${lesson.durationMin} Mins read",
                    fontSize = 12.sp,
                    color = LuxuryGray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = LuxuryGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Instructor Approved",
                    fontSize = 12.sp,
                    color = LuxuryGray
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) LuxuryNavyLight.copy(alpha = 0.3f) else Color.White)
                    .border(
                        1.dp,
                        LuxuryGoldDark.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = LuxuryGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TUTORIAL DESK BLUEPRINT",
                            color = if (isDark) LuxuryGoldLight else LuxuryNavy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = lesson.content,
                        fontSize = 15.sp,
                        color = if (isDark) LuxuryTextLight else LuxuryTextDark,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onToggleComplete()
                    onBack()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("mark_lesson_done_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (lesson.isCompleted) {
                                Modifier.background(Color.Gray)
                            } else {
                                Modifier.background(goldGradient)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lesson.isCompleted) "RESET PROGRESS CHECKMARK" else "MARK AS COMPLETE & RETURN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (lesson.isCompleted) Color.White else LuxuryNavy,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. AI BUSINESS ASSISTANT (CHAT) COMPONENT
// ==========================================
@Composable
fun AiAssistantScreen(
    viewModel: AcademyViewModel,
    user: UserProfile,
    goldGradient: Brush
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatLoading by viewModel.chatLoading.collectAsStateWithLifecycle()

    var userText by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    val helperPrompts = listOf(
        "Suggest product product ideas inside the home category",
        "Compose an Etsy description for organic baby cotton blanket",
        "Generate 5 high-converting titles for Amazon listing",
        "What are the LLC tax registration rules in Wyoming?",
        "Help me draft a simple e-commerce business outline"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else LuxuryBeigeAccent),
            border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(goldGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Coach Sarah",
                        tint = LuxuryNavy,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Entrevia AI Business Coach",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDark) Color.White else LuxuryNavy
                    )
                    Text(
                        text = "Online 24/7 • Custom E-Commerce Mentor",
                        fontSize = 11.sp,
                        color = LuxuryGray
                    )
                }

                IconButton(
                    onClick = { viewModel.resetChat() },
                    modifier = Modifier.testTag("reset_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Clear dialogue log",
                        tint = LuxuryGold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.size <= 1) {
                item {
                    Text(
                        text = "SUGGESTED DISCOVERY QUERIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = LuxuryGold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                }

                items(helperPrompts) { prompt ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.15f)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) LuxuryNavyCard.copy(alpha = 0.6f) else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.sendAiMessage(prompt) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) LuxuryTextLight else LuxuryTextDark,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            items(messages) { message ->
                DialogueBubble(message = message)
            }

            if (chatLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) LuxuryNavyCard else Color.White)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = LuxuryGold,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Coach is computing blueprints...",
                                    fontSize = 12.sp,
                                    color = LuxuryGray,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                placeholder = { Text("Ask about listings, suppliers, plans...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                    focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                    unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                    focusedContainerColor = if (isDark) LuxuryNavyCard else Color.White,
                    unfocusedContainerColor = if (isDark) LuxuryNavyCard else Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (userText.isNotBlank()) {
                                viewModel.sendAiMessage(userText.trim())
                                userText = ""
                            }
                        },
                        enabled = userText.isNotBlank() && !chatLoading,
                        modifier = Modifier.testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = if (userText.isNotBlank() && !chatLoading) LuxuryGold else LuxuryGray
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun DialogueBubble(message: ChatMessage) {
    val isDark = isSystemInDarkTheme()
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentWidth(align = if (isUser) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        LuxuryGold
                    } else {
                        if (isDark) LuxuryNavyCard else Color.White
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) Color.Transparent else LuxuryGoldDark.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = if (isUser) {
                        LuxuryNavy
                    } else {
                        if (isDark) LuxuryTextLight else LuxuryTextDark
                    },
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ==========================================
// 4. RESOURCE LIBRARY COMPONENT
// ==========================================
@Composable
fun ResourceLibraryScreen(
    viewModel: AcademyViewModel,
    user: UserProfile,
    goldGradient: Brush
) {
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    var activeResourceForDetailViewer by remember { mutableStateOf<Resource?>(null) }

    if (activeResourceForDetailViewer != null) {
        ResourceDetailDialog(
            resource = activeResourceForDetailViewer!!,
            onClose = { activeResourceForDetailViewer = null },
            onDownload = {
                viewModel.downloadResourceStub(activeResourceForDetailViewer!!.id, activeResourceForDetailViewer!!.title)
                activeResourceForDetailViewer = null
            },
            goldGradient = goldGradient
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "RESOURCE BLUEPRINTS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = if (isDark) LuxuryGoldLight else LuxuryNavy,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Fully downloadable strategy lists, verified templates, supplier sheets, and registration guides.",
            fontSize = 13.sp,
            color = LuxuryGray,
            lineHeight = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(resources) { resource ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
                    border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeResourceForDetailViewer = resource }
                        .testTag("resource_stub_${resource.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) LuxuryNavyLight else LuxuryBeigeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LuxuryGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = resource.category.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LuxuryGoldDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = resource.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else LuxuryNavy
                            )
                            Text(
                                text = "${resource.fileType} • ${resource.fileSize} • Syncs: ${resource.downloadCount}",
                                fontSize = 11.sp,
                                color = LuxuryGray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Open Blueprint",
                            tint = LuxuryGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceDetailDialog(
    resource: Resource,
    onClose: () -> Unit,
    onDownload: () -> Unit,
    goldGradient: Brush
) {
    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
            border = BorderStroke(1.dp, LuxuryGoldDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LuxuryGold.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = resource.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGold
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = LuxuryGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = resource.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isDark) Color.White else LuxuryNavy
                )

                Text(
                    text = resource.description,
                    color = LuxuryGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Text(
                    text = "STRATEGY ROADMAP PREVIEW",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) LuxuryNavyLight.copy(alpha = 0.5f) else LuxuryBeigeAccent)
                        .padding(16.dp)
                ) {
                    Text(
                        text = resource.contentStub,
                        fontSize = 13.sp,
                        color = if (isDark) LuxuryTextLight else LuxuryTextDark,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDownload,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("download_resource_button_confirm")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(goldGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = LuxuryNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OFFLINE SYNC SHEET (${resource.fileType})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryNavy
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. COMMUNITY COMPONENT
// ==========================================
@Composable
fun CommunitySectionScreen(
    viewModel: AcademyViewModel,
    user: UserProfile,
    goldGradient: Brush
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val viewingPost by viewModel.viewingPost.collectAsStateWithLifecycle()
    val viewingPostReplies by viewModel.viewingPostReplies.collectAsStateWithLifecycle()

    var activeCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    if (viewingPost != null) {
        PostDiscussionThreadScreen(
            post = viewingPost!!,
            replies = viewingPostReplies,
            user = user,
            onBack = { viewModel.stopViewingPost() },
            onLike = { viewModel.likePost(viewingPost!!.id) },
            onSubmitReply = { text -> viewModel.replyToPost(viewingPost!!.id, text) }
        )
    } else {
        if (showCreatePostDialog) {
            CreatePostDialog(
                onClose = { showCreatePostDialog = false },
                onSubmit = { title, content, cat ->
                    viewModel.addCommunityPost(title, content, cat)
                    showCreatePostDialog = false
                },
                goldGradient = goldGradient
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMMUNITY CHAT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = if (isDark) LuxuryGoldLight else LuxuryNavy
                    )
                    Text(
                        text = "Ask questions, share wins, support.",
                        fontSize = 12.sp,
                        color = LuxuryGray
                    )
                }

                Button(
                    onClick = { showCreatePostDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("create_post_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(goldGradient)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+ CREATE CARD", color = LuxuryNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("General", "Q&A", "Wins", "Support")
                filters.forEach { cat ->
                    val isSelected = activeCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) LuxuryGold else if (isDark) LuxuryNavyCard else Color.White
                            )
                            .border(
                                1.dp,
                                if (isSelected) LuxuryGold else if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                activeCategoryFilter = if (isSelected) null else cat
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) LuxuryNavy else if (isDark) Color.White else LuxuryTextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredPosts = if (activeCategoryFilter != null) {
                posts.filter { it.category == activeCategoryFilter }
            } else {
                posts
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredPosts) { post ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
                        border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.startViewingPost(post) }
                            .testTag("post_card_${post.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) LuxuryNavyLight else LuxuryBeigeAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = post.authorName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = LuxuryGold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = post.authorName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isDark) Color.White else LuxuryNavy
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(LuxuryGold.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = post.authorTier.uppercase(),
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = LuxuryGoldDark
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Posted under ${post.category}",
                                        fontSize = 10.sp,
                                        color = LuxuryGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = post.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isDark) Color.White else LuxuryNavy
                            )

                            Text(
                                text = post.content,
                                fontSize = 13.sp,
                                color = if (isDark) LuxuryTextLight else LuxuryTextDark,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Divider(color = LuxuryGold.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.ThumbUp,
                                        contentDescription = "Likes",
                                        tint = LuxuryGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = post.likesCount.toString(), fontSize = 12.sp, color = LuxuryGray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Replies",
                                        tint = LuxuryGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = post.repliesCount.toString(), fontSize = 12.sp, color = LuxuryGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostDiscussionThreadScreen(
    post: CommunityPost,
    replies: List<CommunityReply>,
    user: UserProfile,
    onBack: () -> Unit,
    onLike: () -> Unit,
    onSubmitReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) LuxuryNavy else LuxuryBeige)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) LuxuryGold else LuxuryNavy
                )
            }
            Text(
                text = "DISCUSSION CARD",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = LuxuryGray
            )
        }

        Divider(color = LuxuryGold.copy(alpha = 0.15f))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
                    border = BorderStroke(1.dp, LuxuryGoldDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) LuxuryNavyLight else LuxuryBeigeAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = post.authorName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = LuxuryGold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = post.authorName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color.White else LuxuryNavy
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(LuxuryGold.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = post.authorTier.uppercase(),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LuxuryGoldDark
                                        )
                                    }
                                }
                                Text(
                                    text = "Post Category: ${post.category}",
                                    fontSize = 10.sp,
                                    color = LuxuryGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = post.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDark) Color.White else LuxuryNavy
                        )

                        Text(
                            text = post.content,
                            fontSize = 14.sp,
                            color = if (isDark) LuxuryTextLight else LuxuryTextDark,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total replies: ${post.repliesCount}",
                                fontSize = 11.sp,
                                color = LuxuryGray
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) LuxuryNavyLight else LuxuryBeigeAccent)
                                    .clickable { onLike() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.ThumbUp,
                                        contentDescription = "Like",
                                        tint = LuxuryGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIKE (${post.likesCount})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else LuxuryNavy
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "REPLIES FEED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = LuxuryGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (replies.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No replies yet. Be the first to advise!",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = LuxuryGray
                        )
                    }
                }
            } else {
                items(replies) { reply ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard.copy(alpha = 0.5f) else Color.White),
                        border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(LuxuryGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = reply.authorName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = LuxuryGoldDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = reply.authorName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else LuxuryNavy
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(LuxuryGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = reply.authorTier.uppercase(),
                                        fontSize = 6.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LuxuryGoldDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = reply.content,
                                fontSize = 13.sp,
                                color = if (isDark) LuxuryTextLight else LuxuryTextDark,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Write constructive reply...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                    focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                    unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                    focusedContainerColor = if (isDark) LuxuryNavyCard else Color.White,
                    unfocusedContainerColor = if (isDark) LuxuryNavyCard else Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("reply_input_field"),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSubmitReply(replyText.trim())
                                replyText = ""
                            }
                        },
                        enabled = replyText.isNotBlank(),
                        modifier = Modifier.testTag("submit_reply_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Submit",
                            tint = if (replyText.isNotBlank()) LuxuryGold else LuxuryGray
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun CreatePostDialog(
    onClose: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    goldGradient: Brush
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
            border = BorderStroke(1.dp, LuxuryGoldDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CREATE NEW CARD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = LuxuryGold
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = LuxuryGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic Title") },
                    placeholder = { Text("e.g. Sourcing my first mug designs") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                        focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                        unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_post_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Detail Body") },
                    placeholder = { Text("Share details about your challenge, winning stats, or advice needed!") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                        focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                        unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_post_body_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SELECT BOARD CATEGORY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf("General", "Q&A", "Wins", "Support")
                    categories.forEach { cat ->
                        val isSel = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) LuxuryGold else if (isDark) LuxuryNavyLight else LuxuryBeigeAccent)
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) LuxuryNavy else if (isDark) Color.White else LuxuryTextDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            onSubmit(title, content, category)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("new_post_submit_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(goldGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PUBLISH DISCUSSION CARD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryNavy
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. PROFILE & MEMBERSHIP TIERS COMPONENT
// ==========================================
@Composable
fun ProfileScreen(
    viewModel: AcademyViewModel,
    user: UserProfile,
    goldGradient: Brush,
    onEnterAdminDesk: () -> Unit,
    onTriggerUpgradePay: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
                border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(goldGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "User Avatar",
                            tint = LuxuryNavy,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDark) Color.White else LuxuryNavy
                    )

                    Text(
                        text = user.email,
                        fontSize = 13.sp,
                        color = LuxuryGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LuxuryGold.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user.subscriptionTier.uppercase()} SYSTEM ACCESS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = LuxuryGoldLight
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "ACADEMY MEMBERSHIP SCHEMES",
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) LuxuryGoldLight else LuxuryNavy,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Upgrade your program Tier to unlock premium templates, direct manufacturer lists, or active marketing outlines.",
                    fontSize = 12.sp,
                    color = LuxuryGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                MembershipPlanItemRow(
                    tierName = "Starter",
                    priceLabel = "1 Week Free Trial, then $2.99 / month",
                    benefits = "Unlocks: Amazon SEO list formulas, Shopify layouts setups guide.",
                    isActive = user.subscriptionTier == "Starter",
                    onUpgrade = { onTriggerUpgradePay("Starter") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                MembershipPlanItemRow(
                    tierName = "Growth",
                    priceLabel = "1 Week Free Trial, then $5.99 / month",
                    benefits = "Unlocks: TikTok Creator Affiliate methods, vetted Global Supplier Sheet.",
                    isActive = user.subscriptionTier == "Growth",
                    onUpgrade = { onTriggerUpgradePay("Growth") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                MembershipPlanItemRow(
                    tierName = "VIP",
                    priceLabel = "1 Week Free Trial, then $9.99 / month",
                    benefits = "Unlocks: Meta Video hook scripts, Direct Admin reviews consult console.",
                    isActive = user.subscriptionTier == "VIP",
                    onUpgrade = { onTriggerUpgradePay("VIP") }
                )
            }
        }

        item {
            Column {
                Text(
                    text = "SYSTEM CONTROLS",
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) LuxuryGoldLight else LuxuryNavy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
                    border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEnterAdminDesk() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Admin Desk",
                                tint = LuxuryGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Instructor Administration Desk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDark) Color.White else LuxuryNavy
                                )
                                Text(text = "Publish custom educational courses & downloadable checklists", fontSize = 11.sp, color = LuxuryGray)
                            }
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = LuxuryGold)
                        }

                        Divider(color = LuxuryGold.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.resetAllProgress() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset Progress",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Reset Academy Progress History",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDark) Color.White else LuxuryNavy
                                )
                                Text(text = "Clears completed landmarks and resets active lesson clocks", fontSize = 11.sp, color = LuxuryGray)
                            }
                        }

                        Divider(color = LuxuryGold.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.handleLogout() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = LuxuryGray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Log Out Workspace Session",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDark) Color.White else LuxuryNavy
                                )
                                Text(text = "Secure exit current learning consultation session", fontSize = 11.sp, color = LuxuryGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MembershipPlanItemRow(
    tierName: String,
    priceLabel: String,
    benefits: String,
    isActive: Boolean,
    onUpgrade: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                if (isDark) LuxuryNavyLight else LuxuryBeigeAccent
            } else {
                if (isDark) LuxuryNavyCard else Color.White
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isActive) LuxuryGold else LuxuryGoldDark.copy(alpha = 0.15f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$tierName Plan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDark) Color.White else LuxuryNavy
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LuxuryGold)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuxuryNavy
                                )
                            }
                        }
                    }
                    Text(
                        text = priceLabel,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGoldLight,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (!isActive) {
                    Button(
                        onClick = onUpgrade,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "UPGRADE", color = LuxuryNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = benefits,
                fontSize = 12.sp,
                color = LuxuryGray,
                lineHeight = 16.sp
            )
        }
    }
}

// ==========================================
// 7. INSTRUCTOR ADMIN DESK PANEL
// ==========================================
@Composable
fun AdminCreatorScreen(
    viewModel: AcademyViewModel,
    onClose: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    var courseCategory by remember { mutableStateOf("Amazon Seller") }
    var courseTitle by remember { mutableStateOf("") }
    var courseContent by remember { mutableStateOf("") }
    var courseMinutes by remember { mutableStateOf("15") }
    var courseTierNeeded by remember { mutableStateOf("Free") }

    var resTitle by remember { mutableStateOf("") }
    var resCategory by remember { mutableStateOf("Checklist") }
    var resDesc by remember { mutableStateOf("") }
    var resStub by remember { mutableStateOf("") }

    val coreCategories = listOf(
        "Amazon Seller",
        "TikTok Shop",
        "Walmart Marketplace",
        "Etsy",
        "Shopify",
        "Product Research",
        "Supplier List",
        "Digital Products",
        "Marketing and Ads"
    )

    val tiers = listOf("Free", "Starter", "Growth", "VIP")
    val designGrad = Brush.linearGradient(colors = listOf(LuxuryGoldLight, LuxuryGold, LuxuryGoldDark))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) LuxuryNavy else LuxuryBeige)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "INSTRUCTOR PUBLICATION BOARD",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = LuxuryGold
            )
            Text(
                text = "Add custom lessons and strategy blueprints directly into the student curriculum dynamically.",
                fontSize = 13.sp,
                color = LuxuryGray,
                lineHeight = 18.sp
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
            border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.List, contentDescription = null, tint = LuxuryGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PUBLISH NEW COURSE SECTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isDark) Color.White else LuxuryNavy
                    )
                }

                Text(text = "Target Category", fontSize = 11.sp, color = LuxuryGray)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(coreCategories) { cat ->
                        val isPicked = courseCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPicked) LuxuryGold else if (isDark) LuxuryNavyLight else LuxuryBeigeAccent)
                                .clickable { courseCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPicked) LuxuryNavy else if (isDark) Color.White else LuxuryTextDark
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = courseTitle,
                    onValueChange = { courseTitle = it },
                    label = { Text("Lesson Title Title") },
                    placeholder = { Text("e.g. Sourcing high margins on Etsy") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                        focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                        unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_lesson_title_input")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = courseMinutes,
                        onValueChange = { courseMinutes = it },
                        label = { Text("Est. Minutes duration") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                            focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                            unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Access Lock Tier", fontSize = 11.sp, color = LuxuryGray)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            items(tiers) { t ->
                                val isPicked = courseTierNeeded == t
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isPicked) LuxuryGold else if (isDark) LuxuryNavyLight else LuxuryBeigeAccent)
                                        .clickable { courseTierNeeded = t }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = t,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPicked) LuxuryNavy else if (isDark) Color.White else LuxuryTextDark
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = courseContent,
                    onValueChange = { courseContent = it },
                    label = { Text("Comprehensive Lesson Content") },
                    placeholder = { Text("Draft the complete step-by-step documentation detailing specific links, checks, or outlines...") },
                    minLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                        focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                        unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_lesson_content_input")
                )

                Button(
                    onClick = {
                        if (courseTitle.isNotBlank() && courseContent.isNotBlank()) {
                            viewModel.uploadCourseLesson(
                                category = courseCategory,
                                title = courseTitle.trim(),
                                content = courseContent.trim(),
                                durationMin = courseMinutes.toIntOrNull() ?: 15,
                                tierNeeded = courseTierNeeded
                            )
                            courseTitle = ""
                            courseContent = ""
                            onClose()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("admin_publish_lesson_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(designGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PUBLISH LIVE LESSON",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LuxuryNavy
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
            border = BorderStroke(1.dp, LuxuryGoldDark.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.List, contentDescription = null, tint = LuxuryGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PUBLISH BLUEPRINT CHECKLIST",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isDark) Color.White else LuxuryNavy
                    )
                }

                OutlinedTextField(
                    value = resTitle,
                    onValueChange = { resTitle = it },
                    label = { Text("Resource Title Title") },
                    placeholder = { Text("e.g. Wyoming LLC Registration Checklist") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                        focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                        unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Blueprint ClassType Category", fontSize = 11.sp, color = LuxuryGray)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val classes = listOf("Checklist", "Supplier Sheet", "Template", "Guide")
                    classes.forEach { cl ->
                        val isSel = resCategory == cl
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) LuxuryGold else if (isDark) LuxuryNavyLight else LuxuryBeigeAccent)
                                .clickable { resCategory = cl }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cl,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) LuxuryNavy else if (isDark) Color.White else LuxuryTextDark
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = resDesc,
                    onValueChange = { resDesc = it },
                    label = { Text("Summary Description") },
                    placeholder = { Text("A short summary explanation of what the checklist represents...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                        focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                        unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = resStub,
                    onValueChange = { resStub = it },
                    label = { Text("Checklist Outline Steps") },
                    placeholder = { Text("1. Set company brand name.\n2. Incorporate IRS form details...\nWrite line-by-line steps!") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = if (isDark) LuxuryNavyLight else LuxuryBeigeAccent,
                        focusedTextColor = if (isDark) Color.White else LuxuryTextDark,
                        unfocusedTextColor = if (isDark) Color.White else LuxuryTextDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (resTitle.isNotBlank() && resStub.isNotBlank()) {
                            viewModel.uploadResourceToLibrary(
                                title = resTitle.trim(),
                                category = resCategory,
                                description = resDesc.trim(),
                                fileType = when (resCategory) {
                                    "Supplier Sheet" -> "XLSX"
                                    "Checklist" -> "PDF"
                                    else -> "Google Doc"
                                },
                                fileSize = "Cloud Doc",
                                contentStub = resStub.trim()
                            )
                            resTitle = ""
                            resDesc = ""
                            resStub = ""
                            onClose()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(designGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PUBLISH BLUEPRINT SHEET",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LuxuryNavy
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// 8. NOTIFICATIONS MODAL OVERLAY
// ==========================================
@Composable
fun NotificationsDialog(
    notificationsList: List<AppNotification>,
    onDismiss: () -> Unit,
    onDismissItem: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
            border = BorderStroke(1.dp, LuxuryGoldDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MARKETPLACE BULLETINS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = LuxuryGold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = LuxuryGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (notificationsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active marketplace alerts.",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = LuxuryGray
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(notificationsList) { alert ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) LuxuryNavyLight.copy(alpha = 0.5f) else LuxuryBeigeAccent.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = "Alert",
                                        tint = LuxuryGold,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = alert.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isDark) Color.White else LuxuryNavy
                                        )
                                        Text(
                                            text = alert.content,
                                            fontSize = 11.sp,
                                            color = if (isDark) LuxuryTextLight else LuxuryTextDark,
                                            lineHeight = 15.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDismissItem(alert.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Dismiss",
                                            tint = Color.Red,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            onClearAll()
                            onDismiss()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "CLEAR ALL BULLETINS", color = LuxuryGoldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. SIMULATED CHECKOUT GATEWAY DIALOG
// ==========================================
@Composable
fun SimulatedPaymentDialog(
    tierName: String,
    price: String,
    onClose: () -> Unit,
    onConfirmPay: () -> Unit,
    goldGradient: Brush
) {
    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) LuxuryNavyCard else Color.White),
            border = BorderStroke(1.dp, LuxuryGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SECURE CONSULTATION GATE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = LuxuryGold
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = LuxuryGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Secure Payment",
                        tint = LuxuryGold,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Verify upgrade direction",
                        color = LuxuryGray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${tierName.uppercase()} PLAN MEMBERSHIP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isDark) Color.White else LuxuryNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LuxuryGold)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "7-DAY FREE TRIAL INCLUDED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LuxuryNavy
                        )
                    }
                    Text(
                        text = "1 Week Free, then $price/mo",
                        fontWeight = FontWeight.ExtraBold,
                        color = LuxuryGoldLight,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                    )
                }

                Divider(color = LuxuryGold.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "By clicking 'Simulate Payment' below, we will securely activate progress unlocked permissions inside your profile database. This is a secure prototype simulated checkout action.",
                    fontSize = 11.sp,
                    color = LuxuryGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Button(
                    onClick = onConfirmPay,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("confirm_payment_simulated")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(goldGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SIMULATE PAYMENT & ENTER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LuxuryNavy,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cancel checkout", color = LuxuryGray, fontSize = 11.sp)
                }
            }
        }
    }
}
