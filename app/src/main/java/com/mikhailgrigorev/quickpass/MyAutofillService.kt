package com.mikhailgrigorev.quickpass

import android.app.assist.AssistStructure.ViewNode
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.O)
class MyAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal, callback: FillCallback
    ) {
        val structure =
                request.fillContexts[request.fillContexts.size - 1].structure
        // Создаем пустой список
        val emailFields: MutableList<ViewNode?> = ArrayList()
        val appName = structure.activityComponent.packageName

        // Заполняем список
        identifyEmailFields(structure.getWindowNodeAt(0).rootViewNode, emailFields)
        if (emailFields.isEmpty()) return
        val rvPrimaryEmail = RemoteViews(packageName, R.layout.email_suggestion)
        val rvSecondaryEmail = RemoteViews(packageName, R.layout.email_suggestion)

        val primaryEmail = "QuickPass 1"
        val secondaryEmail = "QuickPass 2"

        // Обновляет TextView
        rvPrimaryEmail.setTextViewText(R.id.email_suggestion_item, primaryEmail)
        rvSecondaryEmail.setTextViewText(R.id.email_suggestion_item, secondaryEmail)

        val emailField = emailFields[0]
        val primaryEmailDataSet = Dataset.Builder(rvPrimaryEmail).setValue(
                emailField?.autofillId!!,
                AutofillValue.forText(primaryEmail)
        ).build()
        val secondaryEmailDataSet = Dataset.Builder(rvSecondaryEmail).setValue(
                emailField.autofillId!!,
                AutofillValue.forText(secondaryEmail)
        ).build()
        val response = FillResponse.Builder().addDataset(primaryEmailDataSet)
                .addDataset(secondaryEmailDataSet)
                .build()
        callback.onSuccess(response)
    }

    private fun identifyEmailFields(
        node: ViewNode,
        emailFields: MutableList<ViewNode?>
    ) {
        if (node.className != null && node.className.contains("EditText")) {
            val viewId = node.idEntry
            if (viewId != null && (viewId.contains("email") || viewId.contains("username"))) {
                emailFields.add(node)
                return
            }
        }
        for (i in 0 until node.childCount) {
            identifyEmailFields(node.getChildAt(i), emailFields)
        }
    }
/*
    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        // Get the structure from the request
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure

        // Traverse the structure looking for nodes to fill out.
        //val parsedStructure: ParsedStructure = parseStructure(structure)

        // Fetch user data that matches the fields.
        //val (username: String, password: String) = fetchUserData(parsedStructure)

        val (username: String, password: String) = UserData("a", "b")

        // Build the presentation of the datasets
        val usernamePresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        usernamePresentation.setTextViewText(android.R.id.text1, "my_username")
        val passwordPresentation = RemoteViews(packageName, R.layout.simple_list_item_1)
        passwordPresentation.setTextViewText(android.R.id.text1, "Password for my_username")

        // Add a dataset to the response
        val fillResponse: FillResponse = FillResponse.Builder()
                .addDataset(Dataset.Builder()
                        .setValue(
                                //parsedStructure.usernameId,
                                AutofillValue.forText(username),
                                usernamePresentation
                        )
                        .setValue(
                                //parsedStructure.passwordId,
                                AutofillValue.forText(password),
                                passwordPresentation
                        )
                        .build())
                .build()

        // If there are no errors, call onSuccess() and pass the response
        callback.onSuccess(fillResponse)
    }*/

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {

    }

    data class ParsedStructure(var usernameId: AutofillId, var passwordId: AutofillId)

    data class UserData(var username: String, var password: String)


}