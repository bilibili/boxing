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
package com.bilibili.boxing.model.task.impl;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.callback.IAlbumTaskCallback;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing.utils.BoxingExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A task to load albums.
 *
 * @author ChenSL
 */
@WorkerThread
public class AlbumTask {
    private static final String UNKNOWN_ALBUM_NAME = "unknow";
    private static final String SELECTION_IMAGE_MIME_TYPE = Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=?";
    private static final String SELECTION_IMAGE_MIME_TYPE_WITHOUT_GIF = Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=?";
    private static final String SELECTION_ID = Media.BUCKET_ID + "=? and (" + SELECTION_IMAGE_MIME_TYPE + " )";
    private static final String SELECTION_ID_WITHOUT_GIF = Media.BUCKET_ID + "=? and (" + SELECTION_IMAGE_MIME_TYPE_WITHOUT_GIF + " )";
    private static final String[] SELECTION_ARGS_IMAGE_MIME_TYPE = {"image/jpeg", "image/png", "image/jpg", "image/gif"};
    private static final String[] SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF = {"image/jpeg", "image/png", "image/jpg"};
    private int mUnknownAlbumNumber = 1;
    private Map<String, AlbumEntity> mBucketMap;
    private AlbumEntity mDefaultAlbum;
    private BoxingConfig mPickerConfig;

    public AlbumTask() {
        this.mBucketMap = new ArrayMap<>();
        this.mDefaultAlbum = AlbumEntity.createDefaultAlbum();
        this.mPickerConfig = BoxingManager.getInstance().getBoxingConfig();
    }

    public void start(@NonNull final ContentResolver cr, @NonNull final IAlbumTaskCallback callback) {
        buildAlbumInfo(cr);
        getAlbumList(callback);
    }

    private void buildAlbumInfo(ContentResolver cr) {
        String[] distinctBucketColumns = new String[]{Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME};
        Cursor bucketCursor = null;
        try {
            bucketCursor = cr.query(Media.EXTERNAL_CONTENT_URI, distinctBucketColumns, "0==0)" + " GROUP BY(" + Media.BUCKET_ID, null,
                    Media.DATE_MODIFIED + " desc");
            if (bucketCursor != null && bucketCursor.moveToFirst()) {
                do {
                    String buckId = bucketCursor.getString(bucketCursor.getColumnIndex(Media.BUCKET_ID));
                    String name = bucketCursor.getString(bucketCursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME));
                    AlbumEntity album = buildAlbumInfo(name, buckId);
                    if (!TextUtils.isEmpty(buckId)) {
                        buildAlbumCover(cr, buckId, album);
                    }
                } while (bucketCursor.moveToNext());
            }
        } finally {
            if (bucketCursor != null) {
                bucketCursor.close();
            }
        }
    }

    /**
     * get the cover and count
     *
     * @param buckId album id
     */
    private void buildAlbumCover(ContentResolver cr, String buckId, AlbumEntity album) {
        String[] photoColumn = new String[]{Media._ID, Media.DATA};
        boolean isNeedGif = mPickerConfig != null && mPickerConfig.isNeedGif();
        String selectionId = isNeedGif ? SELECTION_ID : SELECTION_ID_WITHOUT_GIF;
        String[] args = isNeedGif ? SELECTION_ARGS_IMAGE_MIME_TYPE : SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF;
        String[] selectionArgs = new String[args.length + 1];
        selectionArgs[0] = buckId;
        for (int i = 1; i < selectionArgs.length; i++) {
            selectionArgs[i] = args[i-1];
        }
        Cursor coverCursor = cr.query(Media.EXTERNAL_CONTENT_URI, photoColumn, selectionId,
                selectionArgs, Media.DATE_MODIFIED + " desc");
        try {
            if (coverCursor != null && coverCursor.moveToFirst()) {
                String picPath = coverCursor.getString(coverCursor.getColumnIndex(Media.DATA));
                String id = coverCursor.getString(coverCursor.getColumnIndex(Media._ID));
                album.mCount = coverCursor.getCount();
                album.mImageList.add(new ImageMedia(id, picPath));
                if (album.mImageList.size() > 0) {
                    mBucketMap.put(buckId, album);
                }
            }
        } finally {
            if (coverCursor != null) {
                coverCursor.close();
            }
        }
    }

    private void getAlbumList(@NonNull final IAlbumTaskCallback callback) {
        mDefaultAlbum.mCount = 0;
        List<AlbumEntity> tmpList = new ArrayList<>();
        if (mBucketMap == null) {
            postAlbums(callback, tmpList);
            return;
        }
        for (Map.Entry<String, AlbumEntity> entry : mBucketMap.entrySet()) {
            tmpList.add(entry.getValue());
            mDefaultAlbum.mCount += entry.getValue().mCount;
        }
        if (tmpList.size() > 0 && tmpList.get(0) != null) {
            mDefaultAlbum.mImageList = tmpList.get(0).mImageList;
            tmpList.add(0, mDefaultAlbum);
        }
        postAlbums(callback, tmpList);
        clear();
    }

    private void postAlbums(@NonNull final IAlbumTaskCallback callback, final List<AlbumEntity> result) {
        BoxingExecutor.getInstance().runUI(new Runnable() {
            @Override
            public void run() {
                callback.postAlbumList(result);
            }
        });
    }

    @NonNull
    private AlbumEntity buildAlbumInfo(String bucketName, String bucketId) {
        AlbumEntity album = null;
        if (!TextUtils.isEmpty(bucketId)) {
            album = mBucketMap.get(bucketId);
        }
        if (album == null) {
            album = new AlbumEntity();
            if (!TextUtils.isEmpty(bucketId)) {
                album.mBucketId = bucketId;
            } else {
                album.mBucketId = String.valueOf(mUnknownAlbumNumber);
                mUnknownAlbumNumber++;
            }
            if (!TextUtils.isEmpty(bucketName)) {
                album.mBucketName = bucketName;
            } else {
                album.mBucketName = UNKNOWN_ALBUM_NAME;
                mUnknownAlbumNumber++;
            }
            if (album.mImageList.size() > 0) {
                mBucketMap.put(bucketId, album);
            }
        }
        return album;
    }

    private void clear() {
        if (mBucketMap != null) {
            mBucketMap.clear();
        }
    }

}
