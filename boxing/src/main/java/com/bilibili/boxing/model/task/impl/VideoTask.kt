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
import android.provider.MediaStore
import android.support.annotation.WorkerThread
import com.bilibili.boxing.model.callback.IMediaTaskCallback
import com.bilibili.boxing.model.entity.impl.VideoMedia
import com.bilibili.boxing.model.task.IMediaTask
import com.bilibili.boxing.utils.BoxingExecutor
import java.util.*

/**
 * A Task to load [VideoMedia] in database.

 * @author ChenSL
 */
@WorkerThread
class VideoTask : IMediaTask<VideoMedia> {

    override fun load(cr: ContentResolver, page: Int, id: String, callback: IMediaTaskCallback<VideoMedia>) {
        loadVideos(cr, page, callback)
    }

    private fun loadVideos(cr: ContentResolver, page: Int, callback: IMediaTaskCallback<VideoMedia>) {
        val videoMedias = ArrayList<VideoMedia>()
        val cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MEDIA_COL, null, null,
                MediaStore.Images.Media.DATE_MODIFIED + " desc" + " LIMIT " + page * IMediaTask.PAGE_LIMIT + " , " + IMediaTask.PAGE_LIMIT)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val count = cursor.count
                do {
                    val data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val type = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE))
                    val size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val date = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN))
                    val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    val video = VideoMedia.Builder(id, data).setTitle(title).setDuration(duration)
                            .setSize(size).setDataTaken(date).setMimeType(type).build()
                    videoMedias.add(video)

                } while (!cursor.isLast && cursor.moveToNext())
                postMedias(callback, videoMedias, count)
            } else {
                postMedias(callback, videoMedias, 0)
            }
        } finally {
            cursor?.close()
        }

    }

    private fun postMedias(callback: IMediaTaskCallback<VideoMedia>,
                           videoMedias: List<VideoMedia>, count: Int) {
        BoxingExecutor.instance.runUI(Runnable { callback.postMedia(videoMedias, count) })
    }

    companion object {

        private val MEDIA_COL = arrayOf(MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DATE_TAKEN, MediaStore.Video.Media.DURATION)
    }

}
