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

package com.bilibili.boxing.presenter

import android.text.TextUtils
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.callback.IAlbumTaskCallback
import com.bilibili.boxing.model.callback.IMediaTaskCallback
import com.bilibili.boxing.model.entity.AlbumEntity
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.model.task.IMediaTask
import java.io.File
import java.lang.ref.WeakReference
import java.util.*


/**
 * A presenter implement [com.bilibili.boxing.presenter.PickerContract.Presenter].

 * @author ChenSL
 */
class PickerPresenter(private var mTasksView: PickerContract.View) : PickerContract.Presenter {
    private var mTotalPage: Int = 0
    private var mCurrentPage: Int = 0
    private var mIsLoadingNextPage: Boolean = false

    private var mCurrentAlbumId: String = ""
    private val mLoadMediaCallback: LoadMediaCallback
    private val mLoadAlbumCallback: LoadAlbumCallback

    init {
        this.mTasksView.setPresenter(this)
        this.mLoadMediaCallback = LoadMediaCallback(this)
        this.mLoadAlbumCallback = LoadAlbumCallback(this)
    }

    override fun loadMedias(page: Int, albumId: String) {
        mCurrentAlbumId = albumId
        if (page == 0) {
            mTasksView.clearMedia()
        }
        val cr = mTasksView.appCr
        BoxingManager.instance.loadMedia(cr, page, albumId, mLoadMediaCallback)
    }

    override fun loadAlbums() {
        val cr = mTasksView.appCr
        BoxingManager.instance.loadAlbum(cr, mLoadAlbumCallback)
    }

    override fun destroy() {
    }

    override fun hasNextPage(): Boolean {
        return mCurrentPage < mTotalPage
    }

    override fun canLoadNextPage(): Boolean {
        return !mIsLoadingNextPage
    }

    override fun onLoadNextPage() {
        mCurrentPage++
        mIsLoadingNextPage = true
        loadMedias(mCurrentPage, mCurrentAlbumId)
    }

    override fun checkSelectedMedia(allMedias: List<BaseMedia>, selectedMedias: List<BaseMedia>) {
        if (allMedias.isEmpty() || selectedMedias.isEmpty()) {
            return
        }
        val map: MutableMap<String, ImageMedia> = HashMap(allMedias.size)
        for (allMedia in allMedias) {
            if (allMedia !is ImageMedia) {
                return
            }
            val media = allMedia
            media.isSelected = false
            map.put(media.path, media)
        }
        selectedMedias
                .filter { map.containsKey(it.path) }
                .forEach { map[it.path]?.isSelected = true }
    }

    private class LoadMediaCallback internal constructor(presenter: PickerPresenter) : IMediaTaskCallback<BaseMedia> {
        override fun postMedia(medias: List<BaseMedia>?, count: Int) {
            val presenter = presenter ?: return
            val view = presenter.mTasksView
            view.showMedia(medias, count)
            presenter.mTotalPage = count / IMediaTask.PAGE_LIMIT
            presenter.mIsLoadingNextPage = false
        }

        private val mWr: WeakReference<PickerPresenter> = WeakReference(presenter)

        private val presenter: PickerPresenter?
            get() = mWr.get()

        override fun needFilter(path: String): Boolean {
            return TextUtils.isEmpty(path) || !File(path).exists()
        }
    }

    private class LoadAlbumCallback internal constructor(presenter: PickerPresenter) : IAlbumTaskCallback {
        override fun postAlbumList(list: MutableList<AlbumEntity>?) {
            val presenter = presenter ?: return
            presenter.mTasksView.showAlbum(list)
        }

        private val mWr: WeakReference<PickerPresenter> = WeakReference(presenter)

        private val presenter: PickerPresenter?
            get() = mWr.get()

    }

}
