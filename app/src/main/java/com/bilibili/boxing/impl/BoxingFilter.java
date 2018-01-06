package com.bilibili.boxing.impl;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.bilibili.boxing.loader.IBoxingMediaFilter;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.BaseMedia;

import java.util.Iterator;
import java.util.List;

/**
 * @author jax
 * @date 2018/1/5
 * @description filter album images. sample for filter img width and height.
 */

public class BoxingFilter implements IBoxingMediaFilter {
    private static final String TAG = "BoxingFilter";
    private static final int MIN_WIDTH = 1280;
    private static final int MIN_HEIGHT = 720;

    @Override
    public List<AlbumEntity> filterAlbum(List<AlbumEntity> albums) {
        Iterator<AlbumEntity> entityIterator = albums.iterator();
        while (entityIterator.hasNext()) {
            AlbumEntity entity = entityIterator.next();
            List<BaseMedia> medias = entity.mImageList;
            entity.mImageList = filterMedia(medias);
            if (entity.mImageList.size() <= 0) {
                entityIterator.remove();
            }
        }
        return albums;
    }

    @Override
    public List<BaseMedia> filterMedia(List<BaseMedia> medias) {
        if (medias != null) {
            Iterator<BaseMedia> baseMediaIterator = medias.iterator();
            while (baseMediaIterator.hasNext()) {
                BaseMedia media = baseMediaIterator.next();
                if (!checkWH(media)) {
                    baseMediaIterator.remove();
                }
            }
        }
        return medias;
    }


    private boolean checkWH(BaseMedia media) {
        try {
            String path = media.getPath();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int w = options.outWidth;
            int h = options.outHeight;
            Log.d(TAG, "checkWH: w -->" + w + "  ,h -->" + h);
            return w > MIN_WIDTH && h > MIN_HEIGHT;
        } catch (Exception e) {
            Log.e(TAG, "checkWH: ", e);
        }
        return true;
    }
}
