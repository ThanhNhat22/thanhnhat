package com.app.findback.ui.components.toolbar

data class ToolbarConfig(
    val backgroudResId: Int? = null,
    val titleResId: Int,
    val isShowSearch: Boolean = false,
    val isBack: Boolean = false,
    val imageLogoRes: Int? = null,
    val ib1Res: Int? = null,
    val ib2Res: Int? = null,
    val onIB1: (() -> Unit)? = null,
    val onIB2: (() -> Unit)? = null
)


