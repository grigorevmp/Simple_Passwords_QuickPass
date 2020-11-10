package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.anjlab.android.iab.v3.TransactionDetails
import kotlinx.android.synthetic.main.activity_donut.*

lateinit var mBillingProcessor: BillingProcessor

class DonutActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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