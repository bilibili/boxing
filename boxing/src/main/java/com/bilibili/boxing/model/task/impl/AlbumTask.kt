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
package com.bilibili.boxing.model.task.impl

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore.Images.Media
import android.support.annotation.WorkerThread
import android.support.v4.util.ArrayMap
import android.text.TextUtils
import com.bilibili.boxing.model.callback.IAlbumTaskCallback
import com.bilibili.boxing.model.entity.AlbumEntity
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.utils.BoxingExecutor
import java.util.*

/**
 * A task to load albums.

 * @author ChenSL
 */
@WorkerThread
class AlbumTask {
    private var mUnknownAlbumNumber = 1
    private val mBucketMap: MutableMap<String, AlbumEntity>?
    private val mDefaultAlbum: AlbumEntity

    init {
        this.mBucketMap = ArrayMap<String, AlbumEntity>()
        this.mDefaultAlbum = AlbumEntity.createDefaultAlbum()
    }

    fun start(cr: ContentResolver, callback: IAlbumTaskCallback) {
        buildDefaultAlbum(cr)
        buildAlbumInfo(cr)
        getAlbumList(callback)
    }

    private fun buildDefaultAlbum(cr: ContentResolver) {
        var cursor: Cursor? = null
        try {
            cursor = cr.query(Media.EXTERNAL_CONTENT_URI, arrayOf(Media.BUCKET_ID), null, null, null)
            if (cursor != null) {
                mDefaultAlbum.mCount = cursor.count
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }

    private fun buildAlbumInfo(cr: ContentResolver) {
        val distinctBucketColumns = arrayOf(Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME)
        var bucketCursor: Cursor? = null
        try {
            bucketCursor = cr.query(Media.EXTERNAL_CONTENT_URI, distinctBucketColumns, "0==0)" + " GROUP BY(" + Media.BUCKET_ID, null,
                    Media.DATE_MODIFIED + " desc")
            if (bucketCursor != null && bucketCursor.moveToFirst()) {
                do {
                    val buckId = bucketCursor.getString(bucketCursor.getColumnIndex(Media.BUCKET_ID))
                    val name = bucketCursor.getString(bucketCursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME))
                    val album = buildAlbumInfo(name, buckId)
                    if (!TextUtils.isEmpty(buckId)) {
                        buildAlbumCover(cr, buckId, album)
                    }
                } while (bucketCursor.moveToNext() && !bucketCursor.isLast)
            }
        } finally {
            if (bucketCursor != null) {
                bucketCursor.close()
            }
        }
    }

    /**
     * get the cover and count

     * @param buckId album id
     */
    private fun buildAlbumCover(cr: ContentResolver, buckId: String, album: AlbumEntity) {
        val photoColumn = arrayOf(Media._ID, Media.DATA)
        val coverCursor = cr.query(Media.EXTERNAL_CONTENT_URI, photoColumn, SELECTION_ID,
                arrayOf(buckId, "image/jpeg", "image/png", "image/jpg", "image/gif"), Media.DATE_MODIFIED + " desc")
        try {
            if (coverCursor != null && coverCursor.moveToFirst()) {
                val picPath = coverCursor.getString(coverCursor.getColumnIndex(Media.DATA))
                val id = coverCursor.getString(coverCursor.getColumnIndex(Media._ID))
                album.mCount = coverCursor.count
                album.mImageList.add(ImageMedia(id, picPath))
                if (album.mImageList.isNotEmpty()) {
                    mBucketMap!!.put(buckId, album)
                }
            }
        } finally {
            coverCursor?.close()
        }
    }

    private fun getAlbumList(callback: IAlbumTaskCallback) {
        val tmpList = ArrayList<AlbumEntity>()
        if (mBucketMap == null) {
            postAlbums(callback, tmpList)
        }
        for ((_, value) in mBucketMap!!) {
            tmpList.add(value)
        }
        if (tmpList.size > 0) {
            mDefaultAlbum.mImageList = tmpList[0].mImageList
            tmpList.add(0, mDefaultAlbum)
        }
        postAlbums(callback, tmpList)
        clear()
    }

    private fun postAlbums(callback: IAlbumTaskCallback, result: MutableList<AlbumEntity>) {
        BoxingExecutor.instance.runUI(Runnable { callback.postAlbumList(result) })
    }

    private fun buildAlbumInfo(bucketName: String, bucketId: String): AlbumEntity {
        var album: AlbumEntity? = null
        if (!TextUtils.isEmpty(bucketId)) {
            album = mBucketMap!![bucketId]
        }
        if (album == null) {
            album = AlbumEntity()
            if (!TextUtils.isEmpty(bucketId)) {
                album.mBucketId = bucketId
            } else {
                album.mBucketId = mUnknownAlbumNumber.toString()
                mUnknownAlbumNumber++
            }
            if (!TextUtils.isEmpty(bucketName)) {
                album.mBucketName = bucketName
            } else {
                album.mBucketName = UNKNOWN_ALBUM_NAME
                mUnknownAlbumNumber++
            }
            if (album.mImageList.size > 0) {
                mBucketMap!!.put(bucketId, album)
            }
        }
        return album
    }

    private fun clear() {
        mBucketMap?.clear()
    }

    companion object {
        private val UNKNOWN_ALBUM_NAME = "unknow"
        private val SELECTION_IMAGE_MIME_TYPE = Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=?"
        private val SELECTION_ID = Media.BUCKET_ID + "=? and (" + SELECTION_IMAGE_MIME_TYPE + " )"
    }

}
