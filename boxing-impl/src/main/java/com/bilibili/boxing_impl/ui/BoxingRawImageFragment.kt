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

package com.bilibili.boxing_impl.ui

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.bilibili.boxing.AbsBoxingViewActivity
import com.bilibili.boxing.loader.IBoxingCallback
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.utils.BoxingLog
import com.bilibili.boxing_impl.R
import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher
import java.lang.ref.WeakReference

/**
 * show raw image with the control of finger gesture.

 * @author ChenSL
 */
class BoxingRawImageFragment : BoxingBaseFragment() {

    lateinit private var mImageView: PhotoView
    lateinit private var mProgress: ProgressBar
    private var mMedia: ImageMedia? = null
    lateinit private var mAttacher: PhotoViewAttacher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMedia = arguments.getParcelable<ImageMedia>(BUNDLE_IMAGE)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_boxing_raw_image, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mProgress = view!!.findViewById(R.id.loading) as ProgressBar
        mImageView = view.findViewById(R.id.photo_view) as PhotoView
        mAttacher = PhotoViewAttacher(mImageView)
        mAttacher.setRotatable(true)
        mAttacher.setToRightAngle(true)
    }

    internal override fun setUserVisibleCompat(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            val point = getResizePointer(mMedia!!.getSize())
            (activity as AbsBoxingViewActivity).loadRawImage(mImageView, mMedia!!.path, point.x, point.y, BoxingCallback(this))
        }
    }

    /**
     * resize the image or not according to size.

     * @param size the size of image
     */
    private fun getResizePointer(size: Long): Point {
        val metrics = resources.displayMetrics
        val point = Point(metrics.widthPixels, metrics.heightPixels)
        if (size >= MAX_IMAGE2) {
            point.x = point.x shr 2
            point.y = point.y shr 2
        } else if (size >= MAX_IMAGE1) {
            point.x = point.x shr 1
            point.y = point.y shr 1
        } else if (size > 0) {
            // avoid some images do not have a size.
            point.x = 0
            point.y = 0
        }
        return point
    }

    private fun dismissProgressDialog() {
        mProgress.visibility = View.GONE
        val activity = thisActivity
        if (activity != null && activity.mProgressBar != null) {
            activity.mProgressBar!!.visibility = View.GONE
        }
    }

    private val thisActivity: BoxingViewActivity?
        get() {
            val activity = activity
            if (activity is BoxingViewActivity) {
                return activity
            }
            return null
        }

    override fun onDestroyView() {
        super.onDestroyView()
        mAttacher.cleanup()
    }

    private class BoxingCallback internal constructor(fragment: BoxingRawImageFragment) : IBoxingCallback {
        private val mWr: WeakReference<BoxingRawImageFragment> = WeakReference(fragment)

        override fun onSuccess() {
            if (mWr.get()?.mImageView == null) {
                return
            }
            mWr.get()?.dismissProgressDialog()
            val drawable = mWr.get()?.mImageView!!.drawable
            val attacher = mWr.get()?.mAttacher
            if (attacher != null) {
                if (drawable.intrinsicHeight > drawable.intrinsicWidth shl 2) {
                    // handle the super height image.
                    var scale = drawable.intrinsicHeight / drawable.intrinsicWidth
                    scale = Math.min(MAX_SCALE, scale)
                    attacher.maximumScale = scale.toFloat()
                    attacher.setScale(scale.toFloat(), true)
                }
                attacher.update()
            }
            val activity = mWr.get()?.thisActivity
            if (activity != null && activity.mGallery != null) {
                activity.mGallery!!.visibility = View.VISIBLE
            }
        }

        override fun onFail(t: Throwable) {
            if (mWr.get() == null) {
                return
            }
            t.message?.let { BoxingLog.d(it) }
            mWr.get()?.dismissProgressDialog()
            mWr.get()?.mImageView!!.setImageResource(R.drawable.ic_boxing_broken_image)
            mWr.get()?.mAttacher!!.update()
        }
    }

    companion object {
        private val BUNDLE_IMAGE = "com.bilibili.boxing_impl.ui.BoxingRawImageFragment.image"
        private val MAX_SCALE = 15
        private val MAX_IMAGE1 = 1024 * 1024L
        private val MAX_IMAGE2 = 4 * MAX_IMAGE1

        fun newInstance(image: ImageMedia): BoxingRawImageFragment {
            val fragment = BoxingRawImageFragment()
            val args = Bundle()
            args.putParcelable(BUNDLE_IMAGE, image)
            fragment.arguments = args
            return fragment
        }
    }
}
