package com.fauzan.wthrio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.content.Intent
import java.net.HttpURLConnection
import androidx.cardview.widget.CardView
import java.net.URL

class InfoGempaActivity : AppCompatActivity() {
	
	private lateinit var locationTextView: TextView
    private lateinit var magnitudes: TextView
	private lateinit var potensiText: TextView
	private lateinit var kedalamanText: TextView
	private lateinit var time: TextView
	private lateinit var backButton: CardView
	
	
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_gempa_page)

        // Panggil fungsi untuk fetch data
        fetchGempaDataUsingHttpURLConnection()
		
		locationTextView = findViewById(R.id.lokasi)
		magnitudes = findViewById(R.id.magnitude)
		kedalamanText = findViewById(R.id.kedalaman)
		time = findViewById(R.id.tanggal)
		backButton = findViewById(R.id.cardId)
		potensiText = findViewById(R.id.potensi)
		
		backButton.setOnClickListener {
			val intent = Intent(this, MainActivity::class.java)
intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
startActivity(intent)
finish()
		}
    }

    private fun fetchGempaDataUsingHttpURLConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://data.bmkg.go.id/DataMKG/TEWS/autogempa.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val responseText = inputStream.bufferedReader().use { it.readText() }

                    // Parsing JSON
                    val jsonObject = JSONObject(responseText)
                    val infoGempa = jsonObject.getJSONObject("Infogempa").getJSONObject("gempa")

                    val tanggal = infoGempa.getString("Tanggal")
                    val waktu = infoGempa.getString("Jam")
                    val magnitude = infoGempa.getString("Magnitude")
					val potensi = infoGempa.getString("Potensi")
                    val kedalaman = infoGempa.getString("Kedalaman")
                    val lokasi = infoGempa.getString("Wilayah")

                    // Menampilkan data di UI dengan pindah ke main thread
                    runOnUiThread {
                       locationTextView.text = lokasi
					   magnitudes.text = "$magnitude SR"
					   kedalamanText.text = kedalaman
					   time.text = "$tanggal-$waktu"
                       potensiText.text = "Potensi:$potensi"
                    }
                } else {
                    println("Gagal mengambil data: ${connection.responseCode}")
                }

                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    
                }
            }
        }
    }
}