package com.mikhailgrigorev.quickpassword.service

import android.annotation.SuppressLint
import android.app.assist.AssistStructure.ViewNode
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.dbhelpers.PasswordsDataBaseHelper
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class QuickPassAutoFillService : AutofillService() {

    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"

    @SuppressLint("Recycle")
    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal, callback: FillCallback
    ) {
        val structure =
                request.fillContexts[request.fillContexts.size - 1].structure
        val emailFields: MutableList<ViewNode?> = ArrayList()
        val passFields: MutableList<ViewNode?> = ArrayList()
        val appName = structure.activityComponent.packageName

        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val username = sharedPref.getString(_keyUsername, "none")

        val emails: ArrayList<String> = ArrayList()
        val passwords: ArrayList<String> = ArrayList()
        val names: ArrayList<String> = ArrayList()

        if((username != null)&&(username != "none")) {

            val pdbHelper = PasswordsDataBaseHelper(this, username)
            val pDatabase = pdbHelper.writableDatabase
            try {
                val pCursor: Cursor = pDatabase.query(
                        pdbHelper.TABLE_USERS, arrayOf(
                        pdbHelper.KEY_NAME,
                        pdbHelper.KEY_PASS,
                        pdbHelper.KEY_LOGIN
                ),
                        null, null,
                        null, null, null
                )

                if (pCursor.moveToFirst()) {
                    val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                    val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                    val loginIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_LOGIN)
                    do {
                        val email = pCursor.getString(loginIndex).toString()
                        val password = pCursor.getString(passIndex).toString()
                        val name = pCursor.getString(nameIndex).toString()
                        if(email != "")
                            emails.add(email)
                        else
                            emails.add(username)
                        names.add(name)
                        passwords.add(password)
                    } while (pCursor.moveToNext())
                }
            } catch (e: SQLException) {
            }
        }

        identifyEmailFields(structure.getWindowNodeAt(0).rootViewNode, emailFields)
        identifyPassFields(structure.getWindowNodeAt(0).rootViewNode, passFields)

        val dataSets: ArrayList<Dataset> = ArrayList()

        if (emailFields.isNotEmpty()){

            var i = 0

            for (mail in emails){
                names[i].split("\\s".toRegex()).forEach { partName ->
                    if ((mail.contains("@")) or (partName.toLowerCase(Locale.ROOT).contains(
                                appName.toLowerCase(
                                        Locale.ROOT
                                )
                        )) or (appName.toLowerCase(Locale.ROOT)
                                .contains(partName.toLowerCase(Locale.ROOT)))
                    ) {
                        val remoteView = RemoteViews(packageName, R.layout.autofill_suggestion)
                        remoteView.setTextViewText(R.id.suggestion_item, mail)
                        dataSets.add(
                                Dataset.Builder(remoteView).setValue(
                                        emailFields[0]?.autofillId!!,
                                        AutofillValue.forText(mail)
                                ).build()
                        )
                    }
                }
                i += 1
            }
        }
        if(passFields.isNotEmpty()){

            var i = 0

            var remoteView = RemoteViews(packageName, R.layout.autofill_suggestion)
            remoteView.setTextViewText(R.id.suggestion_item, "Random")
            for (name in names){
                name.split("\\s".toRegex()).forEach { partName ->
                    if ((partName.toLowerCase(Locale.ROOT).contains(appName.toLowerCase(Locale.ROOT))) or
                        (appName.toLowerCase(Locale.ROOT).contains(partName.toLowerCase(Locale.ROOT)))){
                        remoteView = RemoteViews(packageName, R.layout.autofill_suggestion)
                        remoteView.setTextViewText(R.id.suggestion_item, passwords[i])
                        dataSets.add(Dataset.Builder(remoteView).setValue(
                                passFields[0]?.autofillId!!,
                                AutofillValue.forText(passwords[i])
                        ).build())
                    }
                }
                i += 1
                }
        }

        if(dataSets.size != 0){
            val response = FillResponse.Builder()
            for (data in dataSets){
                response.addDataset(data)
            }
            val responseBuilder = response.build()
            callback.onSuccess(responseBuilder)
        }

    }

    private fun identifyEmailFields(
        node: ViewNode,
        emailFields: MutableList<ViewNode?>
    ) {
        if (node.className != null && node.className!!.contains("EditText")) {
            val viewId = node.idEntry
            if (viewId != null && (viewId.contains("email") ||
                        viewId.contains("username") ||
                        viewId.contains("name") ||
                        viewId.contains("mail"))) {
                emailFields.add(node)
                return
            }
        }
        for (i in 0 until node.childCount) {
            identifyEmailFields(node.getChildAt(i), emailFields)
        }
    }

    private fun identifyPassFields(
        node: ViewNode,
        passFields: MutableList<ViewNode?>
    ) {
        if (node.className != null && node.className!!.contains("EditText")) {
            val viewId = node.idEntry
            if (viewId != null && (viewId.contains("password") ||
                        viewId.contains("pass") ||
                        viewId.contains("pin") ||
                        viewId.contains("code") ||
                        viewId.contains("secure"))) {
                passFields.add(node)
                return
            }
        }
        for (i in 0 until node.childCount) {
            identifyPassFields(node.getChildAt(i), passFields)
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
    }

}