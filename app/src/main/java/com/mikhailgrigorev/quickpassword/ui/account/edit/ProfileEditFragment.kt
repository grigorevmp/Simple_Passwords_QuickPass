package com.mikhailgrigorev.quickpassword.ui.account.edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.UserProfileChangeRequest
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.FragmentProfileEditBinding
import com.mikhailgrigorev.quickpassword.ui.account.AccountViewModel

class ProfileEditFragment : Fragment() {

    private lateinit var viewModel: AccountViewModel

    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0
    private var pass2FA = 0
    private var encryptedPass = 0
    private var timeLimit = 0
    private var pins = 0
    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        val view = binding.root

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
                        UserProfileChangeRequest.Builder().apply {
                            displayName = login
                        }.build()
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
        val login = Utils.getLogin()!!
        val name: String = getString(R.string.hi) + " " + login
        binding.tvUsernameText.text = name
    }

}