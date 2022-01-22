package com.mikhailgrigorev.quickpassword

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.mikhailgrigorev.quickpassword.databinding.ActivitySplashBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_splash)
        rotation.fillAfter = true
        binding.logo.startAnimation(rotation)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginAfterSplashActivity::class.java)
            startActivityForResult(intent, 1)
            overridePendingTransition(0, R.anim.fadein)
            finish()
        }, 500)


    }
}


