package com.mikhailgrigorev.simple_password.di.modules.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mikhailgrigorev.simple_password.ui.profile.edit.ProfileEditFragment
import com.mikhailgrigorev.simple_password.ui.profile.view.ProfileFragment
import com.mikhailgrigorev.simple_password.ui.folder.FolderViewActivity
import com.mikhailgrigorev.simple_password.ui.password.PasswordFragment
import com.mikhailgrigorev.simple_password.ui.password_card.create.PasswordCreateActivity
import com.mikhailgrigorev.simple_password.ui.password_card.edit.PasswordEditActivity
import com.mikhailgrigorev.simple_password.ui.password_card.view.PasswordViewActivity
import com.mikhailgrigorev.simple_password.ui.settings.SettingsFragment

inline fun <reified T : ViewModel> FolderViewActivity.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> PasswordCreateActivity.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> PasswordEditActivity.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> PasswordViewActivity.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> ProfileFragment.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> ProfileEditFragment.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> PasswordFragment.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> SettingsFragment.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this, factory)[T::class.java]
}