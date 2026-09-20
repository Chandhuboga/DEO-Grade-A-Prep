package com.chandu.deogradea.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * One day's quest card in the DEO GRADE A — 95-DAY plan (SSC CHSL DEO Grade 'A').
 *
 * Mirrors the daily quest card from the source plan directly:
 *   Quest 1  RNR (1-5-7 revision)       -> rnr
 *   Quest 2  Main Topic                 -> mainQuest / mainSubject / mainTopic
 *   Quest 3  Practice                   -> practice
 *   Quest 4  Secondary Subject          -> secondaryQuest
 *   Quest 5  GA                         -> gaQuest
 *   Quest 6  Computer / DEO             -> computerQuest
 *   Quest 7  Error Monster              -> errorMonster
 */
data class DailyQuest(
    val day: Int,
    val phase: String,
    val mainQuest: String,
    val mainSubject: String,
    val mainTopic: String,
    val secondaryQuest: String,
    val gaQuest: String,
    val computerQuest: String,
    val practice: String,
    val rnr: String,
    val errorMonster: String,
    val isWeeklyBoss: Boolean,
    val isMajorBoss: Boolean,
    val isMockDay: Boolean,
    val isAnalysisDay: Boolean
)

/**
 * DEO GRADE A — 95-DAY "DAILY QUEST" SYSTEM (SSC CHSL Tier-I: English, General
 * Intelligence/Reasoning, Quantitative Aptitude, General Awareness).
 *
 * Days 1-45   SYLLABUS COMPLETION (6 weeks, a different subject-topic every day,
 *             1-5-7 revision engine, Weekly Boss every 7th day, Day 45 = First
 *             45-Day Boss with Green/Yellow/Red topic classification).
 * Days 46-60  QUEST GRIND (no new chapters — daily question volume + RNR).
 * Days 61-75  PYQ DUNGEON (daily rotation through SSC PYQs by subject, Error
 *             Monster categories E1-E5 feed directly back into RNR).
 * Days 76-88  BOSS RUSH (Full mock -> Analysis -> repeat).
 * Days 89-95  FINAL BOSS (pure revision + two final mocks + system review).
 *
 * Player: Bharath.
 */
object DeoGradeAPlan {
    const val TOTAL_DAYS = 95
    const val START_DATE_TEXT = "2026-09-21" // Day 1 = Monday, matching the weekly Mon-Sun rotation below
    val START_DATE: LocalDate = LocalDate.parse(START_DATE_TEXT)
    const val PLAYER_NAME = "Bharath"

    private data class Entry(
        val phase: String,
        val mainQuest: String,
        val secondaryQuest: String? = null,
        val gaQuest: String? = null,
        val computerQuest: String? = null,
        val practice: String? = null,
        val isWeeklyBoss: Boolean = false,
        val isMajorBoss: Boolean = false,
        val isMockDay: Boolean = false,
        val isAnalysisDay: Boolean = false
    )

    private const val GENERIC_SECONDARY = "Reasoning + English \u2014 mixed practice to keep both alive"
    private const val GENERIC_GA = "GA: Current affairs + static GA"
    private const val GENERIC_COMPUTER = "Computer/DEO: rotate \u2014 MCQs / typing / shortcuts / Excel"

    // ── WEEK 1 — ARITHMETIC + FOUNDATION (Days 1-7) ─────────────────────────
    private val week1 = listOf(
        Entry("WEEK 1 \u2014 ARITHMETIC + FOUNDATION", "Maths: Number System", "Reasoning: Analogy + English: Parts of Speech", "GA: Current Affairs", "Computer: Basics"),
        Entry("WEEK 1 \u2014 ARITHMETIC + FOUNDATION", "Maths: HCF + LCM", "Reasoning: Classification + English: Articles", "GA: Polity", "DEO: Typing"),
        Entry("WEEK 1 \u2014 ARITHMETIC + FOUNDATION", "Maths: Simplification", "Reasoning: Number Series + English: Subject-Verb Agreement", "GA: History"),
        Entry("WEEK 1 \u2014 ARITHMETIC + FOUNDATION", "Maths: Percentage", "Reasoning: Alphabet Series + English: Noun/Pronoun", "GA: Geography"),
        Entry("WEEK 1 \u2014 ARITHMETIC + FOUNDATION", "Maths: Ratio & Proportion", "Reasoning: Coding-Decoding + English: Tenses", "GA: Science"),
        Entry("WEEK 1 \u2014 ARITHMETIC + FOUNDATION", "Maths: Average", "Reasoning: Blood Relations + English: Prepositions", null, "Computer: Hardware"),
        Entry("WEEK 1 \u2014 ARITHMETIC + FOUNDATION", "\u2694 WEEKLY BOSS \u2014 100 Mixed Questions", "Full RNR + 1-5-7 review across Week 1", practice = "100 mixed questions across Maths/Reasoning/English/GA, then Error Monster on every miss", isWeeklyBoss = true)
    )

    // ── WEEK 2 — PROFIT / WORK / SPEED (Days 8-14) ──────────────────────────
    private val week2 = listOf(
        Entry("WEEK 2 \u2014 PROFIT / WORK / SPEED", "Maths: Profit & Loss"),
        Entry("WEEK 2 \u2014 PROFIT / WORK / SPEED", "Maths: Discount"),
        Entry("WEEK 2 \u2014 PROFIT / WORK / SPEED", "Maths: Simple Interest"),
        Entry("WEEK 2 \u2014 PROFIT / WORK / SPEED", "Maths: Compound Interest"),
        Entry("WEEK 2 \u2014 PROFIT / WORK / SPEED", "Maths: Partnership"),
        Entry("WEEK 2 \u2014 PROFIT / WORK / SPEED", "Maths: Mixture & Allegation"),
        Entry("WEEK 2 \u2014 PROFIT / WORK / SPEED", "\u2694 WEEKLY BOSS", practice = "Mixed test on Week 2 Maths + everything alive from Week 1", isWeeklyBoss = true)
    )

    // ── WEEK 3 — TIME + ALGEBRA (Days 15-21) ────────────────────────────────
    private val week3 = listOf(
        Entry("WEEK 3 \u2014 TIME + ALGEBRA", "Maths: Time & Work"),
        Entry("WEEK 3 \u2014 TIME + ALGEBRA", "Maths: Pipes & Cisterns"),
        Entry("WEEK 3 \u2014 TIME + ALGEBRA", "Maths: Time-Speed-Distance"),
        Entry("WEEK 3 \u2014 TIME + ALGEBRA", "Maths: Boats & Streams"),
        Entry("WEEK 3 \u2014 TIME + ALGEBRA", "Maths: Trains/Races"),
        Entry("WEEK 3 \u2014 TIME + ALGEBRA", "Maths: Algebra basics"),
        Entry("WEEK 3 \u2014 TIME + ALGEBRA", "\u2694 WEEKLY BOSS", isWeeklyBoss = true)
    )

    // ── WEEK 4 — GEOMETRY + REASONING (Days 22-28) ──────────────────────────
    private val week4 = listOf(
        Entry("WEEK 4 \u2014 GEOMETRY + REASONING", "Maths: Lines & Angles"),
        Entry("WEEK 4 \u2014 GEOMETRY + REASONING", "Maths: Triangles"),
        Entry("WEEK 4 \u2014 GEOMETRY + REASONING", "Maths: Quadrilaterals/Polygons"),
        Entry("WEEK 4 \u2014 GEOMETRY + REASONING", "Maths: Circle"),
        Entry("WEEK 4 \u2014 GEOMETRY + REASONING", "Maths: Mensuration 2D"),
        Entry("WEEK 4 \u2014 GEOMETRY + REASONING", "Maths: Mensuration 3D"),
        Entry("WEEK 4 \u2014 GEOMETRY + REASONING", "\u2694 WEEKLY BOSS", isWeeklyBoss = true)
    )

    // ── WEEK 5 — TRIGONOMETRY + DATA (Days 29-35) ───────────────────────────
    private val week5 = listOf(
        Entry("WEEK 5 \u2014 TRIGONOMETRY + DATA", "Maths: Trigonometric ratios"),
        Entry("WEEK 5 \u2014 TRIGONOMETRY + DATA", "Maths: Standard values/identities"),
        Entry("WEEK 5 \u2014 TRIGONOMETRY + DATA", "Maths: Heights & Distances"),
        Entry("WEEK 5 \u2014 TRIGONOMETRY + DATA", "Maths: Statistics"),
        Entry("WEEK 5 \u2014 TRIGONOMETRY + DATA", "Maths: Data Interpretation"),
        Entry("WEEK 5 \u2014 TRIGONOMETRY + DATA", "Maths: Probability/basic data concepts"),
        Entry("WEEK 5 \u2014 TRIGONOMETRY + DATA", "\u2694 FULL MATHS + REASONING BOSS", practice = "Every Maths + Reasoning topic from Weeks 1-5 is fair game today", isWeeklyBoss = true)
    )

    // ── WEEK 6 — ENGLISH + REASONING + GA FINISH (Days 36-42) ──────────────
    private val week6 = listOf(
        Entry("WEEK 6 \u2014 ENGLISH + REASONING + GA FINISH", "English: Error Detection"),
        Entry("WEEK 6 \u2014 ENGLISH + REASONING + GA FINISH", "English: Sentence Improvement"),
        Entry("WEEK 6 \u2014 ENGLISH + REASONING + GA FINISH", "English: Cloze Test"),
        Entry("WEEK 6 \u2014 ENGLISH + REASONING + GA FINISH", "English: Reading Comprehension"),
        Entry("WEEK 6 \u2014 ENGLISH + REASONING + GA FINISH", "English: Active/Passive"),
        Entry("WEEK 6 \u2014 ENGLISH + REASONING + GA FINISH", "English: Direct/Indirect Speech"),
        Entry("WEEK 6 \u2014 ENGLISH + REASONING + GA FINISH", "\u2694 ENGLISH BOSS", isWeeklyBoss = true)
    )

    // ── DAYS 43-45 — SYLLABUS COMPLETION RAID ───────────────────────────────
    private val raid = listOf(
        Entry(
            "SYLLABUS COMPLETION RAID", "Reasoning: remaining topics",
            secondaryQuest = "Syllogism + Venn + Statement/Conclusion + Mathematical operations + Ranking + Directions + Non-verbal reasoning",
            gaQuest = GENERIC_GA, computerQuest = GENERIC_COMPUTER
        ),
        Entry(
            "SYLLABUS COMPLETION RAID", "GA: completion sweep",
            secondaryQuest = GENERIC_SECONDARY,
            gaQuest = "History + Geography + Polity + Economy + Science + Culture + Current Affairs",
            computerQuest = GENERIC_COMPUTER
        ),
        Entry(
            "SYLLABUS COMPLETION RAID", "\u2694 FIRST 45-DAY BOSS \u2014 Full Mixed Test",
            practice = "Full mixed test across all 4 Tier-I subjects. Classify every topic: Green \u226580%, Yellow 60\u201379%, Red <60%. Red topics become your Days 46\u201360 priority.",
            isMajorBoss = true
        )
    )

    private val days1to45: List<Entry> = week1 + week2 + week3 + week4 + week5 + week6 + raid

    init { check(days1to45.size == 45) { "Expected 45 entries for Days 1-45, found ${days1to45.size}" } }

    private val pyqRotation = listOf("Maths", "Reasoning", "English", "GA", "Computer")

    private fun splitTopic(mainQuest: String): Pair<String, String> {
        val idx = mainQuest.indexOf(':')
        return if (idx == -1) mainQuest to "" else mainQuest.substring(0, idx).trim() to mainQuest.substring(idx + 1).trim()
    }

    /** Days 89-95 are each individually scripted per the source's Final Boss week. */
    private fun finalBossEntry(day: Int): Entry = when (day) {
        89 -> Entry("FINAL BOSS", "Revision: Maths + Reasoning", practice = "Formula sheet + shortcut recall, no new questions")
        90 -> Entry("FINAL BOSS", "Revision: English + GA", practice = "Vocabulary + grammar rules + GA facts, no new questions")
        91 -> Entry("FINAL BOSS", "Revision: Computer + DEO skill", practice = "Typing speed check + Computer MCQ recall")
        92 -> Entry("FINAL BOSS", "\u2694 Full Mock", isMockDay = true)
        93 -> Entry("FINAL BOSS", "Error Monster \u2014 full sweep", practice = "Review every E1\u2013E5 entry logged since Day 61 and clear as many as possible", isAnalysisDay = true)
        94 -> Entry("FINAL BOSS", "\u2694 Final Full Mock", isMockDay = true)
        else -> Entry(
            "FINAL BOSS", "\u2694 FINAL SYSTEM REVIEW",
            practice = "No heavy learning today. Only: formulas \u2192 shortcuts \u2192 vocabulary \u2192 GA facts \u2192 Computer \u2192 PYQ mistakes \u2192 DEO skill.",
            isMajorBoss = true
        )
    }

    private fun entryFor(day: Int): Entry {
        val d = day.coerceIn(1, TOTAL_DAYS)
        return when {
            d <= 45 -> days1to45[d - 1]
            d <= 60 -> Entry(
                "QUEST GRIND", "Quest Grind \u2014 Maths mixed practice (30\u201340 Q)",
                secondaryQuest = "Reasoning (30\u201340 Q) + English (30\u201340 Q)",
                gaQuest = "GA \u2014 30 questions", computerQuest = "Computer \u2014 20 questions + DEO skill 15\u201320 min",
                practice = "No new chapters. Accuracy matters more than raw count \u2014 review every wrong answer immediately."
            )
            d <= 75 -> {
                val subject = pyqRotation[(d - 61) % pyqRotation.size]
                Entry(
                    "PYQ DUNGEON", "$subject PYQ practice",
                    secondaryQuest = "Mix in previous days' PYQ sets for retention",
                    gaQuest = if (subject == "GA") "GA PYQ \u2014 today's focus" else GENERIC_GA,
                    computerQuest = if (subject == "Computer") "Computer PYQ \u2014 today's focus" else GENERIC_COMPUTER,
                    practice = "Every wrong PYQ becomes an Error Monster tagged E1\u2013E5; today's RNR attacks yesterday's tags."
                )
            }
            d <= 88 -> {
                val isMock = (d - 76) % 2 == 0
                if (isMock) Entry("BOSS RUSH", "\u2694 Full Tier-I Mock", isMockDay = true)
                else Entry("BOSS RUSH", "Mock Analysis + Weak-Topic Repair", isAnalysisDay = true, practice = "Mock \u2192 Analysis \u2192 Error Monster \u2192 RNR \u2192 Mock. Never just check the score and move to the next mock.")
            }
            else -> finalBossEntry(d)
        }
    }

    private fun rnrFor(day: Int): String = when {
        day <= 45 -> {
            val refs = listOf(day - 1, day - 5, day - 7).filter { it in 1..45 }
            if (refs.isEmpty()) "Day 1 of the 1-5-7 engine \u2014 no prior topics yet. Learn + Practice today's topics fresh."
            else refs.joinToString(" \u2022 ") { r ->
                val label = when (day - r) { 1 -> "RNR-1"; 5 -> "RNR-5"; else -> "RNR-7" }
                "$label: recall Day $r \u2014 ${entryFor(r).mainQuest}"
            }
        }
        day <= 60 -> "30 min \u2014 attack yesterday's Error Monster entries + keep any late 1-5-7 topics moving."
        day <= 75 -> "This session specifically attacks the E1\u2013E5 error categories logged from today's PYQs."
        day <= 88 -> if ((day - 76) % 2 == 0) "Skip formal RNR today \u2014 the full mock replaces it." else "RNR = today's mock error analysis: reclassify every miss into a category, then re-solve 3\u20135 of them."
        else -> "Final week \u2014 RNR is whatever is weakest in your Error Monster log, not new material."
    }

    private const val ERROR_MONSTER_NOTE =
        "Record only: E1 Concept unknown, E2 Forgot, E3 Calculation mistake, E4 Misread question, E5 Time pressure, or Guess. " +
            "Your next RNR session specifically attacks these categories."

    fun forDay(day: Int): DailyQuest {
        val d = day.coerceIn(1, TOTAL_DAYS)
        val entry = entryFor(d)
        val (subject, topic) = splitTopic(entry.mainQuest)
        return DailyQuest(
            day = d,
            phase = entry.phase,
            mainQuest = entry.mainQuest,
            mainSubject = subject,
            mainTopic = topic,
            secondaryQuest = entry.secondaryQuest ?: GENERIC_SECONDARY,
            gaQuest = entry.gaQuest ?: GENERIC_GA,
            computerQuest = entry.computerQuest ?: GENERIC_COMPUTER,
            practice = entry.practice ?: "Solve questions immediately on today's Main Topic.",
            rnr = rnrFor(d),
            errorMonster = ERROR_MONSTER_NOTE,
            isWeeklyBoss = entry.isWeeklyBoss,
            isMajorBoss = entry.isMajorBoss,
            isMockDay = entry.isMockDay,
            isAnalysisDay = entry.isAnalysisDay
        )
    }

    fun currentDay(today: LocalDate = LocalDate.now()): Int =
        (ChronoUnit.DAYS.between(START_DATE, today) + 1).toInt().coerceIn(1, TOTAL_DAYS)
}
