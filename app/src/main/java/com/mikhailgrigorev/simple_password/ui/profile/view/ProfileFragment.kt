package com.mikhailgrigorev.simple_password.ui.profile.view

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.databinding.FragmentProfileBinding
import com.mikhailgrigorev.simple_password.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.simple_password.di.modules.RoomModule
import com.mikhailgrigorev.simple_password.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.simple_password.ui.auth.auth.AuthActivity
import com.mikhailgrigorev.simple_password.ui.profile.ProfileViewModel
import javax.inject.Inject

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0
    private var pass2FA = 0
    private var encryptedPass = 0
    private var timeLimit = 0
    private var pins = 0
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        checkAnalyze()
        initViewModel()
        setHelloText()
        setObservers()
        setListeners()
        return view
    }

    override fun onResume() {
        super.onResume()
        setHelloText()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setHelloText()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun checkAnalyze() {
        if (!Utils.toggleManager.analyzeToggle.isEnabled()) {
            binding.tvTotalPointsText.visibility = View.GONE
            binding.tvTotalPoints.visibility = View.GONE
            binding.cvQualityCard.visibility = View.GONE
            binding.cvTotalPoints.visibility = View.GONE
            binding.cvSpecialInfo.visibility = View.GONE
            binding.cvEncrypted.visibility = View.GONE
        }
    }

    private fun setHelloText() {
        viewModel.userLogin.observe(viewLifecycleOwner) { login ->
            val name: String = getString(R.string.hi) + " " + login
            binding.tvUsernameText.text = name
        }

        val login = Utils.accountSharedPrefs.getLogin()
        val name: String = getString(R.string.hi) + " " + login

        binding.tvUsernameText.text = name
        binding.tvAvatarSymbol.text = Utils.accountSharedPrefs.getAvatarEmoji().toString()
    }

    private fun setObservers() {
        viewModel.getPasswordNumberWithQuality().first.observe(viewLifecycleOwner) {
            safePass = it
            setPasswordQualityText()
        }

        viewModel.getPasswordNumberWithQuality().second.observe(viewLifecycleOwner) {
            fixPass = it
            setPasswordQualityText()
        }

        viewModel.getPasswordNumberWithQuality().third.observe(viewLifecycleOwner) {
            unsafePass = it
            setPasswordQualityText()
        }

        viewModel.getItemsNumber().observe(viewLifecycleOwner) { passwordNumber ->
            binding.tvAllPasswords.text = passwordNumber.toString()
        }

        viewModel.getItemsNumberWith2fa().observe(viewLifecycleOwner) {
            pass2FA = it
            setPasswordQualityText()
        }

        viewModel.getItemsNumberWithEncrypted().observe(viewLifecycleOwner) {
            encryptedPass = it
            setPasswordQualityText()
        }

        viewModel.getItemsNumberWithTimeLimit().observe(viewLifecycleOwner) {
            timeLimit = it
            setPasswordQualityText()
        }

        viewModel.getPinItems().observe(viewLifecycleOwner) {
            pins = it
            setPasswordQualityText()
        }
    }

    private fun setPasswordQualityText() {
        binding.tvCorrectPasswords.text = resources.getQuantityString(
                R.plurals.correct_passwords,
                safePass,
                safePass
        )
        binding.tvNegativePasswords.text = resources.getQuantityString(
                R.plurals.incorrect_password,
                unsafePass,
                unsafePass
        )
        binding.tvNotSafePasswords.text = resources.getQuantityString(
                R.plurals.need_fix,
                fixPass,
                fixPass
        )
        binding.tvNumberOfUse2faText.text = pass2FA.toString()
        binding.tvNumberOfEncrypted.text = encryptedPass.toString()

        binding.tvNumberOfTimeNotificationText.text = timeLimit.toString()
        binding.tvPinText.text = pins.toString()

        if (safePass + fixPass + unsafePass > 0) {
            if (binding.tvAllPasswords.text.toString() != "0") {
                binding.tvTotalPoints.text = getString(
                        R.string.numericValue,
                        5 * ((safePass.toFloat() + fixPass.toFloat() / 2 + encryptedPass.toFloat() + pass2FA.toFloat()) / 3)
                                / ((safePass + unsafePass + fixPass).toFloat())
                )
            } else {
                binding.tvTotalPoints.text = "0"
            }
        }
    }

    private fun setListeners() {
        binding.ivAboutApp.setOnClickListener {
            findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToAboutFragment()
            )
        }

        binding.cvEditAccount.setOnClickListener {
            findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToProfileEditFragment())
        }

        binding.ivDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)

            builder.apply {
                setTitle(getString(R.string.accountDelete))
                setMessage(getString(R.string.accountDeleteConfirm))
                setPositiveButton(getString(R.string.yes)) { _, _ ->
                    Utils.makeToast(requireContext(), getString(R.string.accountDeleted))
                    deleteAccount()
                }
                setNegativeButton(getString(R.string.no)) { _, _ -> }
                setNeutralButton(getString(R.string.cancel)) { _, _ -> }
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun deleteAccount() {
        Utils.exitAccount()
        removeShortcuts()
    }

    private fun removeShortcuts() {
        val shortcutList = mutableListOf<ShortcutInfo>()

        val shortcutManager: ShortcutManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager =
                    requireContext().getSystemService(ShortcutManager::class.java)!!

            shortcutManager.dynamicShortcuts = shortcutList
        }

        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
    }

    private fun initViewModel() {
        viewModel = this.injectViewModel(viewModelFactory)
    }

}