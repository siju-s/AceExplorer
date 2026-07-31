package com.siju.acexplorer.tools

import com.siju.acexplorer.main.model.groups.Category

/**
 * An entry on the Tools screen. [subtitle] is optional and its row stays hidden when null.
 */
class ToolsInfo(
    val category: Category,
    val icon: Int,
    val text: String,
    val subtitle: String? = null
)
