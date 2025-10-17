package com.kblack.project_base.utils

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("clickSafe")
fun View.setClickSafe(listener: View.OnClickListener?) {
    setOnClickListener(object : View.OnClickListener {
        var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (System.currentTimeMillis() - lastClickTime < 250) return
            listener?.onClick(v)
            lastClickTime = System.currentTimeMillis()
        }
    })
}

@BindingAdapter("onSingleClick")
fun View.setSingleClick(execution: () -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        var lastClickTime: Long = 0

        override fun onClick(p0: View?) {
            if (System.currentTimeMillis() - lastClickTime < 250) return
            lastClickTime = System.currentTimeMillis()
            execution.invoke()
        }
    })
}