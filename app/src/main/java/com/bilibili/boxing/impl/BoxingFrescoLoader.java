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

package com.bilibili.boxing.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bilibili.boxing.demo.R;
import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bilibili.boxing.utils.BoxingFileHelper;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.ByteConstants;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.OrientedDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedDrawable;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

/**
 * use Fresco(https://github.com/facebook/fresco) to display medias.
 * can <b>not</b> be used in Production Environment.
 *
 * fresco strongly suggest to use DraweeView instead of ImageView, according to https://github.com/facebook/fresco/issues/1550.
 *
 * @author ChenSL
 */
public class BoxingFrescoLoader implements IBoxingMediaLoader {
    private static final int MAX_DISK_CACHE_VERYLOW_SIZE = 20 * ByteConstants.MB;
    private static final int MAX_DISK_CACHE_LOW_SIZE = 60 * ByteConstants.MB;
    private static final int MAX_DISK_CACHE_SIZE = 100 * ByteConstants.MB;
    private static final String IMAGE_PIPELINE_CACHE_DIR = "ImagePipeLine";

    public BoxingFrescoLoader(@NonNull Context context) {
        init(context);
    }

    private void init(Context context) {
        ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true);
        String cache = BoxingFileHelper.getCacheDir(context);

        if (TextUtils.isEmpty(cache)) {
            throw new IllegalStateException("the cache dir is null");
        }
        if (!TextUtils.isEmpty(cache)) {
            DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                    .setBaseDirectoryPath(new File(cache))
                    .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)
                    .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
                    .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)
                    .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)
                    .build();
            builder.setMainDiskCacheConfig(diskCacheConfig);
        }
        ImagePipelineConfig config = builder.build();
        Fresco.initialize(context, config);
    }


    @Override
    public void displayThumbnail(@NonNull final ImageView img, @NonNull final String absPath, int width, int height) {
        String finalAbsPath = "file://" + absPath;
        ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(finalAbsPath));
        requestBuilder.setResizeOptions(new ResizeOptions(width, height));
        ImageRequest request = requestBuilder.build();
        final DataSource<CloseableReference<CloseableImage>> dataSource =
                Fresco.getImagePipeline().fetchDecodedImage(request, null);

        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {

            @Override
            protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                String path = (String) img.getTag(R.string.boxing_app_name);
                if (path == null || absPath.equals(path)) {
                    if (dataSource.getResult() == null) {
                        onFailureImpl(dataSource);
                        return;
                    }
                    Drawable drawable = createDrawableFromFetchedResult(img.getContext(), dataSource.getResult().get());
                    img.setImageDrawable(drawable);
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                img.setImageResource(R.drawable.ic_boxing_broken_image);
            }
        }, UiThreadImmediateExecutorService.getInstance());
    }

    @Override
    public void displayRaw(@NonNull ImageView img, @NonNull String absPath, int width, int height,  IBoxingCallback callback) {
        absPath = "file://" + absPath;
        ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(absPath));
        if (width > 0 && height > 0) {
            requestBuilder.setResizeOptions(new ResizeOptions(width, height));
        }
        ImageRequest request = requestBuilder.build();
        loadImage(request, img, callback);
    }

    private void loadImage(final ImageRequest request, final ImageView imageView, final IBoxingCallback callback) {
        final DataSource<CloseableReference<CloseableImage>> dataSource =
                Fresco.getImagePipeline().fetchDecodedImage(request, null);
        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {

            @Override
            protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                CloseableReference<CloseableImage> result = dataSource.getResult();
                if (result == null) {
                    onFailureImpl(dataSource);
                    return;
                }
                try {
                    Drawable drawable = createDrawableFromFetchedResult(imageView.getContext(), result.get());
                    if (drawable == null) {
                        onFailureImpl(dataSource);
                        return;
                    }
                    if (drawable instanceof AnimatedDrawable) {
                        imageView.setImageDrawable(drawable);
                        ((AnimatedDrawable) drawable).start();
                    } else {
                        imageView.setImageDrawable(drawable);
                    }
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (UnsupportedOperationException e) {
                    onFailureImpl(dataSource);
                } finally {
                    CloseableReference.closeSafely(result);
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                if (callback == null) {
                    return;
                }
                if (dataSource == null) {
                    callback.onFail(new NullPointerException("data source is null."));
                } else {
                    callback.onFail(dataSource.getFailureCause());
                }
            }

        }, UiThreadImmediateExecutorService.getInstance());

    }

    private Drawable createDrawableFromFetchedResult(Context context, CloseableImage image) {
        if (image instanceof CloseableStaticBitmap) {
            CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
            BitmapDrawable bitmapDrawable = createBitmapDrawable(context, closeableStaticBitmap.getUnderlyingBitmap());
            return (closeableStaticBitmap.getRotationAngle() != 0 && closeableStaticBitmap.getRotationAngle() != -1 ? new OrientedDrawable(bitmapDrawable, closeableStaticBitmap.getRotationAngle()) : bitmapDrawable);
        } else if (image instanceof CloseableAnimatedImage) {
            AnimatedDrawableFactory animatedDrawableFactory = Fresco.getImagePipelineFactory().getAnimatedFactory().getAnimatedDrawableFactory(context);
            if (animatedDrawableFactory != null) {
                AnimatedDrawable animatedDrawable = (AnimatedDrawable) animatedDrawableFactory.create(image);
                if (animatedDrawable != null) {
                    return animatedDrawable;
                }
            }
        }
        throw new UnsupportedOperationException("Unrecognized image class: " + image);
    }

    private static BitmapDrawable createBitmapDrawable(Context context, Bitmap bitmap) {
        BitmapDrawable drawable;
        if (context != null) {
            drawable = new BitmapDrawable(context.getResources(), bitmap);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && drawable.canApplyTheme()) {
                drawable.applyTheme(context.getTheme());
            }
        } else {
            drawable = new BitmapDrawable(null, bitmap);
        }
        return drawable;
    }

}
