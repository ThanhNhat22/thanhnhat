package com.app.findback.ui.components.toolbar

import com.app.findback.ui.fragments.MapFragment
import com.app.findback.ui.viewmodel.PostViewModel
import kotlin.properties.ReadOnlyProperty

interface ToolbarConfigProvider {
	fun toolbarConfig(): ToolbarConfig
}

