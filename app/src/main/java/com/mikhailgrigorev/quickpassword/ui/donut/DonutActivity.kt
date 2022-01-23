package com.mikhailgrigorev.quickpassword.ui.donut

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.anjlab.android.iab.v3.TransactionDetails
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.databinding.ActivityDonutBinding
import com.mikhailgrigorev.quickpassword.common.utils.GPLAY_LICENSE
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity

private const val preferenceFile = "quickPassPreference"
var condition = true
private const val keyTheme = "themePreference"

class DonutActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    private lateinit var binding: ActivityDonutBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(preferenceFile, Context.MODE_PRIVATE)
        when(pref.getString(keyTheme, "none")){
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
            else -> setTheme(R.style.Theme_QP)
        }
        super.onCreate(savedInstanceState)
        // Finish app after some time
        val handler = Handler(Looper.getMainLooper())
        val r = Runnable {
            if(condition) {
                condition=false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        val time: Long =  100000
        val sharedPref = getSharedPreferences(preferenceFile, Context.MODE_PRIVATE)
        val lockTime = sharedPref.getString("appLockTime", "6")
        if(lockTime != null) {
            if (lockTime != "0")
                handler.postDelayed(r, time * lockTime.toLong())
        }
        else
            handler.postDelayed(r, time*6L)


        binding = ActivityDonutBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val cardRadius = sharedPref.getString("cardRadius", "none")
        if(cardRadius != null)
            if(cardRadius != "none") {
                binding.coffeeDonut.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.appleDonut.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.burgerDonut.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.foodDonut.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
            }

        val mBillingProcessor = BillingProcessor(this, GPLAY_LICENSE, this)

        binding.back.setOnClickListener {
            finish()
        }
        mBillingProcessor.initialize()
        mBillingProcessor.loadOwnedPurchasesFromGoogle()

        binding.coffeeDonut.setOnClickListener {
            mBillingProcessor.purchase(this, "cup_of_coffee")
        }
        binding.appleDonut.setOnClickListener {
            mBillingProcessor.purchase(this, "apple_pack")
        }
        binding.burgerDonut.setOnClickListener {
            mBillingProcessor.purchase(this, "burger")
        }
        binding.foodDonut.setOnClickListener {
            mBillingProcessor.purchase(this, "dinner")
        }

        if (mBillingProcessor.isPurchased("cup_of_coffee")){
            binding.CoffeeText.text = binding.CoffeeText.text.toString() + " " +  getString(R.string.thx)
            binding.coffeeDonut.isClickable = false
            binding.coffeeDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("apple_pack")){
            binding.appleText.text = binding.appleText.text.toString() + " " +  getString(R.string.thx)
            binding.appleDonut.isClickable = false
            binding.appleDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("burger")){
            binding.burgerText.text = binding.burgerText.text.toString() + " " +  getString(R.string.thx)
            binding.burgerDonut.isClickable = false
            binding.burgerDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("dinner")){
            binding.dinnerText.text = binding.dinnerText.text.toString() + " " + getString(R.string.thx)
            binding.foodDonut.isClickable = false
            binding.foodDonut.isFocusable = false
        }


    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        //showMsg("П")
        if (checkIfPurchaseIsValid(details!!.purchaseInfo)) {
            showMsg("purchase: $productId COMPLETED")
            //when (productId) {
            //    //ONE_TIME_PAYMENT -> setupConsumableButtons(true)
            //    //SUBSCRIPTION -> setupSubscription(true)
            //}
        } else {
            showMsg("fakePayment")
        }
    }

    private fun checkIfPurchaseIsValid(purchaseInfo: PurchaseInfo): Boolean {
        Log.d("Valid", purchaseInfo.purchaseData.orderId)
        return true
    }

    override fun onPurchaseHistoryRestored() {
        //showMsg("onPurchaseHistoryRestored")
        handleLoadedItems()
    }

    @SuppressLint("SetTextI18n")
    private fun handleLoadedItems() {

    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
    }

    override fun onBillingInitialized() {
    }

    private fun showMsg(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}