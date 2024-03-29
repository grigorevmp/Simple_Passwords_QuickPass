package com.mikhailgrigorev.simple_password.ui.password

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Point
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.manager.PasswordManager
import com.mikhailgrigorev.simple_password.common.utils.PasswordGettingType
import com.mikhailgrigorev.simple_password.common.utils.PasswordQuality
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.data.dbo.FolderCard
import com.mikhailgrigorev.simple_password.data.dbo.PasswordCard
import com.mikhailgrigorev.simple_password.databinding.FragmentPasswordBinding
import com.mikhailgrigorev.simple_password.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.simple_password.di.modules.RoomModule
import com.mikhailgrigorev.simple_password.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.simple_password.ui.folder.FolderViewActivity
import com.mikhailgrigorev.simple_password.ui.main_activity.MainViewModel
import com.mikhailgrigorev.simple_password.ui.main_activity.adapters.FolderAdapter
import com.mikhailgrigorev.simple_password.ui.main_activity.adapters.PasswordAdapter
import com.mikhailgrigorev.simple_password.ui.password_card.create.PasswordCreateActivity
import com.mikhailgrigorev.simple_password.ui.password_card.view.PasswordViewActivity
import com.thebluealliance.spectrum.SpectrumPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

class PasswordFragment: Fragment() {
    private val defaultRotation = 0F
    private lateinit var viewModel: MainViewModel
    private var globalColor: String = ""

    private var passwordLength = 20
    private var useSymbols = false
    private var useUC = false
    private var useLetters = false
    private var useNumbers = false
    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0

    private var defaultPassFilterType = PasswordGettingType.All
    private var defaultPassFilterValue = 0
    private var defaultPassFilterName = ""
    private var defaultPassFilterSorting = "name"
    private var defaultPassFilterAsc = false

    private var searchCorrect: Boolean = false
    private var searchNegative: Boolean = false
    private var searchNotSafe: Boolean = false

    private var xTouch = 500
    private var changeStatusPopUp: PopupWindow = PopupWindow()
    private var changeFolderPopUp: PopupWindow = PopupWindow()
    private var globalPos: Int = -1
    private var globalFolderPos: Int = -1
    private lateinit var passwordCards: List<PasswordCard>
    private lateinit var folderCards: List<FolderCard>
    private var pm = PasswordManager()
    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setHelloText()
    }

    override fun onResume() {
        super.onResume()
        setHelloText()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        initViewModel()
        setHelloText()
        checkAnalytics()
        initLayouts()
        initSorting()
        setPasswordQualityText()
        setObservers()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) generateShortcuts()

        setSortingOptions()
        setListeners()
        initBottomSheetBehavior()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun initSorting() {
        defaultPassFilterSorting = Utils.sortingColumn()!!
        defaultPassFilterAsc = Utils.sortingAsc()
        setOrderChip(defaultPassFilterSorting)
    }

    private fun setHelloText() {
        viewModel.userLogin.observe(viewLifecycleOwner) { login ->
            val name: String = getString(R.string.hi) + " " + login
            binding.tvUsernameText.text = name
        }

        Utils.accountSharedPrefs.getLogin()?.let { viewModel.setLoginData(it) }
    }

    private fun setArrowOrderIndicator() {
        if (defaultPassFilterAsc)
            binding.ivSortOrder.animate().rotation(0F).setDuration(500).start()
        else
            binding.ivSortOrder.animate().rotation(180F).setDuration(500).start()
    }

    private fun setOrderChip(column: String) {
        defaultPassFilterAsc = if (defaultPassFilterSorting == column)
            !defaultPassFilterAsc
        else {
            true
        }

        defaultPassFilterSorting = column

        when (defaultPassFilterSorting) {
            "name" -> {
                binding.cDateSorting.isChecked = false
                binding.cAlphabeticSorting.isChecked = true
            }
            "time" -> {
                binding.cDateSorting.isChecked = true
                binding.cAlphabeticSorting.isChecked = false
            }
        }

        setArrowOrderIndicator()
        Utils.setSortingType(defaultPassFilterSorting)
        Utils.setSortingAsc(defaultPassFilterAsc)
    }

    private fun setSortingOptions() {
        binding.cAlphabeticSorting.setOnClickListener {
            setOrderChip("name")
            setObservers()
        }

        binding.cDateSorting.setOnClickListener {
            setOrderChip("time")
            setObservers()
        }
    }

    private fun checkAnalytics() {
        if (!Utils.toggleManager.analyzeToggle.isEnabled()) {
            binding.cvQualityCard.visibility = View.GONE
            binding.cvAdditionalInfoCard.visibility = View.GONE
            binding.cvAllPasswords.visibility = View.GONE
        }
    }

    private fun initLayouts() {
        binding.rvPasswordRecycler.setHasFixedSize(true)
        binding.rvPasswordRecycler.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
        )
        binding.rvFoldersRecycler.setHasFixedSize(true)
        binding.rvFoldersRecycler.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
        )
    }

    private fun setPasswordQualityText() {
        binding.tvCorrectPasswords.text =
                resources.getQuantityString(
                        R.plurals.correct_passwords,
                        safePass,
                        safePass
                )
        binding.tvNegativePasswords.text =
                resources.getQuantityString(
                        R.plurals.incorrect_password,
                        fixPass,
                        fixPass
                )
        binding.tvNotSafePasswords.text =
                resources.getQuantityString(
                        R.plurals.need_fix,
                        unsafePass,
                        unsafePass
                )
    }

    private fun setObservers(
        type: PasswordGettingType = defaultPassFilterType,
        name: String = defaultPassFilterName,
        value: Int = defaultPassFilterValue,
        sortColumn: String = defaultPassFilterSorting,
        isAsc: Boolean = defaultPassFilterAsc
    ) {
        defaultPassFilterType = type
        defaultPassFilterValue = value
        defaultPassFilterName = name
        defaultPassFilterSorting = sortColumn
        defaultPassFilterAsc = isAsc

        viewModel.folders.observe(viewLifecycleOwner) {
            folderCards = it
            setFolderAdapter()
        }

        viewModel.getPasswords(
                type,
                name,
                value,
                sortColumn,
                isAsc
        ).observe(viewLifecycleOwner) { passwords ->
            passwordCards = passwords
            if (passwords.isEmpty()) {
                if (defaultPassFilterType != PasswordGettingType.ByName)
                    showNoPasswordsInterface()
                setPasswordAdapter(passwords)
            } else {
                showPasswordsInterface()
                setPasswordAdapter(passwords)
            }
        }

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

        viewModel.getItemsNumberWith2fa().observe(viewLifecycleOwner) { passwordNumber ->
            binding.tvNumberOfUse2faText.text = passwordNumber.toString()
        }

        viewModel.getItemsNumberWithEncrypted()
                .observe(viewLifecycleOwner) { encryptedPasswordNumber ->
                    binding.tvNumberOfEncryptedText.text = encryptedPasswordNumber.toString()
                }
    }

    private fun initBottomSheetBehavior() {
        binding.llAllPasswords.translationZ = 24F
        binding.fabNewPassword.translationZ = 101F

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.llAllPasswords)

        try {
            bottomSheetBehavior.state = Utils.bottomBarState()
        } catch (e: Exception) {
            e.message?.let { Log.d("bottomSheetBehavior", it) }
        }

        binding.ivExpandBottomDialog.animate().rotation(180F * bottomSheetBehavior.state)
                .setDuration(0).start()
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            binding.fabNewPassword.animate().scaleX(0F).scaleY(0F).setDuration(0).start()
            binding.cvWarningRulesCard.animate().alpha(1F).setDuration(0).start()
            binding.cvBackupReminderCard.animate().alpha(1F).setDuration(0).start()
            binding.cvAllPasswords.animate().alpha(1F).setDuration(0).start()
        }

        binding.etSearchPassword.clearFocus()
        binding.etSearchPassword.hideKeyboard()

        bottomSheetBehavior.peekHeight = 800

        if (!Utils.toggleManager.analyzeToggle.isEnabled()) {
            bottomSheetBehavior.peekHeight = 1200
        }

        bottomSheetBehavior.isHideable = true

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState != BottomSheetBehavior.STATE_HIDDEN)
                    Utils.setBottomBarState(newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.ivExpandBottomDialog.animate().rotation(180F * slideOffset).setDuration(0)
                        .start()
                if (slideOffset <= 0) {
                    binding.cvWarningRulesCard.animate().alpha(abs(slideOffset) + 0.5F)
                            .setDuration(0).start()
                    binding.cvAllPasswords.animate().alpha(abs(slideOffset) + 0.5F).setDuration(0)
                            .start()
                    binding.cvAdditionalInfoCard.animate().alpha(abs(slideOffset) + 0.5F)
                            .setDuration(0).start()
                    binding.cvPasswordGenerateButton.animate().alpha(abs(slideOffset) + 0.5F)
                            .setDuration(0).start()
                    binding.cvBackupReminderCard.animate().alpha(abs(slideOffset) + 0.5F)
                            .setDuration(0)
                            .start()
                    binding.fabNewPassword.animate().scaleX(1 - abs(slideOffset))
                            .scaleY(1 - abs(slideOffset))
                            .setDuration(
                                    0
                            ).start()
                }
                binding.etSearchPassword.clearFocus()
                binding.etSearchPassword.hideKeyboard()

            }
        })
    }

    private fun setPasswordGeneratorListeners() {
        binding.cLengthToggle.text = getString(R.string.length, passwordLength)

        binding.cLengthToggle.setOnClickListener {
            if (binding.seekBar.visibility == View.GONE) {
                binding.seekBar.visibility = View.VISIBLE
            } else {
                binding.seekBar.visibility = View.GONE
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                passwordLength = i
                binding.cLengthToggle.text = getString(R.string.length, passwordLength)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val passwordGeneratorRules = mutableListOf<String>()
        for (index in 0 until binding.cgPasswordSettings.childCount) {
            val chip: Chip = binding.cgPasswordSettings.getChildAt(index) as Chip

            chip.setOnCheckedChangeListener { view, isChecked ->
                val deg = binding.generatePassword.rotation + 30f
                binding.generatePassword.animate().rotation(deg).interpolator =
                        AccelerateDecelerateInterpolator()

                when (view.id) {
                    R.id.cLettersToggle -> useLetters = isChecked
                    R.id.cSymToggles -> useSymbols = isChecked
                    R.id.cNumbersToggle -> useNumbers = isChecked
                    R.id.cUpperCaseToggle -> useUC = isChecked
                }

                when (view.id) {
                    R.id.cLengthToggle -> {
                        if (isChecked)
                            passwordGeneratorRules.add("length")
                        else
                            passwordGeneratorRules.remove("length")
                    }
                    else -> {
                        if (isChecked)
                            passwordGeneratorRules.add(view.text.toString())
                        else
                            passwordGeneratorRules.remove(view.text.toString())
                    }
                }
            }
        }

        binding.generatePassword.setOnClickListener {
            if (passwordGeneratorRules.size == 0 || (passwordGeneratorRules.size == 1 && binding.cLengthToggle.isChecked)) {
                binding.tilPasswordToGenerate.error = getString(R.string.noRules)
            } else {
                binding.tilPasswordToGenerate.error = null
                val newPassword: String =
                        pm.generatePassword(
                                useLetters,
                                useUC,
                                useNumbers,
                                useSymbols,
                                passwordLength
                        )
                binding.tePasswordToGenerate.setText(newPassword)
            }
            binding.generatePassword.animate().rotation(defaultRotation).interpolator =
                    AccelerateDecelerateInterpolator()
        }
    }

    private fun setPasswordSearchListener() {
        binding.etSearchPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(passwordName: Editable?) {
                if (passwordName.toString() != "")
                    setObservers(
                            type = PasswordGettingType.ByName,
                            name = passwordName.toString()
                    )
                else
                    setObservers(
                            type = PasswordGettingType.All
                    )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun bottomBarBehaviorListeners() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.llAllPasswords)
        binding.fabExpandButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Utils.setBottomBarState(BottomSheetBehavior.STATE_COLLAPSED)
        }

        binding.ivExpandBottomDialog.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Utils.setBottomBarState(bottomSheetBehavior.state)
        }
    }

    private fun setListeners() {

        setPasswordSearchListener()
        setPasswordGeneratorListeners()
        bottomBarBehaviorListeners()

        binding.fabAddFolder.setOnClickListener {
            val customAlertDialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_add_folder, null, false)
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
            customAlertDialogView.findViewById<SpectrumPalette>(R.id.spPalette)
                    .setOnColorSelectedListener { color ->
                        globalColor = "#${Integer.toHexString(color).uppercase(Locale.getDefault())}"
                    }
            materialAlertDialogBuilder.setView(customAlertDialogView)
            materialAlertDialogBuilder
                    .setView(customAlertDialogView)
                    .setTitle(getString(R.string.folder_creation))
                    .setMessage(getString(R.string.create_folder_config))
                    .setPositiveButton("Ok") { dialog, _ ->
                        val name =
                                customAlertDialogView.findViewById<TextInputEditText>(
                                        R.id.etFolderName
                                ).text.toString()
                        val description =
                                customAlertDialogView.findViewById<TextInputEditText>(
                                        R.id.etFolderDesc
                                ).text.toString()
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.insertCard(
                                    FolderCard(
                                            name = name,
                                            description = description,
                                            colorTag = globalColor
                                    )
                            )
                        }

                        dialog.dismiss()

                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()

        }

        binding.tilPasswordToGenerate.setOnClickListener {
            copyPassword()
        }

        binding.tePasswordToGenerate.setOnClickListener {
            copyPassword()
        }

        binding.cvNoPasswordsCard.setOnClickListener {
            goToNewPasswordActivity()
        }

        binding.fabAddNewPass.setOnClickListener {
            goToNewPasswordActivity()
        }


        binding.fabNewPassword.setOnClickListener {
            goToNewPasswordActivity()
        }

        // Correct passwords

        binding.ivCorrectPasswordsCircle.setOnClickListener {
            correctPasswordsClickedAction()
        }

        binding.tvCorrectPasswords.setOnClickListener {
            correctPasswordsClickedAction()
        }

        // Negative password

        binding.ivNegativePasswordsCircle.setOnClickListener {
            negativePasswordsClickedAction()
        }

        binding.tvNegativePasswords.setOnClickListener {
            negativePasswordsClickedAction()
        }

        // Not safe password

        binding.ivNotSafePasswordsCircle.setOnClickListener {
            notSafePasswordsClickedAction()
        }

        binding.tvNotSafePasswords.setOnClickListener {
            notSafePasswordsClickedAction()
        }
    }

    private fun setPasswordAdapter(passwords: List<PasswordCard>) {
        binding.rvPasswordRecycler.adapter = PasswordAdapter(
                passwords,
                requireContext(),
                clickListener = { passClickListener(it) },
                longClickListener = { i: Int, view: View -> passLongClickListener(i, view) }
        ) {
            tagSearchClicker(it)
        }
    }

    private fun setFolderAdapter() {
        binding.rvFoldersRecycler.adapter = FolderAdapter(
                folderCards,
                requireContext(),
                clickListener = { folderClickListener(it) }
        ) { i: Int, view: View -> folderLongClickListener(i, view) }
    }

    private fun updatePasswordQualityCirclesColor(
        circleNegative: Int,
        circleImprovement: Int,
        circlePositive: Int
    ) {
        binding.ivNegativePasswordsCircle.setImageResource(circleNegative)
        binding.ivNotSafePasswordsCircle.setImageResource(circleImprovement)
        binding.ivCorrectPasswordsCircle.setImageResource(circlePositive)
    }

    private fun copyPassword() {
        if (binding.tePasswordToGenerate.text.toString() != "") {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", binding.tePasswordToGenerate.text.toString())
            clipboard.setPrimaryClip(clip)
            Utils.makeToast(requireContext(), getString(R.string.passCopied))
        }
    }

    private fun goToNewPasswordActivity() {
        val intent = Intent(requireContext(), PasswordCreateActivity::class.java)
        intent.putExtra("pass", binding.tePasswordToGenerate.text.toString())
        intent.putExtra("useLetters", useLetters)
        intent.putExtra("useUC", useUC)
        intent.putExtra("useNumbers", useNumbers)
        intent.putExtra("useSymbols", useSymbols)
        intent.putExtra("length", passwordLength)
        startActivity(intent)
    }

    private fun notSafePasswordsClickedAction() {
        if (searchNotSafe) {
            binding.ivNotSafePasswordsCircle.setImageResource(R.drawable.circle_yellow)

            setObservers(
                    type = PasswordGettingType.All,
                    value = defaultPassFilterValue,
                    name = defaultPassFilterName,
                    sortColumn = defaultPassFilterSorting,
                    isAsc = defaultPassFilterAsc
            )

            searchNotSafe = false
        } else {
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_red,
                    circleImprovement = R.drawable.circle_yellow_fill,
                    circlePositive = R.drawable.circle_green
            )

            setObservers(
                    type = PasswordGettingType.ByQuality,
                    value = PasswordQuality.MEDIUM.value
            )

            searchNegative = false
            searchCorrect = false
            searchNotSafe = true
        }
    }

    private fun negativePasswordsClickedAction() {
        if (searchNegative) {
            binding.ivNegativePasswordsCircle.setImageResource(R.drawable.circle_red)
            setObservers(
                    type = PasswordGettingType.All,
                    value = defaultPassFilterValue,
                    name = defaultPassFilterName,
                    sortColumn = defaultPassFilterSorting,
                    isAsc = defaultPassFilterAsc
            )
            searchNegative = false
        } else {
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_red_fill,
                    circleImprovement = R.drawable.circle_yellow,
                    circlePositive = R.drawable.circle_green
            )
            setObservers(
                    type = PasswordGettingType.ByQuality,
                    value = PasswordQuality.LOW.value
            )
            searchNotSafe = false
            searchCorrect = false
            searchNegative = true
        }
    }

    private fun correctPasswordsClickedAction() {
        if (searchCorrect) {
            binding.ivCorrectPasswordsCircle.setImageResource(R.drawable.circle_green)
            setObservers(
                    type = PasswordGettingType.All,
                    value = defaultPassFilterValue,
                    name = defaultPassFilterName,
                    sortColumn = defaultPassFilterSorting,
                    isAsc = defaultPassFilterAsc
            )
            searchCorrect = false
        } else {
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_red,
                    circleImprovement = R.drawable.circle_yellow,
                    circlePositive = R.drawable.circle_green_fill
            )
            setObservers(
                    type = PasswordGettingType.ByQuality,
                    value = PasswordQuality.HIGH.value
            )
            searchNotSafe = false
            searchNegative = false
            searchCorrect = true
        }
    }

    private fun showNoPasswordsInterface() {
        binding.fabNewPassword.animate().scaleX(0F).scaleY(0F).setDuration(0).start()
        binding.cvWarningRulesCard.animate().alpha(1F).setDuration(0).start()
        binding.cvBackupReminderCard.animate().alpha(1F).setDuration(0).start()
        binding.cvPasswordGenerateButton.animate().alpha(1F).setDuration(0).start()
        binding.cvAllPasswords.visibility = View.GONE
        binding.cvAdditionalInfoCard.visibility = View.GONE
        binding.cvNoPasswordsCard.visibility = View.VISIBLE
        binding.fabAddNewPass.visibility = View.VISIBLE
        binding.ivSmilePasswordCreation.visibility = View.GONE
        binding.fabExpandButton.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.llAllPasswords)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN


    }

    private fun showPasswordsInterface() {
        binding.cvAllPasswords.visibility = View.VISIBLE
        binding.cvAdditionalInfoCard.visibility = View.VISIBLE
        binding.cvNoPasswordsCard.visibility = View.GONE
        binding.ivSmilePasswordCreation.visibility = View.VISIBLE
        binding.ivSmilePasswordCreation.animate().alpha(0.2F).setDuration(10).start()
        binding.fabExpandButton.visibility = View.VISIBLE
        binding.fabAddNewPass.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.llAllPasswords)
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun createIntentForShortcut(passwordId: Int): Intent {
        val intent = Intent(requireContext(), PasswordViewActivity::class.java)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra("password_id", passwordId)
        intent.putExtra("openedFrom", "shortcut")

        return intent
    }

    private fun createShortcut(passwordId: Int, passwordName: String): ShortcutInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val intentForShortcut = createIntentForShortcut(passwordId)
            return ShortcutInfo.Builder(requireContext(), "shortcut_ $passwordId")
                    .setShortLabel(passwordName)
                    .setLongLabel(passwordName)
                    .setIcon(Icon.createWithResource(requireContext(), R.drawable.ic_fav_action))
                    .setIntent(intentForShortcut)
                    .build()
        }
        return null
    }

    private fun generateShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            viewModel.getFavoriteItems().observe(viewLifecycleOwner) { passwords ->
                val shortcutList = mutableListOf<ShortcutInfo>()
                val shortcutManager: ShortcutManager =
                        requireContext().getSystemService(ShortcutManager::class.java)!!
                for (i in (0..min(2, passwords.size - 1))) {
                    shortcutList.add(createShortcut(passwords[i]._id, passwords[i].name)!!)
                }
                shortcutManager.dynamicShortcuts = shortcutList
            }
        }
    }

    private fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun tagSearchClicker(name: String) {
        binding.etSearchPassword.setText(name)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun passLongClickListener(position: Int, view: View) {
        view.setOnTouchListener { _, event ->
            xTouch = event.x.toInt()
            false
        }
        showPopup(position, view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun folderLongClickListener(position: Int, view: View) {
        view.setOnTouchListener { _, event ->
            xTouch = event.x.toInt()
            false
        }
        showFolderEditPopup(position, view)
    }

    @SuppressLint("InflateParams")
    private fun showFolderEditPopup(position: Int, view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val point = Point()
        point.x = location[0]
        point.y = location[1]
        val layoutInflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.item_folder_card_popup, null)

        globalFolderPos = position.toString().toInt()
        changeFolderPopUp = PopupWindow(requireContext())
        changeFolderPopUp.contentView = layout
        changeFolderPopUp.width = LinearLayout.LayoutParams.WRAP_CONTENT
        changeFolderPopUp.height = LinearLayout.LayoutParams.WRAP_CONTENT
        changeFolderPopUp.isFocusable = true
        val offsetX = -50
        val offsetY = 0
        changeFolderPopUp.setBackgroundDrawable(null)
        changeFolderPopUp.animationStyle = R.style.popUpAnim
        changeFolderPopUp.showAtLocation(
                layout,
                Gravity.NO_GRAVITY,
                offsetX + xTouch,
                point.y + offsetY
        )
    }

    @SuppressLint("InflateParams")
    private fun showPopup(position: Int, view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val point = Point()
        point.x = location[0]
        point.y = location[1]
        val layoutInflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.item_password_card_popup, null)

        globalPos = position.toString().toInt()
        changeStatusPopUp = PopupWindow(requireContext())
        changeStatusPopUp.contentView = layout
        changeStatusPopUp.width = LinearLayout.LayoutParams.WRAP_CONTENT
        changeStatusPopUp.height = LinearLayout.LayoutParams.WRAP_CONTENT
        changeStatusPopUp.isFocusable = true
        val offsetX = -50
        val offsetY = 0
        changeStatusPopUp.setBackgroundDrawable(null)
        changeStatusPopUp.animationStyle = R.style.popUpAnim
        changeStatusPopUp.showAtLocation(
                layout,
                Gravity.NO_GRAVITY,
                offsetX + xTouch,
                point.y + offsetY
        )
    }

    private fun passClickListener(position: Int) {
        val intent = Intent(requireContext(), PasswordViewActivity::class.java)
        intent.putExtra("password_id", passwordCards[position]._id)
        startActivity(intent)
    }

    private fun folderClickListener(position: Int) {
        val intent = Intent(requireContext(), FolderViewActivity::class.java)
        intent.putExtra("folder_id", folderCards[position]._id)
        intent.putExtra("folder_name", folderCards[position].name)
        startActivity(intent)
    }

    private fun initViewModel() {
        viewModel = this.injectViewModel(viewModelFactory)
    }



    fun favorite(view: View) {
        Log.d("favorite", view.id.toString())
        val position = globalPos

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.favPassword(passwordCards[position])
        }

        setObservers()
        changeStatusPopUp.dismiss()
    }

    fun editFolder(view: View) {
        val position = globalFolderPos
        val folder = folderCards[position]
        changeFolderPopUp.dismiss()

        val customAlertDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_folder, null, false)

        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        customAlertDialogView.findViewById<SpectrumPalette>(R.id.spPalette)
                .setOnColorSelectedListener { color ->
                    globalColor = "#${Integer.toHexString(color).uppercase(Locale.getDefault())}"
                }

        materialAlertDialogBuilder.setView(customAlertDialogView)

        customAlertDialogView.findViewById<TextInputEditText>(R.id.etFolderName).setText(folder.name)
        customAlertDialogView.findViewById<TextInputEditText>(R.id.etFolderDesc).setText(folder.description)

        materialAlertDialogBuilder
                .setView(customAlertDialogView)
                .setTitle("Folder editor")
                .setMessage("Current configuration details")
                .setPositiveButton("Ok") { dialog, _ ->
                    folder.name = customAlertDialogView.findViewById<TextInputEditText>(R.id.etFolderName).text.toString()
                    folder.description = customAlertDialogView.findViewById<TextInputEditText>(R.id.etFolderDesc).text.toString()
                    folder.colorTag = globalColor

                    lifecycleScope.launch(Dispatchers.IO) { viewModel.updateCard(folder) }

                    dialog.dismiss()

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
    }

    fun deleteFolder(view: View) {
        val position = globalFolderPos
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteCard(folderCards[position])
        }
        changeFolderPopUp.dismiss()
    }

    fun delete(view: View) {
        val position = globalPos

        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
        builder.setTitle(getString(R.string.deletePassword))
        builder.setMessage(getString(R.string.passwordDeleteConfirm))

        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteItem(passwordCards[position])
            }

            setObservers()
        }

        builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
        builder.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        val dialog: AlertDialog = builder.create()
        dialog.show()
        changeStatusPopUp.dismiss()
    }
}