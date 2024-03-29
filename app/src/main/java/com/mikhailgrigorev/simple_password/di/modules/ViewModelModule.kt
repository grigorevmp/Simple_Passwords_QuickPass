package com.mikhailgrigorev.simple_password.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mikhailgrigorev.simple_password.di.modules.viewModel.ViewModelFactory
import com.mikhailgrigorev.simple_password.di.modules.viewModel.ViewModelKey
import com.mikhailgrigorev.simple_password.ui.profile.ProfileViewModel
import com.mikhailgrigorev.simple_password.ui.folder.FolderViewModel
import com.mikhailgrigorev.simple_password.ui.main_activity.MainViewModel
import com.mikhailgrigorev.simple_password.ui.password_card.PasswordViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun postMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PasswordViewModel::class)
    internal abstract fun postPasswordViewModel(viewModel: PasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    internal abstract fun postAccountViewModel(viewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FolderViewModel::class)
    internal abstract fun postFolderViewModel(viewModel: FolderViewModel): ViewModel
}