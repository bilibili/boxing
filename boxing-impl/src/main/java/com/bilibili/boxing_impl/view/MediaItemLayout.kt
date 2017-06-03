/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.bilibili.boxing_impl.view

import android.content.Context
import android.content.res.Configuration
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.model.entity.impl.VideoMedia
import com.bilibili.boxing_impl.BoxingResHelper
import com.bilibili.boxing_impl.R
import com.bilibili.boxing_impl.WindowManagerHelper


/**
 * A media layout for [android.support.v7.widget.RecyclerView] item, including image and video <br></br>

 * @author ChenSL
 */
class MediaItemLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val mCheckImg: ImageView
    private val mVideoLayout: View
    private val mFontLayout: View
    private val mCoverImg: ImageView
    private val mScreenType: ScreenType

    private enum class ScreenType private constructor(value: Int) {
        SMALL(100), NORMAL(180), LARGE(320);

        var value: Int = 0
            internal set

        init {
            this.value = value
        }
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_boxing_media_item, this, true)
        mCoverImg = view.findViewById(R.id.media_item) as ImageView
        mCheckImg = view.findViewById(R.id.media_item_check) as ImageView
        mVideoLayout = view.findViewById(R.id.video_layout)
        mFontLayout = view.findViewById(R.id.media_font_layout)
        mScreenType = getScreenType(context)
        setImageRect(context)
    }

    private fun setImageRect(context: Context) {
        val screenHeight = WindowManagerHelper.getScreenHeight(context)
        val screenWidth = WindowManagerHelper.getScreenWidth(context)
        var width = 100
        if (screenHeight != 0 && screenWidth != 0) {
            width = (screenWidth - resources.getDimensionPixelOffset(R.dimen.boxing_media_margin) * 4) / 3
        }
        mCoverImg.layoutParams.width = width
        mCoverImg.layoutParams.height = width
        mFontLayout.layoutParams.width = width
        mFontLayout.layoutParams.height = width
    }

    private fun getScreenType(context: Context): ScreenType {
        val type = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val result: ScreenType
        when (type) {
            Configuration.SCREENLAYOUT_SIZE_SMALL -> result = ScreenType.SMALL
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> result = ScreenType.NORMAL
            Configuration.SCREENLAYOUT_SIZE_LARGE -> result = ScreenType.LARGE
            else -> result = ScreenType.NORMAL
        }
        return result
    }

    fun setImageRes(@DrawableRes imageRes: Int) {
        mCoverImg.setImageResource(imageRes)
    }

    fun setMedia(media: BaseMedia) {
        if (media is ImageMedia) {
            mVideoLayout.visibility = View.GONE
            setCover(media.thumbnailPath)
        } else if (media is VideoMedia) {
            mVideoLayout.visibility = View.VISIBLE
            val videoMedia = media
            val durationTxt = mVideoLayout.findViewById(R.id.video_duration_txt) as TextView
            durationTxt.text = videoMedia.duration
            durationTxt.setCompoundDrawablesWithIntrinsicBounds(BoxingManager.instance.boxingConfig!!.videoDurationRes, 0, 0, 0)
            (mVideoLayout.findViewById(R.id.video_size_txt) as TextView).text = videoMedia.sizeByUnit
            setCover(videoMedia.path)
        }
    }

    private fun setCover(path: String) {
        mCoverImg.setTag(R.string.boxing_app_name, path)
        BoxingMediaLoader.instance.displayThumbnail(mCoverImg, path, mScreenType.value, mScreenType.value)
    }

    fun setChecked(isChecked: Boolean) {
        if (isChecked) {
            mFontLayout.visibility = View.VISIBLE
            mCheckImg.setImageDrawable(resources.getDrawable(BoxingResHelper.mediaCheckedRes))
        } else {
            mFontLayout.visibility = View.GONE
            mCheckImg.setImageDrawable(resources.getDrawable(BoxingResHelper.mediaUncheckedRes))
        }
    }

    companion object {
        private val BIG_IMG_SIZE = 5 * 1024 * 1024
    }

}
