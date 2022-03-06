package com.mikhailgrigorev.quickpassword.common.manager

import android.content.Context
import android.os.Environment
import android.util.Log
import com.mikhailgrigorev.quickpassword.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec


object BackupManager {
    fun importEncryptedDB(context: Context, DB_NAME: String): Int {
        try {
            val appName = context.resources.getString(R.string.app_name)
            val file =
                    File(Environment.getExternalStorageDirectory().toString() + "/" + appName + "/")
            if (!file.exists()) {
                file.mkdirs()
            }
            val sd = Environment.getExternalStorageDirectory()
            if (sd.canWrite()) {
                val data = Environment.getDataDirectory()
                val currentDBPath = "//data//" + context.packageName + "//databases//" + DB_NAME
                val backupDBPath = "/$appName/$DB_NAME"
                val currentDB = File(sd, backupDBPath)
                val backupDB = File(data, currentDBPath)
                val src = FileInputStream(currentDB)
                val dst = FileOutputStream(backupDB)

                val sks = SecretKeySpec("1234567890123456".toByteArray(), "AES")
                val cipher = Cipher.getInstance("AES")
                cipher.init(Cipher.DECRYPT_MODE, sks)
                val cis = CipherInputStream(src, cipher)
                var b: Int
                val d = ByteArray(8)
                while (cis.read(d).also { b = it } != -1) {
                    dst.write(d, 0, b)
                }
                dst.flush()
                src.close()
                dst.close()
                cis.close()
                Log.e("IMPORT_DB", "Database has been imported.")
                return 1
            } else {
                Log.e("IMPORT_DB", "No storage permission.")
                return 2
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("IMPORT_DB", "Error importing database!")
            return 3
        }
    }

    fun exportEncryptedDB(context: Context, DB_NAME: String): Int {
        try {
            val appName: String = context.resources.getString(R.string.app_name)
            val file =
                    File(Environment.getExternalStorageDirectory().toString() + "/" + appName + "/")
            if (!file.exists()) {
                file.mkdirs()
            }
            val sd: File = Environment.getExternalStorageDirectory()
            if (sd.canWrite()) {
                val data: File = Environment.getDataDirectory()
                val currentDBPath =
                        "//data//" + context.packageName.toString() + "//databases//" + DB_NAME
                val backupDBPath = "/$appName/$DB_NAME"
                val currentDB = File(data, currentDBPath)
                val backupDB = File(sd, backupDBPath)
                val src = FileInputStream(currentDB)
                val dst = FileOutputStream(backupDB)

                val sks = SecretKeySpec("1234567890123456".toByteArray(), "AES")

                val cipher: Cipher = Cipher.getInstance("AES")
                cipher.init(Cipher.ENCRYPT_MODE, sks)
                val cos = CipherOutputStream(dst, cipher)
                val b = ByteArray(8)
                var i: Int = src.read(b)
                while (i != -1) {
                    cos.write(b, 0, i)
                    i = src.read(b)
                }
                src.close()
                dst.close()
                cos.flush()
                cos.close()
                return 1
            } else {
                Log.e("EXPORT_DB", "No storage permission.")
                return 2
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("EXPORT_DB", "Error exporting database!")
            return 3
        }
    }
}