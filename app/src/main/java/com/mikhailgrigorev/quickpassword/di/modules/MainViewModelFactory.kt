package com.mikhailgrigorev.quickpassword.di.modules

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.mikhailgrigorev.quickpassword.ui.account.AccountViewModel
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainViewModel
import com.mikhailgrigorev.quickpassword.ui.password.PasswordFragment
import com.mikhailgrigorev.quickpassword.ui.password_card.PasswordViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

class ViewModelFactory @Inject constructor(
    private val viewModels: MutableMap<Class<out ViewModel>,
            Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
            viewModels[modelClass]?.get() as T
}

@Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

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
    @ViewModelKey(AccountViewModel::class)
    internal abstract fun postAccountViewModel(viewModel: AccountViewModel): ViewModel

    //Add more ViewModels here
}

inline fun <reified T : ViewModel> PasswordFragment.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, factory)[T::class.java]
}