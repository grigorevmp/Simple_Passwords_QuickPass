package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.anjlab.android.iab.v3.TransactionDetails
import kotlinx.android.synthetic.main.activity_donut.*

lateinit var mBillingProcessor: BillingProcessor

private val _preferenceFile = "quickPassPreference"
var condition = true
private val _keyTheme = "themePreference"

class DonutActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    @SuppressLint("SetTextI18n")
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
        // Finish app after some time
        val handler = Handler()
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


        setContentView(R.layout.activity_donut)
        val mBillingProcessor = BillingProcessor(this, GPLAY_LICENSE, this)

        back.setOnClickListener {
            finish()
        }
        mBillingProcessor.initialize()
        mBillingProcessor.loadOwnedPurchasesFromGoogle()

        coffeeDonut.setOnClickListener {
            mBillingProcessor.purchase(this, "cup_of_coffee")
        }
        appleDonut.setOnClickListener {
            mBillingProcessor.purchase(this, "apple_pack")
        }
        burgerDonut.setOnClickListener {
            mBillingProcessor.purchase(this, "burger")
        }
        foodDonut.setOnClickListener {
            mBillingProcessor.purchase(this, "dinner")
        }

        if (mBillingProcessor.isPurchased("cup_of_coffee")){
            CoffeeText.text = CoffeeText.text.toString() + " " +  getString(R.string.thx)
            coffeeDonut.isClickable = false
            coffeeDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("apple_pack")){
            appleText.text = appleText.text.toString() + " " +  getString(R.string.thx)
            appleDonut.isClickable = false
            appleDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("burger")){
            burgerText.text = burgerText.text.toString() + " " +  getString(R.string.thx)
            burgerDonut.isClickable = false
            burgerDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("dinner")){
            dinnerText.text = dinnerText.text.toString() + " " + getString(R.string.thx)
            foodDonut.isClickable = false
            foodDonut.isFocusable = false
        }


    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        showMsg("onProductPurchased")
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
        return true
    }

    override fun onPurchaseHistoryRestored() {
        showMsg("onPurchaseHistoryRestored")
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