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
import android.widget.TextView
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.entity.AlbumEntity
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing_impl.R
import java.util.*


/**
 * Album window adapter.

 * @author ChenSL
 */
class BoxingAlbumAdapter(context: Context) : RecyclerView.Adapter<BoxingAlbumAdapter.AlbumViewHolder>(), View.OnClickListener {
    override fun onBindViewHolder(holder: AlbumViewHolder?, position: Int) {
        val albumViewHolder = holder as AlbumViewHolder
        albumViewHolder.mCoverImg.setImageResource(mDefaultRes)
        val adapterPos = holder.adapterPosition
        val album = mAlums[adapterPos]

        if (album.hasImages()) {
            albumViewHolder.mNameTxt.text = album.mBucketName
            val media = album.mImageList[0] as ImageMedia
            BoxingMediaLoader.instance.displayThumbnail(albumViewHolder.mCoverImg, media.path, 50, 50)
            albumViewHolder.mCoverImg.setTag(R.string.boxing_app_name, media.path)
            albumViewHolder.mLayout.tag = adapterPos
            albumViewHolder.mLayout.setOnClickListener(this)
            albumViewHolder.mCheckedImg.visibility = if (album.mIsSelected) View.VISIBLE else View.GONE
            albumViewHolder.mSizeTxt.text = albumViewHolder.mSizeTxt.resources.getString(R.string.boxing_album_images_fmt, album.mCount)
        } else {
            albumViewHolder.mNameTxt.text = UNKNOW_ALBUM_NAME
            albumViewHolder.mSizeTxt.visibility = View.GONE
        }
    }


    var currentAlbumPos: Int = 0

    private val mAlums: MutableList<AlbumEntity>
    private val mInflater: LayoutInflater
    private var mAlbumOnClickListener: OnAlbumClickListener? = null
    private val mDefaultRes: Int

    init {
        this.mAlums = ArrayList<AlbumEntity>()
        this.mAlums.add(AlbumEntity.createDefaultAlbum())
        this.mInflater = LayoutInflater.from(context)
        this.mDefaultRes = BoxingManager.instance.boxingConfig.albumPlaceHolderRes
    }

    fun setAlbumOnClickListener(albumOnClickListener: OnAlbumClickListener) {
        this.mAlbumOnClickListener = albumOnClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(mInflater.inflate(R.layout.layout_boxing_album_item, parent, false))
    }

    fun addAllData(alums: List<AlbumEntity>) {
        mAlums.clear()
        mAlums.addAll(alums)
        notifyDataSetChanged()
    }

    val alums: List<AlbumEntity>
        get() = mAlums

    val currentAlbum: AlbumEntity?
        get() {
            if (mAlums.size <= 0) {
                return null
            }
            return mAlums[currentAlbumPos]
        }

    override fun getItemCount(): Int {
        return mAlums.size
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.album_layout) {
            mAlbumOnClickListener?.onClick(v, v.tag as Int)
        }
    }

    class AlbumViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var mCoverImg: ImageView = itemView.findViewById(R.id.album_thumbnail) as ImageView
        internal var mNameTxt: TextView = itemView.findViewById(R.id.album_name) as TextView
        internal var mSizeTxt: TextView = itemView.findViewById(R.id.album_size) as TextView
        internal var mLayout: View = itemView.findViewById(R.id.album_layout)
        internal var mCheckedImg: ImageView = itemView.findViewById(R.id.album_checked) as ImageView

    }

    interface OnAlbumClickListener {
        fun onClick(view: View, pos: Int)
    }

    companion object {
        private val UNKNOW_ALBUM_NAME = "?"
    }
}
