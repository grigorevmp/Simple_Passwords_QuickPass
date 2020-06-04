package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_account.*
import android.app.Activity.RESULT_OK
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AlertDialog
import java.io.IOException

class AccountActivity : AppCompatActivity() {

    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private val GET_IMAGE = 1
    private lateinit var filePath : Uri
    private lateinit var bitmap : Bitmap

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_account)


        val args: Bundle? = intent.extras
        val login: String? = args?.get("login").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name

        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            putString(KEY_USERNAME, login)
            commit()
        }

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
                passViewField.setText(ex_infoPassText)
                val id = resources.getIdentifier(
                    ex_infoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }

        logOut.setOnClickListener {
            exit(sharedPref)
        }

        loadAcc.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, GET_IMAGE)
        }

        deleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete this password")
            builder.setMessage("Are you really want to this password?")

            builder.setPositiveButton("YES"){ _, _ ->
                database.delete(dbHelper.TABLE_USERS,
                    "NAME = ?",
                    arrayOf(login))
                toast("You account has been deleted")
                exit(sharedPref)
            }

            builder.setNegativeButton("NO"){ _, _ ->
            }

            builder.setNeutralButton("Cancel"){_,_ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(KEY_USERNAME).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GET_IMAGE && resultCode == RESULT_OK && data != null){
            filePath = data.data!!
            try{
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, filePath)
                imageView!!.setImageBitmap(bitmap)
            }catch (exception : IOException){
                Toast.makeText(this,"Error Loading Image!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}