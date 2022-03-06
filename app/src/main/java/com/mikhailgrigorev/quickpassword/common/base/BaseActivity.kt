package com.mikhailgrigorev.quickpassword.common.base

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginActivity


open class MyBaseActivity : AppCompatActivity() {
    private fun resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback)
        disconnectHandler.postDelayed(disconnectCallback, Utils.getDisconnectTime())
    }

    private fun stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback)
    }

    override fun onUserInteraction() {
        resetDisconnectTimer()
    }

    public override fun onResume() {
        super.onResume()
        resetDisconnectTimer()
    }

    public override fun onStop() {
        super.onStop()
        stopDisconnectTimer()
    }

    private fun goToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val disconnectHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {

        }
    }
    private val disconnectCallback = Runnable {
        if (Utils.getDisconnectTime() != 0L) {
            goToLoginScreen()
        }
    }
}