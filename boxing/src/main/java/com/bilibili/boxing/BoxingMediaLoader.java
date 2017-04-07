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

package com.bilibili.boxing;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;

/**
 * A loader holding {@link IBoxingMediaLoader} to displayThumbnail medias.
 *
 * @author ChenSL
 */
public class BoxingMediaLoader {
    private static final BoxingMediaLoader INSTANCE = new BoxingMediaLoader();
    private IBoxingMediaLoader mLoader;

    private BoxingMediaLoader() {
    }

    public static BoxingMediaLoader getInstance() {
        return INSTANCE;
    }

    public void init(@NonNull IBoxingMediaLoader loader) {
        this.mLoader = loader;
    }

    public void displayThumbnail(@NonNull ImageView img, @NonNull String path, int width, int height) {
        if (ensureLoader()) {
            throw new IllegalStateException("init method should be called first");
        }
        mLoader.displayThumbnail(img, path, width, height);
    }

    public void displayRaw(@NonNull ImageView img, @NonNull String path, int width, int height, IBoxingCallback callback) {
        if (ensureLoader()) {
            throw new IllegalStateException("init method should be called first");
        }
        mLoader.displayRaw(img, path, width, height, callback);
    }

    public IBoxingMediaLoader getLoader() {
        return mLoader;
    }

    private boolean ensureLoader() {
        return mLoader == null;
    }
}
