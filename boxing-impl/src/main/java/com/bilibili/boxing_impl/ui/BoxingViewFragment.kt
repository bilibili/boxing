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

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bilibili.boxing.AbsBoxingViewFragment
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.AlbumEntity
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.utils.BoxingFileHelper
import com.bilibili.boxing_impl.R
import com.bilibili.boxing_impl.WindowManagerHelper
import com.bilibili.boxing_impl.adapter.BoxingAlbumAdapter
import com.bilibili.boxing_impl.adapter.BoxingMediaAdapter
import com.bilibili.boxing_impl.view.MediaItemLayout
import com.bilibili.boxing_impl.view.SpacesItemDecoration
import java.util.*

/**
 * A full implement for [com.bilibili.boxing.presenter.PickerContract.View] supporting all the mode
 * in [BoxingConfig.Mode].
 * use this to pick the picture.

 * @author ChenSL
 */
class BoxingViewFragment : AbsBoxingViewFragment(), View.OnClickListener {
    private var mIsPreview: Boolean = false
    private var mIsCamera: Boolean = false

    lateinit private var mPreBtn: Button
    lateinit private var mOkBtn: Button
    lateinit private var mRecycleView: RecyclerView
    lateinit var mMediaAdapter: BoxingMediaAdapter
        private set
    lateinit private var mAlbumWindowAdapter: BoxingAlbumAdapter
    lateinit private var mDialog: ProgressDialog
    lateinit private var mEmptyTxt: TextView
    lateinit private var mTitleTxt: TextView
    lateinit private var mAlbumPopWindow: PopupWindow
    lateinit private var mLoadingView: ProgressBar

    private var mMaxCount: Int = 0

    override fun onCreateWithSelectedMedias(savedInstanceState: Bundle?, selectedMedias: MutableList<BaseMedia>?) {
        mAlbumWindowAdapter = BoxingAlbumAdapter(context)
        mMediaAdapter = BoxingMediaAdapter(context)
        selectedMedias?.let { mMediaAdapter.selectedMedias = selectedMedias }
        mMaxCount = maxCount
    }

    override fun startLoading() {
        loadMedias()
        loadAlbum()
    }

    override fun onRequestPermissionError(permissions: Array<String>, e: Exception) {
        if (permissions.isNotEmpty()) {
            if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                Toast.makeText(context, R.string.boxing_storage_permission_deny, Toast.LENGTH_SHORT).show()
                showEmptyData()
            } else if (permissions[0] == Manifest.permission.CAMERA) {
                Toast.makeText(context, R.string.boxing_camera_permission_deny, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionSuc(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissions[0] == AbsBoxingViewFragment.Companion.STORAGE_PERMISSIONS[0]) {
            startLoading()
        } else if (permissions[0] == AbsBoxingViewFragment.Companion.CAMERA_PERMISSIONS[0]) {
            startCamera(activity, this, null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragmant_boxing_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews(view)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initViews(view: View) {
        mEmptyTxt = view.findViewById(R.id.empty_txt) as TextView
        mRecycleView = view.findViewById(R.id.media_recycleview) as RecyclerView
        mLoadingView = view.findViewById(R.id.loading) as ProgressBar
        initRecycleView()

        val isMultiImageMode = BoxingManager.instance.boxingConfig.isMultiImageMode
        val multiImageLayout = view.findViewById(R.id.multi_picker_layout)
        multiImageLayout.visibility = if (isMultiImageMode) View.VISIBLE else View.GONE
        if (isMultiImageMode) {
            mPreBtn = view.findViewById(R.id.choose_preview_btn) as Button
            mOkBtn = view.findViewById(R.id.choose_ok_btn) as Button

            mPreBtn.setOnClickListener(this)
            mOkBtn.setOnClickListener(this)
            updateMultiPickerLayoutState(mMediaAdapter.selectedMedias)
        }
    }

    private fun initRecycleView() {
        val gridLayoutManager = GridLayoutManager(activity, GRID_COUNT)
        gridLayoutManager.isSmoothScrollbarEnabled = true
        mRecycleView.layoutManager = gridLayoutManager
        mRecycleView.addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelOffset(R.dimen.boxing_media_margin), GRID_COUNT))
        mMediaAdapter.setOnCameraClickListener(OnCameraClickListener())
        mMediaAdapter.setOnCheckedListener(OnMediaCheckedListener())
        mMediaAdapter.setOnMediaClickListener(OnMediaClickListener())
        mRecycleView.adapter = mMediaAdapter
        mRecycleView.addOnScrollListener(ScrollListener())
    }

    override fun showMedia(medias: List<BaseMedia>?, allCount: Int) {
        if (medias == null || isEmptyData(medias) && isEmptyData(mMediaAdapter.allMedias)) {
            showEmptyData()
            return
        }
        showData()
        mMediaAdapter.addAllData(medias)
        checkSelectedMedia(medias, mMediaAdapter.selectedMedias)
    }

    private fun isEmptyData(medias: List<BaseMedia>): Boolean {
        return medias.isEmpty() && !BoxingManager.instance.boxingConfig.isNeedCamera
    }

    private fun showEmptyData() {
        mLoadingView.visibility = View.GONE
        mEmptyTxt.visibility = View.VISIBLE
        mRecycleView.visibility = View.GONE
    }

    private fun showData() {
        mLoadingView.visibility = View.GONE
        mEmptyTxt.visibility = View.GONE
        mRecycleView.visibility = View.VISIBLE
    }

    override fun showAlbum(albums: List<AlbumEntity>?) {
        if ((albums == null || albums.isEmpty())) {
            mTitleTxt.setCompoundDrawables(null, null, null, null)
            mTitleTxt.setOnClickListener(null)
            return
        }
        mAlbumWindowAdapter.addAllData(albums)
    }

    override fun clearMedia() {
        mMediaAdapter.clearData()
    }

    private fun updateMultiPickerLayoutState(medias: MutableList<BaseMedia>?) {
        updateOkBtnState(medias)
        updatePreviewBtnState(medias)
    }

    private fun updatePreviewBtnState(medias: List<BaseMedia>?) {
        if (medias == null) {
            return
        }
        val enabled = medias.size in 1..mMaxCount
        mPreBtn.isEnabled = enabled
    }

    private fun updateOkBtnState(medias: List<BaseMedia>?) {
        if (medias == null) {
            return
        }
        val enabled = medias.size in 1..mMaxCount
        mOkBtn.isEnabled = enabled
        mOkBtn.text = if (enabled)
            getString(R.string.boxing_image_select_ok_fmt, medias.size.toString(), mMaxCount.toString())
        else
            getString(R.string.boxing_ok)
    }

    override fun onCameraFinish(media: BaseMedia) {
        dismissProgressDialog()
        mIsCamera = false
        if (hasCropBehavior()) {
            startCrop(media, IMAGE_CROP_REQUEST_CODE)
        } else {
            val selectedMedias = mMediaAdapter.selectedMedias
            selectedMedias.add(media)
            onFinish(selectedMedias)
        }
    }

    override fun onCameraError() {
        mIsCamera = false
        dismissProgressDialog()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.choose_ok_btn) {
            onFinish(mMediaAdapter.selectedMedias)
        } else if (id == R.id.choose_preview_btn) {
            if (!mIsPreview) {
                mIsPreview = true
                val medias = mMediaAdapter.selectedMedias as ArrayList<BaseMedia>
                Boxing.get().withIntent(activity, BoxingViewActivity::class.java, medias)
                        .start(this, BoxingViewFragment.IMAGE_PREVIEW_REQUEST_CODE, BoxingConfig.ViewMode.PRE_EDIT)

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PREVIEW_REQUEST_CODE) {
            mIsPreview = false
            val isBackClick = data.getBooleanExtra(BoxingViewActivity.EXTRA_TYPE_BACK, false)
            val selectedMedias = data.getParcelableArrayListExtra<BaseMedia>(Boxing.EXTRA_SELECTED_MEDIA)
            onViewActivityRequest(selectedMedias, mMediaAdapter.allMedias, isBackClick)
            if (isBackClick) {
                mMediaAdapter.selectedMedias = selectedMedias
                mMediaAdapter.notifyDataSetChanged()
            }
            updateMultiPickerLayoutState(selectedMedias)
        }

    }

    private fun onViewActivityRequest(selectedMedias: List<BaseMedia>, allMedias: List<BaseMedia>, isBackClick: Boolean) {
        if (isBackClick) {
            checkSelectedMedia(allMedias, selectedMedias)
        } else {
            onFinish(selectedMedias)
        }
    }


    override fun onCameraActivityResult(requestCode: Int, resultCode: Int) {
        showProgressDialog()
        super.onCameraActivityResult(requestCode, resultCode)
    }

    private fun showProgressDialog() {
        if (mDialog == null) {
            mDialog = ProgressDialog(activity)
            mDialog.isIndeterminate = true
            mDialog.setMessage(getString(R.string.boxing_handling))
        }
        if (!mDialog.isShowing) {
            mDialog.show()
        }
    }

    private fun dismissProgressDialog() {
        if (mDialog.isShowing) {
            mDialog.hide()
            mDialog.dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val medias = mMediaAdapter.selectedMedias as ArrayList<BaseMedia>
        onSaveMedias(outState, medias)
    }

    fun setTitleTxt(titleTxt: TextView) {
        mTitleTxt = titleTxt
        mTitleTxt.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View) {
                val height = WindowManagerHelper.getScreenHeight(v.context) - (WindowManagerHelper.getToolbarHeight(v.context) + WindowManagerHelper.getStatusBarHeight(v.context))
                val windowView = createWindowView()
                mAlbumPopWindow = PopupWindow(windowView, ViewGroup.LayoutParams.MATCH_PARENT,
                        height, true)
                mAlbumPopWindow.animationStyle = R.style.Boxing_PopupAnimation
                mAlbumPopWindow.isOutsideTouchable = true
                mAlbumPopWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(v.context, R.color.boxing_colorPrimaryAlpha)))
                mAlbumPopWindow.contentView = windowView
                mAlbumPopWindow.showAsDropDown(v, 0, 0)
            }

            private fun createWindowView(): View {
                val view = LayoutInflater.from(activity).inflate(R.layout.layout_boxing_album, null)
                val recyclerView = view.findViewById(R.id.album_recycleview) as RecyclerView
                recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
                recyclerView.addItemDecoration(SpacesItemDecoration(2, 1))

                val albumShadowLayout = view.findViewById(R.id.album_shadow)
                albumShadowLayout.setOnClickListener { dismissAlbumWindow() }
                mAlbumWindowAdapter.setAlbumOnClickListener(OnAlbumItemOnClickListener())
                recyclerView.adapter = mAlbumWindowAdapter
                return view
            }
        })
    }

    private fun dismissAlbumWindow() {
        if (mAlbumPopWindow.isShowing) {
            mAlbumPopWindow.dismiss()
        }
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val childCount = recyclerView.childCount
            if (childCount > 0) {
                val lastChild = recyclerView.getChildAt(childCount - 1)
                val outerAdapter = recyclerView.adapter
                val lastVisible = recyclerView.getChildAdapterPosition(lastChild)
                if (lastVisible == outerAdapter.itemCount - 1 && hasNextPage() && canLoadNextPage()) {
                    onLoadNextPage()
                }
            }
        }
    }

    private inner class OnMediaClickListener : View.OnClickListener {

        override fun onClick(v: View) {
            val media = v.tag as BaseMedia
            val pos = v.getTag(R.id.media_item_check) as Int
            val mode = BoxingManager.instance.boxingConfig.mode
            if (mode === BoxingConfig.Mode.SINGLE_IMG) {
                singleImageClick(media)
            } else if (mode === BoxingConfig.Mode.MULTI_IMG) {
                multiImageClick(pos)
            } else if (mode === BoxingConfig.Mode.VIDEO) {
                videoClick(media)
            }
        }

        private fun videoClick(media: BaseMedia) {
            val iMedias = ArrayList<BaseMedia>()
            iMedias.add(media)
            onFinish(iMedias)
        }

        private fun multiImageClick(pos: Int) {
            if (!mIsPreview) {
                val albumMedia = mAlbumWindowAdapter.currentAlbum
                val albumId = if (albumMedia != null) albumMedia.mBucketId else AlbumEntity.DEFAULT_NAME
                mIsPreview = true

                val medias = mMediaAdapter.selectedMedias as ArrayList<BaseMedia>

                Boxing.get().withIntent(context, BoxingViewActivity::class.java, medias, pos, albumId)
                        .start(this@BoxingViewFragment, BoxingViewFragment.IMAGE_PREVIEW_REQUEST_CODE, BoxingConfig.ViewMode.EDIT)

            }
        }

        private fun singleImageClick(media: BaseMedia) {
            val iMedias = ArrayList<BaseMedia>()
            iMedias.add(media)
            if (hasCropBehavior()) {
                startCrop(media, IMAGE_CROP_REQUEST_CODE)
            } else {
                onFinish(iMedias)
            }
        }
    }


    private inner class OnCameraClickListener : View.OnClickListener {

        override fun onClick(v: View) {
            if (!mIsCamera) {
                mIsCamera = true
                startCamera(activity, this@BoxingViewFragment, BoxingFileHelper.DEFAULT_SUB_DIR)
            }
        }
    }

    private inner class OnMediaCheckedListener : BoxingMediaAdapter.OnMediaCheckedListener {

        override fun onChecked(v: View, iMedia: BaseMedia) {
            if (iMedia !is ImageMedia) {
                return
            }
            val photoMedia = iMedia
            val isSelected = !photoMedia.isSelected
            val layout = v as MediaItemLayout
            val selectedMedias = mMediaAdapter.selectedMedias
            if (isSelected) {
                if (selectedMedias.size >= mMaxCount) {
                    val warning = getString(R.string.boxing_too_many_picture_fmt, mMaxCount)
                    Toast.makeText(activity, warning, Toast.LENGTH_SHORT).show()
                    return
                }
                if (!selectedMedias.contains(photoMedia)) {
                    if (photoMedia.isGifOverSize) {
                        Toast.makeText(activity, R.string.boxing_gif_too_big, Toast.LENGTH_SHORT).show()
                        return
                    }
                    selectedMedias.add(photoMedia)
                }
            } else {
                if (selectedMedias.size >= 1 && selectedMedias.contains(photoMedia)) {
                    selectedMedias.remove(photoMedia)
                }
            }
            photoMedia.isSelected = isSelected
            layout.setChecked(isSelected)
            updateMultiPickerLayoutState(selectedMedias)
        }
    }

    private inner class OnAlbumItemOnClickListener : BoxingAlbumAdapter.OnAlbumClickListener {

        override fun onClick(view: View, pos: Int) {
            val adapter = mAlbumWindowAdapter
            if (adapter.currentAlbumPos != pos) {
                val albums = adapter.alums
                adapter.currentAlbumPos = pos

                val albumMedia = albums[pos]
                loadMedias(0, albumMedia.mBucketId)
                mTitleTxt.text = albumMedia.mBucketName

                for (album in albums) {
                    album.mIsSelected = false
                }
                albumMedia.mIsSelected = true
                adapter.notifyDataSetChanged()
            }
            dismissAlbumWindow()
        }
    }

    companion object {
        val TAG = "com.bilibili.boxing_impl.ui.BoxingViewFragment"
        private val IMAGE_PREVIEW_REQUEST_CODE = 9086
        private val IMAGE_CROP_REQUEST_CODE = 9087

        private val GRID_COUNT = 3

        fun newInstance(): BoxingViewFragment {
            return BoxingViewFragment()
        }
    }

}
