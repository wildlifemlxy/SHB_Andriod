package com.ecss.shb_andriod.base

sealed class DrawerItem {
    data class Group(
        val title: String,
        val iconRes: Int,
        val children: List<Child>,
        var expanded: Boolean = false
    ) : DrawerItem()

    data class Child(
        val title: String,
        val iconRes: Int,
        val id: Int
    ) : DrawerItem()

    data class Simple(
        val title: String,
        val iconRes: Int,
        val id: Int
    ) : DrawerItem()
}

