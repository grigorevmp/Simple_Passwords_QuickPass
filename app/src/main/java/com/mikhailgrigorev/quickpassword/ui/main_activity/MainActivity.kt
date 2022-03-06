package com.mikhailgrigorev.quickpassword.ui.main_activity

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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.PasswordGettingType
import com.mikhailgrigorev.quickpassword.common.manager.PasswordManager
import com.mikhailgrigorev.quickpassword.common.PasswordQuality
import com.mikhailgrigorev.quickpassword.common.base.MyBaseActivity
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.dbo.FolderCard
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.ActivityMainBinding
import com.mikhailgrigorev.quickpassword.ui.account.view.AccountViewActivity
import com.mikhailgrigorev.quickpassword.ui.folder.FolderViewActivity
import com.mikhailgrigorev.quickpassword.ui.main_activity.adapters.FolderAdapter
import com.mikhailgrigorev.quickpassword.ui.main_activity.adapters.PasswordAdapter
import com.mikhailgrigorev.quickpassword.ui.password_card.create.PasswordCreateActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.view.PasswordViewActivity
import com.thebluealliance.spectrum.SpectrumPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs
import kotlin.math.min


class MainActivity : MyBaseActivity() {

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
    private lateinit var login: String

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
    private var condition = true

    private lateinit var binding: ActivityMainBinding

    private fun initSorting(){
        defaultPassFilterSorting = Utils.sortingColumn()!!
        defaultPassFilterAsc = Utils.sortingAsc()
        setOrderChip(defaultPassFilterSorting)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        authorization()
        checkAnalytics()
        initLayouts()
        initSorting()
        setPasswordQualityText()
        setObservers()

        // Shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            generateShortcuts()

        setSortingOptions()
        setListeners()
        initBottomSheetBehavior()

    }

    private fun authorization() {
        viewModel.userLogin.observe(this) { login ->
            val name: String = getString(R.string.hi) + " " + login
            binding.tvUsernameText.text = name
            binding.tvAvatarSymbol.text = login[0].toString()
        }
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
        else{
            true
        }

        defaultPassFilterSorting = column

        when(defaultPassFilterSorting){
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

    private fun setSortingOptions(){
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
            if (!Utils.useAnalyze()) {
                binding.cvQualityCard.visibility = View.GONE
                binding.cvAdditionalInfoCard.visibility = View.GONE
                binding.cardView.visibility = View.GONE
            }
    }

    private fun initLayouts() {
        binding.rvPasswordRecycler.setHasFixedSize(true)
        binding.rvPasswordRecycler.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        binding.rvFoldersRecycler.setHasFixedSize(true)
        binding.rvFoldersRecycler.layoutManager = LinearLayoutManager(
                this,
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
                        unsafePass,
                        unsafePass
                )
        binding.tvNotSafePasswords.text =
                resources.getQuantityString(
                        R.plurals.need_fix,
                        fixPass,
                        fixPass
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

        viewModel.folders.observe(this) {
            folderCards = it
            setFolderAdapter()
        }

        viewModel.getPasswords(
                type,
                name,
                value,
                sortColumn,
                isAsc
        ).observe(this) { passwords ->
            passwordCards = passwords
            if (passwords.isEmpty()) {
                if (defaultPassFilterType != PasswordGettingType.ByName)
                    showNoPasswordsInterface()
                setPasswordAdapter(passwords)
            }
            else {
                showPasswordsInterface()
                setPasswordAdapter(passwords)
            }
        }

        viewModel.getPasswordNumberWithQuality().first.observe(this) { safePass_ ->
            safePass = safePass_
            setPasswordQualityText()
        }

        viewModel.getPasswordNumberWithQuality().second.observe(this) { notSafe_ ->
            fixPass = notSafe_
            setPasswordQualityText()
        }

        viewModel.getPasswordNumberWithQuality().third.observe(this) { negative_ ->
            unsafePass = negative_
            setPasswordQualityText()
        }

        viewModel.getItemsNumber().observe(this){ passwordNumber ->
            binding.tvAllPasswords.text = passwordNumber.toString()
        }

        viewModel.getItemsNumberWith2fa().observe(this){ passwordNumber ->
            binding.tvNumberOfUse2faText.text = passwordNumber.toString()
        }

        viewModel.getItemsNumberWithEncrypted().observe(this){ encryptedPasswordNumber ->
            binding.tvNumberOfEncryptedText.text = encryptedPasswordNumber.toString()
        }
    }

    private fun initBottomSheetBehavior() {
        binding.llAllPasswords.translationZ = 24F
        binding.fabNewPassword.translationZ = 101F

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.llAllPasswords)

        bottomSheetBehavior.state = Utils.bottomBarState()
        binding.ivExpandBottomDialog.animate().rotation(180F * bottomSheetBehavior.state).setDuration(0).start()
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            binding.fabNewPassword.animate().scaleX(0F).scaleY(0F).setDuration(0).start()
            binding.cvWarningRulesCard.animate().alpha(1F).setDuration(0).start()
            binding.cvBackupReminderCard.animate().alpha(1F).setDuration(0).start()
        }

        binding.etSearchPassword.clearFocus()
        binding.etSearchPassword.hideKeyboard()

        bottomSheetBehavior.peekHeight = 800 //600

        if (!Utils.useAnalyze()) {
            bottomSheetBehavior.peekHeight = 1200
        }

        bottomSheetBehavior.isHideable = true

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState != BottomSheetBehavior.STATE_HIDDEN)
                    Utils.setBottomBarState(newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.ivExpandBottomDialog.animate().rotation(180F * slideOffset).setDuration(0).start()
                if (slideOffset <= 0) {
                    binding.cvWarningRulesCard.animate().alpha(abs(slideOffset) + 0.5F).setDuration(0).start()
                    binding.cvBackupReminderCard.animate().alpha(abs(slideOffset) + 0.5F).setDuration(0)
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

        // Password generation system
        val passwordGeneratorRules = mutableListOf<String>()
        // Loop through the chips
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

                if (isChecked)
                    passwordGeneratorRules.add(view.text.toString())
                else
                    passwordGeneratorRules.remove(view.text.toString())
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
            val customAlertDialogView = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_add_folder, null, false)
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            customAlertDialogView.findViewById<SpectrumPalette>(R.id.spPalette)
                    .setOnColorSelectedListener { it_ ->
                        globalColor = "#${Integer.toHexString(it_).uppercase(Locale.getDefault())}"
                    }
            materialAlertDialogBuilder.setView(customAlertDialogView)
            materialAlertDialogBuilder
                    .setView(customAlertDialogView)
                    .setTitle("Folder creation")
                    .setMessage("Current configuration details")
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
            condition = false
            goToNewPasswordActivity()
        }

        binding.fabAddNewPass.setOnClickListener {
            condition = false
            goToNewPasswordActivity()
        }


        binding.fabNewPassword.setOnClickListener {
            condition = false
            goToNewPasswordActivity()
        }

        binding.cvAccountAvatar.setOnClickListener {
            condition = false
            goToAccountActivity()
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
                this,
                clickListener = {
                    passClickListener(it)
                },
                longClickListener = { i: Int, view: View ->
                    passLongClickListener(
                            i,
                            view
                    )
                }
        ) {
            tagSearchClicker(it)
        }
    }

    private fun setFolderAdapter() {
        binding.rvFoldersRecycler.adapter = FolderAdapter(
                folderCards,
                this,
                clickListener = {
                    folderClickListener(it)
                }
        ) { i: Int, view: View ->
            folderLongClickListener(
                    i,
                    view
            )
        }
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
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip =
                    ClipData.newPlainText("Password", binding.tePasswordToGenerate.text.toString())
            clipboard.setPrimaryClip(clip)
            Utils.makeToast(this, getString(R.string.passCopied))
        }
    }

    private fun goToNewPasswordActivity() {
        val intent = Intent(this, PasswordCreateActivity::class.java)
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
            binding.ivNotSafePasswordsCircle.setImageResource(R.drawable.circle_improvement)
            setObservers(
                    type = PasswordGettingType.All,
                    value = defaultPassFilterValue,
                    name = defaultPassFilterName,
                    sortColumn = defaultPassFilterSorting,
                    isAsc = defaultPassFilterAsc)
            searchNotSafe = false
        } else {
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_negative,
                    circleImprovement = R.drawable.circle_improvement_fill,
                    circlePositive = R.drawable.circle_positive
            )

            setObservers(
                    type = PasswordGettingType.ByQuality,
                    value = PasswordQuality.LOW.value
            )
            searchNegative = false
            searchCorrect = false
            searchNotSafe = true
        }
    }

    private fun negativePasswordsClickedAction() {
        if (searchNegative) {
            binding.ivNegativePasswordsCircle.setImageResource(R.drawable.circle_negative)
            setObservers(
                    type = PasswordGettingType.All,
                    value = defaultPassFilterValue,
                    name = defaultPassFilterName,
                    sortColumn = defaultPassFilterSorting,
                    isAsc = defaultPassFilterAsc)
            searchNegative = false
        } else {
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_negative_fill,
                    circleImprovement = R.drawable.circle_improvement,
                    circlePositive = R.drawable.circle_positive
            )
            setObservers(
                    type = PasswordGettingType.ByQuality,
                    value = PasswordQuality.MEDIUM.value
            )
            searchNotSafe = false
            searchCorrect = false
            searchNegative = true
        }
    }

    private fun correctPasswordsClickedAction() {
        if (searchCorrect) {
            binding.ivCorrectPasswordsCircle.setImageResource(R.drawable.circle_positive)
            setObservers(
                    type = PasswordGettingType.All,
                    value = defaultPassFilterValue,
                    name = defaultPassFilterName,
                    sortColumn = defaultPassFilterSorting,
                    isAsc = defaultPassFilterAsc)
            searchCorrect = false
        } else {
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_negative,
                    circleImprovement = R.drawable.circle_improvement,
                    circlePositive = R.drawable.circle_positive_fill
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

    private fun goToAccountActivity() {
        val intent = Intent(this, AccountViewActivity::class.java)
        startActivity(intent)
    }

    private fun showNoPasswordsInterface() {
        binding.cvNoPasswordsCard.visibility = View.VISIBLE
        binding.fabAddNewPass.visibility = View.VISIBLE
        binding.cardView.visibility = View.GONE
        binding.cvAdditionalInfoCard.visibility = View.GONE
        binding.ivSmilePasswordCreation.visibility = View.GONE
        binding.fabExpandButton.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.llAllPasswords)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showPasswordsInterface() {
        binding.cvNoPasswordsCard.visibility = View.GONE
        binding.cardView.visibility = View.VISIBLE
        binding.cvAdditionalInfoCard.visibility = View.VISIBLE
        binding.ivSmilePasswordCreation.visibility = View.VISIBLE
        binding.ivSmilePasswordCreation.animate().alpha(0.2F).setDuration(10).start()
        binding.fabExpandButton.visibility = View.VISIBLE
        binding.fabAddNewPass.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.llAllPasswords)
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun createIntentForShortcut(passwordId: Int): Intent {
        val intent = Intent(this, PasswordViewActivity::class.java)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra("login", login)
        intent.putExtra("password_id", passwordId)
        intent.putExtra("openedFrom", "shortcut")

        return intent
    }

    private fun createShortcut(id: Int, passwordId: Int, passwordName: String): ShortcutInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val intentForShortcut = createIntentForShortcut(id)
            return ShortcutInfo.Builder(this, "shortcut_ $passwordId")
                    .setShortLabel(passwordName)
                    .setLongLabel(passwordName)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_fav_action))
                    .setIntent(intentForShortcut)
                    .build()
        }
        return null
    }

    private fun generateShortcuts() {
        val shortcutList = mutableListOf<ShortcutInfo>()

        val shortcutManager: ShortcutManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager =
                    getSystemService(ShortcutManager::class.java)!!

            viewModel.getFavoriteItems().observe(this) { passwords ->
                for (i in (0..min(2, passwords.size - 1))) {
                    shortcutList.add(createShortcut(i, passwords[i]._id, passwords[i].name)!!)
                }
            }

            shortcutManager.dynamicShortcuts = shortcutList
        }
    }

    fun View.hideKeyboard() {
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

    private fun showFolderEditPopup(position: Int, view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val point = Point()
        point.x = location[0]
        point.y = location[1]
        val layoutInflater =
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.item_folder_card_popup, null)

        globalFolderPos = position.toString().toInt()
        changeFolderPopUp = PopupWindow(this)
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

    private fun showPopup(position: Int, view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val point = Point()
        point.x = location[0]
        point.y = location[1]
        val layoutInflater =
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.item_password_card_popup, null)

        globalPos = position.toString().toInt()
        changeStatusPopUp = PopupWindow(this)
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
        condition = false
        val intent = Intent(this, PasswordViewActivity::class.java)
        intent.putExtra("password_id", passwordCards[position]._id)
        startActivity(intent)
    }

    private fun folderClickListener(position: Int) {
        val intent = Intent(this, FolderViewActivity::class.java)
        intent.putExtra("folder_id", folderCards[position]._id)
        intent.putExtra("folder_name", folderCards[position].name)
        startActivity(intent)
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

        val customAlertDialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_folder, null, false)
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        customAlertDialogView.findViewById<SpectrumPalette>(R.id.spPalette)
                .setOnColorSelectedListener { it_ ->
                    globalColor = "#${Integer.toHexString(it_).uppercase(Locale.getDefault())}"
                }
        materialAlertDialogBuilder.setView(customAlertDialogView)
        customAlertDialogView.findViewById<TextInputEditText>(
                R.id.etFolderName
        )
                .setText(
                        folder.name
                )
        customAlertDialogView.findViewById<TextInputEditText>(
                R.id.etFolderDesc
        )
                .setText(
                        folder.description
                )
        materialAlertDialogBuilder
                .setView(customAlertDialogView)
                .setTitle("Folder editor")
                .setMessage("Current configuration details")
                .setPositiveButton("Ok") { dialog, _ ->
                    folder.name =
                            customAlertDialogView.findViewById<TextInputEditText>(
                                    R.id.etFolderName
                            ).text.toString()
                    folder.description =
                            customAlertDialogView.findViewById<TextInputEditText>(
                                    R.id.etFolderDesc
                            ).text.toString()
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.updateCard(
                                folder
                        )
                    }

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

        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle(getString(R.string.deletePassword))
        builder.setMessage(getString(R.string.passwordDeleteConfirm))

        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            viewModel.deleteItem(passwordCards[position])

            setObservers()
        }

        builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
        builder.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        val dialog: AlertDialog = builder.create()
        dialog.show()
        changeStatusPopUp.dismiss()
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val llBottomSheet = binding.llAllPasswords
                val bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    finish()
                else
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        return false
    }
/*
    override fun onResume() {
        super.onResume()
        if (binding.cLengthToggle.isChecked)
            binding.seekBar.visibility = View.VISIBLE
    }*/

    private fun initViewModel() {
        viewModel = ViewModelProvider(
                this,
                MainViewModelFactory()
        )[MainViewModel::class.java]
    }

}