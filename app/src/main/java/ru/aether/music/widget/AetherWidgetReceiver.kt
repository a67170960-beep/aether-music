package ru.aether.music.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import ru.aether.music.R

/**
 * Home screen widget (RemoteViews). Android widgets cannot run full Compose liquid glass;
 * this provides a translucent control surface that opens the app.
 */
class AetherWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_placeholder)
            views.setTextViewText(R.id.widget_title, "Aether Music")
            views.setTextViewText(R.id.widget_subtitle, "Tap to open player")
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
