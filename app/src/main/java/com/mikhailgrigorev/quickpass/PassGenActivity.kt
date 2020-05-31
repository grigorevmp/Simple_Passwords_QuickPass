package com.mikhailgrigorev.quickpass

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_pass_gen.*

class PassGenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_gen)

        val args: Bundle? = intent.extras
        val name: String? = "Hi, " + args?.get("login").toString()
        helloTextId.text = name

        logOut.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}