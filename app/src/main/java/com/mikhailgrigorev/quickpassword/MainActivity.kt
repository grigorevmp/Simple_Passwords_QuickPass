package com.mikhailgrigorev.quickpassword

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.graphics.Point
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.databinding.ActivityMainBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.dbhelpers.PasswordsDataBaseHelper
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private val START_ALPHA = 1F
    private val DEFAULT_ROTATION = 0F

    enum class CATEGORY(val value: String) {
        CORRECT("1"), NEGATIVE("2"), NOT_SAFE("3")
    }

    private val _keyThemeAccent = "themeAccentPreference"
    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"
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
    private val passwords: ArrayList<Pair<String, String>> = ArrayList()
    private var passwordsG: ArrayList<Pair<String, String>> = ArrayList()
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

    @SuppressLint(
            "Recycle", "ClickableViewAccessibility", "ResourceAsColor", "RestrictedApi",
            "SetTextI18n", "ServiceCast"
    )
    override fun onCreate(savedInstanceState: Bundle?) {

        val pref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        when(pref.getString(_keyTheme, "none")){
            "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "none", "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "battery" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        when(pref.getString("themeAccentPreference", "none")){
            "Red" -> setTheme(R.style.AppThemeRed)
            "Pink" -> setTheme(R.style.AppThemePink)
            "Purple" -> setTheme(R.style.AppThemePurple)
            "Violet" -> setTheme(R.style.AppThemeViolet)
            "DViolet" -> setTheme(R.style.AppThemeDarkViolet)
            "Blue" -> setTheme(R.style.AppThemeBlue)
            "Cyan" -> setTheme(R.style.AppThemeCyan)
            "Teal" -> setTheme(R.style.AppThemeTeal)
            "Green" -> setTheme(R.style.AppThemeGreen)
            "LGreen" -> setTheme(R.style.AppThemeLightGreen)
            else -> setTheme(R.style.AppTheme)
        }

        super.onCreate(savedInstanceState)



        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false)
                }
                else{
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
        }

        // Finish app after some time

        val r = Runnable {
            if(condition) {
                condition=false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        val time: Long =  100000
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val lockTime = sharedPref.getString("appLockTime", "6")
        if(lockTime != null) {
            if (lockTime != "0")
                handler.postDelayed(r, time * lockTime.toLong())
        }
        else
            handler.postDelayed(r, time*6L)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        cardRadius = sharedPref.getString("cardRadius", "none")
        if(cardRadius != null)
            if(cardRadius != "none") {
                binding.correctScan.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius!!.toFloat(), resources.displayMetrics)
                binding.cardPass.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius!!.toFloat(), resources.displayMetrics)
                binding.noPasswords.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius!!.toFloat(), resources.displayMetrics)
                binding.warnCard.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius!!.toFloat(), resources.displayMetrics)
                binding.backupCard.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius!!.toFloat(), resources.displayMetrics)
                binding.cardView.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius!!.toFloat(), resources.displayMetrics)
                binding.cardCup.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius!!.toFloat(), resources.displayMetrics)
            }

        useAnalyze = sharedPref.getString("useAnalyze", "none")
        if (useAnalyze != null)
            if (useAnalyze != "none"){
                binding.correctScan.visibility = View.GONE
                binding.cardCup.visibility = View.GONE
                binding.cardView.visibility = View.GONE
            }

        if (sharedPref.getString("themeAccentPreference", "none") == "none") {
            with(sharedPref.edit()) {
                putString(_keyThemeAccent, "Violet")
                commit()
            }
            recreate()
        }

        // Get Extras
        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        val newLogin = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE).getString(
                _keyUsername,
                login
        )

        // Set login
        if(newLogin != login)
            login = newLogin.toString()

        // Set greeting
        val name: String = getString(R.string.hi) + " " + login
        binding.helloTextId.text = name

        // Open users database
        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
                dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_IMAGE),
                "NAME = ?", arrayOf(login),
                null, null, null
        )
        if (cursor.moveToFirst()) {
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                when(cursor.getString(imageIndex).toString()){
                    "ic_account" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account
                            )
                    "ic_account_Pink" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Pink
                            )
                    "ic_account_Red" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Red
                            )
                    "ic_account_Purple" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Purple
                            )
                    "ic_account_Violet" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Violet
                            )
                    "ic_account_Dark_Violet" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Dark_Violet
                            )
                    "ic_account_Blue" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Blue
                            )
                    "ic_account_Cyan" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Cyan
                            )
                    "ic_account_Teal" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Teal
                            )
                    "ic_account_Green" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Green
                            )
                    "ic_account_lightGreen" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_lightGreen
                            )
                    else -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                }
                binding.accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }
        cursor.close()


        // Open passwords database
        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase

        try {
            var pCursor: Cursor = getDataBase(pdbHelper, pDatabase, null)
            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                do {
                    val pass = pCursor.getString(passIndex).toString()
                    val login = pCursor.getString(nameIndex).toString()
                    realPass.add(Pair(login, pass))
                } while (pCursor.moveToNext())
            }

            analyzeDataBase()
            // Second scan to set quality
            pCursor = getDataBase(pdbHelper, pDatabase, pdbHelper.KEY_NAME)
            loadPasswords(pCursor, pdbHelper)

            } catch (e: SQLException) {
        }

        // Sorting
        sorting = sharedPref.getString("sort", "none")
        when (sorting) {
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
                with(sharedPref.edit()) {
                    putString("sort", "alpha")
                    commit()
                }
                sortByAlphaDown()
                when {
                    searchCorrect -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive_fill)
                        searchPasswordByCategory(CATEGORY.CORRECT.value)
                    }
                    searchNegative -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative_fill,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive)
                        searchPasswordByCategory(CATEGORY.NEGATIVE.value)
                    }
                    searchNotSafe -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement_fill,
                                circlePositive = R.drawable.circle_positive)
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
                with(sharedPref.edit()) {
                    putString("sort", "none")
                    commit()
                }
                sortByAlphaUp()
                when {
                    searchCorrect -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive_fill)
                        searchPasswordByCategory(CATEGORY.CORRECT.value)
                    }
                    searchNegative -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative_fill,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive)
                        searchPasswordByCategory(CATEGORY.NEGATIVE.value)
                    }
                    searchNotSafe -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement_fill,
                                circlePositive = R.drawable.circle_positive)
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
                with(sharedPref.edit()) {
                    putString("sort", "date")
                    commit()
                }
                sortByDateUp()
                when {
                    searchCorrect -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive_fill)
                        searchPasswordByCategory(CATEGORY.CORRECT.value)
                    }
                    searchNegative -> {
                            updatePasswordQualityCirclesColor(
                                    circleNegative = R.drawable.circle_negative_fill,
                                    circleImprovement = R.drawable.circle_improvement,
                                    circlePositive = R.drawable.circle_positive)
                            searchPasswordByCategory(CATEGORY.NEGATIVE.value)
                    }
                    searchNotSafe -> {
                            updatePasswordQualityCirclesColor(
                                    circleNegative = R.drawable.circle_negative,
                                    circleImprovement = R.drawable.circle_improvement_fill,
                                    circlePositive = R.drawable.circle_positive)
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
                with(sharedPref.edit()) {
                    putString("sort", "none")
                    commit()
                }
                sortByAlphaUp()
                when {
                    searchCorrect -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive_fill)
                        searchPasswordByCategory(CATEGORY.CORRECT.value)
                    }
                    searchNegative -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative_fill,
                                circleImprovement = R.drawable.circle_improvement,
                                circlePositive = R.drawable.circle_positive)
                        searchPasswordByCategory(CATEGORY.NEGATIVE.value)
                    }
                    searchNotSafe -> {
                        updatePasswordQualityCirclesColor(
                                circleNegative = R.drawable.circle_negative,
                                circleImprovement = R.drawable.circle_improvement_fill,
                                circlePositive = R.drawable.circle_positive)
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

        // Set stat clicker to filter passes by quality

        binding.correctPasswordsCircle.setOnClickListener {
            correctPasswordsClickedAction()
        }

        binding.correctPasswords.setOnClickListener{
            correctPasswordsClickedAction()
        }

        binding.negativePasswordsCircle.setOnClickListener {
            negativePasswordsClickedAction()
        }

        binding.negativePasswords.setOnClickListener{
            negativePasswordsClickedAction()
        }

        binding.notSafePasswordsCircle.setOnClickListener{
            notSafePasswordsClickedAction()
        }

        binding.notSafePasswords.setOnClickListener {
            notSafePasswordsClickedAction()
        }

        // Search passwords
        binding.searchPassField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                val desc2: ArrayList<String> = ArrayList()
                for ((index, pair) in passwords.withIndex()) {
                    if (pair.first.toLowerCase(Locale.ROOT).contains(
                                s.toString().toLowerCase(Locale.ROOT)
                        ) ||
                        (tags[index].toLowerCase(Locale.ROOT).contains(
                                s.toString().toLowerCase(
                                        Locale.ROOT
                                )
                        ))
                        ||
                        ((pair.second != "0") && ("2fa".toLowerCase(Locale.ROOT).contains(
                                s.toString().toLowerCase(Locale.ROOT))))
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

        with(sharedPref.edit()) {
            putString(_keyUsername, login)
            commit()
        }

        // Go to accout
        binding.accountAvatar.setOnClickListener {
            condition=false
            goToAccountActivity()
        }

        // Password generation system
        val passwordGeneratorRules = mutableListOf<String>()
        // Loop through the chips
        for (index in 0 until binding.passSettings.childCount) {
            val chip: Chip = binding.passSettings.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener{ view, isChecked ->
                val deg = binding.generatePassword.rotation + 30f
                binding.generatePassword.animate().rotation(deg).interpolator = AccelerateDecelerateInterpolator()
                if (isChecked){
                    if (view.id == R.id.lettersToggle)
                        useLetters = true
                    if (view.id == R.id.symToggles)
                        useSymbols = true
                    if (view.id == R.id.numbersToggle)
                        useNumbers = true
                    if (view.id == R.id.upperCaseToggle)
                        useUC = true
                    passwordGeneratorRules.add(view.text.toString())
                }else{
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



        binding.lengthToggle.text = getString(R.string.length)  + ": " +  passwordLength
        binding.lengthToggle.setOnClickListener {
            if(binding.seekBar.visibility ==  View.GONE){
                binding.seekBar.visibility =  View.VISIBLE
            }
            else{
                binding.seekBar.visibility =  View.GONE
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

        // Generate random password
        binding.generatePassword.setOnClickListener {
            if(passwordGeneratorRules.size == 0 || (passwordGeneratorRules.size == 1 && binding.lengthToggle.isChecked)){
                binding.genPasswordId.error = getString(R.string.noRules)
            }
            else {
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
            binding.generatePassword.animate().rotation(DEFAULT_ROTATION).interpolator = AccelerateDecelerateInterpolator()
        }

        binding.genPasswordId.setOnClickListener {
            copyPassword()
        }

        binding.genPasswordIdField.setOnClickListener {
            copyPassword()
        }

        // Additinal add new password buttons

        binding.noPasswords.setOnClickListener {
            condition=false
            goToNewPasswordActivity()
        }

        binding.extraNewPass.setOnClickListener {
            condition=false
            goToNewPasswordActivity()
        }


        binding.newPass.setOnClickListener {
            condition=false
            goToNewPasswordActivity()
        }

        // получение вью нижнего экрана
        val llBottomSheet = findViewById<LinearLayout>(R.id.allPassword)

        binding.allPassword.translationZ = 24F
        binding.newPass.translationZ = 101F

        // настройка поведения нижнего экрана
        val bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)

        // настройка состояний нижнего экрана
        //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.state = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                .getInt("__BS", BottomSheetBehavior.STATE_COLLAPSED)
        binding.menuUp.animate().rotation(180F * bottomSheetBehavior.state).setDuration(0).start()
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            binding.newPass.animate().scaleX(0F).scaleY(0F).setDuration(0).start()
            binding.warnCard.animate().alpha(1F).setDuration(0).start()
            binding.backupCard.animate().alpha(1F).setDuration(0).start()
        }

        binding.searchPassField.clearFocus()
        binding.searchPassField.hideKeyboard()

        binding.expand.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            with(sharedPref.edit()) {
                putInt("__BS", BottomSheetBehavior.STATE_COLLAPSED)
                apply()
            }
        }

        binding.menuUp.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            with(sharedPref.edit()) {
                putInt("__BS", BottomSheetBehavior.STATE_EXPANDED)
                apply()
            }
        }

        // настройка максимальной высоты
        bottomSheetBehavior.peekHeight =  800 //600

        if (useAnalyze != null)
            if (useAnalyze != "none") {
                bottomSheetBehavior.peekHeight =  1200
            }


        // настройка возможности скрыть элемент при свайпе вниз
        bottomSheetBehavior.isHideable = true

        // настройка колбэков при изменениях

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                with(sharedPref.edit()) {
                    putInt("__BS", newState)
                    apply()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.menuUp.animate().rotation(180F * slideOffset).setDuration(0).start()
                if (slideOffset <= 0) {
                    binding.warnCard.animate().alpha(abs(slideOffset) + 0.5F).setDuration(0).start()
                    binding.backupCard.animate().alpha(abs(slideOffset) + 0.5F).setDuration(0).start()
                    binding.newPass.animate().scaleX(1 - abs(slideOffset)).scaleY(1 - abs(slideOffset))
                            .setDuration(
                                    0
                            ).start()
                }
                binding.searchPassField.clearFocus()
                binding.searchPassField.hideKeyboard()

            }
        })

    }

    private fun loadPasswords(pCursor: Cursor, pdbHelper: PasswordsDataBaseHelper) {

        var dbLogin: String

        if (pCursor.moveToFirst()) {
            val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
            val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
            val aIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_2FA)
            val tagsIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TAGS)
            val groupIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_GROUPS)
            val timeIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TIME)
            val cIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_CIPHER)
            val descIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_DESC)
            var passwordNumInArray = 0
            do {
                val pass = pCursor.getString(passIndex).toString()
                val dbCipherIndex = pCursor.getString(cIndex).toString()
                val dbTimeIndex = pCursor.getString(timeIndex).toString()
                val dbdescIndex = pCursor.getString(descIndex).toString()

                val qualityNum = evaluatePassword(pass, passwordNumInArray, dbCipherIndex, dbTimeIndex)

                passwordNumInArray++

                var groupNone = false
                var groupFavorite = false
                if (pCursor.getString(groupIndex) == null || pCursor.getString(groupIndex) == "null" || pCursor.getString(groupIndex) == "none")
                    groupNone = true
                if(!groupNone){
                    if(pCursor.getString(groupIndex).contains("favorite"))
                        groupFavorite = true
                }

                if(groupFavorite) {
                    Log.d("favorite - ", pCursor.getString(groupIndex))
                    dbLogin = pCursor.getString(nameIndex).toString()
                    val fa = pCursor.getString(aIndex).toString()
                    if(fa == "1")
                        faNum += 1
                    passwords.add(0, Pair(dbLogin, fa))
                    desc.add(0, dbdescIndex)
                    quality.add(0, qualityNum)
                    val dbTag = pCursor.getString(tagsIndex).toString()
                    tags.add(0, dbTag)
                    group.add(0, "#favorite")
                    dates.add(0, dbTimeIndex)
                }
                else{
                    dbLogin = pCursor.getString(nameIndex).toString()
                    val fa = pCursor.getString(aIndex).toString()
                    if(fa == "1")
                        faNum += 1
                    passwords.add(Pair(dbLogin, fa))
                    desc.add(dbdescIndex)
                    quality.add(qualityNum)
                    val dbTag = pCursor.getString(tagsIndex).toString()
                    tags.add(dbTag)
                    group.add("none")
                    dates.add(dbTimeIndex)
                }
                if(dbCipherIndex == "crypted")
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
            } while (pCursor.moveToNext())
        }
        pCursor.close()
    }

    private fun sortByAlphaUp() {
        binding.sortOrder.animate().rotation(0F).setDuration(500).start()
        for (i in 0 until passwords.size){
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
        val passwords2: ArrayList<Pair<String, String>> = ArrayList()
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

    private fun evaluatePassword(password: String, passwordNum: Int, dbCipherIndex: String, dbTimeIndex: String): String{
        val evaluation: Float = pm.evaluatePassword(password)

        var qualityNum = when {
            evaluation < 0.33 -> "2"
            evaluation < 0.66 -> "3"
            else -> "1"
        }

        if (dbCipherIndex == "crypted" )
            qualityNum = "6"

        if (pm.evaluateDate(dbTimeIndex) || realQuality[passwordNum] != "1")
            qualityNum = "2"

        if (dbCipherIndex != "crypted" && password.length == 4)
            qualityNum = "4"

        if (pm.popularPasswords(password)
            or ((password.length == 4)
                    and pm.popularPin(password))){
            qualityNum = if (qualityNum == "4")
                "5"
            else
                "2"
        }

        return qualityNum
    }

    private fun getDataBase(pdbHelper: PasswordsDataBaseHelper, pDatabase: SQLiteDatabase, orderBy: String?): Cursor {
        return pDatabase.query(
                pdbHelper.TABLE_USERS, arrayOf(
                pdbHelper.KEY_NAME, pdbHelper.KEY_PASS,
                pdbHelper.KEY_TIME, pdbHelper.KEY_2FA,
                pdbHelper.KEY_TAGS, pdbHelper.KEY_GROUPS,
                pdbHelper.KEY_USE_TIME, pdbHelper.KEY_CIPHER,
                pdbHelper.KEY_DESC
        ),
                null, null,
                null, null, orderBy
        )
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
        val intent = Intent(this, NewPasswordActivity::class.java)
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
                    circlePositive = R.drawable.circle_positive)
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
                    circlePositive = R.drawable.circle_positive)
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
                    circlePositive = R.drawable.circle_positive_fill)
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
        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase
        val contentValues = ContentValues()
        if(group[position]=="#favorite")
            contentValues.put(pdbHelper.KEY_GROUPS, "none")
        else
            contentValues.put(pdbHelper.KEY_GROUPS, "#favorite")
        pDatabase.update(
                pdbHelper.TABLE_USERS, contentValues,
                "NAME = ?",
                arrayOf(passwordsG[position].first)
        )

        clearContainers()



        try {
            var pCursor: Cursor = getDataBase(pdbHelper, pDatabase, null)

            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                do {
                    val pass = pCursor.getString(passIndex).toString()
                    val login = pCursor.getString(nameIndex).toString()
                    realPass.add(Pair(login, pass))
                } while (pCursor.moveToNext())
            }

            analyzeDataBase()

            // Second scan to set quality
            pCursor = getDataBase(pdbHelper, pDatabase, pdbHelper.KEY_NAME)
            loadPasswords(pCursor, pdbHelper)

        } catch (e: SQLException) {
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

                try {
                    var pCursor: Cursor = getDataBase(pdbHelper, pDatabase, null)

                    if (pCursor.moveToFirst()) {
                        val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                        val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                        do {
                            val pass = pCursor.getString(passIndex).toString()
                            val login = pCursor.getString(nameIndex).toString()
                            realPass.add(Pair(login, pass))
                        } while (pCursor.moveToNext())
                    }

                    analyzeDataBase()

                    // Second scan to set quality
                    pCursor = getDataBase(pdbHelper, pDatabase, pdbHelper.KEY_NAME)
                    loadPasswords(pCursor, pdbHelper)

                } catch (e: SQLException) {
                }

                if(passwords.size == 0){
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

    override fun onResume(){
        super.onResume()
        if (binding.lengthToggle.isChecked)
            binding.seekBar.visibility =  View.VISIBLE
    }

}