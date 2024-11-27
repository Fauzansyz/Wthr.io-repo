package com.fauzan.wthrio

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

private lateinit var fusedLocationClient: FusedLocationProviderClient
private val apiUrl = "https://api.open-meteo.com/v1/forecast"

// Declare views with findViewById
private lateinit var locationTextView: TextView
private lateinit var updatedTextView: TextView
private lateinit var temperatureTextView: TextView
private lateinit var windTextView: TextView
private lateinit var weatherConditionTextView: TextView

// Map weather codes to descriptions
private val weatherCodeDescriptions = mapOf(
0 to "Cerah",
1 to "Sebagian berawan",
2 to "Berawan",
3 to "Berawan berat",
45 to "Kabut",
48 to "Kabut tebal",
51 to "Gerimis ringan",
53 to "Gerimis sedang",
55 to "Gerimis berat",
61 to "Hujan ringan",
63 to "Hujan sedang",
65 to "Hujan lebat",
71 to "Salju ringan",
73 to "Salju sedang",
75 to "Salju lebat",
95 to "Badai petir"
// Add other codes as needed
)

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_main)

// Initialize views
locationTextView = findViewById(R.id.locationTextView)
updatedTextView = findViewById(R.id.updatedTextView)
temperatureTextView = findViewById(R.id.temperatureTextView)
windTextView = findViewById(R.id.windTextView)
weatherConditionTextView = findViewById(R.id.weatherConditionTextView)

fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

// Create notification channel
createNotificationChannel()

// Check location permissions
checkLocationPermission()
}

private fun checkLocationPermission() {
if (ActivityCompat.checkSelfPermission(
this,
Manifest.permission.ACCESS_FINE_LOCATION
) != PackageManager.PERMISSION_GRANTED
) {
requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
} else {
fetchWeatherData()
}
}

// Request permission launcher
private val requestPermissionLauncher =
registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
if (isGranted) {
fetchWeatherData()
} else {
Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show()
sendPermissionDeniedNotification()
}
}

@SuppressLint("MissingPermission")
private fun fetchWeatherData() {
fusedLocationClient.lastLocation.addOnSuccessListener { location ->
if (location != null) {
val latitude = location.latitude
val longitude = location.longitude
val url = "$apiUrl?latitude=$latitude&longitude=$longitude&current_weather=true"

thread {
try {
val connection = URL(url).openConnection() as HttpURLConnection
connection.requestMethod = "GET"

val responseCode = connection.responseCode
if (responseCode == HttpURLConnection.HTTP_OK) {
val response = connection.inputStream.bufferedReader().use { it.readText() }
val jsonResponse = JSONObject(response)

val currentWeather = jsonResponse.getJSONObject("current_weather")
val temperature = currentWeather.getDouble("temperature")
val windSpeed = currentWeather.getDouble("windspeed")
val weatherCode = currentWeather.getInt("weathercode")
val weatherCondition = weatherCodeDescriptions[weatherCode] ?: "Tidak diketahui"

runOnUiThread {
locationTextView.text = "Your Location"
updatedTextView.text = "Updated: ${System.currentTimeMillis()}"
temperatureTextView.text = "$temperature°C"
windTextView.text = "Wind Speed: $windSpeed km/h"
weatherConditionTextView.text = "$weatherCondition"

// Send notification based on temperature
sendTemperatureNotification(temperature)
}
} else {
runOnUiThread {
Toast.makeText(this, "Failed to fetch weather data!", Toast.LENGTH_SHORT).show()
}
}
} catch (e: Exception) {
runOnUiThread {
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
}
}
}
} else {
Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
}
}
}

private fun createNotificationChannel() {
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
val channelName = "Weather Notifications"
val descriptionText = "Channel for weather temperature alerts"
val importance = NotificationManager.IMPORTANCE_DEFAULT
val channel = NotificationChannel("weather_alerts", channelName, importance).apply {
description = descriptionText
}
val notificationManager: NotificationManager =
getSystemService(NOTIFICATION_SERVICE) as NotificationManager
notificationManager.createNotificationChannel(channel)

val permissionChannel = NotificationChannel(
"permission_alerts",
"Permission Alerts",
NotificationManager.IMPORTANCE_HIGH
).apply {
description = "Channel for permission alerts"
}
notificationManager.createNotificationChannel(permissionChannel)
}
}

private fun sendTemperatureNotification(temperature: Double) {
val notificationBuilder = NotificationCompat.Builder(this, "weather_alerts")
.setSmallIcon(R.drawable.icons) // Replace with your icon
.setContentTitle("Peringatan cuaca")
.setContentText(
if (temperature > 30) {
"Suhu terlalu panas, suhu saat ini: $temperature°C"
} else {
"Suhu terlalu dingin, suhu saat ini: $temperature°C"
}
)
.setPriority(NotificationCompat.PRIORITY_DEFAULT)

with(NotificationManagerCompat.from(this)) {
notify(1, notificationBuilder.build())
}
}

private fun sendPermissionDeniedNotification() {
val notificationBuilder = NotificationCompat.Builder(this, "permission_alerts")
.setSmallIcon(R.drawable.icons) // Replace with your icon
.setContentTitle("Permission Denied")
.setContentText("Location permission is required to fetch weather data.")
.setPriority(NotificationCompat.PRIORITY_HIGH)

with(NotificationManagerCompat.from(this)) {
notify(2, notificationBuilder.build())
}
}

fun about(view: View) {
// Navigate to another activity
val intent = Intent(this, SecondActivity::class.java)
startActivity(intent)
}
}