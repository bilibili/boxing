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

package com.bilibili.boxing.presenter;

import android.content.ContentResolver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.BaseMedia;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 *
 * @author ChenSL
 */
public interface PickerContract {

    /**
     * define the functions of the view, interacting with presenter
     */
    interface View {
        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        /**
         * show a list  the {@link BaseMedia} in the view
         */
        void showMedia(@Nullable List<BaseMedia> medias, int allCount);

        /**
         * show all the {@link AlbumEntity} in the view
         */
        void showAlbum(@Nullable List<AlbumEntity> albums);

        /**
         * get the {@link ContentResolver} in the view
         */
        @NonNull
        ContentResolver getAppCr();

        /**
         * call when the view should be finished or the process is finished
         *
         * @param medias the selection of medias.
         */
        void onFinish(@NonNull List<BaseMedia> medias);

        /**
         * clear all the {@link BaseMedia} in the view
         */
        void clearMedia();

        /**
         * start crop the {@link BaseMedia} in the single media mode
         */
        void startCrop(@NonNull BaseMedia media, int requestCode);

        /**
         * set or update the config.
         *
         * @param config {@link BoxingConfig}
         */
        void setPickerConfig(BoxingConfig config);

    }

    /**
     * define the function of presenter, to control the module to load data and to tell view to displayRaw the data
     */
    interface Presenter {
        /**
         * load the specify data from {@link ContentResolver}
         *
         * @param page    the page need to load
         * @param albumId album albumId
         */
        void loadMedias(int page, String albumId);

        /**
         * load all the album from {@link ContentResolver}
         */
        void loadAlbums();

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        /**
         * has more data to load
         *
         * @return true, have more
         */
        boolean hasNextPage();

        boolean canLoadNextPage();

        /**
         * load next page
         */
        void onLoadNextPage();

        /**
         * Determine the selected allMedias according to mSelectedMedias
         *
         * @param allMedias      all medias
         * @param selectedMedias the medias to be selected
         */
        void checkSelectedMedia(List<BaseMedia> allMedias, List<BaseMedia> selectedMedias);
    }
}
