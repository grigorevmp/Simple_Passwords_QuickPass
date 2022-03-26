package com.mikhailgrigorev.quickpassword.di.component

import com.mikhailgrigorev.quickpassword.common.Application
import com.mikhailgrigorev.quickpassword.di.modules.AppModule
import com.mikhailgrigorev.quickpassword.di.modules.RoomModule
import com.mikhailgrigorev.quickpassword.di.modules.ViewModelModule
import com.mikhailgrigorev.quickpassword.ui.account.edit.ProfileEditFragment
import com.mikhailgrigorev.quickpassword.ui.account.view.ProfileFragment
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainActivity
import com.mikhailgrigorev.quickpassword.ui.password.PasswordFragment
import com.mikhailgrigorev.quickpassword.ui.password_card.create.PasswordCreateActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.edit.PasswordEditActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.view.PasswordViewActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AppModule::class,
            RoomModule::class,
            ViewModelModule::class
        ]
)
interface ApplicationComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: PasswordCreateActivity)
    fun inject(activity: PasswordViewActivity)
    fun inject(activity: PasswordEditActivity)
    fun inject(application: Application)
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: ProfileEditFragment)
    fun inject(fragment: PasswordFragment)
}