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

package com.bilibili.boxing.model.callback;


import android.support.annotation.Nullable;

import com.bilibili.boxing.model.entity.BaseMedia;

import java.util.List;

/**
 * A callback to load {@link BaseMedia}.
 *
 * @author ChenSL
 */
public interface IMediaTaskCallback<T extends BaseMedia> {
    /**
     * get a page of medias in a album
     *
     * @param medias page of medias
     * @param count  the count for the photo in album
     */
    void postMedia(@Nullable List<T> medias, int count);

    /**
     * judge the path needing filer
     *
     * @param path photo path
     * @return true:be filter
     */
    boolean needFilter(String path);
}
