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

package com.bilibili.boxing.model;

import android.content.ContentResolver;
import android.support.annotation.NonNull;

import com.bilibili.boxing.model.callback.IAlbumTaskCallback;
import com.bilibili.boxing.model.callback.IMediaTaskCallback;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.task.IMediaTask;
import com.bilibili.boxing.model.task.impl.AlbumTask;
import com.bilibili.boxing.model.task.impl.ImageTask;
import com.bilibili.boxing.model.task.impl.VideoTask;
import com.bilibili.boxing.utils.BoxingExecutor;

/**
 * The Manager to load {@link IMediaTask} and {@link AlbumTask}, holding {@link BoxingConfig}.
 *
 * @author ChenSL
 */
public class BoxingManager {
    private static final BoxingManager INSTANCE = new BoxingManager();

    private BoxingConfig mConfig;

    private BoxingManager() {
    }

    public static BoxingManager getInstance() {
        return INSTANCE;
    }

    public void setBoxingConfig(BoxingConfig config) {
        mConfig = config;
    }

    public BoxingConfig getBoxingConfig() {
        return mConfig;
    }

    public void loadMedia(@NonNull final ContentResolver cr, final int page,
                          final String id, @NonNull final IMediaTaskCallback callback) {
        final IMediaTask task = mConfig.isVideoMode() ? new VideoTask() : new ImageTask();
        BoxingExecutor.getInstance().runWorker(new Runnable() {
            @Override
            public void run() {
                task.load(cr, page, id, callback);
            }
        });

    }

    public void loadAlbum(@NonNull final ContentResolver cr, @NonNull final IAlbumTaskCallback callback) {
        BoxingExecutor.getInstance().runWorker(new Runnable() {

            @Override
            public void run() {
                new AlbumTask().start(cr, callback);
            }
        });

    }

}
