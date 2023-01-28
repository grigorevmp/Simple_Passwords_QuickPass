package com.mikhailgrigorev.simple_password.ui.profile.edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.databinding.FragmentProfileEditBinding
import com.mikhailgrigorev.simple_password.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.simple_password.di.modules.RoomModule
import com.mikhailgrigorev.simple_password.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.simple_password.ui.profile.ProfileViewModel
import javax.inject.Inject


class ProfileEditFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        val view = binding.root

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        initViewModel()
        initViews()
        setHelloText()
        setListeners()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun initViews() {
        binding.etUserLogin.setText(Utils.accountSharedPrefs.getLogin())
        if(Utils.accountSharedPrefs.getAvatarEmoji() != null){ binding.etUserAvatarEmoji.setText(Utils.accountSharedPrefs.getAvatarEmoji().toString()) }
    }

    private fun isEmoji(message: String): Boolean {
        return message.matches(
                ("(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|" +
                        "[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|" +
                        "[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|" +
                        "[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|" +
                        "[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|" +
                        "[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|" +
                        "[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|" +
                        "[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|" +
                        "[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|" +
                        "[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|" +
                        "[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)+").toRegex()
        )
    }

    private fun setListeners() {
        binding.savePass.setOnClickListener {
            val login = binding.etUserLogin.text.toString()
            val password = binding.etPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val emojiAvatar = binding.etUserAvatarEmoji.text.toString()
            if (password == "") {
                binding.tilPassword.error = getString(R.string.enter_passwords)
            } else {
                binding.cvSaving.visibility = View.VISIBLE
                if (Utils.accountSharedPrefs.isCorrectMasterPassword(password)) {
                        if (login != "") {
                            Utils.accountSharedPrefs.setLogin(login)
                            viewModel.setLoginData(login)
                        }
                        if (newPassword != "") { Utils.accountSharedPrefs.setMasterPassword(newPassword) }
                        if (isEmoji(emojiAvatar)) { Utils.accountSharedPrefs.setAvatarEmoji(emojiAvatar) }
                        try { findNavController().popBackStack() }
                        catch (e: Exception) { Log.d("PopBackStack", "strange error") }
                } else {
                    Log.d("Auth password", password)
                    binding.cvSaving.visibility = View.GONE
                    Utils.makeToast(requireContext(), "Data saving error, please write to the app creator")
                }
            }
        }

        binding.back.setOnClickListener {
            if (parentFragment != null)
                parentFragment?.onResume()
            findNavController().popBackStack()
        }

        binding.generatePassword.setOnClickListener {
            binding.tilNewPassword.error = null
            val newPassword: String =
                    Utils.password_manager.generatePassword(
                            isWithLetters = true,
                            isWithUppercase = true,
                            isWithNumbers = true,
                            isWithSpecial = false,
                            length = 12
                    )
            binding.etNewPassword.setText(newPassword)
            binding.generatePassword.animate()
                    .rotation((binding.generatePassword.rotation + 45F) % 360F).interpolator =
                    AccelerateDecelerateInterpolator()
        }
    }

    private fun setHelloText() {
        viewModel.userLogin.observe(viewLifecycleOwner) { login ->
            val name: String = getString(R.string.hi) + " " + login
            binding.tvUsernameText.text = name
            binding.etUserLogin.setText(Utils.accountSharedPrefs.getLogin())
        }
    }

    private fun initViewModel() {
        viewModel = this.injectViewModel(viewModelFactory)
    }
}