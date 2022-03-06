package com.mikhailgrigorev.quickpassword.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.base.MyBaseActivity
import com.mikhailgrigorev.quickpassword.common.utils.authorBaseMail
import com.mikhailgrigorev.quickpassword.common.utils.authorGitHubLink
import com.mikhailgrigorev.quickpassword.common.utils.authorTelegramLink
import com.mikhailgrigorev.quickpassword.databinding.ActivityAboutBinding

class AboutActivity : MyBaseActivity() {
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

        binding.telegram.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(authorTelegramLink))
            startActivity(i)
        }

        binding.gitHub.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(authorGitHubLink))
            startActivity(i)
        }

        binding.mail.setOnClickListener {
            sendEmail()
        }
    }

    private fun sendEmail() {
        val recipient = authorBaseMail
        val subject = "QuickPassword app: feedback"
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