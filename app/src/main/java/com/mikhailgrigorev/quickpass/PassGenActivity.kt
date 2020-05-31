package com.mikhailgrigorev.quickpass

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pass_gen.*

class PassGenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_gen)

        val args: Bundle? = intent.extras
        val login: String? = args?.get("login").toString()
        val name: String? = "Hi, $login"
        helloTextId.text = name

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS, dbHelper.KEY_ID, dbHelper.KEY_IMAGE),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val idIndex: Int = cursor.getColumnIndex(dbHelper.KEY_ID)
            val nameIndex: Int = cursor.getColumnIndex(dbHelper.KEY_NAME)
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val ex_infoIdText = cursor.getString(idIndex).toString()
                val ex_infoNameText = cursor.getString(nameIndex).toString()
                val ex_infoPassText = cursor.getString(passIndex).toString()
                val ex_infoImgText = cursor.getString(imageIndex).toString()
                val infoIdText = "Id: $ex_infoIdText"
                val infoNameText = "Name: $ex_infoNameText"
                val infoPassText = "Password: $ex_infoPassText"
                val infoImgText = "Avatar src: $ex_infoImgText"
                info_id.text = infoIdText
                info_name.text = infoNameText
                info_pass.text = infoPassText
                info_image.text = infoImgText
                val id = getResources().getIdentifier(
                    ex_infoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }

        logOut.setOnClickListener {
            exit()
        }

        deleteAccount.setOnClickListener {
            database.delete(dbHelper.TABLE_USERS,
                "NAME = ?",
                arrayOf(login))
            toast("You account has been deleted")
            exit()
        }
    }

    private fun exit(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
}