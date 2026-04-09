package com.app.findback.utils.extentions

import android.content.res.Resources

class ExtensionDp {
    companion object {
        fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
        fun Int.pxToDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    }
}