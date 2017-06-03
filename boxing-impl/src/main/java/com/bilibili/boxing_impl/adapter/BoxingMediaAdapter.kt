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

package com.bilibili.boxing_impl.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing_impl.BoxingResHelper
import com.bilibili.boxing_impl.R
import com.bilibili.boxing_impl.view.MediaItemLayout
import java.util.*


/**
 * A RecyclerView.Adapter for image or video picker showing.

 * @author ChenSL
 */
class BoxingMediaAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mOffset: Int
    private val mMultiImageMode: Boolean

    private val mMedias: MutableList<BaseMedia>
    private val mSelectedMedias: MutableList<BaseMedia>
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mMediaConfig: BoxingConfig
    private var mOnCameraClickListener: View.OnClickListener? = null
    private var mOnMediaClickListener: View.OnClickListener? = null
    private val mOnCheckListener: OnCheckListener
    private var mOnCheckedListener: OnMediaCheckedListener? = null
    private val mDefaultRes: Int

    init {
        this.mMedias = ArrayList<BaseMedia>()
        this.mSelectedMedias = ArrayList<BaseMedia>()
        this.mMediaConfig = BoxingManager.instance.boxingConfig
        this.mOffset = if (mMediaConfig.isNeedCamera) 1 else 0
        this.mMultiImageMode = mMediaConfig.mode === BoxingConfig.Mode.MULTI_IMG
        this.mOnCheckListener = OnCheckListener()
        this.mDefaultRes = mMediaConfig.mediaPlaceHolderRes
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && mMediaConfig.isNeedCamera) {
            return CAMERA_TYPE
        }
        return NORMAL_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (CAMERA_TYPE == viewType) {
            return CameraViewHolder(mInflater.inflate(R.layout.layout_boxing_recycleview_header, parent, false))
        }
        return ImageViewHolder(mInflater.inflate(R.layout.layout_boxing_recycleview_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CameraViewHolder) {
            val viewHolder = holder
            viewHolder.mCameraLayout.setOnClickListener(mOnCameraClickListener)
            viewHolder.mCameraImg.setImageResource(BoxingResHelper.cameraRes)
        } else {
            val pos = position - mOffset
            val media = mMedias[pos]
            val vh = holder as ImageViewHolder

            vh.mItemLayout.setImageRes(mDefaultRes)
            vh.mItemLayout.tag = media

            vh.mItemLayout.setOnClickListener(mOnMediaClickListener)
            vh.mItemLayout.setTag(R.id.media_item_check, pos)
            vh.mItemLayout.setMedia(media)
            vh.mItemChecked.visibility = if (mMultiImageMode) View.VISIBLE else View.GONE
            if (mMultiImageMode && media is ImageMedia) {
                vh.mItemLayout.setChecked(media.isSelected)
                vh.mItemChecked.setTag(R.id.media_layout, vh.mItemLayout)
                vh.mItemChecked.tag = media
                vh.mItemChecked.setOnClickListener(mOnCheckListener)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return mMedias.size + mOffset
    }

    fun setOnCameraClickListener(onCameraClickListener: View.OnClickListener) {
        mOnCameraClickListener = onCameraClickListener
    }

    fun setOnCheckedListener(onCheckedListener: OnMediaCheckedListener) {
        mOnCheckedListener = onCheckedListener
    }

    fun setOnMediaClickListener(onMediaClickListener: View.OnClickListener) {
        mOnMediaClickListener = onMediaClickListener
    }

    var selectedMedias: MutableList<BaseMedia>
        get() = mSelectedMedias
        set(selectedMedias) {
            mSelectedMedias.clear()
            selectedMedias?.let { mSelectedMedias.addAll(it)}
        }

    fun addAllData(data: List<BaseMedia>) {
        this.mMedias.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(media: BaseMedia) {
        this.mMedias.add(media)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.mMedias.clear()
    }

    val allMedias: List<BaseMedia>
        get() = mMedias

    private class ImageViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var mItemLayout : MediaItemLayout = itemView.findViewById(R.id.media_layout) as MediaItemLayout
        internal var mItemChecked: View = itemView.findViewById(R.id.media_item_check)

    }

    private class CameraViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var mCameraLayout: View = itemView.findViewById(R.id.camera_layout)
        internal var mCameraImg: ImageView = itemView.findViewById(R.id.camera_img) as ImageView

    }

    private inner class OnCheckListener : View.OnClickListener {

        override fun onClick(v: View) {
            val itemLayout = v.getTag(R.id.media_layout) as MediaItemLayout
            val media = v.tag as BaseMedia
            if (mMediaConfig.mode === BoxingConfig.Mode.MULTI_IMG) {
                if (mOnCheckedListener != null) {
                    mOnCheckedListener!!.onChecked(itemLayout, media)
                }
            }
        }
    }

    interface OnMediaCheckedListener {
        /**
         * In multi image mode, selecting a [BaseMedia] or undo.
         */
        fun onChecked(v: View, iMedia: BaseMedia)
    }

    companion object {
        private val CAMERA_TYPE = 0
        private val NORMAL_TYPE = 1
    }

}
