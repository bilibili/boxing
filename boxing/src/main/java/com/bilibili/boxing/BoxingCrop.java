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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.bilibili.boxing.loader.IBoxingCrop;
import com.bilibili.boxing.model.config.BoxingCropOption;

/**
 * A loader holding {@link IBoxingCrop} to crop images.
 *
 * @author ChenSL
 */
public class BoxingCrop {
    private static final BoxingCrop INSTANCE = new BoxingCrop();
    private IBoxingCrop mCrop;

    private BoxingCrop() {
    }

    public static BoxingCrop getInstance() {
        return INSTANCE;
    }

    public void init(@NonNull IBoxingCrop loader) {
        this.mCrop = loader;
    }

    public void onStartCrop(Activity activity, Fragment fragment, @NonNull BoxingCropOption cropConfig,
                            @NonNull String path, int requestCode) {
        if (ensureLoader()) {
            throw new IllegalStateException("init method should be called first");
        }
        if (cropConfig == null) {
            throw new IllegalArgumentException("crop config is null.");
        }
        mCrop.onStartCrop(activity, fragment, cropConfig, path, requestCode);
    }

    public Uri onCropFinish(int resultCode, Intent data) {
        if (ensureLoader()) {
            throw new IllegalStateException("init method should be called first");
        }
        return mCrop.onCropFinish(resultCode, data);
    }

    public IBoxingCrop getCrop() {
        return mCrop;
    }

    private boolean ensureLoader() {
        return mCrop == null;
    }
}
