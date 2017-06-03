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

package com.bilibili.boxing.model.callback


import com.bilibili.boxing.model.entity.BaseMedia

/**
 * A callback to load [BaseMedia].

 * @author ChenSL
 */
interface IMediaTaskCallback<in T : BaseMedia> {
    /**
     * get a page of medias in a album

     * @param medias page of medias
     * *
     * @param count  the count for the photo in album
     */
    fun postMedia(medias: List<T>?, count: Int)

    /**
     * judge the path needing filer

     * @param path photo path
     * *
     * @return true:be filter
     */
    fun needFilter(path: String): Boolean
}
