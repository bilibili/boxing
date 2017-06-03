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

package com.bilibili.boxing.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bilibili.boxing.AbsBoxingActivity
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.demo.R
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.config.BoxingCropOption
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.utils.BoxingFileHelper
import com.bilibili.boxing.utils.ImageCompressor
import com.bilibili.boxing_impl.ui.BoxingActivity
import com.bilibili.boxing_impl.ui.BoxingBottomSheetActivity
import com.bilibili.boxing_impl.view.SpacesItemDecoration
import java.util.*

/**
 * A demo to show how to use [AbsBoxingActivity] and all the functions.

 * @author ChenSL
 */
class FirstActivity : AppCompatActivity(), View.OnClickListener {
    lateinit private var mRecyclerView: RecyclerView
    lateinit private var mAdapter: MediaResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        createToolbar()

        findViewById(R.id.single_image_btn).setOnClickListener(this)
        findViewById(R.id.single_image_btn_crop_btn).setOnClickListener(this)
        findViewById(R.id.multi_image_btn).setOnClickListener(this)
        findViewById(R.id.video_btn).setOnClickListener(this)
        findViewById(R.id.outside_bs_btn).setOnClickListener(this)

        mRecyclerView = findViewById(R.id.media_recycle_view) as RecyclerView
        mAdapter = MediaResultAdapter()
        mRecyclerView.layoutManager = GridLayoutManager(mRecyclerView.context, 3)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.addItemDecoration(SpacesItemDecoration(8))
        mRecyclerView.setOnClickListener(this)
    }

    private fun createToolbar() {
        val bar = findViewById(R.id.nav_top_bar) as Toolbar
        setSupportActionBar(bar)
        supportActionBar!!.setTitle(R.string.first_demo_title)
        bar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.single_image_btn -> {
                val singleImgConfig = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG).withMediaPlaceHolderRes(R.drawable.ic_boxing_default_image)
                Boxing.of(singleImgConfig).withIntent(v.context, BoxingActivity::class.java).start(this, COMPRESS_REQUEST_CODE)
            }
            R.id.single_image_btn_crop_btn -> {
                val cachePath = BoxingFileHelper.getCacheDir(v.context)
                if (TextUtils.isEmpty(cachePath)) {
                    Toast.makeText(v.context, R.string.boxing_storage_deny, Toast.LENGTH_SHORT).show()
                    return
                }
                val destUri = Uri.Builder()
                        .scheme("file")
                        .appendPath(cachePath)
                        .appendPath(String.format(Locale.US, "%s.jpg", System.currentTimeMillis()))
                        .build()
                val singleCropImgConfig = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG).withCropOption(BoxingCropOption(destUri))
                        .withMediaPlaceHolderRes(R.drawable.ic_boxing_default_image)
                Boxing.of(singleCropImgConfig).withIntent(v.context, BoxingActivity::class.java).start(this, REQUEST_CODE)
            }
            R.id.multi_image_btn -> {
                val config = BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needCamera(R.drawable.ic_boxing_camera_white).needGif()
                Boxing.of(config).withIntent(v.context, BoxingActivity::class.java).start(this, REQUEST_CODE)
            }
            R.id.video_btn -> {
                val videoConfig = BoxingConfig(BoxingConfig.Mode.VIDEO).withVideoDurationRes(R.drawable.ic_boxing_play)
                Boxing.of(videoConfig).withIntent(v.context, BoxingActivity::class.java).start(this, REQUEST_CODE)
            }
            R.id.outside_bs_btn -> {
                val bsConfig = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
                Boxing.of(bsConfig).withIntent(v.context, BoxingBottomSheetActivity::class.java).start(this, REQUEST_CODE)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            mRecyclerView.visibility = View.VISIBLE
            val medias = Boxing.getResult(data)
            if (requestCode == REQUEST_CODE) {
                mAdapter.setList(medias)
            } else if (requestCode == COMPRESS_REQUEST_CODE) {
                val imageMedias = ArrayList<BaseMedia>(1)
                val baseMedia = medias[0] as? ImageMedia ?: return

                val imageMedia = baseMedia
                // the compress task may need time
                if (imageMedia.compress(ImageCompressor(mRecyclerView.context))) {
                    imageMedia.removeExif()
                    imageMedias.add(imageMedia)
                    mAdapter.setList(imageMedias)
                }

            }
        }
    }

    private inner class MediaResultAdapter internal constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val mList: ArrayList<BaseMedia>?

        init {
            mList = ArrayList<BaseMedia>()
        }

        internal fun setList(list: List<BaseMedia>) {
            mList?.clear()
            mList?.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_boxing_simple_media_item, parent, false)
            val height = parent.measuredHeight / 4
            view.minimumHeight = height
            return MediaViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MediaViewHolder) {
                val mediaViewHolder = holder
                mediaViewHolder.mImageView.setImageResource(BoxingManager.instance.boxingConfig.mediaPlaceHolderRes)
                val media = mList!![position]
                val path: String
                if (media is ImageMedia) {
                    path = media.thumbnailPath
                } else {
                    path = media.path
                }
                BoxingMediaLoader.instance.displayThumbnail(mediaViewHolder.mImageView, path, 150, 150)
                mediaViewHolder.itemView.tag = position
            }
        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }

    }

    private inner class MediaViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImageView: ImageView = itemView.findViewById(R.id.media_item) as ImageView

    }

    companion object {
        private val REQUEST_CODE = 1024
        private val COMPRESS_REQUEST_CODE = 2048
    }

}
