package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TasbihEntity
import com.example.ui.QazaViewModel
import com.example.ui.components.FrostedGlassCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MasnoonDua(
    val id: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val target: Int,
    val category: String, // "After Salah", "Morning & Evening", "Custom"
    val isCustom: Boolean = false
)

val MASNOON_DUAS = listOf(
    MasnoonDua(
        id = "subhanallah",
        title = "Subhanallah",
        arabic = "سُبْحَانَ اللَّهِ",
        transliteration = "Subhanallah",
        translation = "Glory be to Allah. (Recite 33 times after each prayer)",
        target = 33,
        category = "After Salah"
    ),
    MasnoonDua(
        id = "alhamdulillah",
        title = "Alhamdulillah",
        arabic = "الْحَمْدُ لِلَّهِ",
        transliteration = "Alhamdulillah",
        translation = "Praise be to Allah. (Recite 33 times after each prayer)",
        target = 33,
        category = "After Salah"
    ),
    MasnoonDua(
        id = "allahu_akbar",
        title = "Allahu Akbar",
        arabic = "اللَّهُ أَكْبَرُ",
        transliteration = "Allahu Akbar",
        translation = "Allah is the Greatest. (Recite 34 times after each prayer)",
        target = 34,
        category = "After Salah"
    ),
    MasnoonDua(
        id = "astaghfirullah",
        title = "Astaghfirullah",
        arabic = "أَسْتَغْفِرُ اللَّهَ",
        transliteration = "Astaghfirullah",
        translation = "I seek Allah's forgiveness. (Excellent for morning and evening)",
        target = 100,
        category = "Morning & Evening"
    ),
    MasnoonDua(
        id = "ayat_al_kursi",
        title = "Ayat al-Kursi",
        arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
        transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum",
        translation = "The Throne Verse. Reciting after prayer protects until the next prayer.",
        target = 1,
        category = "After Salah"
    ),
    MasnoonDua(
        id = "subhanallahi_wa_bihamdihi",
        title = "Subhanallahi wa Bihamdihi",
        arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
        transliteration = "Subhanallahi wa bihamdihi, Subhanallahil-Azheem",
        translation = "Glory be to Allah and His is the praise. Sins are forgiven even if they are like foam of the sea.",
        target = 100,
        category = "Morning & Evening"
    ),
    MasnoonDua(
        id = "la_ilaha_illa_allah",
        title = "La ilaha illa Allah",
        arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
        transliteration = "La ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu, wa Huwa 'ala kulli shay'in Qadir",
        translation = "None has the right to be worshipped but Allah alone, Who has no partner.",
        target = 100,
        category = "After Salah"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    viewModel: QazaViewModel,
    onNavigateToSettings: () -> Unit
) {
    val tasbihList by viewModel.tasbihList.collectAsState()
    val context = LocalContext.current
    
    // Tabs state: 0 = Daily Masnoon, 1 = Personal Tracker
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Daily Masnoon states
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val sharedPrefs = remember { context.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE) }
    
    // Load custom duas
    fun loadCustomDuas(): List<MasnoonDua> {
        val list = mutableListOf<MasnoonDua>()
        val ids = sharedPrefs.getStringSet("custom_dua_ids", emptySet()) ?: emptySet()
        for (id in ids) {
            val title = sharedPrefs.getString("custom_dua_title_$id", "") ?: ""
            val arabic = sharedPrefs.getString("custom_dua_arabic_$id", "") ?: ""
            val transliteration = sharedPrefs.getString("custom_dua_translit_$id", "") ?: ""
            val translation = sharedPrefs.getString("custom_dua_translation_$id", "") ?: ""
            val target = sharedPrefs.getInt("custom_dua_target_$id", 33)
            val category = sharedPrefs.getString("custom_dua_category_$id", "Custom") ?: "Custom"
            if (title.isNotEmpty()) {
                list.add(MasnoonDua(id, title, arabic, transliteration, translation, target, category, isCustom = true))
            }
        }
        return list
    }

    val customDuas = remember {
        mutableStateListOf<MasnoonDua>().apply {
            addAll(loadCustomDuas())
        }
    }

    // Default plans mapping
    fun getDefaultPlan(duaId: String): Set<String> {
        return when (duaId) {
            "subhanallah", "alhamdulillah", "allahu_akbar", "la_ilaha_illa_allah" -> 
                setOf("After Fajr", "After Dhuhr", "After Asr", "After Maghrib", "After Isha", "Anytime")
            "astaghfirullah" -> 
                setOf("Morning", "Evening", "Anytime")
            "ayat_al_kursi" -> 
                setOf("After Fajr", "After Dhuhr", "After Asr", "After Maghrib", "After Isha")
            "subhanallahi_wa_bihamdihi" -> 
                setOf("Morning", "Evening", "Anytime")
            else -> setOf("Anytime")
        }
    }

    // State holding time-slot plans for all loaded duas
    val duaPlans = remember(customDuas.size) {
        mutableStateMapOf<String, Set<String>>().apply {
            MASNOON_DUAS.forEach { dua ->
                val saved = sharedPrefs.getStringSet("plan_${dua.id}", null)
                this[dua.id] = saved ?: getDefaultPlan(dua.id)
            }
            customDuas.forEach { dua ->
                val saved = sharedPrefs.getStringSet("plan_${dua.id}", null)
                this[dua.id] = saved ?: setOf("Anytime")
            }
        }
    }

    // Daily counts for both default and custom duas
    val masnoonCounts = remember(todayDateStr, customDuas.size) {
        mutableStateMapOf<String, Int>().apply {
            MASNOON_DUAS.forEach { dua ->
                val savedCount = sharedPrefs.getInt("masnoon_count_${dua.id}_$todayDateStr", 0)
                this[dua.id] = savedCount
            }
            customDuas.forEach { dua ->
                val savedCount = sharedPrefs.getInt("masnoon_count_${dua.id}_$todayDateStr", 0)
                this[dua.id] = savedCount
            }
        }
    }

    fun deleteCustomDua(duaId: String) {
        customDuas.removeIf { it.id == duaId }
        val ids = sharedPrefs.getStringSet("custom_dua_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        ids.remove(duaId)
        sharedPrefs.edit().apply {
            putStringSet("custom_dua_ids", ids)
            remove("custom_dua_title_$duaId")
            remove("custom_dua_arabic_$duaId")
            remove("custom_dua_translit_$duaId")
            remove("custom_dua_translation_$duaId")
            remove("custom_dua_target_$duaId")
            remove("custom_dua_category_$duaId")
            remove("plan_$duaId")
        }.apply()
    }

    // Current Time Slot tracking for Recommendations
    val currentTimeSlots = remember {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..8 -> listOf("Morning", "After Fajr", "Anytime")
            in 9..11 -> listOf("Morning", "Anytime")
            in 12..15 -> listOf("After Dhuhr", "Anytime")
            in 16..17 -> listOf("After Asr", "Evening", "Anytime")
            in 18..19 -> listOf("After Maghrib", "Evening", "Anytime")
            in 20..23 -> listOf("After Isha", "Anytime")
            else -> listOf("After Isha", "Anytime")
        }
    }

    val currentPeriodLabel = remember {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..8 -> "🌅 Morning (After Fajr)"
            in 9..11 -> "☀️ Mid-Morning"
            in 12..15 -> "☀️ Afternoon (After Dhuhr / Zuhr)"
            in 16..17 -> "🌤️ Late Afternoon (After Asr)"
            in 18..19 -> "🌇 Evening (After Maghrib)"
            in 20..23 -> "🌙 Night (After Isha)"
            else -> "🌌 Late Night (Before Fajr)"
        }
    }

    // Filtered recommended list for "Read Right Now"
    val recommendedDuas = remember(customDuas.size, duaPlans.values.size) {
        val allLoaded = MASNOON_DUAS + customDuas
        allLoaded.filter { dua ->
            val plan = duaPlans[dua.id] ?: emptySet()
            plan.any { it in currentTimeSlots }
        }
    }

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    
    // Active focus modes
    var focusModeDua by remember { mutableStateOf<MasnoonDua?>(null) }
    var focusModeTasbih by remember { mutableStateOf<TasbihEntity?>(null) }

    // Dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTasbihName by remember { mutableStateOf("") }
    var newTasbihTarget by remember { mutableStateOf("1000") }
    
    var showEditTargetDialog by remember { mutableStateOf<TasbihEntity?>(null) }
    var editTargetInput by remember { mutableStateOf("") }

    var isVibrationEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("vibrate_on_tap", true)) }

    var showResetDialog by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

    // Custom daily masnoon states
    var showAddCustomDuaDialog by remember { mutableStateOf(false) }
    var newDuaTitle by remember { mutableStateOf("") }
    var newDuaArabic by remember { mutableStateOf("") }
    var newDuaTranslit by remember { mutableStateOf("") }
    var newDuaTranslation by remember { mutableStateOf("") }
    var newDuaTarget by remember { mutableStateOf("33") }
    var newDuaPlanSlots by remember { mutableStateOf(setOf("Anytime")) }

    var showPlanDialogDua by remember { mutableStateOf<MasnoonDua?>(null) }
    var showDeleteDuaConfirmId by remember { mutableStateOf<String?>(null) }

    // Helper for haptic vibration
    fun triggerVibration() {
        if (!isVibrationEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Scaffold (shown when NO focus mode is active)
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "TASBIH COUNTER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = if (selectedTab == 0) "Daily Masnoon Duas" else "Personal Tracker",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.testTag("tasbih_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Sliding Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)) },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Daily Masnoon", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Personal Tracker", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                if (selectedTab == 0) {
                    // ==========================================
                    // TAB 0: DAILY MASNOON DUAS
                    // ==========================================
                    // Category selection and Add Custom Dua button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Scrolling categories Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            listOf("All", "After Salah", "Morning & Evening", "Custom").forEach { category ->
                                val isSelected = selectedCategoryFilter == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        )
                                        .clickable { selectedCategoryFilter = category }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        // Add custom masnoon button
                        Button(
                            onClick = { showAddCustomDuaDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Filtered list combining standard and custom duas
                    val filteredDuas = remember(selectedCategoryFilter, customDuas.size) {
                        val all = MASNOON_DUAS + customDuas
                        when (selectedCategoryFilter) {
                            "All" -> all
                            "Custom" -> customDuas
                            else -> all.filter { it.category == selectedCategoryFilter }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("masnoon_list_view"),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // 1. RECOMMENDATION HEADER COMPONENT ("Read Right Now")
                        item {
                            FrostedGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                cornerRadius = 18.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Smart Schedule",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "📖 WHAT TO READ RIGHT NOW",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Current period: $currentPeriodLabel",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    if (recommendedDuas.isEmpty()) {
                                        Text(
                                            text = "No recommended Duas planned for this period. Click the clock icon on any Dua to schedule it here!",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            lineHeight = 16.sp
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            recommendedDuas.forEach { dua ->
                                                val count = masnoonCounts[dua.id] ?: 0
                                                val isCompleted = count >= dua.target
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                                        .clickable { focusModeDua = dua }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                            contentDescription = null,
                                                            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = dua.title,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = "$count / ${dua.target} completed",
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                            )
                                                        }
                                                    }
                                                    
                                                    // Quick increment circle
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                            .clickable {
                                                                triggerVibration()
                                                                val newCount = count + 1
                                                                masnoonCounts[dua.id] = newCount
                                                                sharedPrefs.edit().putInt("masnoon_count_${dua.id}_$todayDateStr", newCount).apply()
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "+1",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. PRIMARY MASNOON DUAS LIST
                        items(filteredDuas) { item ->
                            val itemDuaCount = masnoonCounts[item.id] ?: 0
                            val isCompleted = itemDuaCount >= item.target
                            
                            FrostedGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { focusModeDua = item }
                                    .testTag("masnoon_card_${item.id}"),
                                cornerRadius = 18.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isCompleted) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Completed",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            if (item.arabic.isNotEmpty()) {
                                                Text(
                                                    text = item.arabic,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                            if (item.translation.isNotEmpty()) {
                                                Text(
                                                    text = item.translation,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    lineHeight = 15.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            // Progress text
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "$itemDuaCount / ${item.target} completed",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                LinearProgressIndicator(
                                                    progress = { (if (item.target > 0) itemDuaCount.toFloat() / item.target else 0f).coerceIn(0f, 1f) },
                                                    modifier = Modifier
                                                        .width(60.dp)
                                                        .height(4.dp)
                                                        .clip(CircleShape),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Compact quick tap increment clicker on the right
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                .clickable {
                                                    triggerVibration()
                                                    val newCount = itemDuaCount + 1
                                                    masnoonCounts[item.id] = newCount
                                                    sharedPrefs.edit().putInt("masnoon_count_${item.id}_$todayDateStr", newCount).apply()
                                                }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Quick Add",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "+1",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    // Action bar: Scheduling info & Configure Button
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val plans = duaPlans[item.id] ?: emptySet()
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (plans.isEmpty()) {
                                                Text(
                                                    text = "Not planned",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                plans.take(3).forEach { p ->
                                                    val emoji = when (p) {
                                                        "Morning" -> "🌅"
                                                        "Evening" -> "🌆"
                                                        "After Fajr" -> "🌄"
                                                        "After Dhuhr" -> "☀️"
                                                        "After Asr" -> "🌤️"
                                                        "After Maghrib" -> "🌇"
                                                        "After Isha" -> "🌙"
                                                        else -> "⏳"
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "$emoji $p",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                if (plans.size > 3) {
                                                    Text(
                                                        text = "+${plans.size - 3}",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        // Edit plan and Delete buttons
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { showPlanDialogDua = item },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = "Plan Dua Time",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            
                                            if (item.isCustom) {
                                                IconButton(
                                                    onClick = { showDeleteDuaConfirmId = item.id },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Custom Dua",
                                                        tint = Color.Red.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // ==========================================
                    // TAB 1: PERSONAL TASBIH GOAL TRACKER
                    // ==========================================
                    // Header Row with a beautiful "+ New Counter" button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CUSTOM COUNTERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            letterSpacing = 1.2.sp
                        )

                        Button(
                            onClick = {
                                newTasbihName = ""
                                newTasbihTarget = "1000"
                                showCreateDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("create_tasbih_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add New", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Counter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (tasbihList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No custom counters yet.\nTap 'New Counter' to start tracking your own custom dhikr!",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("personal_tasbih_list"),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(tasbihList) { item ->
                                FrostedGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { focusModeTasbih = item }
                                        .testTag("personal_card_${item.id}"),
                                    cornerRadius = 18.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Progress: ${item.count} / ${item.target} completed",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Linear Progress bar
                                            LinearProgressIndicator(
                                                progress = { item.progress.coerceIn(0f, 1f) },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.9f)
                                                    .height(5.dp)
                                                    .clip(CircleShape),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Compact direct clicker on the right
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                .clickable {
                                                    triggerVibration()
                                                    viewModel.incrementTasbih(item.id)
                                                }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Quick Add",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "+1",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // IMMERSIVE FULL SCREEN FOCUS COUNTING OVERLAYS
        // ==========================================
        
        // 1. FOCUS MODE FOR DAILY MASNOON
        focusModeDua?.let { activeDua ->
            val currentCount = masnoonCounts[activeDua.id] ?: 0
            val progress = if (activeDua.target > 0) currentCount.toFloat() / activeDua.target else 0f
            val progressAnimation by animateFloatAsState(
                targetValue = progress.coerceIn(0f, 1f),
                label = "masnoon_focus_progress"
            )

            // Overlap UI occupying full screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
                    .clickable(enabled = false) {} // block click propagation
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row Navigation & Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { focusModeDua = null },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "FOCUS MODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )

                        IconButton(
                            onClick = {
                                isVibrationEnabled = !isVibrationEnabled
                                sharedPrefs.edit().putBoolean("vibrate_on_tap", isVibrationEnabled).apply()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isVibrationEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        ) {
                            Icon(
                                imageVector = if (isVibrationEnabled) Icons.Default.Vibration else Icons.Default.VolumeMute,
                                contentDescription = "Toggle vibration",
                                tint = if (isVibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // Middle Section: Calligraphy & Text details
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        if (activeDua.arabic.isNotEmpty()) {
                            Text(
                                text = activeDua.arabic,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                lineHeight = 44.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Text(
                            text = activeDua.transliteration.ifEmpty { activeDua.title },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        if (activeDua.translation.isNotEmpty()) {
                            Text(
                                text = activeDua.translation,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // TACTILE HUGE CIRCULAR CLICKER
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(250.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .clickable {
                                triggerVibration()
                                val newCount = currentCount + 1
                                masnoonCounts[activeDua.id] = newCount
                                sharedPrefs.edit().putInt("masnoon_count_${activeDua.id}_$todayDateStr", newCount).apply()
                            }
                            .testTag("focus_giant_clicker")
                    ) {
                        CircularProgressIndicator(
                            progress = { progressAnimation },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            strokeCap = StrokeCap.Round
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentCount.toString(),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TAP TO COUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    // Bottom Adjustments Controls (Reset, Decrement, Goal Details)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = "Daily Goal: ${activeDua.target}   •   Remaining: ${(activeDua.target - currentCount).coerceAtLeast(0)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decrement
                            IconButton(
                                onClick = {
                                    val newCount = (currentCount - 1).coerceAtLeast(0)
                                    masnoonCounts[activeDua.id] = newCount
                                    sharedPrefs.edit().putInt("masnoon_count_${activeDua.id}_$todayDateStr", newCount).apply()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Subtract 1",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Reset
                            Button(
                                onClick = {
                                    masnoonCounts[activeDua.id] = 0
                                    sharedPrefs.edit().putInt("masnoon_count_${activeDua.id}_$todayDateStr", 0).apply()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.RotateLeft, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. FOCUS MODE FOR PERSONAL TASBIH
        focusModeTasbih?.let { activeTasbihEntity ->
            // Retrieve up-to-date values from list to reflect recompositions
            val currentTasbih = tasbihList.find { it.id == activeTasbihEntity.id } ?: activeTasbihEntity
            val progressAnimation by animateFloatAsState(
                targetValue = currentTasbih.progress.coerceIn(0f, 1f),
                label = "personal_focus_progress"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row Navigation & Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { focusModeTasbih = null },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "FOCUS MODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )

                        IconButton(
                            onClick = {
                                isVibrationEnabled = !isVibrationEnabled
                                sharedPrefs.edit().putBoolean("vibrate_on_tap", isVibrationEnabled).apply()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isVibrationEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        ) {
                            Icon(
                                imageVector = if (isVibrationEnabled) Icons.Default.Vibration else Icons.Default.VolumeMute,
                                contentDescription = "Toggle vibration",
                                tint = if (isVibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = currentTasbih.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Custom Counter",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // TACTILE HUGE CIRCULAR CLICKER
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(250.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .clickable {
                                triggerVibration()
                                viewModel.incrementTasbih(currentTasbih.id)
                            }
                            .testTag("focus_giant_clicker_personal")
                    ) {
                        CircularProgressIndicator(
                            progress = { progressAnimation },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            strokeCap = StrokeCap.Round
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentTasbih.count.toString(),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TAP TO COUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    // Bottom Adjustments Controls (Reset, Decrement, Delete, Edit)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            // Target Goals Edit Chip
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        editTargetInput = currentTasbih.target.toString()
                                        showEditTargetDialog = currentTasbih
                                    }
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Goal",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Target: ${currentTasbih.target}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Progress percentage
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Completed: ${(currentTasbih.progress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decrement
                            IconButton(
                                onClick = { viewModel.decrementTasbih(currentTasbih.id) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Subtract 1",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Reset
                            Button(
                                onClick = { showResetDialog = currentTasbih.id },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.RotateLeft, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Delete Tasbih
                            IconButton(
                                onClick = { showDeleteDialog = currentTasbih.id },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // CONFIRMATION, ADDITION & PLANNING DIALOGS
    // ==========================================

    // Plan / Schedule Dua Dialog
    showPlanDialogDua?.let { dua ->
        val currentPlan = duaPlans[dua.id] ?: emptySet()
        val allTimeSlots = listOf(
            "Morning" to "🌅 Morning Adhkar",
            "After Fajr" to "🌄 After Fajr",
            "After Dhuhr" to "☀️ After Dhuhr (Zuhr)",
            "After Asr" to "🌤️ After Asr",
            "After Maghrib" to "🌇 After Maghrib",
            "After Isha" to "🌙 After Isha",
            "Evening" to "🌆 Evening Adhkar",
            "Anytime" to "⏳ Anytime / General"
        )
        
        var tempPlan by remember(dua.id) { mutableStateOf(currentPlan) }
        
        AlertDialog(
            onDismissRequest = { showPlanDialogDua = null },
            title = { Text("Plan / Schedule Dua") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select when you want to read \"${dua.title}\" during the day:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 250.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allTimeSlots) { (slotId, label) ->
                            val isChecked = slotId in tempPlan
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        tempPlan = if (isChecked) tempPlan - slotId else tempPlan + slotId
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        tempPlan = if (checked == true) tempPlan + slotId else tempPlan - slotId
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = label, fontSize = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sharedPrefs.edit().putStringSet("plan_${dua.id}", tempPlan).apply()
                        duaPlans[dua.id] = tempPlan
                        showPlanDialogDua = null
                    }
                ) {
                    Text("Save Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlanDialogDua = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Custom Masnoon Dua Dialog
    if (showAddCustomDuaDialog) {
        val allTimeSlots = listOf(
            "Morning" to "🌅 Morning Adhkar",
            "After Fajr" to "🌄 After Fajr",
            "After Dhuhr" to "☀️ After Dhuhr (Zuhr)",
            "After Asr" to "🌤️ After Asr",
            "After Maghrib" to "🌇 After Maghrib",
            "After Isha" to "🌙 After Isha",
            "Evening" to "🌆 Evening Adhkar",
            "Anytime" to "⏳ Anytime / General"
        )
        
        AlertDialog(
            onDismissRequest = { showAddCustomDuaDialog = false },
            title = { Text("Add Custom Masnoon Dua") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newDuaTitle,
                        onValueChange = { newDuaTitle = it },
                        label = { Text("Dua Name / Title (Required)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = newDuaArabic,
                        onValueChange = { newDuaArabic = it },
                        label = { Text("Arabic Text (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = newDuaTranslit,
                        onValueChange = { newDuaTranslit = it },
                        label = { Text("Transliteration (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = newDuaTranslation,
                        onValueChange = { newDuaTranslation = it },
                        label = { Text("Translation / Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = newDuaTarget,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                newDuaTarget = it
                            }
                        },
                        label = { Text("Daily Target Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "Plan / Schedule (When to read):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    allTimeSlots.forEach { (slotId, label) ->
                        val isChecked = slotId in newDuaPlanSlots
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    newDuaPlanSlots = if (isChecked) newDuaPlanSlots - slotId else newDuaPlanSlots + slotId
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    newDuaPlanSlots = if (checked == true) newDuaPlanSlots + slotId else newDuaPlanSlots - slotId
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = label, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDuaTitle.isNotBlank()) {
                            val id = "custom_dua_${System.currentTimeMillis()}"
                            val targetVal = newDuaTarget.toIntOrNull() ?: 33
                            
                            // Save to SharedPreferences
                            val ids = sharedPrefs.getStringSet("custom_dua_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                            ids.add(id)
                            
                            sharedPrefs.edit().apply {
                                putStringSet("custom_dua_ids", ids)
                                putString("custom_dua_title_$id", newDuaTitle)
                                putString("custom_dua_arabic_$id", newDuaArabic)
                                putString("custom_dua_translit_$id", newDuaTranslit)
                                putString("custom_dua_translation_$id", newDuaTranslation)
                                putInt("custom_dua_target_$id", targetVal)
                                putString("custom_dua_category_$id", "Custom")
                                putStringSet("plan_$id", newDuaPlanSlots)
                            }.apply()
                            
                            // Re-load custom list
                            customDuas.clear()
                            customDuas.addAll(loadCustomDuas())
                            
                            // Reset input fields
                            newDuaTitle = ""
                            newDuaArabic = ""
                            newDuaTranslit = ""
                            newDuaTranslation = ""
                            newDuaTarget = "33"
                            newDuaPlanSlots = setOf("Anytime")
                            showAddCustomDuaDialog = false
                        }
                    }
                ) {
                    Text("Add Dua")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDuaDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Custom Dua confirmation
    showDeleteDuaConfirmId?.let { id ->
        val dua = customDuas.find { it.id == id }
        AlertDialog(
            onDismissRequest = { showDeleteDuaConfirmId = null },
            title = { Text("Delete Custom Dua?") },
            text = { Text("Are you sure you want to permanently delete \"${dua?.title ?: ""}\"? This will also erase its recorded counts for today.") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteCustomDua(id)
                        showDeleteDuaConfirmId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDuaConfirmId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create New Tasbih Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Tasbih") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTasbihName,
                        onValueChange = { newTasbihName = it },
                        label = { Text("Tasbih Name / Phrase") },
                        placeholder = { Text("e.g. Subhanallah") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_new_tasbih_name")
                    )

                    OutlinedTextField(
                        value = newTasbihTarget,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                newTasbihTarget = it
                            }
                        },
                        label = { Text("Target Goal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_new_tasbih_target")
                    )

                    // Target presets selection
                    Text(
                        text = "Suggested Goals:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(33, 100, 1000, 100000).forEach { preset ->
                            val label = if (preset >= 100000) "1 Lakh" else preset.toString()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .clickable { newTasbihTarget = preset.toString() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetVal = newTasbihTarget.toIntOrNull() ?: 100
                        if (newTasbihName.isNotBlank() && targetVal > 0) {
                            viewModel.addTasbih(newTasbihName, targetVal)
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_new_tasbih_confirm")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Target Goal Dialog
    showEditTargetDialog?.let { tasbih ->
        AlertDialog(
            onDismissRequest = { showEditTargetDialog = null },
            title = { Text("Edit Target Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Change target for: ${tasbih.name}", fontSize = 14.sp)
                    
                    OutlinedTextField(
                        value = editTargetInput,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                editTargetInput = it
                            }
                        },
                        label = { Text("New Target Goal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_edit_target_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newTarget = editTargetInput.toIntOrNull() ?: tasbih.target
                        if (newTarget > 0) {
                            viewModel.updateTasbihTarget(tasbih.id, newTarget)
                            showEditTargetDialog = null
                        }
                    },
                    modifier = Modifier.testTag("dialog_edit_target_confirm")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTargetDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    showResetDialog?.let { tasbihId ->
        AlertDialog(
            onDismissRequest = { showResetDialog = null },
            title = { Text("Reset Count?") },
            text = { Text("Are you sure you want to reset this tasbih counter back to 0? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetTasbih(tasbihId)
                        showResetDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_reset_confirm")
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { tasbihId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Counter?") },
            text = { Text("Are you sure you want to delete this tasbih counter? All recorded counts and progress will be permanently lost.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTasbih(tasbihId)
                        showDeleteDialog = null
                        focusModeTasbih = null // also exit focus mode if active
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_delete_confirm")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
