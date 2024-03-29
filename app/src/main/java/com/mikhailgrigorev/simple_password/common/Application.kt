package com.mikhailgrigorev.simple_password.common

import android.app.Application
import android.os.StrictMode
import androidx.viewbinding.BuildConfig
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.di.component.ApplicationComponent
import com.mikhailgrigorev.simple_password.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.simple_password.di.modules.RoomModule


class Application : Application() {

    private lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerApplicationComponent.builder()
                .roomModule(RoomModule(this))
                .build()
        appComponent.inject(this)
        component = appComponent

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

    fun getComponent(): ApplicationComponent {
        return appComponent
    }

    companion object {
        lateinit var instance: Application
            private set

        lateinit var component: ApplicationComponent
            private set
    }
}