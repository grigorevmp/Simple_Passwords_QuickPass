package com.mikhailgrigorev.quickpassword.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private var condition = true
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners(){
        binding.back.setOnClickListener {
            finish()
        }

        // My link to Telegram
        binding.telegram.setOnClickListener {
            condition=false
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/grigorevmp"))
            startActivity(i)
        }

        // My link to VK
        binding.vkontakte.setOnClickListener {
            condition=false
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/grigorevmp"))
            startActivity(i)
        }

        // My link to GitHub
        binding.gitHub.setOnClickListener {
            condition=false
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/grigorevmp/QuickPass-Mobile-Password-manager/"))
            startActivity(i)
        }

        // Direct Mail sending
        binding.mail.setOnClickListener {
            sendEmail()
        }
    }

    private fun sendEmail() {
        condition=false
        val recipient = "16112000m@gmail.com"
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