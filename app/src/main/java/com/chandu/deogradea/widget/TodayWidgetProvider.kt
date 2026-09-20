package com.chandu.deogradea.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.chandu.deogradea.R
import com.chandu.deogradea.MainActivity
import com.chandu.deogradea.data.DeoGradeAPlan
import java.time.LocalDate

/**
 * Home-screen widget: shows today's quest topic + objective progress at a
 * glance, so the player doesn't need to open the app and scroll to find
 * "what do I do today". Tapping it opens straight to the Quest screen.
 */
class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateWidget(context, appWidgetManager, it) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(android.content.ComponentName(context, TodayWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val prefs = context.getSharedPreferences("gate_progress", Context.MODE_PRIVATE)
            val day = DeoGradeAPlan.currentDay(LocalDate.now())
            val quest = DeoGradeAPlan.forDay(day)
            val done = prefs.getInt("day_${day}_done", 0).coerceIn(0, 7)

            val views = RemoteViews(context.packageName, R.layout.widget_today)
            views.setTextViewText(R.id.widget_header, "⚔ TODAY'S QUEST • DAY $day/95")
            views.setTextViewText(R.id.widget_title, quest.mainQuest)
            views.setTextViewText(R.id.widget_topic, quest.phase)
            views.setTextViewText(R.id.widget_workout, "📘 SECONDARY — ${quest.secondaryQuest}")
            views.setTextViewText(R.id.widget_progress, "Objectives: $done / 7")

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(MainActivity.EXTRA_OPEN_QUEST, true)
            }
            val pending = PendingIntent.getActivity(
                context, 5500, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, pending)
            views.setOnClickPendingIntent(R.id.widget_title, pending)
            views.setOnClickPendingIntent(R.id.widget_workout, pending)
            views.setOnClickPendingIntent(R.id.widget_progress, pending)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
