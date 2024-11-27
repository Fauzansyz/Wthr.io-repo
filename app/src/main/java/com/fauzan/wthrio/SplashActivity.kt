package com.fauzan.wthrio

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Durasi Splash Screen (dalam milidetik)
        val splashScreenDuration = 3000L

        Handler().postDelayed({
            // Pindah ke MainActivity setelah Splash Screen
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, splashScreenDuration)
    }
}