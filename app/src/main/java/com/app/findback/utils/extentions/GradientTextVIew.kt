package com.app.findback.utils.extentions

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet

class GradientTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    private var startColor = Color.parseColor("#1E88E5")
    private var endColor = Color.parseColor("#43A047")


    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        val shader = LinearGradient(
            0f, 0f,
            width.toFloat(), 0f,
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
    }
}