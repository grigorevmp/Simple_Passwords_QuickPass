package com.mikhailgrigorev.quickpassword.ui.donut

//import com.anjlab.android.iab.v3.TransactionDetails
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.GPLAY_LICENSE
import com.mikhailgrigorev.quickpassword.databinding.ActivityDonutBinding


private const val preferenceFile = "quickPassPreference"
private const val keyTheme = "themePreference"

class DonutActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    private lateinit var binding: ActivityDonutBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mBillingProcessor = BillingProcessor(this, GPLAY_LICENSE, this)

        binding.back.setOnClickListener {
            finish()
        }
        mBillingProcessor.initialize()
        //mBillingProcessor.loadOwnedPurchasesFromGoogle()

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
            binding.CoffeeText.text = binding.CoffeeText.text.toString() + " " +  getString(R.string.thxText)
            binding.coffeeDonut.isClickable = false
            binding.coffeeDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("apple_pack")){
            binding.appleText.text = binding.appleText.text.toString() + " " +  getString(R.string.thxText)
            binding.appleDonut.isClickable = false
            binding.appleDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("burger")){
            binding.burgerText.text = binding.burgerText.text.toString() + " " +  getString(R.string.thxText)
            binding.burgerDonut.isClickable = false
            binding.burgerDonut.isFocusable = false
        }
        if (mBillingProcessor.isPurchased("dinner")){
            binding.dinnerText.text = binding.dinnerText.text.toString() + " " + getString(R.string.thxText)
            binding.foodDonut.isClickable = false
            binding.foodDonut.isFocusable = false
        }


    }

    /*override fun onProductPurchased(productId: String, details: TransactionDetails?) {
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
    }*/

    private fun checkIfPurchaseIsValid(purchaseInfo: PurchaseInfo): Boolean {
        Log.d("Valid", purchaseInfo.purchaseData.orderId)
        return true
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        TODO("Not yet implemented")
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