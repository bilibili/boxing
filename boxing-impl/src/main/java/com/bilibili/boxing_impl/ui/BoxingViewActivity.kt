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

package com.bilibili.boxing_impl.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.bilibili.boxing.AbsBoxingViewActivity
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.model.task.IMediaTask
import com.bilibili.boxing_impl.BoxingResHelper
import com.bilibili.boxing_impl.R
import com.bilibili.boxing_impl.view.HackyViewPager
import java.util.*

/**
 * An Activity to show raw image by holding [BoxingViewFragment].

 * @author ChenSL
 */
open class BoxingViewActivity : AbsBoxingViewActivity() {

    lateinit var mGallery: HackyViewPager
    lateinit var mProgressBar: ProgressBar

    private var mNeedEdit: Boolean = false
    private var mNeedLoading: Boolean = false
    private var mFinishLoading: Boolean = false
    private var mNeedAllCount = true
    private var mCurrentPage: Int = 0
    private var mTotalCount: Int = 0
    private var mStartPos: Int = 0
    private var mPos: Int = 0
    private var mMaxCount: Int = 0

    private var mAlbumId: String = ""
    lateinit private var mToolbar: Toolbar
    lateinit private var mAdapter: ImagesAdapter
    private var mCurrentImageItem: ImageMedia? = null
    private var mOkBtn: Button? = null
    private var mImages: ArrayList<BaseMedia> = ArrayList()
    private var mSelectedImages: ArrayList<BaseMedia> = selectedImages
    private var mSelectedMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boxing_view)
        createToolbar()
        initData()
        initView()
        startLoading()
    }

    private fun createToolbar() {
        mToolbar = findViewById(R.id.nav_top_bar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mToolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initData() {
        mSelectedImages = selectedImages
        mAlbumId = albumId
        mStartPos = startPos
        mNeedLoading = BoxingManager.instance.boxingConfig.isNeedLoading
        mNeedEdit = BoxingManager.instance.boxingConfig.isNeedEdit
        mMaxCount = maxCount
        if (!mNeedLoading) {
            mImages.addAll(mSelectedImages)
        }
    }

    private fun initView() {
        mAdapter = ImagesAdapter(supportFragmentManager)
        mOkBtn = findViewById(R.id.image_items_ok) as Button
        mGallery = findViewById(R.id.pager) as HackyViewPager
        mProgressBar = findViewById(R.id.loading) as ProgressBar
        mGallery.adapter = mAdapter
        mGallery.addOnPageChangeListener(OnPagerChangeListener())
        if (!mNeedEdit) {
            val chooseLayout = findViewById(R.id.item_choose_layout)
            chooseLayout.visibility = View.GONE
        } else {
            setOkTextNumber()
            mOkBtn!!.setOnClickListener { finishByBackPressed(false) }
        }
    }

    private fun setOkTextNumber() {
        if (mNeedEdit) {
            val selectedSize = mSelectedImages.size
            val size = Math.max(mSelectedImages.size, mMaxCount)
            mOkBtn!!.text = getString(R.string.boxing_image_preview_ok_fmt, selectedSize.toString(), size.toString())
            mOkBtn!!.isEnabled = selectedSize > 0
        }
    }

    private fun finishByBackPressed(value: Boolean) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA, mSelectedImages)
        intent.putExtra(EXTRA_TYPE_BACK, value)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        if (mNeedEdit) {
            menuInflater.inflate(R.menu.activity_boxing_image_viewer, menu)
            mSelectedMenuItem = menu.findItem(R.id.menu_image_item_selected)
            if (mCurrentImageItem != null) {
                setMenuIcon(mCurrentImageItem!!.isSelected)
            } else {
                setMenuIcon(false)
            }
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_image_item_selected) {
            if (mCurrentImageItem == null) {
                return false
            }
            if (mSelectedImages.size >= mMaxCount && !mCurrentImageItem!!.isSelected) {
                val warning = getString(R.string.boxing_max_image_over_fmt, mMaxCount)
                Toast.makeText(this, warning, Toast.LENGTH_SHORT).show()
                return true
            }
            if (mCurrentImageItem!!.isSelected) {
                cancelImage()
            } else {
                if (!mSelectedImages.contains(mCurrentImageItem!!)) {
                    if (mCurrentImageItem!!.isGifOverSize) {
                        Toast.makeText(applicationContext, R.string.boxing_gif_too_big, Toast.LENGTH_SHORT).show()
                        return true
                    }
                    mCurrentImageItem!!.isSelected = true
                    mSelectedImages.add(mCurrentImageItem!!)
                }
            }
            setOkTextNumber()
            setMenuIcon(mCurrentImageItem!!.isSelected)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun cancelImage() {
        if (mSelectedImages.contains(mCurrentImageItem!!)) {
            mSelectedImages.remove(mCurrentImageItem!!)
        }
        mCurrentImageItem!!.isSelected = false
    }


    private fun setMenuIcon(isSelected: Boolean) {
        if (mNeedEdit) {
            mSelectedMenuItem!!.setIcon(if (isSelected) BoxingResHelper.mediaCheckedRes else BoxingResHelper.mediaUncheckedRes)
        }
    }

    override fun startLoading() {
        if (!mNeedLoading) {
            mCurrentImageItem = mSelectedImages[mStartPos] as ImageMedia
            if (mStartPos > 0 && mStartPos < mSelectedImages.size) {
                mGallery.setCurrentItem(mStartPos, false)
            }
            mToolbar.title = getString(R.string.boxing_image_preview_title_fmt, (mStartPos + 1).toString(), mSelectedImages.size.toString())
            mProgressBar.visibility = View.GONE
            mGallery.visibility = View.VISIBLE
            mAdapter.setMedias(mImages)
        } else {
            loadMedia(mAlbumId, mStartPos, mCurrentPage)
            mAdapter.setMedias(mImages)
        }
    }

    private fun loadMedia(albumId: String, startPos: Int, page: Int) {
        this.mPos = startPos
        loadMedias(page, albumId)
    }

    override fun showMedia(medias: List<BaseMedia>?, allCount: Int) {
        if (medias == null || allCount <= 0) {
            return
        }
        mImages.addAll(medias)
        mAdapter.notifyDataSetChanged()
        checkSelectedMedia(mImages, mSelectedImages)
        setupGallery()

        if (mNeedAllCount) {
            mToolbar.title = getString(R.string.boxing_image_preview_title_fmt,
                    (++mPos).toString(), allCount.toString())
            mNeedAllCount = false
        }
        loadOtherPagesInAlbum(allCount)
    }

    @SuppressLint("RestrictedApi")
    private fun setupGallery() {
        val startPos = mStartPos
        if (startPos < 0) {
            return
        }
        if (startPos < mImages.size && !mFinishLoading) {
            mGallery.setCurrentItem(mStartPos, false)
            mCurrentImageItem = mImages[startPos] as ImageMedia
            mProgressBar.visibility = View.GONE
            mGallery.visibility = View.VISIBLE
            mFinishLoading = true
            invalidateOptionsMenu()
        } else if (startPos >= mImages.size) {
            mProgressBar.visibility = View.VISIBLE
            mGallery.visibility = View.GONE
        }
    }

    private fun loadOtherPagesInAlbum(totalCount: Int) {
        mTotalCount = totalCount
        if (mCurrentPage <= mTotalCount / IMediaTask.PAGE_LIMIT) {
            mCurrentPage++
            loadMedia(mAlbumId, mStartPos, mCurrentPage)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA, mSelectedImages)
        outState.putString(Boxing.EXTRA_ALBUM_ID, mAlbumId)
        super.onSaveInstanceState(outState)
    }


    override fun onBackPressed() {
        finishByBackPressed(true)
    }

    private inner class ImagesAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private var mMedias: ArrayList<BaseMedia>? = null

        override fun getItem(i: Int): Fragment {
            return BoxingRawImageFragment.newInstance(mMedias!![i] as ImageMedia)
        }

        override fun getCount(): Int {
            return if (mMedias == null) 0 else mMedias!!.size
        }

        fun setMedias(medias: ArrayList<BaseMedia>?) {
            this.mMedias = medias
            notifyDataSetChanged()
        }
    }

    private inner class OnPagerChangeListener : ViewPager.SimpleOnPageChangeListener() {

        @SuppressLint("RestrictedApi")
        override fun onPageSelected(position: Int) {
            if (position < mImages.size) {
                mToolbar.title = getString(R.string.boxing_image_preview_title_fmt, (position + 1).toString(), if (mNeedLoading) mTotalCount.toString() else mImages!!.size.toString())
                mCurrentImageItem = mImages[position] as ImageMedia
                invalidateOptionsMenu()
            }
        }
    }

    companion object {
        val EXTRA_TYPE_BACK = "com.bilibili.boxing_impl.ui.BoxingViewActivity.type_back"
    }
}
