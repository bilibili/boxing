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

package com.bilibili.boxing.loader;

import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Define how media display.
 *
 * @author ChenSL
 */
public interface IBoxingMediaLoader {
    /**
     * display thumbnail images for a ImageView.
     *
     * @param img     the display ImageView. Through ImageView.getTag(R.string.boxing_app_name) to get the absolute path of the exact path to display.
     * @param absPath the absolute path to display, may be out of date when fast scrolling.
     * @param width   the resize with for the image.
     * @param height  the resize height for the image.
     */
    void displayThumbnail(@NonNull ImageView img, @NonNull String absPath, int width, int height);

    /**
     * display raw images for a ImageView, need more work to do.
     *
     * @param img      the display ImageView.Through ImageView.getTag(R.string.boxing_app_name) to get the absolute path of the exact path to display.
     * @param absPath  the absolute path to display, may be out of date when fast scrolling.
     * @param width the expected width, 0 means the raw width.
     * @param height the expected height, 0 means the raw height.
     * @param callback the callback for the load result.
     */
    void displayRaw(@NonNull ImageView img, @NonNull String absPath, int width, int height, IBoxingCallback callback);
}
