package com.rahul.dogbreedidentifierapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        supportActionBar?.hide()
        Handler().postDelayed(Runnable{
            val intent = Intent(this@SplashScreenActivity, CameraActivity::class.java)
            startActivity(intent)
            finish()
        }, 800)
    }
}