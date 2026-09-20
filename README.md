# V10.0 — DEO Grade A Preparation (95-Day SSC CHSL Tier-I Plan) Replaces Target Master

Full second content + branding swap. Everything below the V8.0 section describes the prior
120-day programming/AI plan; it has now been fully replaced.

- **New plan**: "DEO GRADE A PREPARATION" — a 95-day SSC CHSL DEO Grade 'A' Tier-I system for player **Bharath**. Days 1–45 Syllabus Completion (6 themed weeks + a Day 43–45 completion raid, each week ending in a Weekly Boss), Days 46–60 Quest Grind (volume practice, no new chapters), Days 61–75 PYQ Dungeon (daily subject-rotation PYQs), Days 76–88 Boss Rush (mock → analysis, repeating), Days 89–95 Final Boss (pure revision + two final mocks + system review). All 95 days are individually authored from the source plan.
- **Day 1 = Monday, September 21, 2026.** This was chosen deliberately, not arbitrarily: the plan's own weekly table treats Sunday as the Weekly Test day, and the Weekly Boss lands correctly on Day 7/14/21/28/35/42 only if Day 1 is a Monday. Sep 20, 2026 is a Sunday, so Sep 21 is the only start date consistent with the plan's own structure. This is a single constant (`START_DATE_TEXT` in `DeoGradeAPlan`) — trivial to change if a different start is wanted.
- **App renamed** "Target Master" → **DEO Grade A Prep** (`app_name`), with the in-app/splash/widget/notification heading now "DEO GRADE A PREPARATION" and the player identified as **Bharath** throughout (header, splash screen, Plan tab, Settings).
- **Daily quest card restructured** to the plan's own 7-quest format: RNR (1-5-7), Main Topic, Practice, Secondary Subject, GA, Computer/DEO, Error Monster — replacing the prior 5-item card.
- **1-5-7 revision engine**: for Days 1–45, each day's RNR quest dynamically looks up and names the actual topics from Day-1, Day-5, and Day-7 back, per the plan's own "Percentage RNR, Coding-Decoding RNR, Tenses RNR" style. Days 46+ use the phase-appropriate RNR description (Quest Grind / PYQ Dungeon / Boss Rush / Final Boss).
- **Daily XP System** implemented exactly per the plan's table: RNR +20, Main Topic +30 (+40 on a Weekly Boss, +100 on a Full Mock), Practice +20, Secondary Subject +20, GA +15, Computer/DEO +15, Error Monster +20, quest-clear bonus +25 ("1-5-7 completed").
- **Error Monster categories** updated to the plan's own taxonomy: E1 Concept unknown, E2 Forgot, E3 Calculation mistake, E4 Misread question, E5 Time pressure, or Guessing.
- Kept unchanged: XP/level/rank progression, streak tracking, notification channels + scheduling (5:30 AM / 2:30 PM / 6:00 PM), home-screen widget, "Today's Quest" app shortcut, Achievements/lifetime-stats tab, and Settings-screen diagnostics.

### Not touched in this pass
Application ID (`com.chandu.gatesystem`) and internal package/class names besides `TargetMasterPlan` → `DeoGradeAPlan` were left as-is to minimize build risk.

# Target Master V8.0 — 120-Day Programming + AI/ML Plan Replaces the GATE Plan

## V8.0 — full content + branding replacement
The entire daily plan, app name, and in-app/notification text have been replaced end-to-end:

- **New plan**: "TARGET MASTER — 120 DAYS", a 120-day programming + AI/ML/DL/GenAI foundation cycle (C → DSA → AI/ML → Algorithms → CN/OS/DBMS → Python/Tools → Boss/Project rotation, toughening every 40 days across three arcs: Days 1–40, 41–80, 81–120). All 120 days are hand-authored from the source plan, including the 12 Boss days and the Day 120 Final Boss (C / DSA / Algorithms / ML-AI / Python / Project defense).
- **App renamed** "GATE Solo Leveling System" → **Target Master** (`app_name`, splash screen, header, widget, shortcuts).
- **Daily quest card restructured** to match the new plan's own format exactly: 🎯 Main Quest, 💻 Code Quest, 🧠 Problem Quest, ⚔️ GATE Recall, 🔁 RNR, 👹 Error Monster. "GATE Recall" is kept as its own daily block on purpose — the plan explicitly keeps the existing GATE Solo Leveling exam-prep track running in parallel, independent of this 120-day skill-building cycle.
- **Revision engine** rebuilt around the plan's own schedule: Day 1 learn+solve → Day 4 recall → Day 7 timed test → Day 14 mixed → Day 30/60/90/120 random-retrieval checkpoints, shown as the daily RNR note.
- **5 daily objectives** (was 6): Main Skill studied, Practice done, GATE recall/PYQ done, Scheduled revision done, Error Monster logged — matching the plan's own "Daily architecture" breakdown one-for-one.
- **Removed** (GATE-specific extras that don't belong to the new plan): Subject Mastery tracker, Weak Topic Detector, Boss Battle HP bars, Formula/Concept Vault, and the daily gym Workout Quest. The RANKS tab (formerly "HUNTER") now shows only Achievements + lifetime stats, both of which are generic gamification, not GATE-specific.
- **Day counters** updated everywhere (header, splash, notifications, widget, Plan list) from `/150` to `/120`.
- Kept unchanged: XP/level/rank progression, streak tracking, Error Monster mistake-logging system, notification channels + scheduling (5:30 AM / 2:30 PM / 6:00 PM daily reminders, danger/inactivity alerts), home-screen widget, "Today's Quest" app shortcut, and all Settings-screen diagnostics.

### Not touched in this pass
Application ID (`com.chandu.gatesystem`) and internal package/class names besides `GatePlan` → `TargetMasterPlan` were left as-is to minimize build risk — happy to do a full package rename in a follow-up if wanted.

# GATE Solo Leveling System V6.7 — Hunter Tab: Mastery, Weak Topics, Bosses, Achievements, Formula Vault

## V6.7 — real, working subset of the "40-feature" master list
Implementing literally all 40 features from the master feature list isn't realistic in one pass — several (a real PYQ question bank, a timed 65-question mock-test engine, AI study assistant, marks/rank prediction) need either thousands of hand-authored questions or infrastructure this app doesn't have, and faking that content would make the app worse, not better. What's added here is a real, working slice of the Phase 1–3 features that run entirely on local data (no question bank needed):

- **New "👑 HUNTER" tab** added to the bottom nav (SYSTEM / QUEST / PLAN / HUNTER / SET).
- **Topic Mastery System**: self-rated 0–100% per subject (10 GATE CS subjects), adjustable with +/− in 5% steps, persisted locally.
- **Weak Topic Detector**: automatically lists every subject under 60% mastery, weakest first, with a VERY HIGH / HIGH / MEDIUM priority label — mirrors the "⚠ WEAK AREAS" behavior from the feature spec.
- **Boss Battle System**: every subject is a boss with 1000 HP; your mastery % is your damage dealt, so raising mastery visibly drops the boss's remaining HP until it's defeated at 100%.
- **Achievement System**: 12 achievements (first quest cleared, streak milestones, level milestones, error-monsters-defeated milestones, total XP milestones), evaluated live from your existing stats — no separate tracking system to keep in sync.
- **Formula / Concept Vault**: 15 seeded GATE CS formulas across Digital Logic, COA, OS, DBMS, TOC, CN, Engineering Maths, DSA and Compiler Design, each toggleable as ★ Favorite or ⚠ Difficult/frequently-forgotten.
- **Mistake Book upgrade**: Error Monsters now carry a mistake **category** (Concept / Calculation / Silly / Time pressure / Misread question / Guessing), matching the Mistake Book spec, and defeating one (3/3) now counts toward the "Error Monsters Defeated" achievements.
- New `total_quests_cleared` and `total_monsters_defeated` counters are tracked so achievements have real, permanent data to evaluate against (not just today's numbers).

### Deliberately not attempted in this pass (would need real content or infra, not just code)
Question Bank, Exam/Test Engine (topic/subject/full mock tests with negative marking), PYQ Intelligence, Marks Prediction Engine, Difficulty Adaptation, Anti-Cheating/Integrity Mode for mocks, AI Study Assistant, and the "GATE 85 Engine" auto-planner that reallocates your schedule from live test performance. These all depend on a real bank of graded questions/PYQs with correct answers, which nobody has authored yet — building the *engine* without real content behind it would just produce a hollow UI. Happy to start on the Question Bank data model next if you want to go there.

# GATE Solo Leveling System V6.6 — 147-Rule GA Cycle + Basic Daily Quest Notifications

## V6.6 changes
- **General Aptitude now shows all 4 chapters every day**: Verbal Aptitude, Quantitative Aptitude, Analytical & Logical Reasoning, and Spatial Aptitude are combined into every day's GA block instead of a flat "20/40 questions" label — you always see exactly which sub-topics from each of the 4 chapters to practice.
- **"147 rule" applied to GA as a 3-day repeating cycle**: the full GA syllabus is split into 3 balanced slots. Day 1, 4, 7, 10… always get slot 1; Day 2, 5, 8, 11… get slot 2; Day 3, 6, 9, 12… get slot 3 — so the same slot resurfaces every 3rd day across all 150 days for spaced repetition, instead of being covered once and forgotten. Visible in the Dashboard, Quest screen, and the full 150-day Plan list.
- **Engineering Mathematics + GA syllabus fully expanded** on the Plan → Syllabus screen: Linear Algebra, Calculus, Probability & Statistics, and Discrete Mathematics are each broken into their real sub-topics (vector spaces, Cayley-Hamilton, Bayes' theorem, Boolean algebra, graph theory, etc.), and GA is broken into its 4 named chapters with their real chapter lists instead of one merged line.
- **Instagram "mana-leak" monitoring removed completely**: `InstagramMonitor.kt` and `InstagramCheckReceiver.kt` are deleted, `PACKAGE_USAGE_STATS` permission is removed from the manifest, and all related Settings-screen UI is gone.
- **Replaced with a plain basic "Daily Quest" notification**, sent 3 times a day: 5:30 AM, 2:30 PM, and 6:00 PM, each summarizing today's quest (title, primary subject, current GA cycle slot, objectives done). Unlike the danger/inactivity/level-up alerts, this channel is `IMPORTANCE_DEFAULT` with the plain system notification sound — no full-screen intent, no alarm-style vibration — since it's a routine reminder, not an emergency alert. Settings screen has an enable/disable toggle and a "SEND TEST NOTIFICATION NOW" button.
- **Error Monsters** (add a mistake, defeat the same concept 3 times to clear it) — already present since V6.3.2 and unchanged; still available on the Quest screen.
- **New sound mapping** applied across `SoundManager`: system open / level-up / selecting → `arise_system.wav`; notifications and gaining XP/gold/objectives → `arise.wav`; everything else → `level_up_notification.wav`.

# GATE Solo Leveling System V6.5.3 — Test Alert Now Reports Real Failures

## V6.5.3 fix
The "SEND TEST ALERT NOW" button previously called `NotificationHelper.showInstagramWarning()` with no feedback at all — if it failed silently (blocked channel, exception, disabled notifications), you'd have no way to know why. It now:
- Wraps the call in try/catch and shows the exact exception via Toast if one occurs, instead of failing silently.
- Checks whether the "System — Mana Leak" channel specifically has been muted (Android lets a user block one channel while others still work) and tells you if so.
- Detects and warns if **Do Not Disturb** is active — DND silently swallows notifications below "priority" and is a very common reason a correctly-coded alert never appears even with every permission granted.
- On success, shows a Toast confirming the call actually ran, so "nothing appeared" now always comes with an explanation.

If your permissions all show green (as they did) and the test still shows nothing after this update, the Toast message it gives you is the real next clue — send me that exact text.



## V6.5.2 additions (Settings → Instagram Mana-Leak Alert)
- **Live minutes-today readout** so you can see the app is actually detecting Instagram usage in real time, instead of guessing.
- **Notification permission status** (Android 13+) with a direct link to the app's notification settings if it's off.
- **Exact-alarm permission status** (Android 12+) with a direct link to grant it — without this, the 5-minute background check can be delayed by Doze.
- **"SEND TEST ALERT NOW" button** — fires the exact same notification path as a real alert, immediately, bypassing usage tracking and the alarm scheduler entirely. This isolates the problem in one tap:
  - Test alert doesn't show → it's a phone-level notification/channel/permission block, not the app.
  - Test alert shows, but real 10-minute alerts never do → your background check is being killed between app opens. That's almost always **OEM battery/autostart restriction** (very common on Xiaomi/MIUI, Vivo, Oppo, Realme, Samsung with aggressive battery saver) — the app needs to be whitelisted from battery optimization and (on MIUI-style phones) have "Autostart" enabled, or the 5-minute self-re-arming alarm chain gets silently stopped.

# GATE Solo Leveling System V6.5.1 — Cinematic System UI

## V6.5.1 additions (in-app visual pass, no notification changes)
- **Glowing corner-bracket panels**: every `Window()` panel (Player Status, Daily Quest, Plan, etc.) now pulses its border and draws the same corner brackets used on the notification frames, instead of a flat static line.
- **HUD background**: a faint grid + scanline + top vignette now sits behind every screen (`SystemBackground`), drawn once with Canvas — cheap and matches the notification aesthetic.
- **Typewriter text**: the quest title on the Awakening splash and the Quest window now reveals character-by-character instead of appearing instantly.
- **Cinematic light-pillar burst**: a full-screen vertical beam + falling sparks + vertical "NOTIFICATION" text, shown for ~1.9s only on an actual level-up or rank-up (hooked into the existing `progressionLevel`/`progressionRank` checks in `completeObjective()`/`clearQuest()`). Deliberately not used for every notification so it stays a rare payoff moment. Tap anywhere to skip it early.
- Custom game-style fonts (e.g. Rajdhani) were **not** added in this pass — that needs a `.ttf` file bundled into `res/font`, which requires network access this environment doesn't have. Drop a font file into `app/src/main/res/font/` and I can wire it into the theme in one line whenever you have one, or point me to a specific Google Fonts family name next time you're online and I'll do it then.

# GATE Solo Leveling System V6.5.0 — Custom Notification UI Edition

## V6.5.0 additions
- **Custom glowing-frame notification UI**: alerts (inactivity, danger, mana leak, level-up) now render with the angular cut-corner frame + corner brackets from the widget redesign, both collapsed and expanded, instead of the stock notification look.
- **Per-alert notification sound**: each alert type now has its own notification channel with a dedicated sound — level_up_notification.wav (level up), arise_system.wav (danger), arise.wav (inactivity), system_tick.wav (mana leak). See the comment at the top of `NotificationHelper.kt` for the exact priority mapping — flag it if you want it reordered.
- **Instagram tracking fix**: switched from the legacy MOVE_TO_FOREGROUND/BACKGROUND usage events to ACTIVITY_RESUMED/ACTIVITY_PAUSED (API 29+), and now ignores foreground bursts under 8 seconds. This should stop mana-leak alerts firing from brief, non-deliberate opens (a shared link, a widget refresh, a notification tap-through) rather than actually using the app. Worth a day of real-world testing since I can't reproduce the original bug on-device myself — let me know if it still over-fires.

# GATE Solo Leveling System V6.4.0 — Notifications + Widget Edition

GATE CS 2027 150-day Solo Leveling-style Android study system.

## V6.4.0 additions
- **Instagram "mana-leak" alert**: warns you every 10 minutes of Instagram use in a day, System-style. Requires manually granting "Usage Access" once (Settings screen has a one-tap shortcut to the right page — Android does not allow this to be auto-granted).
- **Awakening window**: a full-screen "System" boot screen shown each time you open the app — player rank, level, streak, and today's quest, before you tap through to the dashboard.
- **Home-screen widget**: add the "GATE System" widget to your home screen to see today's quest topic + today's workout + objective progress without opening the app at all. Tapping it opens straight to the Quest screen. Updates automatically, and instantly whenever you tick an objective.
- **"Today's Quest" app shortcut**: long-press the app icon for a shortcut straight into today's quest (same as tapping a notification).
- **Streak tracker**: counts consecutive days you fully cleared the quest.
- **GATE exam countdown**: editable exam date on the dashboard, shows days remaining.
- Existing V6.3.2 features preserved: 4-hour inactivity alert, 8:30 PM danger alert, Error Monsters, 85+ Plan Readiness Index, 150-day syllabus/plan.

## Notification & background behavior
- All alerts use native Android AlarmManager + notification channels (no extra battery-hungry services).
- Exact alarms are used when Android allows them; otherwise the app falls back to an allowed inexact idle alarm.
- Notification permission is requested on Android 13+.
- Inactivity reminders are limited to daytime and use a four-hour cooldown.
- 8:30 PM danger notification is skipped when all 6 objectives are complete.
- Instagram checks run roughly every 5 minutes in the background; on some devices Doze/battery-optimization may space these out further. For most reliable delivery, exclude the app from battery optimization (Settings → Apps → GATE Solo Leveling System → Battery → Unrestricted).

## Setup after install
1. Open the app once, allow notifications when prompted.
2. Go to Settings tab → grant Usage Access for the Instagram alert to work.
3. Long-press your home screen → Widgets → find "GATE Solo Leveling System" → add the "SYSTEM" widget.

## Build
GitHub Actions builds the debug APK with Java 17, Android SDK 35, and Gradle 8.10.2.
