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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.presenter.PickerPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for {@link AbsBoxingActivity} and {@link AbsBoxingViewFragment}.<br/>
 * 1.call {@link #of(BoxingConfig)} to pick a mode.<br/>
 * 2.to use {@link AbsBoxingActivity} + {@link AbsBoxingViewFragment} combination,
 * call {@link #withIntent(Context, Class)} to make a intent and {@link #start(Activity)} to start a new Activity.<br/>
 * to use {@link AbsBoxingViewFragment} only, just call {@link #setupFragment(AbsBoxingViewFragment, OnBoxingFinishListener)}.<br/>
 * 3 4.to get result from a new Activity, call {@link #getResult(Intent)} in {@link Activity#onActivityResult(int, int, Intent)}.
 *
 * @author ChenSL
 */
public class Boxing {
    public static final String EXTRA_SELECTED_MEDIA = "com.bilibili.boxing.Boxing.selected_media";
    public static final String EXTRA_ALBUM_ID = "com.bilibili.boxing.Boxing.album_id";

    static final String EXTRA_CONFIG = "com.bilibili.boxing.Boxing.config";
    static final String EXTRA_RESULT = "com.bilibili.boxing.Boxing.result";
    static final String EXTRA_START_POS = "com.bilibili.boxing.Boxing.start_pos";

    private Intent mIntent;

    private Boxing(BoxingConfig config) {
        BoxingManager.getInstance().setBoxingConfig(config);
        this.mIntent = new Intent();
    }

    /**
     * get the media result.
     */
    @Nullable
    public static ArrayList<BaseMedia> getResult(Intent data) {
        if (data != null) {
            return data.getParcelableArrayListExtra(EXTRA_RESULT);
        }
        return null;
    }

    /**
     * call {@link #of(BoxingConfig)} first to specify the mode otherwise {@link BoxingConfig.Mode#MULTI_IMG} is used.<br/>
     */
    public static Boxing get() {
        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        if (config == null) {
            config = new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needGif();
            BoxingManager.getInstance().setBoxingConfig(config);
        }
        return new Boxing(config);
    }

    /**
     * create a boxing entry.
     *
     * @param config {@link BoxingConfig}
     */
    public static Boxing of(BoxingConfig config) {
        return new Boxing(config);
    }

    /**
     * create a boxing entry.
     *
     * @param mode {@link BoxingConfig.Mode}
     */
    public static Boxing of(BoxingConfig.Mode mode) {
        return new Boxing(new BoxingConfig(mode));
    }

    /**
     * create a boxing entry. use {@link BoxingConfig.Mode#MULTI_IMG}.
     */
    public static Boxing of() {
        BoxingConfig config = new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needGif();
        return new Boxing(config);
    }

    /**
     * get the intent build by boxing after call {@link #withIntent}.
     */
    public Intent getIntent() {
        return mIntent;
    }

    /**
     * same as {@link Intent#setClass(Context, Class)}
     */
    public Boxing withIntent(Context context, Class<?> cls) {
        return withIntent(context, cls, null);
    }

    /**
     * {@link Intent#setClass(Context, Class)} with input medias.
     */
    public Boxing withIntent(Context context, Class<?> cls, ArrayList<? extends BaseMedia> selectedMedias) {
        mIntent.setClass(context, cls);
        if (selectedMedias != null && !selectedMedias.isEmpty()) {
            mIntent.putExtra(EXTRA_SELECTED_MEDIA, selectedMedias);
        }
        return this;
    }

    /**
     * use to start image viewer.
     *
     * @param medias selected medias.
     * @param pos    the start position.
     */
    public Boxing withIntent(Context context, Class<?> cls, ArrayList<? extends BaseMedia> medias, int pos) {
        withIntent(context, cls, medias, pos, "");
        return this;
    }


    /**
     * use to start image viewer.
     *
     * @param medias  selected medias.
     * @param pos     the start position.
     * @param albumId the specify album id.
     */
    public Boxing withIntent(Context context, Class<?> cls, ArrayList<? extends BaseMedia> medias, int pos, String albumId) {
        mIntent.setClass(context, cls);
        if (medias != null && !medias.isEmpty()) {
            mIntent.putExtra(EXTRA_SELECTED_MEDIA, medias);
        }
        if (pos >= 0) {
            mIntent.putExtra(EXTRA_START_POS, pos);
        }
        if (albumId != null) {
            mIntent.putExtra(EXTRA_ALBUM_ID, albumId);
        }
        return this;
    }


    /**
     * same as {@link Activity#startActivity(Intent)}
     */
    public void start(@NonNull Activity activity) {
        activity.startActivity(mIntent);
    }

    /**
     * use to start raw image viewer.
     *
     * @param viewMode {@link BoxingConfig.ViewMode}
     */
    public void start(@NonNull Activity activity, BoxingConfig.ViewMode viewMode) {
        BoxingManager.getInstance().getBoxingConfig().withViewer(viewMode);
        activity.startActivity(mIntent);
    }

    /**
     * same as {@link Activity#startActivityForResult(Intent, int, Bundle)}
     */
    public void start(@NonNull Activity activity, int requestCode) {
        activity.startActivityForResult(mIntent, requestCode);
    }

    /**
     * same as {@link Fragment#startActivityForResult(Intent, int, Bundle)}
     */
    public void start(@NonNull Fragment fragment, int requestCode) {
        fragment.startActivityForResult(mIntent, requestCode);
    }

    /**
     * use to start raw image viewer.
     *
     * @param viewMode {@link BoxingConfig.ViewMode}
     */
    public void start(@NonNull Fragment fragment, int requestCode, BoxingConfig.ViewMode viewMode) {
        BoxingManager.getInstance().getBoxingConfig().withViewer(viewMode);
        fragment.startActivityForResult(mIntent, requestCode);
    }

    /**
     * same as {@link android.app.Fragment#startActivityForResult(Intent, int, Bundle)}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void start(@NonNull android.app.Fragment fragment, int requestCode) {
        fragment.startActivityForResult(mIntent, requestCode);
    }

    /**
     * use to start raw image viewer.
     *
     * @param viewMode {@link BoxingConfig.ViewMode}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void start(@NonNull android.app.Fragment fragment, int requestCode, BoxingConfig.ViewMode viewMode) {
        BoxingManager.getInstance().getBoxingConfig().withViewer(viewMode);
        fragment.startActivityForResult(mIntent, requestCode);
    }

    /**
     * set up a subclass of {@link AbsBoxingViewFragment} without a {@link AbsBoxingActivity}.
     *
     * @param fragment         subclass of {@link AbsBoxingViewFragment}
     * @param onFinishListener a listener fo media result
     */
    public void setupFragment(@NonNull AbsBoxingViewFragment fragment, OnBoxingFinishListener onFinishListener) {
        fragment.setPresenter(new PickerPresenter(fragment));
        fragment.setOnFinishListener(onFinishListener);
    }

    /**
     * work with a subclass of {@link AbsBoxingViewFragment} without a {@link AbsBoxingActivity}.
     */
    public interface OnBoxingFinishListener {

        /**
         * live with {@link com.bilibili.boxing.presenter.PickerContract.View#onFinish(List)}
         *
         * @param medias the selection of medias.
         */
        void onBoxingFinish(Intent intent, @Nullable List<BaseMedia> medias);
    }

}
