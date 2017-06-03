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

package com.bilibili.boxing.model

import android.content.ContentResolver
import com.bilibili.boxing.model.callback.IAlbumTaskCallback
import com.bilibili.boxing.model.callback.IMediaTaskCallback
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.task.IMediaTask
import com.bilibili.boxing.model.task.impl.AlbumTask
import com.bilibili.boxing.model.task.impl.ImageTask
import com.bilibili.boxing.model.task.impl.VideoTask
import com.bilibili.boxing.utils.BoxingExecutor

/**
 * The Manager to load [IMediaTask] and [AlbumTask], holding [BoxingConfig].

 * @author ChenSL
 */
class BoxingManager private constructor() {

    lateinit var boxingConfig: BoxingConfig

    fun loadMedia(cr: ContentResolver, page: Int,
                  id: String, callback: IMediaTaskCallback<BaseMedia>) {
        val task = if (boxingConfig.isVideoMode) VideoTask() else ImageTask()
        BoxingExecutor.instance.runWorker(Runnable {
            task.load(cr, page, id, callback)
        })

    }

    fun loadAlbum(cr: ContentResolver, callback: IAlbumTaskCallback) {
        BoxingExecutor.instance.runWorker(Runnable { AlbumTask().start(cr, callback) })

    }

    companion object {
        val instance = BoxingManager()
    }

}
