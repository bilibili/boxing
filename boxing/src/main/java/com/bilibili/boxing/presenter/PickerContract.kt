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

import android.content.ContentResolver

import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.AlbumEntity
import com.bilibili.boxing.model.entity.BaseMedia

/**
 * This specifies the contract between the view and the presenter.

 * @author ChenSL
 */
interface PickerContract {

    /**
     * define the functions of the view, interacting with presenter
     */
    interface View {
        /**
         * set the presenter attaching to the view
         */
        fun setPresenter(presenter: Presenter)

        /**
         * show a list  the [BaseMedia] in the view
         */
        fun showMedia(medias: List<BaseMedia>?, allCount: Int)

        /**
         * show all the [AlbumEntity] in the view
         */
        fun showAlbum(albums: List<AlbumEntity>?)

        /**
         * get the [ContentResolver] in the view
         */
        val appCr: ContentResolver

        /**
         * call when the view should be finished or the process is finished

         * @param medias the selection of medias.
         */
        fun onFinish(medias: List<BaseMedia>)

        /**
         * clear all the [BaseMedia] in the view
         */
        fun clearMedia()

        /**
         * start crop the [BaseMedia] in the single media mode
         */
        fun startCrop(media: BaseMedia, requestCode: Int)

        /**
         * set or update the config.

         * @param config [BoxingConfig]
         */
        fun setPickerConfig(config: BoxingConfig)

    }

    /**
     * define the function of presenter, to control the module to load data and to tell view to displayRaw the data
     */
    interface Presenter {
        /**
         * load the specify data from [ContentResolver]

         * @param page    the page need to load
         * *
         * @param albumId album albumId
         */
        fun loadMedias(page: Int, albumId: String)

        /**
         * load all the album from [ContentResolver]
         */
        fun loadAlbums()

        /**
         * destroy the presenter and set the view null
         */
        fun destroy()

        /**
         * has more data to load

         * @return true, have more
         */
        fun hasNextPage(): Boolean

        fun canLoadNextPage(): Boolean

        /**
         * load next page
         */
        fun onLoadNextPage()

        /**
         * Determine the selected allMedias according to mSelectedMedias

         * @param allMedias      all medias
         * *
         * @param selectedMedias the medias to be selected
         */
        fun checkSelectedMedia(allMedias: List<BaseMedia>, selectedMedias: List<BaseMedia>)
    }
}
