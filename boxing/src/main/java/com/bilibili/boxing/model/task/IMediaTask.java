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

package com.bilibili.boxing.model.task;

import android.content.ContentResolver;

import com.bilibili.boxing.model.callback.IMediaTaskCallback;
import com.bilibili.boxing.model.entity.BaseMedia;


/**
 * The interface to load {@link BaseMedia}.
 *
 * @author ChenSL
 */
public interface IMediaTask<T extends BaseMedia> {
    int PAGE_LIMIT = 1000;

    void load(ContentResolver cr, int page, String id, IMediaTaskCallback<T> callback);

}
