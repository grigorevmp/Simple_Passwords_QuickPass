package com.mikhailgrigorev.simple_password.common.manager

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.mikhailgrigorev.simple_password.data.database.FOLDER_CARD_DB_NAME
import com.mikhailgrigorev.simple_password.data.database.PASSWORD_CARD_DB_NAME
import java.io.File


object BackupManager {
    fun generateFile(context: Context, fileName: String): File? {
        val csvFile = File(context.filesDir, fileName)
        csvFile.createNewFile()
        return if (csvFile.exists()) {
            csvFile
        } else {
            null
        }
    }

    fun goToFileIntent(context: Context, file: File): Intent {
        val contentUri = FileProvider.getUriForFile(
                context,
                "com.mikhailgrigorev.simple_password.provider",
                file
        )


        val intent = Intent(Intent.ACTION_VIEW)
                .addFlags( Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION)

        intent.setDataAndType(contentUri, "text/comma-separated-values")

        return intent
    }

    fun getCSVFileName(folder: Boolean): String =
            if (folder) "$FOLDER_CARD_DB_NAME.csv" else "$PASSWORD_CARD_DB_NAME.csv"
}