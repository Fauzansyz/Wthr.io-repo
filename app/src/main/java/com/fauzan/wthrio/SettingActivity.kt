package com.fauzan.wthrio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.appcompat.widget.SwitchCompat
import com.fauzan.wthrio.StateManager // Import StateManager

class SettingActivity : AppCompatActivity() {
	
	private lateinit var buttonCard: CardView
	
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
		
		buttonCard = findViewById(R.id.cardId)
		
		buttonCard.setOnClickListener {
val intent = Intent(this, MainActivity::class.java)
intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
startActivity(intent)
finish()
}
val switchToggle = findViewById<SwitchCompat>(R.id.switchCompat)
val sharedPreferences = getSharedPreferences("ToggleStateButton", MODE_PRIVATE)

// Ambil nilai state yang tersimpan saat aplikasi pertama kali dijalankan
var isOn = sharedPreferences.getBoolean("state", false)

switchToggle.isChecked = isOn
        // Mendengarkan perubahan pada SwitchCompat
        switchToggle.setOnCheckedChangeListener { _, isChecked -> 
		StateManager.switchState = isChecked
            // Menampilkan Toast atau melakukan aksi berdasarkan state switch
			val editor = sharedPreferences.edit()
    editor.putBoolean("state", isChecked)
    editor.apply()
            if (isChecked) {
		       // Ketika switch dalam keadaan ON
               // Toast.makeText(this, "Switch is ON", Toast.LENGTH_SHORT).show()
            } else {
		    // Ketika switch dalam keadaan OFF
              //  Toast.makeText(this, "Switch is OFF", Toast.LENGTH_SHORT).show()
            }
    

        // Mengambil state toggle saat ini (ON/OFF)
        val currentState = switchToggle.isChecked
        if (currentState) {
            // Jika ON
          //  Toast.makeText(this, "Initial state: ON", Toast.LENGTH_SHORT).show()
        } else {
            // Jika OFF
         //   Toast.makeText(this, "Initial state: OFF", Toast.LENGTH_SHORT).show()
          }
		}
    }
}
