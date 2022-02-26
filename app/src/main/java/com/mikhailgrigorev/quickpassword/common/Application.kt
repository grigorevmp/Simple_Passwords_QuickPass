package com.mikhailgrigorev.quickpassword.common

import android.app.Application
import android.os.StrictMode
import androidx.viewbinding.BuildConfig
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.database.FolderCardDatabase
import com.mikhailgrigorev.quickpassword.data.database.PasswordCardDatabase

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        PasswordCardDatabase.setInstance(this)
        FolderCardDatabase.setInstance(this)
        
        Utils.init(this)
        Utils.setSharedPreferences()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                            .detectDiskReads()
                            .detectDiskWrites()
                            .detectNetwork()
                            .penaltyLog()
                            //.penaltyDeath()
                            .build()
            )
            StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                            .detectLeakedSqlLiteObjects()
                            .detectLeakedClosableObjects()
                            .penaltyLog()
                            //.penaltyDeath()
                            .build()
            )
        }
    }

    init {
        instance = this
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}