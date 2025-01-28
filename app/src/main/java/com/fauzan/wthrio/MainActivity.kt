package com.fauzan.wthrio

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread
import android.content.ComponentName
import androidx.constraintlayout.widget.ConstraintLayout
import android.appwidget.AppWidgetManager

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiUrl = "https://api.open-meteo.com/v1/forecast"

    // UI Components
	private lateinit var gempaPage: ConstraintLayout
 
    private lateinit var locationTextView: TextView
    private lateinit var updatedTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var windTextView: TextView
	private lateinit var humidityText: TextView
    private lateinit var weatherConditionTextView: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var rootLayout: View
    private lateinit var settingIcon: ImageView
private lateinit var sharedPreferences: SharedPreferences
    // Weather code descriptions and icons
    private val weatherCodeDescriptions = mapOf(
        0 to "Cerah", 1 to "Sebagian berawan", 2 to "Berawan", 3 to "Berawan berat",
        45 to "Kabut", 48 to "Kabut tebal", 51 to "Gerimis ringan", 53 to "Gerimis sedang",
        55 to "Gerimis berat", 61 to "Hujan ringan", 63 to "Hujan sedang", 65 to "Hujan lebat"
    )
	

    private val weatherCodeIcons = mapOf(
        0 to R.drawable.icon_sunny, 1 to R.drawable.icon_partly_cloudy,
        2 to R.drawable.icon_cloudy, 3 to R.drawable.icon_overcast,
        45 to R.drawable.icon_fog, 48 to R.drawable.icon_thick_fog,
        51 to R.drawable.icon_drizzle_light, 53 to R.drawable.icon_drizzle_moderate,
        61 to R.drawable.icon_drizzle_light, 63 to R.drawable.icon_drizzle_moderate
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
		
		//val textViewState:TextView = findViewById(R.id.stateTxt)
			 val sharedPreferences = getSharedPreferences("ToggleState", MODE_PRIVATE)

// Ambil nilai state yang tersimpan saat aplikasi pertama kali dijalankan
 var isOn = sharedPreferences.getBoolean("state", false)

// Set listener untuk perubahan state
StateManager.setOnStateChangeListener { isChecked ->
    // Simpan nilai baru ke SharedPreferences
    val editor = sharedPreferences.edit()
    editor.putBoolean("state", isChecked)
    editor.apply()  // Simpan perubahan

    // Ambil nilai terbaru dari SharedPreferences setelah perubahan
    isOn = sharedPreferences.getBoolean("state", false)

    // Update UI dengan nilai terbaru
   // textViewState.text = if (isOn) "Switch is ON" else "Switch is OFF"
}

        // Initialize UI components
		humidityText = findViewById(R.id.data3)
        locationTextView = findViewById(R.id.locationTextView)
        updatedTextView = findViewById(R.id.updatedTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        windTextView = findViewById(R.id.windTextView)
        weatherConditionTextView = findViewById(R.id.weatherConditionTextView)
        weatherIcon = findViewById(R.id.weatherIcon)
        rootLayout = findViewById(R.id.content)
        settingIcon = findViewById(R.id.imageSetting)
		gempaPage = findViewById(R.id.bottomCard)
	
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
        checkLocationPermission()
		
		gempaPage.setOnClickListener {
			val intent = Intent(this, InfoGempaActivity::class.java)
            startActivity(intent)
		}

        settingIcon.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        val buttonGoToSecondActivity = findViewById<ImageView>(R.id.centerCard)
        buttonGoToSecondActivity.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
		
            startActivity(intent)
        }
    }
	


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchWeatherData()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchWeatherData()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak!", Toast.LENGTH_SHORT).show()
                sendPermissionDeniedNotification()
            }
        }

@SuppressLint("MissingPermission")
private fun fetchWeatherData() {
fusedLocationClient.lastLocation.addOnSuccessListener { location ->
if (location != null) {
val latitude = location.latitude
val longitude = location.longitude
val weatherUrl = "$apiUrl?latitude=$latitude&longitude=$longitude&current_weather=true"
val nominatimUrl = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$latitude&lon=$longitude"
val metNorwayUrl = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$latitude&lon=$longitude"

thread {
try {
// Fetch location name
val addressConnection = URL(nominatimUrl).openConnection() as HttpURLConnection
val addressResponse = addressConnection.inputStream.bufferedReader().use { it.readText() }
val addressJson = JSONObject(addressResponse)
val address = addressJson.optString("display_name", "Lokasi tidak diketahui")

// Fetch weather data from Open Meteo
val weatherConnection = URL(weatherUrl).openConnection() as HttpURLConnection
val weatherResponse = weatherConnection.inputStream.bufferedReader().use { it.readText() }
val weatherJson = JSONObject(weatherResponse)
val currentWeather = weatherJson.getJSONObject("current_weather")
val temperature = currentWeather.getDouble("temperature")
val windSpeed = currentWeather.getDouble("windspeed")
val weatherCode = currentWeather.getInt("weathercode")
val weatherCondition = weatherCodeDescriptions[weatherCode] ?: "Tidak diketahui"

// Fetch humidity from Met Norway API
val metConnection = URL(metNorwayUrl).openConnection() as HttpURLConnection
metConnection.setRequestProperty("User-Agent", "wthrio/1.0")

val metResponse = metConnection.inputStream.bufferedReader().use { it.readText() }
val metJson = JSONObject(metResponse)
val timeseries = metJson.getJSONObject("properties").getJSONArray("timeseries")
val humidity = timeseries.getJSONObject(0)
.getJSONObject("data").getJSONObject("instant")
.getJSONObject("details").getDouble("relative_humidity")

val sharedPreferences = getSharedPreferences("Cuaca", MODE_PRIVATE)
								val editor = sharedPreferences.edit()
								editor.putString("W_CONDITION","$weatherCondition" )
								editor.putString("TEMPERATURE","$temperature°C" )
								editor.putString("FORECAST",weatherCondition)
								
								editor.apply()
								
								val intent = Intent(this, WeatherWidget::class.java)
intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(
    ComponentName(this, WeatherWidget::class.java)
)
intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
sendBroadcast(intent)

runOnUiThread {
// Update UI
locationTextView.text = address
updatedTextView.text = "${Date()}"
temperatureTextView.text = "$temperature°C"
windTextView.text = "Angin: $windSpeed km/h"
weatherConditionTextView.text = weatherCondition
humidityText.text = "Kelembapan: $humidity%"

// Update weather icon
val weatherIconRes = weatherCodeIcons[weatherCode] ?: R.drawable.icon_unknown
weatherIcon.setImageResource(weatherIconRes)

// Optional: send temperature notification
sendTemperatureNotification(temperature)
}
} catch (e: Exception) {
runOnUiThread {
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
}
}
}
} else {
Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
}
}
}

    private fun setBackgroundGradient(weatherCondition: String) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val baseColor = if (currentHour in 6..18) {
            when {
                weatherCondition.contains("Cerah") -> R.color.day_clear
                weatherCondition.contains("Hujan") -> R.color.day_rain
                else -> R.color.day_cloudy
            }
        } else {
            when {
                weatherCondition.contains("Cerah") -> R.color.night_clear
                weatherCondition.contains("Hujan") -> R.color.night_rain
                else -> R.color.night_cloudy
            }
        }
        rootLayout.setBackgroundResource(baseColor)
    }

    private fun sendTemperatureNotification(temperature: Double) {
        val notificationText = when {
            temperature > 30 -> "Suhu terlalu panas! ,Suhu saat ini: $temperature°C"
            temperature < 15 -> "Suhu terlalu dingin!,Suhu saat ini: $temperature°C"
            else -> "Suhu normal,suhu saat ini: $temperature°C"
        }
        val notificationBuilder = NotificationCompat.Builder(this, "weather_alerts")
            .setSmallIcon(R.drawable.icons)
            .setContentTitle("Peringatan Cuaca")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify(1, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "weather_alerts",
                "Weather Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
	

    private fun sendPermissionDeniedNotification() {
        val notificationBuilder = NotificationCompat.Builder(this, "weather_alerts")
            .setSmallIcon(R.drawable.icons)
            .setContentTitle("Izin Lokasi Ditolak")
            .setContentText("Aplikasi tidak dapat mengakses lokasi Anda.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify(2, notificationBuilder.build())
        }
    }
}