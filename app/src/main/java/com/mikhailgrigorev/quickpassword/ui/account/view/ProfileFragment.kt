package com.mikhailgrigorev.quickpassword.ui.account.view

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
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.FragmentProfileBinding
import com.mikhailgrigorev.quickpassword.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.quickpassword.di.modules.RoomModule
import com.mikhailgrigorev.quickpassword.ui.account.AccountViewModel
import com.mikhailgrigorev.quickpassword.ui.auth.auth.AuthActivity
import com.mikhailgrigorev.quickpassword.ui.donut.DonutActivity
import javax.inject.Inject

class ProfileFragment : Fragment() {

    private lateinit var viewModel: AccountViewModel

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkAnalyze() {
        if (!Utils.useAnalyze()) {
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
            binding.tvAvatarSymbol.text = login[0].toString().uppercase()
        }
    }

    private fun setObservers() {
        viewModel.getPasswordNumberWithQuality().first.observe(viewLifecycleOwner) { safePass_ ->
            safePass = safePass_
            setPasswordQualityText()
        }

        viewModel.getPasswordNumberWithQuality().second.observe(viewLifecycleOwner) { notSafe_ ->
            fixPass = notSafe_
            setPasswordQualityText()
        }

        viewModel.getPasswordNumberWithQuality().third.observe(viewLifecycleOwner) { negative_ ->
            unsafePass = negative_
            setPasswordQualityText()
        }
        viewModel.getItemsNumber().observe(viewLifecycleOwner) { passwordNumber ->
            binding.tvAllPasswords.text = passwordNumber.toString()
        }

        viewModel.getItemsNumberWith2fa().observe(viewLifecycleOwner) { pass2FA_ ->
            pass2FA = pass2FA_
            setPasswordQualityText()
        }

        viewModel.getItemsNumberWithEncrypted().observe(viewLifecycleOwner) { encryptedPass_ ->
            encryptedPass = encryptedPass_
            setPasswordQualityText()
        }
        viewModel.getItemsNumberWithTimeLimit().observe(viewLifecycleOwner) { timeLimit_ ->
            timeLimit = timeLimit_
            setPasswordQualityText()
        }
        viewModel.getPinItems().observe(viewLifecycleOwner) { pins_ ->
            pins = pins_
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
                fixPass,
                fixPass
        )
        binding.tvNotSafePasswords.text = resources.getQuantityString(
                R.plurals.need_fix,
                unsafePass,
                unsafePass
        )
        binding.tvNumberOfUse2faText.text = pass2FA.toString()
        binding.tvNumberOfEncrypted.text = encryptedPass.toString()

        binding.tvNumberOfTimeNotificationText.text = timeLimit.toString()
        binding.tvPinText.text = pins.toString()

        if (safePass + fixPass + unsafePass > 0) {
            if (binding.tvAllPasswords.text.toString() != "0") {
                binding.tvTotalPoints.text = getString(
                        R.string.numericValue,
                        (safePass.toFloat() + fixPass.toFloat() / 2 + unsafePass.toFloat() * 0 + encryptedPass.toFloat() + pass2FA.toFloat())
                                / (7 / 3 * (safePass.toFloat() + unsafePass.toFloat() + fixPass.toFloat()))
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
        binding.cvAdditionalInfoCard.setOnClickListener {
            val intent = Intent(context!!, DonutActivity::class.java)
            startActivity(intent)
        }
        binding.ivLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(context!!, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.exit_account))
            builder.setMessage(getString(R.string.accountExitConfirm))
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                exit()
            }
            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.cvEditAccount.setOnClickListener {
            findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToProfileEditFragment())
        }

        binding.ivDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(context!!, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.accountDelete))
            builder.setMessage(getString(R.string.accountDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                Utils.makeToast(context!!, getString(R.string.accountDeleted))
                deleteAccount()
            }

            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun deleteAccount() {
        if (Utils.auth.currentUser != null) {
            Utils.auth.currentUser!!.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Utils.exitAccount()
                            removeShortcuts()
                        } else {
                            task.exception
                        }
                    }
        }
    }

    private fun exit() {
        Utils.exitAccount()
        Utils.auth.signOut()
        removeShortcuts()
    }

    private fun removeShortcuts() {
        val shortcutList = mutableListOf<ShortcutInfo>()

        val shortcutManager: ShortcutManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager =
                    context!!.getSystemService(ShortcutManager::class.java)!!

            shortcutManager.dynamicShortcuts = shortcutList
        }

        val intent = Intent(context!!, AuthActivity::class.java)
        startActivity(intent)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
                this,
                viewModelFactory
        )[AccountViewModel::class.java]
    }

}