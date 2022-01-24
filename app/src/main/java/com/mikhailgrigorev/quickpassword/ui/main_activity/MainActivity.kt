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
import android.os.Handler
import android.os.Looper
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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.PasswordCategory
import com.mikhailgrigorev.quickpassword.common.PasswordGettingType
import com.mikhailgrigorev.quickpassword.common.PasswordManager
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.ActivityMainBinding
import com.mikhailgrigorev.quickpassword.ui.account.view.AccountActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.create.CreatePasswordActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.view.PasswordViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private val DEFAULT_ROTATION = 0F
    private lateinit var viewModel: MainViewModel

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
    private var globalPos: Int = -1
    private lateinit var passwordCards: List<PasswordCard>
    private var pm = PasswordManager()
    private var condition = true

    private lateinit var binding: ActivityMainBinding

    private fun setQuitTimer() {
        var condition = true
        val handler = Handler(Looper.getMainLooper())
        val r = Runnable {
            if (condition) {
                condition = false
                //val intent = Intent(this, LoginAfterSplashActivity::class.java)
                //startActivity(intent)
                //finish()
            }
        }

        val lockTime = Utils.lockTime()
        if (lockTime != "0") {
            handler.postDelayed(
                    r, Utils.lock_default_interval * lockTime!!.toLong()
            )
        }
    }

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
        setQuitTimer()

        checkAnalytics()

        // Get Extras
        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        val newLogin = Utils.userName()

        // Set login
        if (newLogin != login)
            login = newLogin.toString()

        initLayouts()
        initSorting()
        setPasswordQualityText()
        setObservers()

        // Shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            generateShortcuts()


        binding.passwordRecycler.setHasFixedSize(true)

        setSortingOptions()
        setListeners()

        Utils.setUserName(login)

        initBottomSheetBehavior()

    }

    private fun setArrowOrderIndicator(){
        if (defaultPassFilterAsc)
            binding.sortOrder.animate().rotation(0F).setDuration(500).start()
        else
            binding.sortOrder.animate().rotation(180F).setDuration(500).start()
    }

    private fun setOrderChip(column: String){
        defaultPassFilterAsc = if (defaultPassFilterSorting == column)
            !defaultPassFilterAsc
        else{
            true
        }

        defaultPassFilterSorting = column

        when(defaultPassFilterSorting){
            "name" -> {
                binding.dateSort.isChecked = false
                binding.alphaSort.isChecked = true
            }
            "time" -> {
                binding.dateSort.isChecked = true
                binding.alphaSort.isChecked = false
            }
        }

        setArrowOrderIndicator()
        Utils.setSortingType(defaultPassFilterSorting)
        Utils.setSortingAsc(defaultPassFilterAsc)
    }

    private fun setSortingOptions(){
        binding.alphaSort.setOnClickListener {
            setOrderChip("name")
            setObservers()
        }

        binding.dateSort.setOnClickListener {
            setOrderChip("time")
            setObservers()
        }
    }

    private fun checkAnalytics() {
        if (Utils.useAnalyze() != null)
            if (Utils.useAnalyze() != "none") {
                binding.correctScan.visibility = View.GONE
                binding.cardCup.visibility = View.GONE
                binding.cardView.visibility = View.GONE
            }
    }

    private fun initLayouts() {
        val name: String = getString(R.string.hi) + " " + login
        binding.helloTextId.text = name
        binding.passwordRecycler.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
    }

    private fun setPasswordQualityText() {
        binding.correctPasswords.text =
                resources.getQuantityString(
                        R.plurals.correct_passwords,
                        safePass,
                        safePass
                )
        binding.negativePasswords.text =
                resources.getQuantityString(
                        R.plurals.incorrect_password,
                        unsafePass,
                        unsafePass
                )
        binding.notSafePasswords.text =
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

        viewModel.getPasswords(
                type,
                name,
                value,
                sortColumn,
                isAsc
        ).observe(this) { passwords ->
            passwordCards = passwords
            if (passwords.isEmpty())
                showNoPasswordsInterface()
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
    }

    private fun initBottomSheetBehavior() {
        binding.allPassword.translationZ = 24F
        binding.newPass.translationZ = 101F

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.allPassword)

        bottomSheetBehavior.state = Utils.bottomBarState()
        binding.menuUp.animate().rotation(180F * bottomSheetBehavior.state).setDuration(0).start()
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            binding.newPass.animate().scaleX(0F).scaleY(0F).setDuration(0).start()
            binding.warnCard.animate().alpha(1F).setDuration(0).start()
            binding.backupCard.animate().alpha(1F).setDuration(0).start()
        }

        binding.searchPassField.clearFocus()
        binding.searchPassField.hideKeyboard()

        bottomSheetBehavior.peekHeight = 800 //600

        if (Utils.useAnalyze() != null)
            if (Utils.useAnalyze() != "none") {
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
                binding.menuUp.animate().rotation(180F * slideOffset).setDuration(0).start()
                if (slideOffset <= 0) {
                    binding.warnCard.animate().alpha(abs(slideOffset) + 0.5F).setDuration(0).start()
                    binding.backupCard.animate().alpha(abs(slideOffset) + 0.5F).setDuration(0)
                            .start()
                    binding.newPass.animate().scaleX(1 - abs(slideOffset))
                            .scaleY(1 - abs(slideOffset))
                            .setDuration(
                                    0
                            ).start()
                }
                binding.searchPassField.clearFocus()
                binding.searchPassField.hideKeyboard()

            }
        })
    }

    private fun setPasswordGeneratorListeners() {
        binding.lengthToggle.text = getString(R.string.length) + ": " + passwordLength

        binding.lengthToggle.setOnClickListener {
            if (binding.seekBar.visibility == View.GONE) {
                binding.seekBar.visibility = View.VISIBLE
            } else {
                binding.seekBar.visibility = View.GONE
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                passwordLength = i
                binding.lengthToggle.text = getString(R.string.length) + ": " + passwordLength
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Password generation system
        val passwordGeneratorRules = mutableListOf<String>()
        // Loop through the chips
        for (index in 0 until binding.passSettings.childCount) {
            val chip: Chip = binding.passSettings.getChildAt(index) as Chip

            chip.setOnCheckedChangeListener { view, isChecked ->
                val deg = binding.generatePassword.rotation + 30f
                binding.generatePassword.animate().rotation(deg).interpolator =
                        AccelerateDecelerateInterpolator()

                when (view.id) {
                    R.id.lettersToggle -> useLetters = isChecked
                    R.id.symToggles -> useSymbols = isChecked
                    R.id.numbersToggle -> useNumbers = isChecked
                    R.id.upperCaseToggle -> useUC = isChecked
                }

                if (isChecked)
                    passwordGeneratorRules.add(view.text.toString())
                else
                    passwordGeneratorRules.remove(view.text.toString())
            }
        }

        binding.generatePassword.setOnClickListener {
            if (passwordGeneratorRules.size == 0 || (passwordGeneratorRules.size == 1 && binding.lengthToggle.isChecked)) {
                binding.genPasswordId.error = getString(R.string.noRules)
            } else {
                binding.genPasswordId.error = null
                val newPassword: String =
                        pm.generatePassword(
                                useLetters,
                                useUC,
                                useNumbers,
                                useSymbols,
                                passwordLength
                        )
                binding.genPasswordIdField.setText(newPassword)
            }
            binding.generatePassword.animate().rotation(DEFAULT_ROTATION).interpolator =
                    AccelerateDecelerateInterpolator()
        }
    }

    private fun setPasswordSearchListener() {
        binding.searchPassField.addTextChangedListener(object : TextWatcher {
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
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.allPassword)
        binding.expand.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Utils.setBottomBarState(BottomSheetBehavior.STATE_COLLAPSED)
        }

        binding.menuUp.setOnClickListener {
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

        binding.genPasswordId.setOnClickListener {
            copyPassword()
        }

        binding.genPasswordIdField.setOnClickListener {
            copyPassword()
        }

        binding.noPasswords.setOnClickListener {
            condition = false
            goToNewPasswordActivity()
        }

        binding.extraNewPass.setOnClickListener {
            condition = false
            goToNewPasswordActivity()
        }


        binding.newPass.setOnClickListener {
            condition = false
            goToNewPasswordActivity()
        }

        binding.accountAvatar.setOnClickListener {
            condition = false
            goToAccountActivity()
        }

        // Correct passwords

        binding.correctPasswordsCircle.setOnClickListener {
            correctPasswordsClickedAction()
        }

        binding.correctPasswords.setOnClickListener {
            correctPasswordsClickedAction()
        }

        // Negative password

        binding.negativePasswordsCircle.setOnClickListener {
            negativePasswordsClickedAction()
        }

        binding.negativePasswords.setOnClickListener {
            negativePasswordsClickedAction()
        }

        // Not safe password

        binding.notSafePasswordsCircle.setOnClickListener {
            notSafePasswordsClickedAction()
        }

        binding.notSafePasswords.setOnClickListener {
            notSafePasswordsClickedAction()
        }
    }

    private fun setPasswordAdapter(passwords: List<PasswordCard>) {
        binding.passwordRecycler.adapter = PasswordAdapter(
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

    private fun updatePasswordQualityCirclesColor(circleNegative: Int, circleImprovement: Int, circlePositive: Int) {
        binding.negativePasswordsCircle.setImageResource(circleNegative)
        binding.notSafePasswordsCircle.setImageResource(circleImprovement)
        binding.correctPasswordsCircle.setImageResource(circlePositive)
    }

    private fun copyPassword() {
        if(binding.genPasswordIdField.text.toString() != ""){
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", binding.genPasswordIdField.text.toString())
            clipboard.setPrimaryClip(clip)
            Utils.makeToast(this, getString(R.string.passCopied))
        }
    }

    private fun goToNewPasswordActivity() {
        val intent = Intent(this, CreatePasswordActivity::class.java)
        intent.putExtra("login", login)
        intent.putExtra("pass", binding.genPasswordIdField.text.toString())
        intent.putExtra("useLetters", useLetters)
        intent.putExtra("useUC", useUC)
        intent.putExtra("useNumbers", useNumbers)
        intent.putExtra("useSymbols", useSymbols)
        intent.putExtra("length", passwordLength)
        startActivityForResult(intent, 1)
    }

    private fun notSafePasswordsClickedAction() {
        if (searchNotSafe) {
            binding.notSafePasswordsCircle.setImageResource(R.drawable.circle_improvement)
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
                    value = PasswordCategory.NOT_SAFE.value
            )
            searchNegative = false
            searchCorrect = false
            searchNotSafe = true
        }
    }

    private fun negativePasswordsClickedAction() {
        if (searchNegative) {
            binding.negativePasswordsCircle.setImageResource(R.drawable.circle_negative)
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
                    value = PasswordCategory.NEGATIVE.value
            )
            searchNotSafe = false
            searchCorrect = false
            searchNegative = true
        }
    }

    private fun correctPasswordsClickedAction() {
        if (searchCorrect) {
            binding.correctPasswordsCircle.setImageResource(R.drawable.circle_positive)
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
                    value = PasswordCategory.CORRECT.value
            )
            searchNotSafe = false
            searchNegative = false
            searchCorrect = true
        }
    }

    private fun goToAccountActivity() {
        val intent = Intent(this, AccountActivity::class.java)
        intent.putExtra("login", login)
        intent.putExtra("activity", "menu")
        startActivityForResult(intent, 1)
    }

    private fun showNoPasswordsInterface() {
        binding.noPasswords.visibility = View.VISIBLE
        binding.extraNewPass.visibility = View.VISIBLE
        binding.cardView.visibility = View.GONE
        binding.cardCup.visibility = View.GONE
        binding.smile.visibility = View.GONE
        binding.expand.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.allPassword)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showPasswordsInterface() {
        binding.noPasswords.visibility = View.GONE
        binding.cardView.visibility = View.VISIBLE
        binding.cardCup.visibility = View.VISIBLE
        binding.smile.visibility = View.VISIBLE
        binding.expand.visibility = View.VISIBLE
        binding.extraNewPass.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.allPassword)
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
        binding.searchPassField.setText(name)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun passLongClickListener(position: Int, view: View) {
        view.setOnTouchListener { _, event ->
            xTouch = event.x.toInt()
            false
        }
        showPopup(position, view)
    }

    private fun showPopup(position: Int, view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val point = Point()
        point.x = location[0]
        point.y = location[1]
        val layoutInflater =
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.popup, null)

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
        condition=false
        val intent = Intent(this, PasswordViewActivity::class.java)
        intent.putExtra("login", login)
        intent.putExtra("password_id", passwordCards[position]._id)
        startActivityForResult(intent, 1)
    }

    @SuppressLint("Recycle")
    fun favorite(view: View) {
        Log.d("favorite", view.id.toString())
        val position = globalPos

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.favPassword(passwordCards[position])
        }

        setObservers()

        changeStatusPopUp.dismiss()
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
                val llBottomSheet = binding.allPassword
                val bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    finish()
                else
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            condition=false
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.lengthToggle.isChecked)
            binding.seekBar.visibility = View.VISIBLE
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
                this,
                MainViewModelFactory()
        )[MainViewModel::class.java]
    }

}