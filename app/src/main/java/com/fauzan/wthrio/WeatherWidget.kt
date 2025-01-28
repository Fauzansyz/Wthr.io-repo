package com.fauzan.wthrio
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews

class WeatherWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            updateWeatherWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWeatherWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Ambil data dari SharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("Cuaca", Context.MODE_PRIVATE)

        val conditions = sharedPreferences.getString("W_CONDITION", "N/A")
        val temperature = sharedPreferences.getString("TEMPERATURE", "N/A")
        val forecast = sharedPreferences.getString("FORECAST", "N/A")

        // Update tampilan widget
        val views = RemoteViews(context.packageName, R.layout.widget_weather_layout)
        views.setTextViewText(R.id.condition, conditions)
        views.setTextViewText(R.id.temp_now, temperature)
     
	   val imageResource = when (conditions) {
    "Cerah" -> R.drawable.icon_sunny
    "Sebagian berawan" -> R.drawable.icon_partly_cloudy
    "Berawan" -> R.drawable.icon_cloudy
	"Berawan berat" -> R.drawable.icon_overcast
	"Kabut" -> R.drawable.icon_fog
	"Kabut tebal" -> R.drawable.icon_thick_fog
	"Gerimis ringan" -> R.drawable.icon_drizzle_light
	"Gerimis sedang" -> R.drawable.icon_drizzle_moderate
	"Gerimis berat" -> R.drawable.icon_unknown
	"Hujan ringan" -> R.drawable.icon_drizzle_light
	"Hujan sedang" -> R.drawable.icon_drizzle_moderate
    else -> R.drawable.icon_unknown // Gambar default jika nama tidak cocok
}

views.setImageViewResource(R.id.weather_icon, imageResource)

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)

        // Perbarui semua widget jika diperlukan
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WeatherWidget::class.java)
        )
        for (appWidgetId in widgetIds) {
            updateWeatherWidget(context, appWidgetManager, appWidgetId)
        }
    }
}