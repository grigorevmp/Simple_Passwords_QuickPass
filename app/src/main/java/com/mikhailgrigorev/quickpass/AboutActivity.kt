package com.mikhailgrigorev.quickpass

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    private val _keyTHEME = "themePreference"
    private val _preferenceFile = "quickPassPreference"

    var condition = false
    override fun onCreate(savedInstanceState: Bundle?) {

        // Set Theme

        val pref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        when(pref.getString(_keyTHEME, "none")){
            "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "none", "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "battery" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }

        super.onCreate(savedInstanceState)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        // Finish app after some time

        val handler = Handler()
        val r = Runnable {
            if(condition) {
                condition=false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        handler.postDelayed(r, 600000)

        setContentView(R.layout.activity_about)

        // Exit from activity

        back.setOnClickListener {
            finish()
        }

        // My link to Telegram
        telegram.setOnClickListener {
            condition=false
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/grigorevmp"))
            startActivity(i)
        }

        // My link to VK
        vkontakte.setOnClickListener {
            condition=false
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/grigorevmp"))
            startActivity(i)
        }

        // My link to GitHub
        gitHub.setOnClickListener {
            condition=false
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/grigorevmp/QuickPass-Mobile-Password-manager/"))
            startActivity(i)
        }

        // Direct Mail sending
        mail.setOnClickListener {
            sendEmail()
        }

    }

    private fun sendEmail() {
        condition=false
        val recipient = "16112000m@gmai.com"
        val subject = "Quick password app"
        val message = "Hello, Mikhail \n"

        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        mIntent.putExtra(Intent.EXTRA_TEXT, message)
        try {
            startActivity(Intent.createChooser(mIntent, getString(R.string.chooseEmail)))
        }
        catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }
}