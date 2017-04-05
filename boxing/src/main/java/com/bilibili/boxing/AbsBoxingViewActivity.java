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

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.presenter.PickerContract;
import com.bilibili.boxing.presenter.PickerPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * A abstract class which implements {@link PickerContract.View} for custom media view.
 * For view big images.
 *
 * @author ChenSL
 */
public abstract class AbsBoxingViewActivity extends AppCompatActivity implements PickerContract.View {
    ArrayList<BaseMedia> mSelectedImages;
    String mAlbumId;
    int mStartPos;

    private PickerContract.Presenter mPresenter;

    /**
     * start loading when the permission request is completed.
     * call {@link #loadMedias()} or {@link #loadMedias(int, String)}.
     */
    public abstract void startLoading();

    /**
     * override this method to handle the medias.
     * make sure {@link #loadMedias()} ()} being called first.
     *
     * @param medias the results of medias
     */
    @Override
    public void showMedia(@Nullable List<BaseMedia> medias, int allCount) {
    }

    @Override
    public void showAlbum(@Nullable List<AlbumEntity> albums) {
    }

    /**
     * to clear all medias the first time(the page number is 0). do some clean work.
     */
    @Override
    public void clearMedia() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BoxingConfig config;
        if (savedInstanceState != null) {
            config = savedInstanceState.getParcelable(Boxing.EXTRA_CONFIG);
        } else {
            config = BoxingManager.getInstance().getBoxingConfig();
        }
        setPickerConfig(config);
        parseSelectedMedias(savedInstanceState, getIntent());
        setPresenter(new PickerPresenter(this));
    }

    private void parseSelectedMedias(Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState != null) {
            mSelectedImages = savedInstanceState.getParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA);
            mAlbumId = savedInstanceState.getString(Boxing.EXTRA_ALBUM_ID);
            mStartPos = savedInstanceState.getInt(Boxing.EXTRA_START_POS, 0);
        } else if (intent != null) {
            mStartPos = intent.getIntExtra(Boxing.EXTRA_START_POS, 0);
            mSelectedImages = intent.getParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA);
            mAlbumId = intent.getStringExtra(Boxing.EXTRA_ALBUM_ID);
        }
    }

    @Override
    public final void setPresenter(@NonNull PickerContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    /**
     * get the {@link ContentResolver}
     */
    @NonNull
    @Override
    public final ContentResolver getAppCr() {
        return getApplicationContext().getContentResolver();
    }

    public final void loadRawImage(@NonNull ImageView img, @NonNull String path, int width, int height, IBoxingCallback callback) {
        BoxingMediaLoader.getInstance().displayRaw(img, path, width, height, callback);
    }

    /**
     * called the job is done.Click the ok button, take a photo from camera, crop a photo.
     * most of the time, you do not have to override.
     *
     * @param medias the list of selection
     */
    @Override
    public void onFinish(@NonNull List<BaseMedia> medias) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Boxing.EXTRA_RESULT, (ArrayList<BaseMedia>) medias);
    }

    /**
     * need crop or not
     *
     * @return true, need it.
     */
    public final boolean hasCropBehavior() {
        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        return config != null && config.isSingleImageMode() && config.getCropOption() != null;
    }

    /**
     * to start the crop behavior, call it when {@link #hasCropBehavior()} return true.
     *
     * @param media       the media to be cropped.
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     */
    @Override
    public final void startCrop(@NonNull BaseMedia media, int requestCode) {
    }

    /**
     * set or update the config.most of the time, you do not have to call it.
     *
     * @param config {@link BoxingConfig}
     */
    @Override
    public final void setPickerConfig(BoxingConfig config) {
        if (config == null) {
            return;
        }
        BoxingManager.getInstance().setBoxingConfig(config);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable(Boxing.EXTRA_CONFIG, BoxingManager.getInstance().getBoxingConfig());
    }

    /**
     * call this to clear resource.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
    }

    /**
     * in {@link BoxingConfig.Mode#MULTI_IMG}, call this to pick the selected medias in all medias.
     */
    public final void checkSelectedMedia(List<BaseMedia> allMedias, List<BaseMedia> selectedMedias) {
        mPresenter.checkSelectedMedia(allMedias, selectedMedias);
    }

    /**
     * load first page of medias.
     * use {@link #showMedia(List, int)} to get the result.
     */
    public final void loadMedias() {
        mPresenter.loadMedias(0, AlbumEntity.DEFAULT_NAME);
    }

    /**
     * load the medias for the specify page and album id.
     * use {@link #showMedia(List, int)} to get the result.
     *
     * @param page    page numbers.
     * @param albumId the album id is {@link AlbumEntity#mBucketId}.
     */
    public final void loadMedias(int page, String albumId) {
        mPresenter.loadMedias(page, albumId);
    }

    /**
     * get the max count set before
     */
    public final int getMaxCount() {
        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        if (config == null) {
            return BoxingConfig.DEFAULT_SELECTED_COUNT;
        }
        return config.getMaxCount();
    }

    @NonNull
    public final ArrayList<BaseMedia> getSelectedImages() {
        if (mSelectedImages != null) {
            return mSelectedImages;
        }
        return new ArrayList<>();
    }

    public final String getAlbumId() {
        return mAlbumId;
    }

    public final int getStartPos() {
        return mStartPos;
    }
}
