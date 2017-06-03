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

package com.bilibili.boxing.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.widget.ImageView
import com.bilibili.boxing.demo.R
import com.bilibili.boxing.loader.IBoxingCallback
import com.bilibili.boxing.loader.IBoxingMediaLoader
import com.bilibili.boxing.utils.BoxingFileHelper
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.common.util.ByteConstants
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.OrientedDrawable
import com.facebook.imagepipeline.animated.base.AnimatedDrawable
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.image.CloseableAnimatedImage
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.image.CloseableStaticBitmap
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import java.io.File

/**
 * use Fresco(https://github.com/facebook/fresco) to display medias.
 * can **not** be used in Production Environment.

 * fresco strongly suggest to use DraweeView instead of ImageView, according to https://github.com/facebook/fresco/issues/1550.

 * @author ChenSL
 */
class BoxingFrescoLoader(context: Context) : IBoxingMediaLoader {

    init {
        init(context)
    }

    private fun init(context: Context) {
        val builder = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true)
        val cache = BoxingFileHelper.getCacheDir(context)

        if (TextUtils.isEmpty(cache)) {
            throw IllegalStateException("the cache dir is null")
        }
        if (!TextUtils.isEmpty(cache)) {
            val diskCacheConfig = DiskCacheConfig.newBuilder(context)
                    .setBaseDirectoryPath(File(cache!!))
                    .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)
                    .setMaxCacheSize(MAX_DISK_CACHE_SIZE.toLong())
                    .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE.toLong())
                    .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE.toLong())
                    .build()
            builder.setMainDiskCacheConfig(diskCacheConfig)
        }
        val config = builder.build()
        Fresco.initialize(context, config)
    }


    override fun displayThumbnail(img: ImageView, absPath: String, width: Int, height: Int) {
        val finalAbsPath = "file://" + absPath
        val requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(finalAbsPath))
        requestBuilder.resizeOptions = ResizeOptions(width, height)
        val request = requestBuilder.build()
        val dataSource = Fresco.getImagePipeline().fetchDecodedImage(request, null)

        dataSource.subscribe(object : BaseDataSubscriber<CloseableReference<CloseableImage>>() {

            override fun onNewResultImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                val path = img.getTag(R.string.boxing_app_name) as String
                if (TextUtils.isEmpty(path) || absPath == path) {
                    if (dataSource.result == null) {
                        onFailureImpl(dataSource)
                        return
                    }
                    dataSource.result?.let {
                        val drawable = createDrawableFromFetchedResult(img.context, it.get())
                        img.setImageDrawable(drawable)
                    }
                }
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                img.setImageResource(R.drawable.ic_boxing_broken_image)
            }
        }, UiThreadImmediateExecutorService.getInstance())
    }

    override fun displayRaw(img: ImageView, absPath: String, width: Int, height: Int, callback: IBoxingCallback?) {
        val absPath = "file://" + absPath
        val requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(absPath))
        if (width > 0 && height > 0) {
            requestBuilder.resizeOptions = ResizeOptions(width, height)
        }
        val request = requestBuilder.build()
        loadImage(request, img, callback)
    }

    private fun loadImage(request: ImageRequest, imageView: ImageView, callback: IBoxingCallback?) {
        val dataSource = Fresco.getImagePipeline().fetchDecodedImage(request, null)
        dataSource.subscribe(object : BaseDataSubscriber<CloseableReference<CloseableImage>>() {

            override fun onNewResultImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                val result = dataSource.result
                if (result == null) {
                    onFailureImpl(dataSource)
                    return
                }
                try {
                    val drawable = createDrawableFromFetchedResult(imageView.context, result.get())
                    if (drawable == null) {
                        onFailureImpl(dataSource)
                        return
                    }
                    if (drawable is AnimatedDrawable) {
                        imageView.setImageDrawable(drawable)
                        drawable.start()
                    } else {
                        imageView.setImageDrawable(drawable)
                    }
                    callback?.onSuccess()
                } catch (e: UnsupportedOperationException) {
                    onFailureImpl(dataSource)
                } finally {
                    CloseableReference.closeSafely(result)
                }
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                if (callback == null) {
                    return
                }
                if (dataSource == null) {
                    callback.onFail(NullPointerException("data source is null."))
                } else {
                    callback.onFail(dataSource.failureCause!!)
                }
            }

        }, UiThreadImmediateExecutorService.getInstance())

    }

    private fun createDrawableFromFetchedResult(context: Context, image: CloseableImage): Drawable? {
        if (image is CloseableStaticBitmap) {
            val closeableStaticBitmap = image
            val bitmapDrawable = createBitmapDrawable(context, closeableStaticBitmap.underlyingBitmap)
            return if (closeableStaticBitmap.rotationAngle != 0 && closeableStaticBitmap.rotationAngle != -1) OrientedDrawable(bitmapDrawable, closeableStaticBitmap.rotationAngle) else bitmapDrawable
        } else if (image is CloseableAnimatedImage) {
            val animatedDrawableFactory = Fresco.getImagePipelineFactory().animatedFactory.getAnimatedDrawableFactory(context)
            if (animatedDrawableFactory != null) {
                val animatedDrawable = animatedDrawableFactory.create(image) as AnimatedDrawable
                return animatedDrawable
            }
        }
        throw UnsupportedOperationException("Unrecognized image class: " + image)
    }

    companion object {
        private val MAX_DISK_CACHE_VERYLOW_SIZE = 20 * ByteConstants.MB
        private val MAX_DISK_CACHE_LOW_SIZE = 60 * ByteConstants.MB
        private val MAX_DISK_CACHE_SIZE = 100 * ByteConstants.MB
        private val IMAGE_PIPELINE_CACHE_DIR = "ImagePipeLine"

        private fun createBitmapDrawable(context: Context?, bitmap: Bitmap): BitmapDrawable {
            val drawable: BitmapDrawable
            if (context != null) {
                drawable = BitmapDrawable(context.resources, bitmap)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && drawable.canApplyTheme()) {
                    drawable.applyTheme(context.theme)
                }
            } else {
                drawable = BitmapDrawable(null, bitmap)
            }
            return drawable
        }
    }

}
