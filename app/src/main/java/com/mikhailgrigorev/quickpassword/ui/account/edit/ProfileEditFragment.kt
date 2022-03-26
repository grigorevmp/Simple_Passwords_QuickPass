package com.mikhailgrigorev.quickpassword.ui.account.edit

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
import com.mikhailgrigorev.quickpassword.ui.account.AccountViewModel
import javax.inject.Inject

class ProfileEditFragment : Fragment() {

    private lateinit var viewModel: AccountViewModel
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
    }

    private fun setListeners() {
        binding.savePass.setOnClickListener {
            val login = binding.etUserLogin.text.toString()
            val mail = binding.etUserEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            if (password == "") {
                binding.tilPassword.error = "Where is my password??"
            } else {
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
                        Utils.makeToast(context!!, "Saved")
                        findNavController().popBackStack()
                    }
                }.addOnFailureListener { exception ->
                    Log.d("Auth mail", Utils.getMail()!!)
                    Log.d("Auth password", password)
                    Utils.makeToast(context!!, "Data saving error, please write to the app creator")
                    exception.message?.let { Utils.makeToast(context!!, it) }
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