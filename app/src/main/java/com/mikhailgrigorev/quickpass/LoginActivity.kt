package com.mikhailgrigorev.quickpass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private val TAG = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Start animation
        loginFab.show()

        // Fab handler
        loginFab.setOnClickListener {
            if (signUpChip.isChecked){
                if (validate(inputLoginIdField.text.toString(), inputPasswordIdField.text.toString()))
                    signUp(inputLoginIdField.text.toString(), inputPasswordIdField.text.toString())
            }
            else{
                if (validate(inputLoginIdField.text.toString(), inputPasswordIdField.text.toString()))
                    signIn(inputLoginIdField.text.toString(), inputPasswordIdField.text.toString())
            }
        }

        // Chip handler
        signUpChipGroup.setOnCheckedChangeListener{ group, checkedId ->
            // Get the checked chip instance from chip group
            signUpChip?.let {
                if (signUpChip.isChecked){
                    loginFab.hide()
                    loginFab.text = getString (R.string.sign_up)
                    loginFab.show()
                }
                else{
                    loginFab.hide()
                    loginFab.text = getString (R.string.sign_in)
                    loginFab.show()
                }
            }
        }
    }

    private fun validate(login: String, password:String): Boolean {
        var valid = false
        if (login.isEmpty() || login.length < 3) {
            inputLoginId.error = getString(R.string.errNumOfText)
        } else {
            inputLoginId.error = null
            valid = true
        }
        if (password.isEmpty() || password.length < 4) {
            inputPasswordId.error = getString(R.string.errPass)
            valid = false;
        } else {
            inputPasswordId.error = null
        }
        return valid
    }

    private fun signUp (login: String, password:String) {
        Log.d(TAG, "SignUp");
        toast("You signed up")
        signIn(login, password)
    }

    private fun signIn (login: String, password:String){
        Log.d(TAG, "SignIn");
        toast("You signed in")
        // создание объекта Intent для запуска SecondActivity

        val intent = Intent(this, PassGenActivity::class.java)
        intent.putExtra("login", login)
        startActivity(intent)
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}