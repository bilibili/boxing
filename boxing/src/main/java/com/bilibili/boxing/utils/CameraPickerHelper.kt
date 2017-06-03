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

package com.bilibili.boxing.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import com.bilibili.boxing.AbsBoxingViewFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

/**
 * A helper to start camera.<br></br>
 * used by [AbsBoxingViewFragment]

 * @author ChenSL
 */
class CameraPickerHelper(savedInstance: Bundle?) {

    var sourceFilePath: String = ""
        private set
    private var mOutputFile: File? = null
    private var mCallback: Callback? = null

    interface Callback {
        fun onFinish(helper: CameraPickerHelper)

        fun onError(helper: CameraPickerHelper)
    }

    init {
        if (savedInstance != null) {
            val state = savedInstance.getParcelable<SavedState>(STATE_SAVED_KEY)
            if (state != null) {
                mOutputFile = state.mOutputFile
                sourceFilePath = state.mSourceFilePath
            }
        }
    }

    fun setPickCallback(callback: Callback) {
        this.mCallback = callback
    }

    fun onSaveInstanceState(out: Bundle) {
        val state = SavedState()
        state.mOutputFile = mOutputFile
        state.mSourceFilePath = sourceFilePath
        out.putParcelable(STATE_SAVED_KEY, state)
    }

    /**
     * start system camera to take a picture

     * @param activity      not null if fragment is null.
     * *
     * @param fragment      not null if activity is null.
     * *
     * @param subFolderPath a folder in external DCIM,must start with "/".
     */
    fun startCamera(activity: Activity, fragment: Fragment, subFolderPath: String?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !takePhotoSecure(activity, fragment, subFolderPath)) {
            val task = BoxingExecutor.instance.runWorker(Callable {
                try {
                    // try...try...try
                    val camera = Camera.open()
                    camera.release()
                } catch (e: Exception) {
                    BoxingLog.d("camera is not available.")
                    return@Callable false
                }

                true
            })
            try {
                if (task != null && task.get()) {
                    startCameraIntent(activity, fragment, subFolderPath, MediaStore.ACTION_IMAGE_CAPTURE, REQ_CODE_CAMERA)
                } else {
                    callbackError()
                }
            } catch (ignore: InterruptedException) {
                callbackError()
            } catch (ignore: ExecutionException) {
                callbackError()
            }

        }
    }

    private fun takePhotoSecure(activity: Activity, fragment: Fragment, subDir: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                startCameraIntent(activity, fragment, subDir, MediaStore.ACTION_IMAGE_CAPTURE, REQ_CODE_CAMERA)
                return true
            } catch (ignore: ActivityNotFoundException) {
                return false
            }

        }
        return false
    }

    private fun callbackFinish() {
        if (mCallback != null) {
            mCallback!!.onFinish(this@CameraPickerHelper)
        }
    }

    private fun callbackError() {
        if (mCallback != null) {
            mCallback!!.onError(this@CameraPickerHelper)
        }
    }

    @Throws(ActivityNotFoundException::class)
    private fun startActivityForResult(activity: Activity, fragment: Fragment?, intent: Intent, reqCodeCamera: Int) {
        if (fragment == null) {
            activity.startActivityForResult(intent, reqCodeCamera)
        } else {
            fragment.startActivityForResult(intent, reqCodeCamera)
        }
    }

    private fun startCameraIntent(activity: Activity, fragment: Fragment, subFolder: String?,
                                  action: String, requestCode: Int) {
        val cameraOutDir = BoxingFileHelper.getExternalDCIM(subFolder)
        try {
            if (BoxingFileHelper.createFile(cameraOutDir!!)) {
                mOutputFile = File(cameraOutDir, System.currentTimeMillis().toString() + ".jpg")
                sourceFilePath = mOutputFile!!.path
                val intent = Intent(action)
                val uri = getFileUri(activity.applicationContext, mOutputFile!!)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                try {
                    startActivityForResult(activity, fragment, intent, requestCode)
                } catch (ignore: ActivityNotFoundException) {
                    callbackError()
                }

            }
        } catch (e: ExecutionException) {
            BoxingLog.d("create file$cameraOutDir error.")
        } catch (e: InterruptedException) {
            BoxingLog.d("create file$cameraOutDir error.")
        }

    }

    private fun getFileUri(context: Context, file: File): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context,
                    context.applicationContext.packageName + ".file.provider", mOutputFile)
        } else {
            return Uri.fromFile(file)
        }
    }

    /**
     * deal with the system camera's shot.
     */
    fun onActivityResult(requestCode: Int, resultCode: Int): Boolean {
        if (requestCode != REQ_CODE_CAMERA) {
            return false
        }
        if (resultCode != Activity.RESULT_OK) {
            callbackError()
            return false
        }
        val task = BoxingExecutor.instance.runWorker(Callable { rotateImage(resultCode) })
        try {
            if (task != null && task.get()) {
                callbackFinish()
            } else {
                callbackError()
            }
        } catch (ignore: InterruptedException) {
            callbackError()
        } catch (ignore: ExecutionException) {
            callbackError()
        }

        return true
    }

    @Throws(IOException::class)
    private fun rotateSourceFile(file: File?): Boolean {
        if (file == null || !file.exists()) {
            return false
        }
        var outputStream: FileOutputStream? = null
        var bitmap: Bitmap? = null
        var outBitmap: Bitmap? = null
        try {
            val degree = BoxingExifHelper.getRotateDegree(file.absolutePath)
            if (degree == 0) {
                return true
            }
            val quality = if (file.length() >= MAX_CAMER_PHOTO_SIZE) 90 else 100
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            outBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap!!.width, bitmap.height, matrix, false)
            outputStream = FileOutputStream(file)
            outBitmap!!.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            return true
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    BoxingLog.d("IOException when output stream closing!")
                }

            }
            if (bitmap != null) {
                bitmap.recycle()
            }
            if (outBitmap != null) {
                outBitmap.recycle()
            }
        }
    }

    @Throws(IOException::class)
    private fun rotateImage(resultCode: Int): Boolean {
        return resultCode == Activity.RESULT_OK && rotateSourceFile(mOutputFile)
    }

    fun release() {
        mOutputFile = null
    }

    private class SavedState : Parcelable {
        var mOutputFile: File? = null
        var mSourceFilePath: String = ""

        internal constructor() {}

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeSerializable(this.mOutputFile)
            dest.writeString(this.mSourceFilePath)
        }

        internal constructor(`in`: Parcel) {
            this.mOutputFile = `in`.readSerializable() as File
            this.mSourceFilePath = `in`.readString()
        }

        companion object {

            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private val MAX_CAMER_PHOTO_SIZE = 4 * 1024 * 1024
        val REQ_CODE_CAMERA = 0x2001
        private val STATE_SAVED_KEY = "com.bilibili.boxing.utils.CameraPickerHelper.saved_state"
    }

}
