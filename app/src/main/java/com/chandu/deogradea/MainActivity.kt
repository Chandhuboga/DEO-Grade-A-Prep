package com.chandu.deogradea

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import kotlin.math.max
import com.chandu.deogradea.data.DeoGradeAPlan
import com.chandu.deogradea.data.DailyQuest
import com.chandu.deogradea.data.Achievements
import com.chandu.deogradea.data.AchievementStats
import com.chandu.deogradea.notifications.SoundManager
import com.chandu.deogradea.notifications.SystemNotificationScheduler
import com.chandu.deogradea.notifications.NotificationHelper
import com.chandu.deogradea.widget.TodayWidgetProvider

class MainActivity : ComponentActivity() {
    companion object { const val EXTRA_OPEN_QUEST = "open_quest" }
    private lateinit var sounds: SoundManager
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sounds = SoundManager(this)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val prefs = getSharedPreferences("gate_progress", Context.MODE_PRIVATE)
        SystemNotificationScheduler.scheduleAll(this)
        setContent { DeoGradeAApp(sounds, prefs, wantsQuestScreen(intent)) }
        sounds.play(SoundManager.Event.SYSTEM_BOOT)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (wantsQuestScreen(intent)) {
            val prefs = getSharedPreferences("gate_progress", Context.MODE_PRIVATE)
            setContent { DeoGradeAApp(sounds, prefs, true) }
        }
    }

    // True if launched from a notification tap, the home-screen widget, or the
    // "Today's Quest" long-press app shortcut (which arrives as ACTION_VIEW).
    private fun wantsQuestScreen(intent: Intent): Boolean =
        intent.getBooleanExtra(EXTRA_OPEN_QUEST, false) || intent.action == Intent.ACTION_VIEW

    override fun onDestroy() { sounds.release(); super.onDestroy() }
}

private val Bg = Color(0xFF03070D)
private val Panel = Color(0xFF07101A)
private val Cyan = Color(0xFF63D8FF)
private val Gold = Color(0xFFFFD166)
private val Purple = Color(0xFFB98CFF)
private val Green = Color(0xFF68F5B0)
private val Red = Color(0xFFFF6B7A)
private val Muted = Color(0xFF91A1AF)

// Daily XP System (per source plan): RNR +20, Main Topic +30 (+40 on Weekly Boss,
// +100 on a Full Mock), Practice +20, Secondary Subject +20, GA +15, Computer/DEO +15,
// Error Monster +20. Quest-clear bonus = "1-5-7 completed" +25.
private const val OBJECTIVE_COUNT = 7
private const val QUEST_BONUS_XP = 25
private const val GOLD_PER_OBJECTIVE = 25
private const val QUEST_BONUS_GOLD = 50

/** XP for completing objective at 0-based [index] today, given today's [quest]. */
private fun objectiveXp(quest: DailyQuest, index: Int): Int = when (index) {
    0 -> 20 // RNR
    1 -> if (quest.isMockDay) 100 else if (quest.isWeeklyBoss || quest.isMajorBoss) 40 else 30 // Main Topic
    2 -> 20 // Practice
    3 -> 20 // Secondary Subject
    4 -> 15 // GA
    5 -> 15 // Computer / DEO
    else -> 20 // Error Monster
}

@Composable
private fun DeoGradeAApp(sounds: SoundManager, prefs: android.content.SharedPreferences, openQuest: Boolean) {
    var tab by remember { mutableIntStateOf(if (openQuest) 1 else 0) }
    var showSplash by remember { mutableStateOf(!openQuest) }
    val context = LocalContext.current
    val today = LocalDate.now()
    val day = DeoGradeAPlan.currentDay(today)
    val quest = remember(day) { DeoGradeAPlan.forDay(day) }
    var doneCount by remember(day) { mutableIntStateOf(prefs.getInt("day_${day}_done", 0).coerceIn(0, OBJECTIVE_COUNT)) }
    var questCleared by remember(day) { mutableStateOf(prefs.getBoolean("day_${day}_cleared", false)) }
    var totalXp by remember { mutableIntStateOf(prefs.getInt("xp", 0).coerceAtLeast(0)) }
    var gold by remember { mutableIntStateOf(prefs.getInt("gold", 0).coerceAtLeast(0)) }
    var streak by remember { mutableIntStateOf(prefs.getInt("streak", 0).coerceAtLeast(0)) }
    var soundOn by remember { mutableStateOf(true) }
    var vibrationOn by remember { mutableStateOf(true) }
    var dailyQuestNotifsOn by remember { mutableStateOf(prefs.getBoolean("daily_quest_notifs_enabled", true)) }
    var burst by remember { mutableStateOf<SystemBurst?>(null) }

    LaunchedEffect(Unit) {
        if (!prefs.contains("last_progress_at")) {
            prefs.edit().putLong("last_progress_at", System.currentTimeMillis()).apply()
        }
        SystemNotificationScheduler.scheduleAll(context)
        TodayWidgetProvider.updateAll(context)
    }

    LaunchedEffect(dailyQuestNotifsOn) { prefs.edit().putBoolean("daily_quest_notifs_enabled", dailyQuestNotifsOn).apply() }

    val level = progressionLevel(totalXp)
    val rank = progressionRank(level)
    val levelBaseXp = levelBase(level)
    val nextLevelXp = levelBase(level + 1)
    val levelProgress = ((totalXp - levelBaseXp).toFloat() / max(1, nextLevelXp - levelBaseXp)).coerceIn(0f, 1f)
    val playerPower = totalXp / 10 + doneCount * 25

    LaunchedEffect(soundOn, vibrationOn) { sounds.enabled = soundOn; sounds.vibrationEnabled = vibrationOn }

    fun save() {
        prefs.edit().putInt("day_${day}_done", doneCount).putBoolean("day_${day}_cleared", questCleared).putInt("xp", totalXp).putInt("gold", gold).apply()
    }

    fun completeObjective() {
        if (doneCount >= OBJECTIVE_COUNT) return
        val oldLevel = progressionLevel(totalXp)
        val gainedXp = objectiveXp(quest, doneCount)
        doneCount += 1
        totalXp += gainedXp
        gold += GOLD_PER_OBJECTIVE
        val leveledUp = progressionLevel(totalXp) > oldLevel

        // Persist XP/level/progress FIRST, before any sound/vibration/animation effect
        // runs. Those effects are already crash-proofed internally, but saving first
        // means even an unforeseen effect failure can never cost real progress.
        prefs.edit().putLong("last_progress_at", System.currentTimeMillis()).apply()
        save()

        sounds.play(SoundManager.Event.CHECKBOX_TICK)
        sounds.play(SoundManager.Event.EXP_GAIN)
        if (leveledUp) {
            sounds.play(SoundManager.Event.LEVEL_UP)
            burst = SystemBurst("\u25B2 LEVEL UP", "YOU HAVE REACHED LEVEL ${progressionLevel(totalXp)}")
        }
        SystemNotificationScheduler.scheduleInactivityFromProgress(context, System.currentTimeMillis())
        TodayWidgetProvider.updateAll(context)
    }

    fun clearQuest() {
        if (questCleared || doneCount < OBJECTIVE_COUNT) return
        val oldRank = progressionRank(progressionLevel(totalXp))
        totalXp += QUEST_BONUS_XP
        gold += QUEST_BONUS_GOLD
        questCleared = true
        val lastClearedDay = prefs.getString("last_cleared_date", null)
        val yesterday = today.minusDays(1).toString()
        streak = if (lastClearedDay == yesterday) streak + 1 else 1
        val rankedUp = progressionRank(progressionLevel(totalXp)) != oldRank

        // Same principle: persist first, cosmetic effects after.
        prefs.edit().putInt("streak", streak).putInt("total_quests_cleared", prefs.getInt("total_quests_cleared", 0) + 1).putString("last_cleared_date", today.toString()).apply()
        save()

        sounds.play(SoundManager.Event.QUEST_APPEAR)
        if (rankedUp) {
            sounds.play(SoundManager.Event.RANK_UP)
            burst = SystemBurst("\u2605 RANK UP", "RANK ${progressionRank(progressionLevel(totalXp))} ATTAINED")
        }
        TodayWidgetProvider.updateAll(context)
    }

    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Panel, primary = Cyan)) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
          Box(Modifier.fillMaxSize()) {
            SystemBackground(Modifier.fillMaxSize())
            if (showSplash) {
                AwakeningWindow(day, rank, level, quest, streak) { showSplash = false; sounds.play(SoundManager.Event.ARISE_SYSTEM) }
            } else {
                Column(Modifier.fillMaxSize().padding(14.dp)) {
                    SystemHeader(level, rank, totalXp, playerPower, day)
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().weight(1f)) {
                        when (tab) {
                            0 -> Dashboard(level, rank, totalXp, gold, playerPower, levelProgress, day, quest, doneCount, questCleared, streak, prefs, sounds, ::completeObjective)
                            1 -> QuestScreen(day, quest, doneCount, questCleared, totalXp, gold, sounds, prefs, ::completeObjective, ::clearQuest)
                            2 -> PlanScreen(day)
                            3 -> HunterScreen(prefs, totalXp, level, streak)
                            else -> SettingsScreen(soundOn, vibrationOn, dailyQuestNotifsOn, { soundOn = it }, { vibrationOn = it }, { dailyQuestNotifsOn = it }, sounds, context, { doneCount = 0; questCleared = false; totalXp = 0; gold = 0; streak = 0; prefs.edit().clear().apply(); SystemNotificationScheduler.scheduleAll(context); sounds.play(SoundManager.Event.WARNING) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NavButton("\u25C8 SYSTEM", tab == 0) { tab = 0 }
                        NavButton("\u2694 QUEST", tab == 1) { tab = 1; sounds.play(SoundManager.Event.QUEST_APPEAR) }
                        NavButton("\u25A3 PLAN", tab == 2) { tab = 2; sounds.play(SoundManager.Event.QUEST_APPEAR) }
                        NavButton("\uD83C\uDFC6 RANKS", tab == 3) { tab = 3; sounds.play(SoundManager.Event.QUEST_APPEAR) }
                        NavButton("\u2699 SET", tab == 4) { tab = 4 }
                    }
                }
            }
            burst?.let { b -> CinematicBurstOverlay(b) { burst = null } }
          }
        }
    }
}

/** Full-screen "System" awakening window shown once per app open, Solo-Leveling style. */
@Composable
private fun AwakeningWindow(day: Int, rank: String, level: Int, quest: DailyQuest, streak: Int, onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, animationSpec = tween(700), label = "splashAlpha")
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.35f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glowAnim"
    )
    LaunchedEffect(Unit) { visible = true; delay(2600); onDismiss() }

    Box(
        Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .alpha(alpha)
                .fillMaxWidth()
                .border(1.dp, Cyan.copy(alpha = glow), CutCornerShape(14.dp))
                .background(Panel)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("\u25C8 \u25C8 \u25C8", color = Cyan.copy(alpha = glow), fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            Text("THE SYSTEM HAS AWAKENED", color = Cyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("DEO GRADE A PREPARATION", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text("95-DAY SSC CHSL TIER-I SYSTEM", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(14.dp))
            Text("PLAYER  ${DeoGradeAPlan.PLAYER_NAME.uppercase()}", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("RANK $rank  \u2022  LEVEL $level  \u2022  DAY $day / ${DeoGradeAPlan.TOTAL_DAYS}", color = Muted, fontSize = 12.sp)
            if (streak > 0) Text("STREAK  \uD83D\uDD25 $streak DAY${if (streak == 1) "" else "S"}", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text("TODAY'S MAIN QUEST", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            TypewriterText(quest.mainQuest, color = Color.White, fontSize = 18.sp, textAlign = TextAlign.Center)
            Text(quest.phase, color = Cyan, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Text("[ TAP TO ENTER ]", color = Muted, fontSize = 11.sp)
        }
    }
}

private fun levelBase(level: Int): Int = if (level <= 1) 0 else (level - 1) * 500
private fun progressionLevel(xp: Int): Int = (xp / 500) + 1
private fun progressionRank(level: Int): String = when { level >= 21 -> "S"; level >= 16 -> "A"; level >= 11 -> "B"; level >= 7 -> "C"; level >= 4 -> "D"; else -> "E" }

@Composable
private fun SystemHeader(level: Int, rank: String, xp: Int, playerPower: Int, day: Int) {
    Column(Modifier.fillMaxWidth().border(1.dp, Cyan.copy(alpha = .7f), CutCornerShape(8.dp)).padding(14.dp)) {
        Text("\u25A3 SYSTEM ONLINE \u2022 V9.0", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Text("DEO GRADE A PREPARATION", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 0.3.sp)
        Text("PLAYER ${DeoGradeAPlan.PLAYER_NAME.uppercase()} \u2022 RANK $rank \u2022 LEVEL $level \u2022 POWER $playerPower", color = Muted, fontSize = 11.sp)
        Text("DAY $day / ${DeoGradeAPlan.TOTAL_DAYS}  \u2022  XP $xp / NEXT ${levelBase(level + 1)}", color = Gold, fontSize = 10.sp)
    }
}

@Composable
private fun Dashboard(level: Int, rank: String, xp: Int, gold: Int, playerPower: Int, progress: Float, day: Int, quest: DailyQuest, done: Int, cleared: Boolean, streak: Int, prefs: android.content.SharedPreferences, sounds: SoundManager, onObjective: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        item { Window("\u25C8 PLAYER STATUS \u2022 ${DeoGradeAPlan.PLAYER_NAME.uppercase()}") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Stat("RANK", rank); Stat("LEVEL", level.toString()); Stat("EXP", xp.toString()); Stat("GOLD", gold.toString()) }
            Spacer(Modifier.height(10.dp)); Text("LEVEL PROGRESS", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp))
            Spacer(Modifier.height(5.dp)); Text("PLAYER POWER  $playerPower", color = Gold, fontWeight = FontWeight.Bold)
        }}
        item { StreakAndCountdownWindow(streak, prefs) }
        item { DailyQuestWindow(day, quest, done, cleared, sounds, onObjective) }
        item { ReadinessWindow(day, done, cleared) }
        item { Window("\u267B RNR \u2022 1-5-7") {
            Text(quest.rnr, color = Color.White, fontSize = 12.sp)
        }}
    }
}

@Composable
private fun StreakAndCountdownWindow(streak: Int, prefs: android.content.SharedPreferences) {
    var targetDateText by remember { mutableStateOf(prefs.getString("cycle_end_date", DeoGradeAPlan.START_DATE.plusDays(DeoGradeAPlan.TOTAL_DAYS.toLong()).toString()) ?: "") }
    var editing by remember { mutableStateOf(false) }
    val daysLeft = remember(targetDateText) {
        try { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(targetDateText)).toInt() } catch (_: Exception) { null }
    }
    Window("\uD83D\uDD25 STREAK  \u2022  \u23F3 CYCLE COUNTDOWN") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("$streak", color = if (streak > 0) Green else Muted, fontSize = 26.sp, fontWeight = FontWeight.Black); Text("DAY STREAK", color = Muted, fontSize = 10.sp) }
            Column(horizontalAlignment = Alignment.End) {
                Text(daysLeft?.let { "$it" } ?: "\u2014", color = Gold, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text("DAYS TO FINAL SYSTEM REVIEW", color = Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        if (editing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = targetDateText, onValueChange = { targetDateText = it }, label = { Text("Cycle end date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.weight(1f))
                TextButton(onClick = { prefs.edit().putString("cycle_end_date", targetDateText).apply(); editing = false }) { Text("SAVE") }
            }
        } else {
            TextButton(onClick = { editing = true }) { Text("SET CYCLE END DATE: $targetDateText", color = Cyan, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun DailyQuestWindow(day: Int, quest: DailyQuest, done: Int, cleared: Boolean, sounds: SoundManager, onObjective: () -> Unit) {
    Window("\u2694 DAILY QUEST \u2022 DAY $day / ${DeoGradeAPlan.TOTAL_DAYS}") {
        Text(quest.phase, color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        TypewriterText(quest.mainQuest, color = Color.White, fontSize = 19.sp)
        if (quest.isMockDay) Text("\u2694\uFE0F FULL MOCK DAY", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        else if (quest.isWeeklyBoss) Text("\u2694\uFE0F WEEKLY BOSS", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        else if (quest.isMajorBoss) Text("\uD83C\uDFC6 MAJOR BOSS", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        QuestField("\uD83D\uDCD8 SECONDARY SUBJECT", quest.secondaryQuest)
        QuestField("\uD83C\uDF0D GA", quest.gaQuest)
        QuestField("\uD83D\uDCBB COMPUTER / DEO", quest.computerQuest)
        Spacer(Modifier.height(7.dp))
        Text("Objectives: $done / $OBJECTIVE_COUNT", color = Color.White, fontWeight = FontWeight.Bold)
        DailyObjectivesChecklist(quest, done, sounds, onObjective)
        if (cleared) Text("QUEST CLEARED \u2022 REWARDS CLAIMED \u2713", color = Green, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuestField(label: String, value: String) {
    Text(label, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    Text(value, color = Color.White, fontSize = 12.sp)
    Spacer(Modifier.height(5.dp))
}

@Composable
private fun DailyObjectivesChecklist(quest: DailyQuest, done: Int, sounds: SoundManager, onObjective: () -> Unit) {
    QuestCheck("Quest 1 \u2022 RNR / 1-5-7 done (20\u201330 min) \u2022 +${objectiveXp(quest, 0)} XP", done >= 1, sounds, onObjective)
    QuestCheck("Quest 2 \u2022 Main Topic: ${quest.mainQuest} (60\u201390 min) \u2022 +${objectiveXp(quest, 1)} XP", done >= 2, sounds, onObjective)
    QuestCheck("Quest 3 \u2022 Practice done (30\u201345 min) \u2022 +${objectiveXp(quest, 2)} XP", done >= 3, sounds, onObjective)
    QuestCheck("Quest 4 \u2022 Secondary Subject (45\u201360 min) \u2022 +${objectiveXp(quest, 3)} XP", done >= 4, sounds, onObjective)
    QuestCheck("Quest 5 \u2022 GA (20\u201330 min) \u2022 +${objectiveXp(quest, 4)} XP", done >= 5, sounds, onObjective)
    QuestCheck("Quest 6 \u2022 Computer / DEO (20\u201330 min) \u2022 +${objectiveXp(quest, 5)} XP", done >= 6, sounds, onObjective)
    QuestCheck("Quest 7 \u2022 Error Monster logged (15\u201320 min) \u2022 +${objectiveXp(quest, 6)} XP", done >= 7, sounds, onObjective)
}

@Composable
private fun QuestScreen(day: Int, quest: DailyQuest, done: Int, cleared: Boolean, xp: Int, gold: Int, sounds: SoundManager, prefs: android.content.SharedPreferences, onObjective: () -> Unit, onClear: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        item { Window("\u2694 QUEST WINDOW") {
            Text("DAY $day / ${DeoGradeAPlan.TOTAL_DAYS} \u2022 ${quest.phase}", color = Purple, fontWeight = FontWeight.Bold)
            Text(quest.mainQuest, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            if (quest.isMockDay) Text("\u2694\uFE0F FULL MOCK DAY", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            else if (quest.isWeeklyBoss) Text("\u2694\uFE0F WEEKLY BOSS", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            else if (quest.isMajorBoss) Text("\uD83C\uDFC6 MAJOR BOSS", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            else if (quest.isAnalysisDay) Text("\uD83D\uDD0D ANALYSIS DAY", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            QuestField("\uD83D\uDCD8 SECONDARY SUBJECT", quest.secondaryQuest)
            QuestField("\uD83C\uDF0D GA", quest.gaQuest)
            QuestField("\uD83D\uDCBB COMPUTER / DEO", quest.computerQuest)
            QuestField("\uD83D\uDCDD PRACTICE", quest.practice)
        }}
        item { Window("\u267B RNR \u2022 1-5-7") { Text(quest.rnr, color = Color.White, fontSize = 12.sp) } }
        item { Window("\u2611 OBJECTIVES") { DailyObjectivesChecklist(quest, done, sounds, onObjective) } }
        item { ErrorMonsterWindow(day, quest, prefs, sounds) }
        item { Window("\u2611 REWARD WINDOW") {
            Text("Current XP: $xp", color = Cyan); Text("Current Gold: $gold", color = Gold)
            Spacer(Modifier.height(6.dp)); Button(onClick = onClear, enabled = done >= OBJECTIVE_COUNT && !cleared) { Text(if (cleared) "QUEST REWARD CLAIMED" else "CLEAR QUEST + CLAIM REWARD (+$QUEST_BONUS_XP XP)") }
            if (cleared) Text("SYSTEM: QUEST CLEARED \u2713", color = Green, fontWeight = FontWeight.Bold)
        }}
    }
}

@Composable
private fun ReadinessWindow(day: Int, done: Int, cleared: Boolean) {
    val planProgress = (day.coerceIn(1, DeoGradeAPlan.TOTAL_DAYS) / DeoGradeAPlan.TOTAL_DAYS.toFloat())
    val todayProgress = done / OBJECTIVE_COUNT.toFloat()
    val index = ((planProgress * 70f) + (todayProgress * 30f)).coerceIn(0f, 100f)
    Window("\u2605 CYCLE PROGRESS") {
        Text("95-DAY PLAN PROGRESS  ${index.toInt()}%", color = if (index >= 85f) Green else Gold, fontSize = 18.sp, fontWeight = FontWeight.Black)
        LinearProgressIndicator(progress = { index / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
        Spacer(Modifier.height(5.dp))
        Text("Based on plan progress + today's execution", color = Muted, fontSize = 10.sp)
        Text(if (cleared) "TODAY'S QUEST: CLEARED \u2713" else "TODAY'S QUEST: ${done}/${OBJECTIVE_COUNT} OBJECTIVES", color = if (cleared) Green else Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

data class ErrorMonster(val topic: String, val reason: String, val defeats: Int, val category: String = "Concept unknown (E1)")

private val mistakeCategories = listOf(
    "Concept unknown (E1)", "Forgot (E2)", "Calculation mistake (E3)", "Misread question (E4)", "Time pressure (E5)", "Guessing"
)

private fun loadErrorMonsters(prefs: android.content.SharedPreferences, day: Int): List<ErrorMonster> {
    val raw = prefs.getString("error_monsters_$day", "[]") ?: "[]"
    return try {
        val array = org.json.JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(ErrorMonster(o.optString("topic"), o.optString("reason"), o.optInt("defeats").coerceIn(0, 3), o.optString("category").ifBlank { "Concept unknown (E1)" }))
            }
        }
    } catch (_: Exception) { emptyList() }
}

private fun saveErrorMonsters(prefs: android.content.SharedPreferences, day: Int, list: List<ErrorMonster>) {
    val array = org.json.JSONArray()
    list.forEach {
        array.put(org.json.JSONObject().apply {
            put("topic", it.topic)
            put("reason", it.reason)
            put("defeats", it.defeats)
            put("category", it.category)
        })
    }
    prefs.edit().putString("error_monsters_$day", array.toString()).apply()
}

@Composable
private fun ErrorMonsterWindow(day: Int, quest: DailyQuest, prefs: android.content.SharedPreferences, sounds: SoundManager) {
    var monsters by remember(day) { mutableStateOf(loadErrorMonsters(prefs, day)) }
    var topic by remember(day) { mutableStateOf("") }
    var reason by remember(day) { mutableStateOf("") }
    var category by remember(day) { mutableStateOf(mistakeCategories.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    Window("\u2620 ERROR MONSTER") {
        Text(quest.errorMonster, color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(5.dp))
        OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(5.dp))
        OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Mistake / reason") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(5.dp))
        Box {
            OutlinedButton(onClick = { categoryExpanded = true }) { Text("Category: $category") }
            DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                mistakeCategories.forEach { c ->
                    DropdownMenuItem(text = { Text(c) }, onClick = { category = c; categoryExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Button(onClick = {
            if (topic.isNotBlank() && reason.isNotBlank()) {
                monsters = monsters + ErrorMonster(topic.trim(), reason.trim(), 0, category)
                saveErrorMonsters(prefs, day, monsters)
                topic = ""; reason = ""
                sounds.play(SoundManager.Event.EXP_GAIN)
            }
        }) { Text("ADD ERROR MONSTER") }
        Spacer(Modifier.height(5.dp))
        if (monsters.isEmpty()) Text("No error monsters yet.", color = Muted, fontSize = 11.sp)
        monsters.forEachIndexed { index, monster ->
            Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Text(monster.topic, color = if (monster.defeats >= 3) Green else Red, fontWeight = FontWeight.Bold)
                Text(monster.reason, color = Color.White, fontSize = 10.sp)
                Text(monster.category, color = Purple, fontSize = 10.sp)
                Text(if (monster.defeats >= 3) "DEFEATED \u2713" else "DEFEATS ${monster.defeats}/3", color = if (monster.defeats >= 3) Green else Gold, fontSize = 10.sp)
                if (monster.defeats < 3) {
                    TextButton(onClick = {
                        val newDefeats = (monster.defeats + 1).coerceAtMost(3)
                        val updated = monsters.toMutableList()
                        updated[index] = monster.copy(defeats = newDefeats)
                        monsters = updated
                        saveErrorMonsters(prefs, day, monsters)
                        if (newDefeats >= 3) {
                            prefs.edit().putInt("total_monsters_defeated", prefs.getInt("total_monsters_defeated", 0) + 1).apply()
                        }
                        sounds.play(SoundManager.Event.EXP_GAIN)
                    }) { Text("DEFEAT +1", color = Red) }
                }
            }
        }
    }
}

@Composable
private fun PlanScreen(currentDay: Int) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Window("\u25A3 DEO GRADE A \u2022 95 DAYS") {
                Text("PLAYER: ${DeoGradeAPlan.PLAYER_NAME.uppercase()}", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("START: ${DeoGradeAPlan.START_DATE_TEXT} (Day 1)", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("GOAL: SSC CHSL TIER-I \u2014 ENGLISH \u2022 REASONING \u2022 MATHS \u2022 GA", color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text("Days 1\u201345 Syllabus Completion \u2192 46\u201360 Quest Grind \u2192 61\u201375 PYQ Dungeon \u2192 76\u201388 Boss Rush \u2192 89\u201395 Final Boss.", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Text("Every day: RNR (1-5-7) \u2192 Main Topic \u2192 Practice \u2192 Secondary Subject \u2192 GA \u2192 Computer/DEO \u2192 Error Monster.", color = Muted, fontSize = 11.sp)
                Text("A different subject every day \u2014 never 4\u20135 continuous days on one subject. All 4 Tier-I subjects + Computer/DEO stay alive every week.", color = Muted, fontSize = 11.sp)
            }
        }

        items(DeoGradeAPlan.TOTAL_DAYS) { index ->
            val d = index + 1
            val q = DeoGradeAPlan.forDay(d)
            val date = DeoGradeAPlan.START_DATE.plusDays(index.toLong())
            val active = d == currentDay

            Window(if (active) "\u25B6 DAY $d \u2022 TODAY" else "DAY $d") {
                Text(date.toString(), color = Gold, fontSize = 10.sp)
                Text(q.phase, color = Purple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(q.mainQuest, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                if (q.isMockDay) Text("\u2694\uFE0F FULL MOCK", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                else if (q.isWeeklyBoss) Text("\u2694\uFE0F WEEKLY BOSS", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                else if (q.isMajorBoss) Text("\uD83C\uDFC6 MAJOR BOSS", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("SECONDARY \u2022 ${q.secondaryQuest}", color = Cyan, fontSize = 10.sp)
                Text("GA \u2022 ${q.gaQuest}", color = Muted, fontSize = 10.sp)
                Text("COMPUTER/DEO \u2022 ${q.computerQuest}", color = Muted, fontSize = 10.sp)
                Text("RNR \u2022 ${q.rnr}", color = Gold, fontSize = 10.sp)
            }
        }
    }
}

// ── RANKS TAB ── Achievements, evaluated live from your existing stats ──

@Composable
private fun HunterScreen(prefs: android.content.SharedPreferences, totalXp: Int, level: Int, streak: Int) {
    val questsCleared = prefs.getInt("total_quests_cleared", 0)
    val monstersDefeated = prefs.getInt("total_monsters_defeated", 0)
    val stats = AchievementStats(totalXp, level, streak, questsCleared, monstersDefeated)
    val earned = remember(totalXp, level, streak, questsCleared, monstersDefeated) { Achievements.earned(stats).map { it.id }.toSet() }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        item {
            Window("\uD83C\uDFC6 ACHIEVEMENTS  (${earned.size}/${Achievements.all.size})") {
                Achievements.all.forEach { a ->
                    val done = earned.contains(a.id)
                    Text("${a.icon} ${a.title}", color = if (done) Green else Muted, fontWeight = if (done) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, modifier = Modifier.padding(vertical = 1.dp))
                }
            }
        }
        item {
            Window("\uD83D\uDCCA LIFETIME STATS") {
                Text("Quests cleared: $questsCleared", color = Color.White, fontSize = 12.sp)
                Text("Error Monsters defeated: $monstersDefeated", color = Color.White, fontSize = 12.sp)
                Text("Current streak: $streak day${if (streak == 1) "" else "s"}", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SettingsScreen(soundOn: Boolean, vibrationOn: Boolean, dailyQuestNotifsOn: Boolean, setSound: (Boolean) -> Unit, setVibration: (Boolean) -> Unit, setDailyQuestNotifs: (Boolean) -> Unit, sounds: SoundManager, context: android.content.Context, onReset: () -> Unit) {
    var canExactAlarm by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 31) context.getSystemService(android.app.AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
            else true
        )
    }
    val hasPostNotif = if (Build.VERSION.SDK_INT >= 33) {
        androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= 31) {
                    canExactAlarm = context.getSystemService(android.app.AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        item { Window("\uD83D\uDD14 DAILY QUEST NOTIFICATIONS") {
            Text("Plain basic notifications about today's quest, three times a day.", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Text("\u2022 5:30 AM \u2014 Morning quest briefing", color = Color.White, fontSize = 11.sp)
            Text("\u2022 2:30 PM \u2014 Afternoon progress check", color = Color.White, fontSize = 11.sp)
            Text("\u2022 6:00 PM \u2014 Evening quest reminder", color = Color.White, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            SettingRow("Enable Daily Quest Notifications", dailyQuestNotifsOn) { setDailyQuestNotifs(it); if (it) SystemNotificationScheduler.scheduleAll(context) }
            Spacer(Modifier.height(10.dp))
            if (!hasPostNotif) {
                Text("\u2717 Notification permission NOT granted \u2014 nothing will show", color = Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Button(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    )
                }) { Text("OPEN NOTIFICATION SETTINGS") }
            } else {
                Text("\u2713 Notification permission granted", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            if (!canExactAlarm) {
                Text("\u26A0 Exact alarms not permitted \u2014 reminders may run late", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Button(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, android.net.Uri.parse("package:${context.packageName}"))
                    )
                }) { Text("ALLOW EXACT ALARMS") }
            } else {
                Text("\u2713 Exact alarms permitted", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            val powerManager = context.getSystemService(android.os.PowerManager::class.java)
            val ignoringBatteryOpt = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
            if (!ignoringBatteryOpt) {
                Text("\u2717 Battery optimization is ON for this app \u2014 background alarms may be delayed", color = Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Button(onClick = {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, android.net.Uri.parse("package:${context.packageName}"))
                        )
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }
                }) { Text("DISABLE BATTERY OPTIMIZATION") }
            } else {
                Text("\u2713 Battery optimization disabled for this app", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            val nm = context.getSystemService(NotificationManager::class.java)
            val interruptionFilter = nm?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
            if (interruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
                Text("\u26A0 Do Not Disturb is ON \u2014 this may silently block alerts", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Pull down your quick settings and turn DND off, or add this app to DND exceptions.", color = Muted, fontSize = 10.sp)
                Spacer(Modifier.height(10.dp))
            }
            Button(onClick = {
                try {
                    val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                    when {
                        !enabled -> Toast.makeText(context, "BLOCKED: notifications are disabled for this app in system settings", Toast.LENGTH_LONG).show()
                        else -> {
                            val day = DeoGradeAPlan.currentDay(LocalDate.now())
                            NotificationHelper.showDailyQuest(context, day, "Test", DeoGradeAPlan.forDay(day).mainQuest)
                            Toast.makeText(context, "Sent. Pull down the notification shade now.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "ERROR: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }) { Text("SEND TEST NOTIFICATION NOW") }
        }}
        item { Window("\u2699 SYSTEM AUDIO") {
            SettingRow("System Sounds", soundOn) { setSound(it) }; SettingRow("Haptic Feedback", vibrationOn) { setVibration(it) }
            Spacer(Modifier.height(8.dp)); Button(onClick = { sounds.play(SoundManager.Event.ARISE_SYSTEM) }) { Text("TEST SYSTEM") }
            Button(onClick = { sounds.play(SoundManager.Event.LEVEL_UP) }) { Text("TEST LEVEL-UP") }; Button(onClick = { sounds.play(SoundManager.Event.ARISE) }) { Text("TEST ARISE") }
        }}
        item { Window("\u25A3 V9.0 DEO GRADE A SYSTEM") {
            Text("PLAYER: ${DeoGradeAPlan.PLAYER_NAME}", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("95-day plan \u2022 RNR 1-5-7 \u2022 Main/Secondary/GA/Computer Quests \u2022 Error Monster (E1\u2013E5)", color = Cyan, fontSize = 12.sp)
            Text("Today is calculated from ${DeoGradeAPlan.START_DATE_TEXT} as Day 1.", color = Muted, fontSize = 11.sp)
            Text("Tip: add the SYSTEM widget to your home screen for a one-glance view of today's quest.", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp)); OutlinedButton(onClick = onReset) { Text("RESET ALL PROGRESS", color = Red) }
        }}
    }
}

@Composable private fun Window(title: String, content: @Composable ColumnScope.() -> Unit) {
    val glow by rememberInfiniteTransition(label = "panelGlow").animateFloat(
        initialValue = 0.28f, targetValue = 0.62f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "panelGlowAnim"
    )
    Box(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .border(1.dp, Cyan.copy(alpha = glow), CutCornerShape(8.dp))
                .background(Panel)
                .padding(12.dp)
        ) {
            Text(title, color = Cyan, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(7.dp))
            content()
        }
        SystemCornerBrackets(glow)
    }
}

/** Draws the same angular corner-bracket motif used in the notification frames on every in-app panel. */
@Composable
private fun BoxScope.SystemCornerBrackets(alpha: Float) {
    Canvas(Modifier.matchParentSize()) {
        val len = 9.dp.toPx()
        val stroke = 1.6.dp.toPx()
        val color = Cyan.copy(alpha = (alpha + 0.35f).coerceAtMost(1f))
        drawLine(color, Offset(0f, 0f), Offset(len, 0f), stroke)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(size.width - len, 0f), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, len), stroke)
        drawLine(color, Offset(0f, size.height), Offset(len, size.height), stroke)
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - len), stroke)
        drawLine(color, Offset(size.width, size.height), Offset(size.width - len, size.height), stroke)
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - len), stroke)
    }
}

/** Faint HUD grid + scanlines + top vignette drawn once behind all screen content. */
@Composable
private fun SystemBackground(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val gridColor = Cyan.copy(alpha = 0.05f)
        val step = 26.dp.toPx()
        var x = 0f
        while (x < size.width) { drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
        var y = 0f
        while (y < size.height) { drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f); y += step }
        val scanColor = Color.White.copy(alpha = 0.02f)
        val scanStep = 3.dp.toPx()
        var sy = 0f
        while (sy < size.height) { drawLine(scanColor, Offset(0f, sy), Offset(size.width, sy), 1f); sy += scanStep }
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                center = Offset(size.width / 2f, 0f),
                radius = size.height * 0.95f
            )
        )
    }
}

/** Reveals text one character at a time, System-message style. */
@Composable
private fun TypewriterText(
    text: String,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Black,
    textAlign: TextAlign? = null,
    modifier: Modifier = Modifier
) {
    var shown by remember(text) { mutableStateOf("") }
    LaunchedEffect(text) {
        shown = ""
        for (i in text.indices) { shown = text.substring(0, i + 1); delay(22L) }
    }
    Text(shown, color = color, fontSize = fontSize, fontWeight = fontWeight, textAlign = textAlign, modifier = modifier)
}

private data class SystemBurst(val label: String, val title: String)

/**
 * Full-screen "light pillar" moment, reserved for level-up / rank-up only so it stays
 * a rare flourish rather than something the player sees on every action.
 */
@Composable
private fun CinematicBurstOverlay(burst: SystemBurst, onDone: () -> Unit) {
    val beamAlpha by rememberInfiniteTransition(label = "beam").animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "beamAlpha"
    )
    LaunchedEffect(burst) { delay(1900); onDone() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF010207))
            .clickable { onDone() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .width(70.dp)
                .fillMaxHeight()
                .blur(20.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Cyan.copy(alpha = beamAlpha * 0.45f),
                            Color.White.copy(alpha = beamAlpha),
                            Cyan.copy(alpha = beamAlpha * 0.45f),
                            Color.Transparent
                        )
                    )
                )
        )
        listOf(-26, -12, 2, 16, -34, 10, 28).forEachIndexed { i, x -> FallingSpark(i, x) }
        Text(
            "NOTIFICATION",
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 6.sp,
            modifier = Modifier.graphicsLayer(rotationZ = 90f)
        )
        Column(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(burst.label, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(4.dp))
            Text(burst.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FallingSpark(index: Int, xDp: Int) {
    val y by rememberInfiniteTransition(label = "spark$index").animateFloat(
        initialValue = -40f, targetValue = 900f,
        animationSpec = infiniteRepeatable(tween(1400 + index * 260, easing = LinearEasing), RepeatMode.Restart),
        label = "sparkY$index"
    )
    val sparkAlpha = (1f - (y / 900f)).coerceIn(0f, 1f) * 0.9f
    Box(
        Modifier
            .offset(x = xDp.dp, y = y.dp)
            .size(width = 2.dp, height = 16.dp)
            .background(Color.White.copy(alpha = sparkAlpha))
    )
}
@Composable private fun Stat(label: String, value: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp); Text(label, color = Muted, fontSize = 9.sp) } }
@Composable private fun QuestCheck(label: String, checked: Boolean, sounds: SoundManager, onChange: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { if (it && !checked) { sounds.play(SoundManager.Event.CHECKBOX_TICK); onChange() } })
        Text(label, color = if (checked) Muted else Color.White, fontSize = 12.sp)
    }
}
@Composable private fun SettingRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Color.White); Switch(checked = checked, onCheckedChange = onChecked) } }
@Composable private fun RowScope.NavButton(text: String, selected: Boolean, onClick: () -> Unit) { Button(onClick = onClick, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) Cyan.copy(alpha = .20f) else Panel)) { Text(text, fontSize = 10.sp, color = if (selected) Cyan else Muted) } }
