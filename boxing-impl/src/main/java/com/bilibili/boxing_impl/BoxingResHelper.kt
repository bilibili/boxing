package com.bilibili.boxing_impl

import android.support.annotation.DrawableRes

import com.bilibili.boxing.model.BoxingManager

/**
 * Help getting the resource in config.

 * @author ChenSL
 */

object BoxingResHelper {

    val mediaCheckedRes: Int
        @DrawableRes
        get() {
            val result = BoxingManager.instance.boxingConfig!!.mediaCheckedRes
            return if (result > 0) result else R.drawable.ic_boxing_checked
        }

    val mediaUncheckedRes: Int
        @DrawableRes
        get() {
            val result = BoxingManager.instance.boxingConfig!!.mediaUnCheckedRes
            return if (result > 0) result else R.drawable.shape_boxing_unchecked
        }

    val cameraRes: Int
        @DrawableRes
        get() = BoxingManager.instance.boxingConfig!!.cameraRes
}
