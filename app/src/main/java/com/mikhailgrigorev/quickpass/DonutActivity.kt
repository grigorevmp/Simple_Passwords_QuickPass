package com.mikhailgrigorev.quickpass

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.anjlab.android.iab.v3.TransactionDetails
import kotlinx.android.synthetic.main.activity_about.back
import kotlinx.android.synthetic.main.activity_donut.*


class DonutActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donut)
        val mBillingProcessor = BillingProcessor(this, GPLAY_LICENSE, this)

        back.setOnClickListener {
            finish()
        }


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
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
    }

    override fun onBillingInitialized() {
    }

    private fun showMsg(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}