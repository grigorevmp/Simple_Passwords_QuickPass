package com.mikhailgrigorev.quickpass

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_splash)
        rotation.fillAfter = true
        logo.startAnimation(rotation)

        Handler().postDelayed({
            val intent = Intent(this, LoginAfterSplashActivity::class.java)
            startActivityForResult(intent, 1)
            overridePendingTransition(0, R.anim.fadein)
            finish()
        }, 500)


    }
}


