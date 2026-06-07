package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AcademyRepository(private val db: AppDatabase) {

    // DAOs
    private val userDao = db.userProfileDao()
    private val lessonDao = db.lessonDao()
    private val resourceDao = db.resourceDao()
    private val postDao = db.communityPostDao()
    private val replyDao = db.communityReplyDao()

    // Expose core Flows
    val activeProfile: Flow<UserProfile?> = userDao.getActiveUserProfile()
    val allLessons: Flow<List<Lesson>> = lessonDao.getAllLessons()
    val completedLessons: Flow<List<Lesson>> = lessonDao.getCompletedLessons()
    val lastAccessedLesson: Flow<Lesson?> = lessonDao.getLastAccessedLesson()
    val allResources: Flow<List<Resource>> = resourceDao.getAllResources()
    val allPosts: Flow<List<CommunityPost>> = postDao.getAllPosts()

    // ==========================================
    // User Profiles Operations
    // ==========================================
    suspend fun createOrLoginUser(email: String, name: String) = withContext(Dispatchers.IO) {
        userDao.logoutAll() // Logout existing sessions first
        val profile = UserProfile(
            email = email,
            name = name,
            subscriptionTier = "Free",
            isLoggedIn = true,
            isPremium = false
        )
        userDao.insertProfile(profile)
    }

    suspend fun upgradeSubscription(email: String, tier: String) = withContext(Dispatchers.IO) {
        val isPremium = tier != "Free"
        userDao.updateSubscription(email, tier, isPremium)
    }

    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        userDao.logoutAll()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        userDao.clearAll()
        lessonDao.resetAllProgress()
    }

    // ==========================================
    // Lessons Operations
    // ==========================================
    suspend fun updateLessonProgress(id: Int, completed: Boolean) = withContext(Dispatchers.IO) {
        lessonDao.updateProgress(id, completed, System.currentTimeMillis())
    }

    suspend fun markLessonAccessed(id: Int) = withContext(Dispatchers.IO) {
        lessonDao.updateLastAccessed(id, System.currentTimeMillis())
    }

    suspend fun uploadCustomLesson(lesson: Lesson) = withContext(Dispatchers.IO) {
        lessonDao.insertLesson(lesson.copy(isUploadedByAdmin = true))
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        lessonDao.resetAllProgress()
    }

    // ==========================================
    // Resources Operations
    // ==========================================
    suspend fun uploadCustomResource(resource: Resource) = withContext(Dispatchers.IO) {
        resourceDao.insertResource(resource.copy(isUploadedByAdmin = true))
    }

    suspend fun incrementDownloadCount(id: Int) = withContext(Dispatchers.IO) {
        resourceDao.incrementDownloadCount(id)
    }

    // ==========================================
    // Community Operations
    // ==========================================
    suspend fun createCommunityPost(title: String, content: String, authorName: String, authorTier: String, category: String) = withContext(Dispatchers.IO) {
        val post = CommunityPost(
            title = title,
            content = content,
            authorName = authorName,
            authorTier = authorTier,
            category = category
        )
        postDao.insertPost(post)
    }

    suspend fun likePost(postId: Int) = withContext(Dispatchers.IO) {
        postDao.likePost(postId)
    }

    fun getRepliesForPost(postId: Int): Flow<List<CommunityReply>> {
        return replyDao.getRepliesForPost(postId)
    }

    suspend fun replyToPost(postId: Int, authorName: String, authorTier: String, content: String) = withContext(Dispatchers.IO) {
        val reply = CommunityReply(
            postId = postId,
            authorName = authorName,
            authorTier = authorTier,
            content = content
        )
        replyDao.insertReply(reply)
        postDao.incrementRepliesCount(postId)
    }

    // ==========================================
    // Database Pre-population
    // ==========================================
    suspend fun prepopulateIfNeeded() = withContext(Dispatchers.IO) {
        val currentLessons = allLessons.firstOrNull()
        if (currentLessons.isNullOrEmpty()) {
            // 1. Populate default profile
            userDao.insertProfile(
                UserProfile(
                    email = "student@entrevia.com",
                    name = "Aspiring Founder",
                    subscriptionTier = "Starter", // Default to Starter to show premium contents right away!
                    isLoggedIn = true,
                    isPremium = true
                )
            )

            // 2. Populate default lessons (curated curriculum)
            val defaultLessons = listOf(
                // Category: Amazon Seller
                Lesson(
                    category = "Amazon Seller",
                    title = "Amazon FBA Business Model 101",
                    durationMin = 15,
                    isPremium = false,
                    tierNeeded = "Free",
                    content = """
                        # Amazon FBA Business Model 101

                        Amazon FBA (Fulfillment by Amazon) is a business model where you source products, ship them to Amazon’s global warehouses, and Amazon takes care of:
                        - Warehousing the inventory
                        - Packing and shipping orders immediately to buyers
                        - Customer service and managing product returns

                        ## Key Benefits for Founders
                        1. **Prime Shipping Integration**: Your products automatically qualify for Prime shipping, increasing conversions.
                        2. **Passive Delivery**: You focus on sourcing and branding; Amazon handles shipping.
                        3. **Customer Trust**: Buyers trust Amazon's quick delivery.

                        ## Initial Setup Checklist
                        - Register for an Amazon Professional Seller Client Account.
                        - Match seller requirements: ID, tax identifier, bank statements.
                        - Understand fees: Referral fee (typically 15%) and FBA fulfillment fees.
                    """.trimIndent()
                ),
                Lesson(
                    category = "Amazon Seller",
                    title = "Product Listing & SEO Optimization",
                    durationMin = 20,
                    isPremium = true,
                    tierNeeded = "Starter",
                    content = """
                        # Product Listing & SEO Optimization

                        Optimizing your product page determines whether searchers find your items in Amazon's massive database.

                        ## 3 Pillars of Amazon SEO
                        1. **Keywords**: Insert your primary high-volume keyword at the front of the Product Title. Fill bullet points and backend search terms with secondary keywords.
                        2. **Product Images**: Provide high-definition images on pure white backgrounds showing every angle, scale, and utility. Include infographic cards.
                        3. **Click-Through Rate (CTR) & Conversion Rate**: Amazon measures search-relevance by actual orders. Lowering initial price or running coupons boosts review generation.

                        ## Bullet Point Formula
                        - Bullet 1: Core utility / primary benefit.
                        - Bullet 2: Materials used or dimensions.
                        - Bullet 3: Distinct feature distinguishing it from standard competitors.
                        - Bullet 4: Value bundle / packaging assets.
                        - Bullet 5: Warranty and total refund security guarantee.
                    """.trimIndent()
                ),

                // Category: TikTok Shop
                Lesson(
                    category = "TikTok Shop",
                    title = "Getting Approved & Seller Guidelines",
                    durationMin = 12,
                    isPremium = false,
                    tierNeeded = "Free",
                    content = """
                        # Getting Approved & Seller Guidelines

                        TikTok Shop is the fastest-growing dynamic e-commerce marketplace in the world. It merges video discovery with checkout.

                        ## Setup Requirements
                        - Valid US Business Registration documents (LLC or Corporation) or US Passport/Driver's License for individuals.
                        - Bank account in the same tax region.
                        - Active TikTok Account with cleanly integrated tax identity.

                        ## Launch Formula
                        1. Create an account on the **TikTok Shop Seller Center** website.
                        2. Upload corporate registries and wait 24-48 hours for verified validation.
                        3. Connect your TikTok business account to access direct live and video shopping buttons.
                    """.trimIndent()
                ),
                Lesson(
                    category = "TikTok Shop",
                    title = "Earning with TikTok Shop Affiliates",
                    durationMin = 18,
                    isPremium = true,
                    tierNeeded = "Growth",
                    content = """
                        # Earning with TikTok Shop Affiliates

                        If you are shy or do not want to film yourself, the TikTok Shop Affiliate Network is the absolute best system. 

                        ## The Creator Synergy Formula
                        Instead of filming products yourself:
                        1. List your product in the **Open Collaboration** or **Targeted Affiliate** catalog in the Seller Center.
                        2. Offer a competitive commission (Typically 15% to 25%).
                        3. Ship free sample items to creators who match your target demographic.
                        4. Watch creators create viral shoppable videos driving direct commissions!
                    """.trimIndent()
                ),

                // Category: Shopify
                Lesson(
                    category = "Shopify",
                    title = "Launching a High-Converting Shopify Store",
                    durationMin = 25,
                    isPremium = false,
                    tierNeeded = "Free",
                    content = """
                        # Launching a High-Converting Shopify Store

                        A custom Shopify website provides you with direct user data ownership and total branding freedom, uninhibited by marketplace restrictions.

                        ## Core Framework Checklist
                        - **Domain Purchase**: Secure a modern, clean, 1-word or 2-word brand domain.
                        - **Premium Minimalist Theme**: Install a lightning-fast responsive theme like "Dawn" or "Sense". Keep fonts to a clean Sans-Serif pair.
                        - **Product Descriptions**: Focus on benefits rather than standard specs. Speak to user pain points directly.

                        ## 3 Must-Have Shopify Apps
                        1. **Klaviyo**: For configuring automated email recovery when a customer abandons their checkout basket.
                        2. **Judge.me**: To gather photo reviews which builds instant user credibility.
                        3. **Incart Upsell**: Prompts complementary additions during selection.
                    """.trimIndent()
                ),

                // Category: Etsy
                Lesson(
                    category = "Etsy",
                    title = "Etsy SEO Secrets for Handmade & Vintage Products",
                    durationMin = 14,
                    isPremium = false,
                    tierNeeded = "Free",
                    content = """
                        # Etsy SEO Secrets

                        Etsy is a magical search engine for handcrafts, customized gifts, vintage items, and personalized digital bundles.

                        ## The Etsy Algorithm Checklist
                        - **Tag Matching**: Use all 13 tags fully! Do not leave any blank. Combine compound keys like "gold bridesmaid gift" rather than just "gold" and "gift".
                        - **Listing Title**: Pack the title with search terms divided by clean characters (e.g. `|` or `/`).
                        - **Personalization Panel**: Toggle the personalization selector on to request detailed custom strings from customers during checkout.
                    """.trimIndent()
                ),

                // Category: Walmart Marketplace
                Lesson(
                    category = "Walmart Marketplace",
                    title = "Walmart WFS vs Amazon FBA",
                    durationMin = 15,
                    isPremium = true,
                    tierNeeded = "Starter",
                    content = """
                        # Walmart WFS vs Amazon FBA

                        Walmart Marketplace offers a younger, lesssaturated platform with competitive prime shipping speeds through Walmart Fulfillment Services (WFS).

                        ## Core Comparison
                        - **Market Density**: Walmart has significantly fewer sellers compared to Amazon, making search ranking much easier.
                        - **Commission Tiers**: Referral rates are comparable, but storage fees are often cheaper during non-holiday periods.
                        - **WFS Convenience**: Ship bulk inventory into Walmart fulfillment hubs, and Walmart supplies national shipping under the reliable "2-Day Shipping" label.
                    """.trimIndent()
                ),

                // Category: Product Research
                Lesson(
                    category = "Product Research",
                    title = "Selecting High-Margin Winning Products",
                    durationMin = 22,
                    isPremium = false,
                    tierNeeded = "Free",
                    content = """
                        # Selecting High-Margin Winning Products

                        Do not guess what products will succeed. Base your inventory choices on solid market evidence and mathematical analysis.

                        ## The Mathematical Selection Criteria
                        1. **Retail Price**: $25 to $65 (The sweet spot for impulsive purchasing behavior).
                        2. **Markup Tier**: Source price should be at most 20-25% of final retail price.
                        3. **Size/Weight**: Must fit inside a standard shoebox and weigh less than 2 lbs (Saves massive shipping and storage fees).
                        4. **Complexity**: No electrical currents, complicated mechanical links, or intricate parts (Reduces defects and returns).
                    """.trimIndent()
                ),

                // Category: Supplier List
                Lesson(
                    category = "Supplier List",
                    title = "Negotiating Secure Sourcing on Alibaba",
                    durationMin = 18,
                    isPremium = true,
                    tierNeeded = "Growth",
                    content = """
                        # Negotiating Secure Sourcing on Alibaba

                        Safeguarding your capital during manufacturing setup is crucial to business survival.

                        ## Safe Sourcing Rules
                        - **Trade Assurance Only**: Buy exclusively from suppliers who take Alibaba Trade Assurance. This locks your payment in escrow until manufacturing conforms to delivery rules.
                        - **Gold Supplier Status**: Verify that the supplier is an active validated "Gold Supplier" for at least 3 consecutive years.
                        - **Custom Auditing**: Hire a local sourcing inspector (like SGS) to inspect sample batches before shipping.
                    """.trimIndent()
                ),

                // Category: Digital Products
                Lesson(
                    category = "Digital Products",
                    title = "Designing and Selling High-Profit Canva Templates",
                    durationMin = 12,
                    isPremium = false,
                    tierNeeded = "Free",
                    content = """
                        # Canva Templates & Digital Sells

                        Digital products carry a magnificent 99% profit margin since they are manufactured completely once and sold millions of times.

                        ## Product Blueprints
                        - Social media content reels templates.
                        - Modern business proposal spreadsheets.
                        - Real estate agency marketing flyers.
                        - Wedding invitation cards with interactive links.

                        ## Setup Sells
                        Use Canva to design mockups, configure a PDF template sharing a restricted shareable design link, and list that PDF asset as an instant digital download on Etsy or Shopify!
                    """.trimIndent()
                ),

                // Category: Marketing and Ads
                Lesson(
                    category = "Marketing and Ads",
                    title = "Mastering Meta & TikTok Video Ads",
                    durationMin = 20,
                    isPremium = true,
                    tierNeeded = "VIP",
                    content = """
                        # Mastering Meta & TikTok Video Ads

                        Scaling your marketplace listings requires active outside traffic injection.

                        ## Hook, Line, and Sinker Structure
                        1. **0-3 Seconds (The Hook)**: Show an immediate disruption, problem statement, or aesthetic transformation. Do not start with your brand name.
                        2. **3-15 Seconds (The Body)**: Address how the product works. Focus entirely on emotional benefits.
                        3. **15-20 Seconds (Call To Action)**: Mention your URL, discount codes, or marketplace buttons explicitly. Include a strong sense of urgency.
                    """.trimIndent()
                )
            )
            lessonDao.insertAll(defaultLessons)

            // 3. Populate default resources
            val defaultResources = listOf(
                Resource(
                    title = "Amazon FBA Product Launched Checklist",
                    category = "Checklist",
                    description = "A meticulous step-by-step PDF roadmap for launching private label items safely on Amazon.",
                    fileType = "PDF",
                    fileSize = "320 KB",
                    contentStub = """
                        1. REGISTRATION: Ensure business tax identity matches utility statements exactly to prevent instant lockouts.
                        2. PRODUCT CRITERIA check: Verify size, weight under 1.8 lbs, and no hazard items.
                        3. SAMPLE COMPARISON: Order samples from at least 3 separate Alibaba suppliers.
                        4. PROFESSIONAL PHOTOS: Acquire white background and real-world lifestyle photos.
                        5. BARCODE ASSIGNMENT: Buy official GS1 barcodes. Avoid recycled keys.
                        6. CREATE FBA SHIPMENT: Generate warehouse labels and schedule courier pickups.
                    """.trimIndent()
                ),
                Resource(
                    title = "Verified Global Supplier Directory 2026",
                    category = "Supplier Sheet",
                    description = "Pre-vetted premium manufacturers for home decor, premium apparel, wellness, and custom electronics.",
                    fileType = "Google Sheet",
                    fileSize = "Cloud Doc",
                    contentStub = """
                        - Home & Decor Alliance: specializes in gold accented tabletop ceramics. Accept Trade Assurance, MOQ 50 pcs.
                        - Luxe Aesthetics Textiles: high-fashion hoodies, tees, organic blanks. OEM custom tags, MOQ 100 pcs.
                        - ZenTech Electronics: mini wireless audio accessories, portable mist humidifiers. FCC certified, MOQ 100 pcs.
                    """.trimIndent()
                ),
                Resource(
                    title = "TikTok Shop Virality & Hook Blueprint",
                    category = "Template",
                    description = "The exact verbal scripting and pacing styles used to generate over ${'$'}100k in monthly affiliate sales.",
                    fileType = "DOCX",
                    fileSize = "1.2 MB",
                    contentStub = """
                        - Hook 1: "This TikTok hack completely changed how I deal with..."
                        - Hook 2: "Stop buying boring things! Look what arrived in the post today..."
                        - Hook 3: "If you're a busy parent and want to save some hours, this is for you."
                        - Body Pacing: Cut visually every 1.5 seconds. Use energetic background beats.
                        - Call-to-action: "Point at the orange shopping cart below to claim yours today!"
                    """.trimIndent()
                ),
                Resource(
                    title = "Business Registration & LLC Blueprint",
                    category = "Guide",
                    description = "Understand state registration rules, EIN applications, and tax setup guidelines for beginners.",
                    fileType = "PDF",
                    fileSize = "410 KB",
                    contentStub = """
                        1. CHOOSE YOUR STATE: Wyoming or Delaware are popular for online non-residents. Otherwise, register in your home state.
                        2. STATE INCORPORATION: File Articles of Organization. Average fee: ${'$'}50 to ${'$'}150.
                        3. IRS EIN TAX ID: Apply online via the official IRS.gov website for instant free delivery.
                        4. SELLER CREDENTIALS Setup: Open a dedicated business checkings bank account (e.g., Mercury, Relay, or Wise).
                    """.trimIndent()
                ),
                Resource(
                    title = "Shopify Core Funnel Optimization Checklist",
                    category = "Checklist",
                    description = "A conversion-focused blueprint ensuring your landing page and product detail sheets turn visits into checkouts.",
                    fileType = "PDF",
                    fileSize = "195 KB",
                    contentStub = """
                        - IMAGE SPEED OPTIMIZATION: Compress all assets using TinyPNG. Slow loads equal lost customers.
                        - TRIPLE BADGE BRANDING: Place 'Secure Payment', '7-Day Refund', and 'Direct Support' trust seals right below 'Add to Cart'.
                        - ONE-PAGE CHECKOUT: Turn on Shopify's dynamic single-page checkout to bypass multi-step friction.
                        - VISUAL POP: Use clear contrasting colors (e.g. bold dark primary with Luxury Gold accents) for clickable shopping targets.
                        - ADD-ON UPSELLS: Configure complimentary cross-sell items directly above shipping calculation modules.
                    """.trimIndent()
                ),
                Resource(
                    title = "Influencer Outreach & Affiliate Campaign Scripts",
                    category = "Template",
                    description = "Proven, copy-paste direct messages and emails to convince TikTok and Instagram creators to represent your brand on commission.",
                    fileType = "DOCX",
                    fileSize = "142 KB",
                    contentStub = """
                        - DM Outreach (TikTok/IG): "Hey [Creator Name]! Absolutely loved your recent clip on [Topic]. We have a premium launched organic product that your audience would adore. We'd love to ship you a complimentary sample for your honest review. We offer an industry-leading 20% affiliate commission for orders through your cart tracker link. Let us know if you are open to collaborate!"
                        - Email Outreach: Subject: [Collaboration Proposal] Premium Custom Decor Launch x [Creator Name]
                          "Hi [Creator Name], We've been following your lifestyle feed and think your visual aesthetic aligns beautifully with our gold ceramic launch. We provide free shipping on test samples, a 25% recurring payout tier, and unique tracking codes for your fans. Reply with your mailing coordinates if you'd like to try it!"
                    """.trimIndent()
                ),
                Resource(
                    title = "Income Statement & Financial Unit Calculator",
                    category = "Template",
                    description = "An interactive spreadsheet layout to compute Cost of Goods Sold (COGS), warehouse storage fees, ad spend, and net product margins.",
                    fileType = "XLSX",
                    fileSize = "520 KB",
                    contentStub = """
                        - RETRIEVING BASE UNIT COSTS:
                          - Factory Price / Unit: ${'$'}4.50
                          - Quality Inspection / Unit: ${'$'}0.25
                          - Packaging & Label / Unit: ${'$'}0.50
                        - FREIGHT & DUTY ACCRUED: Air shipping (${'$'}3.00/unit) or Ocean freight (${'$'}0.80/unit).
                        - WAREHOUSE/FBA ACQUISITION: Standard transit plus picking-and-packing service fees.
                        - MARKUP DETERMINATION: Goal markup is 4x to 5x of baseline production cost to survive active ad bidding.
                    """.trimIndent()
                ),
                Resource(
                    title = "Digital Passive Products Evergreen Blueprint",
                    category = "Guide",
                    description = "Detailed roadmap on creating, marketing, and selling digital download assets (Notion blocks, Canva sets, PDFs) with zero inventory overhead.",
                    fileType = "PDF",
                    fileSize = "280 KB",
                    contentStub = """
                        - NICHE EXPLORATION: Target high-utility templates like 'Wedding Planning Spreadsheet', 'Productivity Tracker', or 'SMM Flyers'.
                        - DESIGN RULES: Create high-contrast interactive structures using free software tiers. Keep your template link strictly editable-only.
                        - EXPORT SCHEMES: Save as interactive PDF files embedding beautiful high-resolution workspace mockups.
                        - TRAFFIC ENGINE: Share short Reels or TikToks documenting the speed of your custom workflow using the template.
                    """.trimIndent()
                ),
                Resource(
                    title = "Vetted Freight Forwarders & Logistics Directory",
                    category = "Supplier Sheet",
                    description = "Our verified list of ocean, air cargo, and customs clearance agents specializing in FBA delivery and duty payouts.",
                    fileType = "Google Sheet",
                    fileSize = "Cloud Doc",
                    contentStub = """
                        - Pacific Freight Solutions: Specializes in LCL (Less than Container Load) from Shenzhen to West Coast FBA hubs. Includes DDP (Delivered Duty Paid) pricing.
                        - AirMax Express Forwarders: Premium priority air cargo tracking. Best for prompt restocks under 500 kg.
                        - Global Trade Services Brokers: In-house import clearers and compliance agents. Helps bypass bond delays and entry custom hurdles.
                    """.trimIndent()
                ),
                Resource(
                    title = "Etsy SEO Tags & Optimized Copy Checklist",
                    category = "Checklist",
                    description = "Simple checklist to maximize your shop listings visibility on Etsy search channels.",
                    fileType = "PDF",
                    fileSize = "110 KB",
                    contentStub = """
                        - ALL 13 TAG LINES: Always use all 13 available fields. Utilize multi-word keywords rather than single descriptors.
                        - MATERIAL SPECIFICS: Explicitly detail materials (e.g., organic cotton, ceramic clay) to populate secondary search fields.
                        - COMPATIBLE ALTERNATIVES: List product sizes, potential variations, or customized gifts directions in your opening descriptions.
                        - THUMBNAIL TEST: Zoom out to a 100x100 thumbnail. Ensure clean high-contrast text and product display lines remain instantly readable.
                    """.trimIndent()
                ),
                Resource(
                    title = "Walmart WFS Onboarding & Transit Guide",
                    category = "Guide",
                    description = "Step-by-step directions to apply, list, and ship inventory deep into Walmart's rapid fulfillment system.",
                    fileType = "PDF",
                    fileSize = "340 KB",
                    contentStub = """
                        - QUALIFICATION CRITERIA: Walmart favors established seller reviews on Amazon/Shopify. Have business EIN ready.
                        - SYSTEM SETUP: Map UPC barcodes directly into Seller platforms. Enable 'Ship with Walmart Fulfillment Services (WFS)' toggles.
                        - PALLETIZING AND PREP: Adhere strictly to Walmart's specific pallet guidelines. Avoid loose cartoon packages.
                    """.trimIndent()
                ),
                Resource(
                    title = "Private Label Branding and Custom Box Spec Template",
                    category = "Template",
                    description = "Standard technical packaging blueprint for creating custom labeled boxes with barcodes and eco-friendly seals.",
                    fileType = "DOCX",
                    fileSize = "1.8 MB",
                    contentStub = """
                        - PACKAGING SPECS SHEET:
                          - Box material: Kraft double-wall corrugated card for optimal product drop protection.
                          - Surface design: Embossed logo with matte gold UV lining.
                          - Mandatory indicators: Include country of origin (e.g. 'Made in [Region]'), custom bar codes, and safe transit handling labels.
                    """.trimIndent()
                ),
                Resource(
                    title = "High-Converting Facebook & Video Ad Creative Checklist",
                    category = "Checklist",
                    description = "Ensure your video assets contain the necessary hooks and components before launching ad traffic campaigns.",
                    fileType = "PDF",
                    fileSize = "150 KB",
                    contentStub = """
                        - FIRST 3 SECONDS: Dynamic visual transition, surprise outcome, or high-definition screen split. No intros.
                        - CAPTION LAYERS: Ensure open-captions cover 100% of oral scripts as more than 75% of viewers watch muted.
                        - USER EMOTION PULLS: Demonstrate the painful 'before' scenario, followed by the seamless 'after' satisfaction.
                        - THE EXPLICIT CALL: Show clear cursor steps pushing on 'Order Now' button or visiting '[Your Brand].com'.
                    """.trimIndent()
                ),
                Resource(
                    title = "Automated Email Flows Script & Copy Templates",
                    category = "Template",
                    description = "Pre-written email templates to configure in Klaviyo/Shopify for Abandoned Cart recovery, Welcome strings, and Win-back promotions.",
                    fileType = "DOCX",
                    fileSize = "290 KB",
                    contentStub = """
                        - ABANDONED CART EMAIL 1 (Send 2 hours after exit):
                          - Subject: "Did your internet disconnect? We saved your shopping cart!"
                          - Body Copy: "Hi [First Name], we noticed you left our premium [Product Name] behind. We've temporarily locked in your reserve pack. Tap below to finish securely!"
                        - WELCOME SERIES EMAIL 1 (Send instantly upon subscription):
                          - Subject: "Welcome to [Brand Name]! Here is your 10% Welcome gift code."
                          - Body Copy: "Hi [First Name], we are thrilled to have you! Here is custom discount code: [WELCOME10]. Unlock your first purchase today."
                    """.trimIndent()
                ),
                Resource(
                    title = "USPTO Brand Trademark Search & Filing Guide",
                    category = "Guide",
                    description = "Avoid costly legal shutdowns by following our trademark lookup and filing instructions for complete brand security.",
                    fileType = "PDF",
                    fileSize = "210 KB",
                    contentStub = """
                        - USPTO TESS DIRECTORY ENTRY: Go to USPTO.gov, click TESS, select 'Basic Word Mark Search'. Check for active duplicates.
                        - CATEGORY OF SERVICE SELECT: Identify your business category Class (e.g., Class 35 for retail shops, Class 14 for custom jewelry).
                        - EVIDENCE OF USE FILE: Maintain digital photos showing your brand logo printed cleanly on actual products or packaging boxes.
                    """.trimIndent()
                )
            )
            resourceDao.insertAll(defaultResources)

            // 4. Populate default community posts
            val defaultPosts = listOf(
                CommunityPost(
                    title = "Made my first $1,500 selling on TikTok Shop! 🎉",
                    content = "Guys! I'm completely in shock. I listened to the Creator Affiliate lesson, reached out to some micro-influencers, offered 20% commission on my ceramic coffee mugs, and woke up to over 42 orders from a single viral video! It works!",
                    authorName = "Clara H. (Active Parent Team)",
                    authorTier = "Growth",
                    likesCount = 34,
                    repliesCount = 2,
                    category = "Wins"
                ),
                CommunityPost(
                    title = "Alibaba manufacturer requesting deposit outside the system, is this safe?",
                    content = "Hey everyone! I am searching for a customized water bottle supplier. They insist on taking 30% deposit via direct bank wire instead of Trade Assurance to 'speed up production'. Red flag or standard practice?",
                    authorName = "John K. (Founder)",
                    authorTier = "Starter",
                    likesCount = 8,
                    repliesCount = 2,
                    category = "Q&A"
                ),
                CommunityPost(
                    title = "Amazon FBA seller requirements question: Utility utility bill matches",
                    content = "I have heard several stories of sellers getting banned in 10 minutes because names mismatch. Should my seller account name match my personal ID or LLC documents exactly?",
                    authorName = "Sophia L.",
                    authorTier = "Free",
                    likesCount = 4,
                    repliesCount = 1,
                    category = "Support"
                )
            )

            // Insert initial posts
            defaultPosts.forEach { postDao.insertPost(it) }

            // Insert replies
            replyDao.insertReply(
                CommunityReply(
                    postId = 2,
                    authorName = "Devon M.",
                    authorTier = "VIP",
                    content = "ABSOLUTE RED FLAG! Never pay outside Alibaba for your first order. If anything goes wrong with quality, Alibaba will refuse to cover your losses and your bank wire represents a complete loss of capital. Insist on Trade Assurance."
                )
            )
            replyDao.insertReply(
                CommunityReply(
                    postId = 2,
                    authorName = "Instructor Sarah",
                    authorTier = "Admin",
                    content = "Devon is 100% correct! Standard professional factories have no issues with Trade Assurance. Keep searching -- there are thousands of other high-quality partners who value your security."
                )
            )
            replyDao.insertReply(
                CommunityReply(
                    postId = 3,
                    authorName = "Admin Team",
                    authorTier = "Admin",
                    content = "Yes, Sophia. Amazon uses OCR algorithms. The name, address, and spelling on your registration form MUST match your uploaded utility bill and ID to the exact letter. Any difference causes a suspension."
                )
            )
        }
    }
}
