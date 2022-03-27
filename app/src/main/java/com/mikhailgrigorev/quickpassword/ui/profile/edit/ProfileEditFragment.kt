package com.mikhailgrigorev.quickpassword.ui.profile.edit

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.UserProfileChangeRequest
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.FragmentProfileEditBinding
import com.mikhailgrigorev.quickpassword.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.quickpassword.di.modules.RoomModule
import com.mikhailgrigorev.quickpassword.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.quickpassword.ui.profile.ProfileViewModel
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
        binding.etUserEmail.setText(
                Utils.auth.currentUser?.email
        )
        binding.etUserLogin.setText(
                Utils.auth.currentUser?.displayName
        )
        if (Utils.auth.currentUser?.photoUrl != null) {
            binding.etUserAvatarEmoji.setText(
                    Utils.auth.currentUser?.photoUrl.toString()
            )
        }
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
            val mail = binding.etUserEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val emojiAvatar = binding.etUserAvatarEmoji.text.toString()
            if (password == "") {
                binding.tilPassword.error = "Where is my password??"
            } else {
                this.context?.let { it1 -> Utils.makeToast(it1, "Saving...") }
                Utils.auth.signInWithEmailAndPassword(
                        Utils.getMail()!!,
                        password
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (mail != "") {
                            Utils.auth.currentUser?.updateEmail(mail)
                            Utils.setMail(mail)
                        }
                        if (login != "") {
                            Utils.auth.currentUser?.updateProfile(
                                    UserProfileChangeRequest.Builder().apply {
                                        displayName = login
                                    }.build()
                            )
                            Utils.setLogin(login)
                        }
                        if (newPassword != "") {
                            Utils.auth.currentUser?.updatePassword(newPassword)
                        }
                        if (isEmoji(emojiAvatar)) {
                            Utils.auth.currentUser?.updateProfile(
                                    UserProfileChangeRequest.Builder().apply {
                                        photoUri = Uri.parse(emojiAvatar)
                                    }.build()
                            )
                        }
                        try {
                            findNavController().popBackStack()
                        } catch (e: Exception) {
                            Log.d("PopBackStack", "strange error")
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.d("Auth mail", Utils.getMail()!!)
                    Log.d("Auth password", password)
                    Utils.makeToast(requireContext(), "Data saving error, please write to the app creator")
                    exception.message?.let { Utils.makeToast(requireContext(), it) }
                }
            }
        }

        binding.back.setOnClickListener {
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
            binding.etUserLogin.setText(
                    Utils.auth.currentUser?.displayName
            )
        }
    }

    private fun initViewModel() {
        viewModel = this.injectViewModel(viewModelFactory)
    }

}