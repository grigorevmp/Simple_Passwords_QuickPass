package com.mikhailgrigorev.quickpassword.ui.main_activity

import android.annotation.SuppressLint
import android.content.*
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.*
import com.mikhailgrigorev.quickpassword.common.PasswordManager
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.ActivityMainBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.PasswordsDataBaseHelper
import com.mikhailgrigorev.quickpassword.ui.account.view.AccountActivity
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.create.CreatePasswordActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.view.PasswordViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private val START_ALPHA = 1F
    private val DEFAULT_ROTATION = 0F
    private lateinit var viewModel: MainViewModel

    enum class CATEGORY(val value: String) {
        CORRECT("1"), NEGATIVE("2"), NOT_SAFE("3")
    }

    private val _preferenceFile = "quickPassPreference"
    private var passwordLength = 20
    private var useSymbols = false
    private var useUC = false
    private var useLetters = false
    private var useNumbers = false
    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0
    private var faNum = 0
    private var tlNum = 0
    private val passwords: ArrayList<Pair<String, Boolean>> = ArrayList()
    private var passwordsG: ArrayList<Pair<String, Boolean>> = ArrayList()
    private val realPass: ArrayList<Pair<String, String>> = ArrayList()
    private val realQuality: ArrayList<String> = ArrayList()
    private val realMap: MutableMap<String, ArrayList<String>> = mutableMapOf()
    private val quality: ArrayList<String> = ArrayList()
    private val dates: ArrayList<String> = ArrayList()
    private val tags: ArrayList<String> = ArrayList()
    private val desc: ArrayList<String> = ArrayList()
    private val group: ArrayList<String> = ArrayList()
    private lateinit var login: String
    var useAnalyze: String? = null
    var cardRadius: String? = null
    private var sorting: String? = "none"

    private var searchCorrect: Boolean = false
    private var searchNegative: Boolean = false
    private var searchNotSafe: Boolean = false
    val handler = Handler()

    private var xTouch = 500
    private var changeStatusPopUp: PopupWindow = PopupWindow()
    private var globalPos: Int = -1
    private var pm = PasswordManager()
    private var condition = true

    private lateinit var binding: ActivityMainBinding

    private fun setQuitTimer() {
        var condition = true
        val handler = Handler(Looper.getMainLooper())
        val r = Runnable {
            if (condition) {
                condition = false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        val lockTime = Utils.lockTime()
        if (lockTime != "0") {
            handler.postDelayed(
                    r, Utils.lock_default_interval * lockTime!!.toLong()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        setQuitTimer()

        if (Utils.useAnalyze() != null)
            if (Utils.useAnalyze() != "none") {
                binding.correctScan.visibility = View.GONE
                binding.cardCup.visibility = View.GONE
                binding.cardView.visibility = View.GONE
            }


        // Get Extras
        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        val newLogin = Utils.userName()

        // Set login
        if (newLogin != login)
            login = newLogin.toString()

        // Set greeting
        val name: String = getString(R.string.hi) + " " + login
        binding.helloTextId.text = name

        // Open passwords database
        viewModel.passwords.observe(this) { passwords ->
            for (password in passwords) {
                realPass.add(Pair(password.name, password.password))
            }
            analyzeDataBase()
            loadPasswords(passwords!!)
        }

        // Sorting
        when (Utils.sortingType()!!) {
            "alpha" -> {
                sortByAlphaDown()
            }
            "date" -> {
                sortByDateUp()
            }
            else -> {
                sortByAlphaUp()
            }
        }

        // Shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            generateShortcuts()

        // First greeting
        if(passwords.size == 0) {
            showInterfaceIfNoPasswords()
        }

        // Set stats
        binding.correctPasswords.text = resources.getQuantityString(
                R.plurals.correct_passwords,
                safePass,
                safePass
        )
        binding.negativePasswords.text = resources.getQuantityString(
                R.plurals.incorrect_password,
                unsafePass,
                unsafePass
        )
        binding.notSafePasswords.text = resources.getQuantityString(R.plurals.need_fix, fixPass, fixPass)
        binding.passwordRecycler.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )

        binding.passwordRecycler.setHasFixedSize(true)

        //Alpha Sorting

        binding.alphaSort.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.dateSort.isChecked = false
                binding.sortOrder.animate().rotation(180F).setDuration(500).start()
                Utils.setSortingType("alpha")
                sortByAlphaDown()
                when {
                    searchCorrect -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive_fill
                        )
                        searchPasswordByCategory(CATEGORY.CORRECT.value)
                    }
                    searchNegative -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative_fill,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive
                        )
                        searchPasswordByCategory(CATEGORY.NEGATIVE.value)
                    }
                    searchNotSafe -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement_fill,
                                circlePositive = R.drawable.circle_positive
                        )
                        searchPasswordByCategory(CATEGORY.NOT_SAFE.value)
                    }
                    binding.searchPassField.text.toString() != "" -> {
                        binding.searchPassField.text = binding.searchPassField.text
                    }
                    else -> {
                        setDefaultPasswordAdapter()
                    }
                }

            }
            else{
                binding.sortOrder.animate().rotation(0F).setDuration(500).start()
                Utils.setSortingType("none")
                sortByAlphaUp()
                when {
                    searchCorrect -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive_fill
                        )
                        searchPasswordByCategory(CATEGORY.CORRECT.value)
                    }
                    searchNegative -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative_fill,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive
                        )
                        searchPasswordByCategory(CATEGORY.NEGATIVE.value)
                    }
                    searchNotSafe -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement_fill,
                                circlePositive = R.drawable.circle_positive
                        )
                        searchPasswordByCategory(CATEGORY.NOT_SAFE.value)

                    }
                    binding.searchPassField.text.toString() != "" -> {
                        binding.searchPassField.text = binding.searchPassField.text
                    }
                    else -> {
                        setDefaultPasswordAdapter()
                    }
                }
            }
        }

        binding.dateSort.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.alphaSort.isChecked = false
                binding.sortOrder.animate().rotation(180F).setDuration(500).start()
                Utils.setSortingType("date")
                sortByDateUp()
                when {
                    searchCorrect -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive_fill
                        )
                        searchPasswordByCategory(CATEGORY.CORRECT.value)
                    }
                    searchNegative -> {
                            updatePasswordQualityCirclesColor(
                                    circleNegative = R.drawable.circle_negative_fill,
                                    circleImprovement = R.drawable.circle_improvement,
                                    circlePositive = R.drawable.circle_positive
                            )
                            searchPasswordByCategory(CATEGORY.NEGATIVE.value)
                    }
                    searchNotSafe -> {
                            updatePasswordQualityCirclesColor(
                                    circleNegative = R.drawable.circle_negative,
                                    circleImprovement = R.drawable.circle_improvement_fill,
                                    circlePositive = R.drawable.circle_positive
                            )
                            searchPasswordByCategory(CATEGORY.NOT_SAFE.value)
                    }
                    binding.searchPassField.text.toString() != "" -> {
                        binding.searchPassField.text = binding.searchPassField.text
                    }
                    else -> {
                        setDefaultPasswordAdapter()
                    }
                }

            }
            else{
                binding.sortOrder.animate().rotation(0F).setDuration(500).start()
                Utils.setSortingType("none")
                sortByAlphaUp()
                when {
                    searchCorrect -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive_fill
                        )
                        searchPasswordByCategory(CATEGORY.CORRECT.value)
                    }
                    searchNegative -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative_fill,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive
                        )
                        searchPasswordByCategory(CATEGORY.NEGATIVE.value)
                    }
                    searchNotSafe -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement_fill,
                                circlePositive = R.drawable.circle_positive
                        )
                        searchPasswordByCategory(CATEGORY.NOT_SAFE.value)
                    }
                    binding.searchPassField.text.toString() != "" -> {
                        binding.searchPassField.text = binding.searchPassField.text
                    }
                    else -> {
                        setDefaultPasswordAdapter()
                    }
                }
            }
        }

        // Set passwords adapter
        setDefaultPasswordAdapter()

        setListeners()

        // Search passwords
        binding.searchPassField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val passwords2: ArrayList<Pair<String, Boolean>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                val desc2: ArrayList<String> = ArrayList()
                for ((index, pair) in passwords.withIndex()) {
                    if (pair.first.lowercase(Locale.ROOT).contains(
                                s.toString().lowercase(Locale.ROOT)
                        ) ||
                        (tags[index].lowercase(Locale.ROOT).contains(
                                s.toString().lowercase(
                                        Locale.ROOT
                                )
                        ))
                        ||
                        ((pair.second) && ("2fa".lowercase(Locale.ROOT).contains(
                                s.toString().lowercase(Locale.ROOT)
                        )))
                    )
                     {
                        passwords2.add(pair)
                        quality2.add(quality[index])
                        tags2.add(tags[index])
                        group2.add(group[index])
                        desc2.add(desc[index])
                    }
                }

                passwordsG = passwords2
                binding.passwordRecycler.adapter = PasswordAdapter(
                        passwords2,
                        quality2,
                        tags2,
                        group2,
                        desc2,
                        useAnalyze,
                        cardRadius,
                        resources.displayMetrics,
                        this@MainActivity,
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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        Utils.setUserName(login)

        bottomSheetBehavior()

    }

    private fun bottomSheetBehavior() {
        // получение вью нижнего экрана
        binding.allPassword.translationZ = 24F
        binding.newPass.translationZ = 101F

        // настройка поведения нижнего экрана
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.allPassword)

        // настройка состояний нижнего экрана
        //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.state = Utils.bottomBarState()
        binding.menuUp.animate().rotation(180F * bottomSheetBehavior.state).setDuration(0).start()
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            binding.newPass.animate().scaleX(0F).scaleY(0F).setDuration(0).start()
            binding.warnCard.animate().alpha(1F).setDuration(0).start()
            binding.backupCard.animate().alpha(1F).setDuration(0).start()
        }

        binding.searchPassField.clearFocus()
        binding.searchPassField.hideKeyboard()


        // настройка максимальной высоты
        bottomSheetBehavior.peekHeight = 800 //600

        if (useAnalyze != null)
            if (useAnalyze != "none") {
                bottomSheetBehavior.peekHeight = 1200
            }


        // настройка возможности скрыть элемент при свайпе вниз
        bottomSheetBehavior.isHideable = true

        // настройка колбэков при изменениях

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                with(Utils.sharedPreferences!!.edit()) {
                    putInt("__BS", newState)
                    apply()
                }
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

    private fun setListeners() {
        binding.lengthToggle.text = getString(R.string.length) + ": " + passwordLength
        binding.lengthToggle.setOnClickListener {
            if (binding.seekBar.visibility == View.GONE) {
                binding.seekBar.visibility = View.VISIBLE
            } else {
                binding.seekBar.visibility = View.GONE
            }
        }

        // Set a SeekBar change listener
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                passwordLength = i
                binding.lengthToggle.text = getString(R.string.length) + ": " + passwordLength
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.allPassword)

        binding.expand.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            with(Utils.sharedPreferences!!.edit()) {
                putInt("__BS", BottomSheetBehavior.STATE_COLLAPSED)
                apply()
            }
        }

        binding.menuUp.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            with(Utils.sharedPreferences!!.edit()) {
                putInt("__BS", BottomSheetBehavior.STATE_EXPANDED)
                apply()
            }
        }

        // Generate random password

        // Password generation system
        val passwordGeneratorRules = mutableListOf<String>()
        // Loop through the chips
        for (index in 0 until binding.passSettings.childCount) {
            val chip: Chip = binding.passSettings.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->
                val deg = binding.generatePassword.rotation + 30f
                binding.generatePassword.animate().rotation(deg).interpolator =
                        AccelerateDecelerateInterpolator()
                if (isChecked) {
                    if (view.id == R.id.lettersToggle)
                        useLetters = true
                    if (view.id == R.id.symToggles)
                        useSymbols = true
                    if (view.id == R.id.numbersToggle)
                        useNumbers = true
                    if (view.id == R.id.upperCaseToggle)
                        useUC = true
                    passwordGeneratorRules.add(view.text.toString())
                } else {
                    if (view.id == R.id.lettersToggle)
                        useLetters = false
                    if (view.id == R.id.symToggles)
                        useSymbols = false
                    if (view.id == R.id.numbersToggle)
                        useNumbers = false
                    if (view.id == R.id.upperCaseToggle)
                        useUC = false
                    passwordGeneratorRules.remove(view.text.toString())
                }
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

        binding.correctPasswordsCircle.setOnClickListener {
            correctPasswordsClickedAction()
        }

        binding.correctPasswords.setOnClickListener {
            correctPasswordsClickedAction()
        }

        binding.negativePasswordsCircle.setOnClickListener {
            negativePasswordsClickedAction()
        }

        binding.negativePasswords.setOnClickListener {
            negativePasswordsClickedAction()
        }

        binding.notSafePasswordsCircle.setOnClickListener {
            notSafePasswordsClickedAction()
        }

        binding.notSafePasswords.setOnClickListener {
            notSafePasswordsClickedAction()
        }
    }

    private fun loadPasswords(passwordList: List<PasswordCard>) {
        /*
        Load main password configuration
         */
        for (password in passwordList) {

            val qualityNum = evaluatePassword(password)

            if (password.use_2fa)
                faNum += 1
            passwords.add(0, Pair(password.name, password.use_2fa))
            desc.add(0, password.description)

            if (password.favorite) {
                quality.add(0, qualityNum)
                val dbTag = password.tags
                tags.add(0, dbTag)
                group.add(0, "#favorite")
                dates.add(0, password.time.toString())
            } else {
                val dbTag = password.tags
                tags.add(dbTag)
                group.add("none")
                dates.add(0, password.time.toString())
            }
            if (password.encrypted)
                tlNum += 1

            when (qualityNum) {
                "1" -> safePass += 1
                "2" -> unsafePass += 1
                "3" -> fixPass += 1
                "4" -> safePass += 1
                "6" -> safePass += 1
            }

            binding.allPass.text = (safePass + unsafePass + fixPass).toString()
            binding.afText.text = faNum.toString()
            binding.tlText.text = tlNum.toString()
        }
    }

    private fun sortByAlphaUp() {
        binding.sortOrder.animate().rotation(0F).setDuration(500).start()
        for (i in 0 until passwords.size) {
            for (j in 0 until passwords.size){
                if(group[i].contains("favorite") == group[j].contains("favorite"))
                    if(passwords[i].first > passwords[j].first){
                        val temp = passwords[j]
                        passwords[j] = passwords[i]
                        passwords[i] = temp
                        var temp2 = quality[j]
                        quality[j] = quality[i]
                        quality[i]  = temp2
                        temp2 = tags[j]
                        tags[j] = tags[i]
                        tags[i]  = temp2
                        temp2 = group[j]
                        group[j] = group[i]
                        group[i]  = temp2
                        temp2 = desc[j]
                        desc[j] = desc[i]
                        desc[i]  = temp2
                        temp2 = dates[j]
                        dates[j] = dates[i]
                        dates[i]  = temp2
                    }
            }
        }
    }

    private fun sortByDateUp() {
        binding.sortOrder.animate().rotation(180F).setDuration(500).start()
        binding.dateSort.isChecked = true
        for (i in 0 until passwords.size){
            for (j in 0 until passwords.size){
                if(group[i].contains("favorite") == group[j].contains("favorite"))
                    if(dates[i] > dates[j]){
                        val temp = passwords[j]
                        passwords[j] = passwords[i]
                        passwords[i] = temp
                        var temp2 = quality[j]
                        quality[j] = quality[i]
                        quality[i]  = temp2
                        temp2 = tags[j]
                        tags[j] = tags[i]
                        tags[i]  = temp2
                        temp2 = group[j]
                        group[j] = group[i]
                        group[i]  = temp2
                        temp2 = desc[j]
                        desc[j] = desc[i]
                        desc[i]  = temp2
                        temp2 = dates[j]
                        dates[j] = dates[i]
                        dates[i]  = temp2
                    }
            }
        }
    }

    private fun sortByAlphaDown() {
        binding.sortOrder.animate().rotation(180F).setDuration(500).start()
        binding.alphaSort.isChecked = true
        for (i in 0 until passwords.size){
            for (j in 0 until passwords.size){
                if(group[i].contains("favorite") == group[j].contains("favorite"))
                    if(passwords[i].first < passwords[j].first){
                        val temp = passwords[j]
                        passwords[j] = passwords[i]
                        passwords[i] = temp
                        var temp2 = quality[j]
                        quality[j] = quality[i]
                        quality[i]  = temp2
                        temp2 = tags[j]
                        tags[j] = tags[i]
                        tags[i]  = temp2
                        temp2 = group[j]
                        group[j] = group[i]
                        group[i]  = temp2
                        temp2 = desc[j]
                        desc[j] = desc[i]
                        desc[i]  = temp2
                        temp2 = dates[j]
                        dates[j] = dates[i]
                        dates[i]  = temp2
                    }
            }
        }
    }

    private fun setDefaultPasswordAdapter() {
        passwordsG = passwords
        binding.passwordRecycler.adapter = PasswordAdapter(
                passwords,
                quality,
                tags,
                group,
                desc,
                useAnalyze,
                cardRadius,
                resources.displayMetrics,
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

    private fun searchPasswordByCategory(passwordType: String) {
        val passwords2: ArrayList<Pair<String, Boolean>> = ArrayList()
        val quality2: ArrayList<String> = ArrayList()
        val tags2: ArrayList<String> = ArrayList()
        val group2: ArrayList<String> = ArrayList()
        val desc2: ArrayList<String> = ArrayList()
        for ((index, value) in quality.withIndex()) {
            if (value == passwordType){
                passwords2.add(passwords[index])
                quality2.add(quality[index])
                tags2.add(tags[index])
                group2.add(group[index])
                desc2.add(desc[index])
            }
        }

        passwordsG = passwords2
        binding.passwordRecycler.adapter = PasswordAdapter(
                passwords2,
                quality2,
                tags2,
                group2,
                desc2,
                useAnalyze,
                cardRadius,
                resources.displayMetrics,
                this@MainActivity,
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

    private fun evaluatePassword(password: PasswordCard): String {
        val evaluation: Float = Utils.password_manager.evaluatePassword(password.password)

        var qualityScore = when {
            evaluation < 0.33 -> "2"
            evaluation < 0.66 -> "3"
            else -> "1"
        }

        if (password.encrypted)
            qualityScore = "6"

        if (Utils.password_manager.evaluateDate(password.time.toString()))
            qualityScore = "2"

        if (!password.encrypted && password.password.length == 4)
            qualityScore = "4"

        if (Utils.password_manager.popularPasswords(password.password)
            or ((password.password.length == 4)
                    and Utils.password_manager.popularPin(password.password))
        ) {
            qualityScore = if (qualityScore == "4")
                "5"
            else
                "2"
        }

        return qualityScore
    }

    private fun copyPassword() {
        if(binding.genPasswordIdField.text.toString() != ""){
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", binding.genPasswordIdField.text.toString())
            clipboard.setPrimaryClip(clip)
            toast(getString(R.string.passCopied))
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
        if(searchNotSafe){
            binding.notSafePasswordsCircle.setImageResource(R.drawable.circle_improvement)
            setDefaultPasswordAdapter()
            searchNotSafe = false
        }
        else{
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_negative,
                    circleImprovement = R.drawable.circle_improvement_fill,
                    circlePositive = R.drawable.circle_positive
            )
            searchPasswordByCategory(CATEGORY.NOT_SAFE.value)
            searchNegative = false
            searchCorrect = false
            searchNotSafe = true
        }
    }

    private fun negativePasswordsClickedAction() {
        if(searchNegative){
            binding.negativePasswordsCircle.setImageResource(R.drawable.circle_negative)
            setDefaultPasswordAdapter()
            searchNegative = false
        }
        else{
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_negative_fill,
                    circleImprovement = R.drawable.circle_improvement,
                    circlePositive = R.drawable.circle_positive
            )
            searchPasswordByCategory(CATEGORY.NEGATIVE.value)
            searchNotSafe = false
            searchCorrect = false
            searchNegative = true
        }
    }

    private fun correctPasswordsClickedAction() {
        if(searchCorrect){
            binding.correctPasswordsCircle.setImageResource(R.drawable.circle_positive)
            setDefaultPasswordAdapter()
            searchCorrect = false
        }
        else{
            updatePasswordQualityCirclesColor(
                    circleNegative = R.drawable.circle_negative,
                    circleImprovement = R.drawable.circle_improvement,
                    circlePositive = R.drawable.circle_positive_fill
            )
            searchPasswordByCategory(CATEGORY.CORRECT.value)
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

    private fun showInterfaceIfNoPasswords() {
        binding.allPassword.visibility = View.GONE
        binding.noPasswords.visibility = View.VISIBLE
        binding.cardView.visibility = View.GONE
        binding.cardCup.visibility = View.GONE
        binding.smile.visibility = View.GONE
        binding.expand.visibility = View.GONE
        binding.newPass.visibility = View.GONE
        binding.extraNewPass.visibility = View.VISIBLE
        binding.warnCard.animate().alpha(abs(START_ALPHA)).start()
        binding.backupCard.animate().alpha(abs(START_ALPHA)).start()
    }

    private fun createIntentForShortcut(passwordIndex: Int): Intent{
        val intent = Intent(this, PasswordViewActivity::class.java)

        var isPass = false

        intent.action = Intent.ACTION_VIEW
        intent.putExtra("login", login)
        intent.putExtra("passName", passwords[passwordIndex].first)
        intent.putExtra("openedFrom", "shortcut")

        var str = getString(R.string.sameParts)
        if (realMap.containsKey(passwords[passwordIndex].first)) {
            for (pass in realMap[passwords[passwordIndex].first]!!) {
                isPass = true
                str += "$pass "
            }
        }
        if (isPass)
            intent.putExtra("sameWith", str)
        else
            intent.putExtra("sameWith", "none")

        return intent
    }

    private fun createShortcut(passwordIndex: Int):ShortcutInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val intentForShortcut = createIntentForShortcut(passwordIndex)
            return ShortcutInfo.Builder(this, "shortcut_ $passwordIndex")
                    .setShortLabel(passwords[passwordIndex].first)
                    .setLongLabel(passwords[passwordIndex].first)
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

            for (i in (0..min(2, passwords.size-1))){
                shortcutList.add(createShortcut(i)!!)
            }

            shortcutManager.dynamicShortcuts = shortcutList
        }
    }

    fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun analyzeDataBase() {
        var gSubContains: Boolean
        for (pass in realPass){
            gSubContains = false
            for (pass2 in realPass){
                if(pass.first != pass2.first){
                    if (pass2.second.contains(pass.second)){
                            gSubContains = true
                            if (realMap.containsKey(pass.first))
                                realMap[pass.first]?.add(pass2.first)
                            else {
                                val c = arrayListOf(pass2.first)
                                realMap[pass.first] = c
                            }
                            break
                        }
                }
            }
            if (gSubContains) {
                realQuality.add("0")
            }
            else
                realQuality.add("1")
        }
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


    @SuppressLint("Recycle", "InflateParams")
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
        var isPass = false
        intent.putExtra("login", login)
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("__PASSNAME", passwordsG[position].first)
            commit()
        }
        intent.putExtra("passName", passwordsG[position].first)
        var str = getString(R.string.sameParts) + " "
        var j = 0
        if (realMap.containsKey(passwordsG[position].first)){
            for(pass in realMap[passwordsG[position].first]!!) {
                if (pass !in str) {
                    if (j == 0)
                        j += 1
                    else
                        str += ", "
                    isPass = true
                    str += pass
                }
            }
        }
        if(isPass)
            intent.putExtra("sameWith", str)
        else
            intent.putExtra("sameWith", "none")
        startActivityForResult(intent, 1)
    }

    private fun Context.toast(message: String)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    @SuppressLint("Recycle")
    fun favorite(view: View) {
        Log.d("favorite", view.id.toString())
        val position = globalPos

        lifecycleScope.launch(Dispatchers.IO) {
            // viewModel.favPassword(passwordsG[position])
        }
        clearContainers()


        viewModel.passwords.observe(this) { passwords ->
            for (password in passwords) {
                realPass.add(Pair(password.name, password.password))
            }
            analyzeDataBase()
            loadPasswords(passwords!!)
        }

        when (sorting) {
            "alpha" -> {
                sortByAlphaDown()
            }
            "none" -> {
                sortByAlphaUp()
            }
            "date" -> {
                sortByDateUp()
            }
        }

        setDefaultPasswordAdapter()

        changeStatusPopUp.dismiss()
    }

    private fun clearContainers() {
        passwords.clear()
        quality.clear()
        tags.clear()
        group.clear()
        realPass.clear()
        realQuality.clear()
        realMap.clear()
        desc.clear()
        dates.clear()
    }

    @SuppressLint("Recycle")
    fun delete(view: View) {
        Log.d("deleted", view.id.toString())
        val position = globalPos
        val pdbHelper = PasswordsDataBaseHelper(this, login)
            val pDatabase = pdbHelper.writableDatabase
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.deletePassword))
            builder.setMessage(getString(R.string.passwordDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                pDatabase.delete(
                        pdbHelper.TABLE_USERS,
                        "NAME = ?",
                        arrayOf(passwordsG[position].first)
                )

                clearContainers()

                safePass = 0
                unsafePass = 0
                fixPass = 0

                viewModel.passwords.observe(this) { passwords ->
                    for (password in passwords) {
                        realPass.add(Pair(password.name, password.password))
                    }
                    analyzeDataBase()
                    loadPasswords(passwords!!)
                }
                if (passwords.size == 0) {
                    binding.correctPasswords.text = resources.getQuantityString(
                            R.plurals.correct_passwords,
                            0,
                            0
                    )
                    binding.negativePasswords.text = resources.getQuantityString(
                            R.plurals.incorrect_password,
                            0,
                            0
                    )
                    binding.notSafePasswords.text = resources.getQuantityString(R.plurals.need_fix, 0, 0)
                    showInterfaceIfNoPasswords()
                }

                when (sorting) {
                    "alpha" -> {
                        sortByAlphaDown()
                    }
                    "none" -> {
                        sortByAlphaUp()
                    }
                    "date" -> {
                        sortByDateUp()
                    }
                }

                setDefaultPasswordAdapter()
            }

            builder.setNegativeButton(getString(R.string.no)){ _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)){ _, _ ->
            }
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
            clearContainers()
            recreate()
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
                MainViewModelFactory(application)
        )[MainViewModel::class.java]
    }

}