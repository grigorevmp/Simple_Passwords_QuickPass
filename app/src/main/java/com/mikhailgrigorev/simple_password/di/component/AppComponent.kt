package com.mikhailgrigorev.simple_password.di.component

import com.mikhailgrigorev.simple_password.common.Application
import com.mikhailgrigorev.simple_password.di.modules.AppModule
import com.mikhailgrigorev.simple_password.di.modules.RoomModule
import com.mikhailgrigorev.simple_password.di.modules.ViewModelModule
import com.mikhailgrigorev.simple_password.ui.profile.edit.ProfileEditFragment
import com.mikhailgrigorev.simple_password.ui.profile.view.ProfileFragment
import com.mikhailgrigorev.simple_password.ui.folder.FolderViewActivity
import com.mikhailgrigorev.simple_password.ui.main_activity.MainActivity
import com.mikhailgrigorev.simple_password.ui.password.PasswordFragment
import com.mikhailgrigorev.simple_password.ui.password_card.create.PasswordCreateActivity
import com.mikhailgrigorev.simple_password.ui.password_card.edit.PasswordEditActivity
import com.mikhailgrigorev.simple_password.ui.password_card.view.PasswordViewActivity
import com.mikhailgrigorev.simple_password.ui.settings.SettingsFragment
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
    fun inject(activity: FolderViewActivity)
    fun inject(activity: PasswordCreateActivity)
    fun inject(activity: PasswordViewActivity)
    fun inject(activity: PasswordEditActivity)
    fun inject(application: Application)
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: ProfileEditFragment)
    fun inject(fragment: PasswordFragment)
    fun inject(fragment: SettingsFragment)
}