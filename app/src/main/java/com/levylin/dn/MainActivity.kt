package com.levylin.dn

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        show_popup_btn1.setOnClickListener({ v -> showPopup(v) })
        show_popup_btn2.setOnClickListener({ v -> showPopup(v) })
        show_popup_btn3.setOnClickListener({ v -> showPopup(v) })
        show_popup_btn4.setOnClickListener({ v -> showPopup(v) })
    }

    private fun showPopup(view: View) {
        Log.e("Test","showPopup"+view)
        val popupWindow = PopupWindow(this)
        val textView = TextView(this)
        textView.height = 200
        textView.width = 200
        textView.text = "弹框"
        popupWindow.contentView = textView
        popupWindow.isFocusable = true
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        popupWindow.showAsDropDown(view)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
