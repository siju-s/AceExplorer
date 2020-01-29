package com.siju.acexplorer.premium

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.R
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper.AlertDialogListener
import com.siju.acexplorer.main.viewmodel.MainViewModel

class Premium(private val activity : AppCompatActivity, private val mainViewModel: MainViewModel) {

    fun showPremiumDialog(context: Context) {
        val text = arrayOf(context.getString(R.string.unlock_full_version),
                context.getString(R.string.full_version_buy_ask),
                context.getString(R.string.yes),
                context.getString(R.string.no))
        DialogHelper.showAlertDialog(context, text, alertDialogListener)
    }


    private val alertDialogListener: AlertDialogListener = object : AlertDialogListener {
        override fun onPositiveButtonClick(view: View) {
            showPurchaseDialog(activity)
        }
        override fun onNegativeButtonClick(view: View) {
            optOutPremiumDialog()
        }
        override fun onNeutralButtonClick(view: View) {}
    }


    private fun showPurchaseDialog(activity: AppCompatActivity) {
        mainViewModel.buyPremiumVersion(activity)
    }


    private fun optOutPremiumDialog() {
        val pref: SharedPreferences = activity.getSharedPreferences(PremiumUtils.PREF_NAME, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(PremiumUtils.KEY_OPT_OUT, true).apply()
    }
}