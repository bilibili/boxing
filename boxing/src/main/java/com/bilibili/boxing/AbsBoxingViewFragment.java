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

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.bilibili.boxing.model.BoxingBuilderConfig;
import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.config.BoxingCropOption;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing.presenter.PickerContract;
import com.bilibili.boxing.utils.CameraPickerHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

/**
 * A abstract class which implements {@link PickerContract.View} for custom media view.
 * only one methods need to override {@link #startLoading()}, but there is more function to achieve by
 * checking every method can override.
 *
 * @author ChenSL
 */
public abstract class AbsBoxingViewFragment extends Fragment implements PickerContract.View {
    public static final String[] STORAGE_PERMISSIONS =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final String[] CAMERA_PERMISSIONS = {Manifest.permission.CAMERA};

    private static final int REQUEST_CODE_PERMISSION = 233;

    private PickerContract.Presenter mPresenter;
    private CameraPickerHelper mCameraPicker;
    private Boxing.OnBoxingFinishListener mOnFinishListener;

    /**
     * start loading when the permission request is completed.
     * call {@link #loadMedias()} or {@link #loadMedias(int, String)}, call {@link #loadAlbum()} if albums needed.
     */
    public abstract void startLoading();

    /**
     * called when request {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} and {@link Manifest.permission#CAMERA} permission error.
     *
     * @param e a IllegalArgumentException, IllegalStateException or SecurityException will be throw
     */
    public void onRequestPermissionError(String[] permissions, Exception e) {
    }

    /**
     * called when request {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} and {@link Manifest.permission#CAMERA} permission successfully.
     */
    public void onRequestPermissionSuc(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    /**
     * get the result of using camera to take a photo.
     *
     * @param media {@link BaseMedia}
     */
    public void onCameraFinish(BaseMedia media) {
    }

    /**
     * called when camera start error
     */
    public void onCameraError() {
    }

    /**
     * must override when care about the input medias, which means you call {@link #setSelectedBundle(ArrayList)} first.
     * this method is called in {@link Fragment#onCreate(Bundle)}, so override this rather than {@link Fragment#onCreate(Bundle)}.
     *
     * @param bundle         If the fragment is being re-created from
     *                       a previous saved state, this is the state.
     * @param selectedMedias the input medias, the parameter of {@link #setSelectedBundle(ArrayList)}.
     */
    public void onCreateWithSelectedMedias(Bundle bundle, @Nullable List<BaseMedia> selectedMedias) {
    }

    /**
     * override this method to handle the medias.
     * make sure {@link #loadMedias()} ()} being called first.
     *
     * @param medias the results of medias
     */
    @Override
    public void showMedia(@Nullable List<BaseMedia> medias, int allCount) {
    }

    /**
     * override this method to handle the album.
     * make sure {@link #loadAlbum()} being called first.
     *
     * @param albums the results of albums
     */
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
        BoxingConfig config;
        if (savedInstanceState != null) {
            config = savedInstanceState.getParcelable(Boxing.EXTRA_CONFIG);
        } else {
            config = BoxingManager.getInstance().getBoxingConfig();
        }
        setPickerConfig(config);
        onCreateWithSelectedMedias(savedInstanceState, parseSelectedMedias(savedInstanceState, getArguments()));
        super.onCreate(savedInstanceState);

        initCameraPhotoPicker(savedInstanceState);
    }

    @Nullable
    private ArrayList<BaseMedia> parseSelectedMedias(Bundle savedInstanceState, Bundle argument) {
        ArrayList<BaseMedia> selectedMedias = null;
        if (savedInstanceState != null) {
            selectedMedias = savedInstanceState.getParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA);
        } else if (argument != null) {
            selectedMedias = argument.getParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA);
        }
        return selectedMedias;
    }

    private void initCameraPhotoPicker(Bundle savedInstanceState) {
        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        if (config == null || !config.isNeedCamera()) {
            return;
        }
        mCameraPicker = new CameraPickerHelper(savedInstanceState);
        mCameraPicker.setPickCallback(new CameraListener(this));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkPermissionAndLoad();
    }

    private void checkPermissionAndLoad() {
        try {
            if (!BoxingBuilderConfig.TESTING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(getActivity(), STORAGE_PERMISSIONS[0]) != PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getActivity(), STORAGE_PERMISSIONS[1]) != PERMISSION_GRANTED) {
                requestPermissions(STORAGE_PERMISSIONS, REQUEST_CODE_PERMISSION);
            } else {
                startLoading();
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            onRequestPermissionError(STORAGE_PERMISSIONS, e);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE_PERMISSION == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionSuc(requestCode, permissions, grantResults);
            } else {
                onRequestPermissionError(permissions,
                        new SecurityException("request android.permission.READ_EXTERNAL_STORAGE error."));
            }
        }
    }

    /**
     * called when you have input medias, then call {@link #onCreateWithSelectedMedias(Bundle, List)} to get the input medias.
     *
     * @param selectedMedias input medias
     * @return {@link AbsBoxingViewFragment}
     */
    public final AbsBoxingViewFragment setSelectedBundle(ArrayList<BaseMedia> selectedMedias) {
        Bundle bundle = new Bundle();
        if (selectedMedias != null && !selectedMedias.isEmpty()) {
            bundle.putParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA, selectedMedias);
        }
        setArguments(bundle);
        return this;
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
        return getActivity().getApplicationContext().getContentResolver();
    }

    /**
     * if {@link AbsBoxingViewFragment} is not working with {@link AbsBoxingActivity}, it needs a listener to call
     * when the jobs done.
     *
     * @param onFinishListener {@link Boxing.OnBoxingFinishListener}
     */
    final void setOnFinishListener(Boxing.OnBoxingFinishListener onFinishListener) {
        mOnFinishListener = onFinishListener;
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
        if (mOnFinishListener != null) {
            mOnFinishListener.onBoxingFinish(intent, medias);
        }

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
        BoxingCropOption cropConfig = BoxingManager.getInstance().getBoxingConfig().getCropOption();
        BoxingCrop.getInstance().onStartCrop(getActivity(), this, cropConfig, media.getPath(), requestCode);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCameraPicker != null && requestCode == CameraPickerHelper.REQ_CODE_CAMERA) {
            onCameraActivityResult(requestCode, resultCode);
        }
        if (hasCropBehavior()) {
            onCropActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCameraPicker != null) {
            mCameraPicker.onSaveInstanceState(outState);
        }
        outState.putParcelable(Boxing.EXTRA_CONFIG, BoxingManager.getInstance().getBoxingConfig());
    }

    /**
     * in {@link BoxingConfig.Mode#MULTI_IMG}, call this in {@link Fragment#onSaveInstanceState(Bundle)}.
     *
     * @param outState Bundle in which to place your saved state.
     * @param selected the selected medias.
     */
    public final void onSaveMedias(Bundle outState, ArrayList<BaseMedia> selected) {
        if (selected != null && !selected.isEmpty()) {
            outState.putParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA, selected);
        }
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
        if (mCameraPicker != null) {
            mCameraPicker.release();
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
     * extra call to load albums in database, use {@link #showAlbum(List)} to get result.
     * In {@link BoxingConfig.Mode#VIDEO} it is not necessary.
     */
    public void loadAlbum() {
        if (!BoxingManager.getInstance().getBoxingConfig().isVideoMode()) {
            mPresenter.loadAlbums();
        }
    }

    public final boolean hasNextPage() {
        return mPresenter.hasNextPage();
    }

    public final boolean canLoadNextPage() {
        return mPresenter.canLoadNextPage();
    }

    public final void onLoadNextPage() {
        mPresenter.onLoadNextPage();
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

    /**
     * successfully get result from camera in {@link #onActivityResult(int, int, Intent)}.
     * call this after other operations.
     */
    public void onCameraActivityResult(int requestCode, int resultCode) {
        mCameraPicker.onActivityResult(requestCode, resultCode);
    }

    /**
     * successfully get result from crop in {@link #onActivityResult(int, int, Intent)}
     */
    public void onCropActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        Uri output = BoxingCrop.getInstance().onCropFinish(resultCode, data);
        if (output != null) {
            List<BaseMedia> medias = new ArrayList<>(1);
            ImageMedia media = new ImageMedia(String.valueOf(System.currentTimeMillis()), output.getPath());
            medias.add(media);
            onFinish(medias);
        }
    }

    /**
     * start camera to take a photo.
     *
     * @param activity      the caller activity.
     * @param fragment      the caller fragment, may be null.
     * @param subFolderPath the folder name in "DCIM/bili/boxing/"
     */
    public final void startCamera(Activity activity, Fragment fragment, String subFolderPath) {
        try {
            if (!BoxingBuilderConfig.TESTING && ContextCompat.checkSelfPermission(getActivity(), CAMERA_PERMISSIONS[0]) != PERMISSION_GRANTED) {
                requestPermissions(CAMERA_PERMISSIONS, REQUEST_CODE_PERMISSION);
            } else {
                if (!BoxingManager.getInstance().getBoxingConfig().isVideoMode()) {
                    mCameraPicker.startCamera(activity, fragment, subFolderPath);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            onRequestPermissionError(CAMERA_PERMISSIONS, e);
        }
    }

    private static final class CameraListener implements CameraPickerHelper.Callback {
        private WeakReference<AbsBoxingViewFragment> mWr;

        CameraListener(AbsBoxingViewFragment fragment) {
            mWr = new WeakReference<>(fragment);
        }

        @Override
        public void onFinish(@NonNull CameraPickerHelper helper) {
            AbsBoxingViewFragment fragment = mWr.get();
            if (fragment == null) {
                return;
            }
            File file = new File(helper.getSourceFilePath());

            if (!file.exists()) {
                onError(helper);
                return;
            }
            ImageMedia cameraMedia = new ImageMedia(file);
            cameraMedia.saveMediaStore(fragment.getAppCr());
            fragment.onCameraFinish(cameraMedia);
        }

        @Override
        public void onError(@NonNull CameraPickerHelper helper) {
            AbsBoxingViewFragment fragment = mWr.get();
            if (fragment == null) {
                return;
            }
            fragment.onCameraError();
        }

    }
}
