package com.siju.acexplorer.home.types

import com.siju.acexplorer.home.view.CategoryMenuHelper
import com.siju.acexplorer.main.model.groups.Category

data class CategoryData(val path : String?, val category : Category, val title : String, val categoryMenuHelper: CategoryMenuHelper)