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
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bilibili.boxing.AbsBoxingViewFragment
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.presenter.PickerContract
import com.bilibili.boxing.utils.BoxingFileHelper
import com.bilibili.boxing_impl.R
import com.bilibili.boxing_impl.adapter.BoxingMediaAdapter
import com.bilibili.boxing_impl.view.SpacesItemDecoration
import java.util.*

/**
 * the most easy to implement [PickerContract.View] to show medias with google's Bottom Sheet
 * for simplest purpose, it only support SINGLE_IMG and VIDEO Mode.
 * for MULTI_IMG mode, use [BoxingViewFragment] instead.

 * @author ChenSL
 */
open class BoxingBottomSheetFragment : AbsBoxingViewFragment(), View.OnClickListener {

    private var mIsCamera: Boolean = false

    lateinit private var mMediaAdapter: BoxingMediaAdapter
    private var mDialog: ProgressDialog? = null
    private var mRecycleView: RecyclerView? = null
    private var mEmptyTxt: TextView? = null
    private var mLoadingView: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMediaAdapter = BoxingMediaAdapter(activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_boxing_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mEmptyTxt = view.findViewById(R.id.empty_txt) as TextView
        mRecycleView = view.findViewById(R.id.media_recycleview) as RecyclerView
        mLoadingView = view.findViewById(R.id.loading) as ProgressBar
        val gridLayoutManager = GridLayoutManager(activity, GRID_COUNT)
        gridLayoutManager.isSmoothScrollbarEnabled = true
        mRecycleView!!.layoutManager = gridLayoutManager
        mRecycleView!!.addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelOffset(R.dimen.boxing_media_margin), GRID_COUNT))
        mRecycleView!!.adapter = mMediaAdapter
        mRecycleView!!.addOnScrollListener(ScrollListener())
        mMediaAdapter!!.setOnMediaClickListener(OnMediaClickListener())
        mMediaAdapter!!.setOnCameraClickListener(OnCameraClickListener())
        view.findViewById(R.id.finish_txt).setOnClickListener(this)
    }


    override fun onCameraActivityResult(requestCode: Int, resultCode: Int) {
        showProgressDialog()
        super.onCameraActivityResult(requestCode, resultCode)
    }

    private fun showProgressDialog() {
        if (mDialog == null) {
            mDialog = ProgressDialog(activity)
            mDialog!!.isIndeterminate = true
            mDialog!!.setMessage(getString(R.string.boxing_handling))
        }
        if (!mDialog!!.isShowing) {
            mDialog!!.show()
        }
    }

    private fun dismissProgressDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.hide()
            mDialog!!.dismiss()
        }
    }


    override fun showMedia(medias: List<BaseMedia>?, count: Int) {
        if (medias == null || isEmptyData(medias) && isEmptyData(mMediaAdapter!!.allMedias)) {
            showEmptyData()
            return
        }
        showData()
        mMediaAdapter!!.addAllData(medias)
    }

    private fun isEmptyData(medias: List<BaseMedia>): Boolean {
        return medias.isEmpty() && !BoxingManager.instance.boxingConfig.isNeedCamera
    }

    private fun showEmptyData() {
        mEmptyTxt!!.visibility = View.VISIBLE
        mRecycleView!!.visibility = View.GONE
        mLoadingView!!.visibility = View.GONE
    }

    private fun showData() {
        mLoadingView!!.visibility = View.GONE
        mEmptyTxt!!.visibility = View.GONE
        mRecycleView!!.visibility = View.VISIBLE
    }

    override fun onCameraFinish(media: BaseMedia) {
        dismissProgressDialog()
        mIsCamera = false
        val selectedMedias = mMediaAdapter.selectedMedias
        selectedMedias.add(media)
        this@BoxingBottomSheetFragment.onFinish(selectedMedias)
    }

    override fun onCameraError() {
        mIsCamera = false
        dismissProgressDialog()
    }

    override fun startLoading() {
        loadMedias()
    }

    override fun onRequestPermissionError(permissions: Array<String>, e: Exception) {
        if (permissions.size > 0) {
            if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                showEmptyData()
                Toast.makeText(context, R.string.boxing_storage_permission_deny, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionSuc(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissions[0] == AbsBoxingViewFragment.Companion.STORAGE_PERMISSIONS[0]) {
            startLoading()
        }
    }


    override fun clearMedia() {
        mMediaAdapter!!.clearData()
    }


    override fun onClick(v: View) {
        val id = v.id
        if (R.id.finish_txt == id) {
            onFinish(null!!)
        }
    }


    private inner class OnMediaClickListener : View.OnClickListener {

        override fun onClick(v: View) {
            val iMedias = ArrayList<BaseMedia>()
            val media = v.tag as BaseMedia
            iMedias.add(media)
            onFinish(iMedias)
        }
    }

    private inner class OnCameraClickListener : View.OnClickListener {

        override fun onClick(v: View) {
            if (!mIsCamera) {
                mIsCamera = true
                startCamera(activity, this@BoxingBottomSheetFragment, BoxingFileHelper.DEFAULT_SUB_DIR)
            }
        }
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            val childCount = recyclerView!!.childCount
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

    companion object {
        val TAG = "com.bilibili.boxing_impl.ui.BoxingBottomSheetFragment"

        private val GRID_COUNT = 3

        fun newInstance(): BoxingBottomSheetFragment {
            return BoxingBottomSheetFragment()
        }
    }

}
