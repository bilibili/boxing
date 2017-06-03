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

package com.bilibili.boxing

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.view.View
import com.bilibili.boxing.model.BoxingBuilderConfig
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.AlbumEntity
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.presenter.PickerContract
import com.bilibili.boxing.utils.CameraPickerHelper
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

/**
 * A abstract class which implements [PickerContract.View] for custom media view.
 * only one methods need to override [.startLoading], but there is more function to achieve by
 * checking every method can override.

 * @author ChenSL
 */
abstract class AbsBoxingViewFragment : Fragment(), PickerContract.View {

    lateinit private var mPresenter: PickerContract.Presenter
    lateinit private var mCameraPicker: CameraPickerHelper
    private var mOnFinishListener: Boxing.OnBoxingFinishListener? = null

    /**
     * start loading when the permission request is completed.
     * call [.loadMedias] or [.loadMedias], call [.loadAlbum] if albums needed.
     */
    abstract fun startLoading()

    /**
     * called when request [Manifest.permission.WRITE_EXTERNAL_STORAGE] and [Manifest.permission.CAMERA] permission error.

     * @param e a IllegalArgumentException, IllegalStateException or SecurityException will be throw
     */
    open fun onRequestPermissionError(permissions: Array<String>, e: Exception) {}

    /**
     * called when request [Manifest.permission.WRITE_EXTERNAL_STORAGE] and [Manifest.permission.CAMERA] permission successfully.
     */
    open fun onRequestPermissionSuc(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {}

    /**
     * get the result of using camera to take a photo.

     * @param media [BaseMedia]
     */
    open fun onCameraFinish(media: BaseMedia) {}

    /**
     * called when camera start error
     */
    open fun onCameraError() {}

    /**
     * must override when care about the input medias, which means you call [.setSelectedBundle] first.
     * this method is called in [Fragment.onCreate], so override this rather than [Fragment.onCreate].

     * @param bundle         If the fragment is being re-created from
     * *                       a previous saved state, this is the state.
     * *
     * @param selectedMedias the input medias, the parameter of [.setSelectedBundle].
     */
    open fun onCreateWithSelectedMedias(savedInstanceState: Bundle?, selectedMedias: MutableList<BaseMedia>?) {}

    /**
     * override this method to handle the medias.
     * make sure [.loadMedias] ()} being called first.

     * @param medias the results of medias
     */
    override fun showMedia(medias: List<BaseMedia>?, allCount: Int) {}

    /**
     * override this method to handle the album.
     * make sure [.loadAlbum] being called first.

     * @param albums the results of albums
     */
    override fun showAlbum(albums: List<AlbumEntity>?) {}

    /**
     * to clear all medias the first time(the page number is 0). do some clean work.
     */
    override fun clearMedia() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        val config: BoxingConfig
        if (savedInstanceState != null) {
            config = savedInstanceState.getParcelable<BoxingConfig>(Boxing.EXTRA_CONFIG)
        } else {
            config = BoxingManager.instance.boxingConfig
        }
        setPickerConfig(config)
        onCreateWithSelectedMedias(savedInstanceState, parseSelectedMedias(savedInstanceState, arguments))
        super.onCreate(savedInstanceState)

        initCameraPhotoPicker(savedInstanceState)
    }

    private fun parseSelectedMedias(savedInstanceState: Bundle?, argument: Bundle?): ArrayList<BaseMedia>? {
        var selectedMedias: ArrayList<BaseMedia>? = null
        if (savedInstanceState != null) {
            selectedMedias = savedInstanceState.getParcelableArrayList<BaseMedia>(Boxing.EXTRA_SELECTED_MEDIA)
        } else if (argument != null) {
            selectedMedias = argument.getParcelableArrayList<BaseMedia>(Boxing.EXTRA_SELECTED_MEDIA)
        }
        return selectedMedias
    }

    private fun initCameraPhotoPicker(savedInstanceState: Bundle?) {
        val config = BoxingManager.instance.boxingConfig
        if (!config.isNeedCamera) {
            return
        }
        mCameraPicker = CameraPickerHelper(savedInstanceState)
        mCameraPicker.setPickCallback(CameraListener(this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissionAndLoad()
    }

    private fun checkPermissionAndLoad() {
        try {
            if (!BoxingBuilderConfig.TESTING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(activity, STORAGE_PERMISSIONS[0]) != PERMISSION_GRANTED) {
                requestPermissions(STORAGE_PERMISSIONS, REQUEST_CODE_PERMISSION)
            } else {
                startLoading()
            }
        } catch (e: IllegalArgumentException) {
            onRequestPermissionError(STORAGE_PERMISSIONS, e)
        } catch (e: IllegalStateException) {
            onRequestPermissionError(STORAGE_PERMISSIONS, e)
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (REQUEST_CODE_PERMISSION == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionSuc(requestCode, permissions, grantResults)
            } else {
                onRequestPermissionError(permissions, SecurityException("request " + permissions[0] + " error."))
            }
        }
    }

    /**
     * called when you have input medias, then call [.onCreateWithSelectedMedias] to get the input medias.

     * @param selectedMedias input medias
     * *
     * @return [AbsBoxingViewFragment]
     */
    fun setSelectedBundle(selectedMedias: ArrayList<BaseMedia>?): AbsBoxingViewFragment {
        val bundle = Bundle()
        if (selectedMedias != null && !selectedMedias.isEmpty()) {
            bundle.putParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA, selectedMedias)
        }
        arguments = bundle
        return this
    }

    override fun setPresenter(presenter: PickerContract.Presenter) {
        this.mPresenter = presenter
    }

    /**
     * get the [ContentResolver]
     */
    override val appCr: ContentResolver
        get() = activity.applicationContext.contentResolver

    /**
     * if [AbsBoxingViewFragment] is not working with [AbsBoxingActivity], it needs a listener to call
     * when the jobs done.

     * @param onFinishListener [Boxing.OnBoxingFinishListener]
     */
    fun setOnFinishListener(onFinishListener: Boxing.OnBoxingFinishListener) {
        mOnFinishListener = onFinishListener
    }

    /**
     * called the job is done.Click the ok button, take a photo from camera, crop a photo.
     * most of the time, you do not have to override.

     * @param medias the list of selection
     */
    override fun onFinish(medias: List<BaseMedia>) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(Boxing.EXTRA_RESULT, medias as ArrayList<BaseMedia>)
        if (mOnFinishListener != null) {
            mOnFinishListener!!.onBoxingFinish(intent, medias)
        }

    }

    /**
     * need crop or not

     * @return true, need it.
     */
    fun hasCropBehavior(): Boolean {
        val config = BoxingManager.instance.boxingConfig
        return config.isSingleImageMode && config.cropOption != null
    }

    /**
     * to start the crop behavior, call it when [.hasCropBehavior] return true.

     * @param media       the media to be cropped.
     * *
     * @param requestCode The integer request code originally supplied to
     * *                    startActivityForResult(), allowing you to identify who this
     * *                    result came from.
     */
    override fun startCrop(media: BaseMedia, requestCode: Int) {
        val cropConfig = BoxingManager.instance.boxingConfig.cropOption
        BoxingCrop.instance.onStartCrop(activity, this, cropConfig!!, media.path, requestCode)
    }

    /**
     * set or update the config.most of the time, you do not have to call it.

     * @param config [BoxingConfig]
     */
    override fun setPickerConfig(config: BoxingConfig) {
        BoxingManager.instance.boxingConfig = config
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CameraPickerHelper.REQ_CODE_CAMERA) {
            onCameraActivityResult(requestCode, resultCode)
        }
        if (hasCropBehavior()) {
            onCropActivityResult(requestCode, resultCode, data!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mCameraPicker.onSaveInstanceState(outState)
        outState.putParcelable(Boxing.EXTRA_CONFIG, BoxingManager.instance.boxingConfig)
    }

    /**
     * in [BoxingConfig.Mode.MULTI_IMG], call this in [Fragment.onSaveInstanceState].

     * @param outState Bundle in which to place your saved state.
     * *
     * @param selected the selected medias.
     */
    fun onSaveMedias(outState: Bundle, selected: ArrayList<BaseMedia>?) {
        if (selected != null && !selected.isEmpty()) {
            outState.putParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA, selected)
        }
    }

    /**
     * call this to clear resource.
     */
    override fun onDestroy() {
        super.onDestroy()
        mPresenter.destroy()
        mCameraPicker.release()
    }

    /**
     * in [BoxingConfig.Mode.MULTI_IMG], call this to pick the selected medias in all medias.
     */
    fun checkSelectedMedia(allMedias: List<BaseMedia>, selectedMedias: List<BaseMedia>) {
        mPresenter.checkSelectedMedia(allMedias, selectedMedias)
    }

    /**
     * load first page of medias.
     * use [.showMedia] to get the result.
     */
    fun loadMedias() {
        mPresenter.loadMedias(0, AlbumEntity.DEFAULT_NAME)
    }

    /**
     * load the medias for the specify page and album id.
     * use [.showMedia] to get the result.

     * @param page    page numbers.
     * *
     * @param albumId the album id is [AlbumEntity.mBucketId].
     */
    fun loadMedias(page: Int, albumId: String) {
        mPresenter.loadMedias(page, albumId)
    }

    /**
     * extra call to load albums in database, use [.showAlbum] to get result.
     * In [BoxingConfig.Mode.VIDEO] it is not necessary.
     */
    fun loadAlbum() {
        if (!BoxingManager.instance.boxingConfig.isVideoMode) {
            mPresenter.loadAlbums()
        }
    }

    fun hasNextPage(): Boolean {
        return mPresenter.hasNextPage()
    }

    fun canLoadNextPage(): Boolean {
        return mPresenter.canLoadNextPage()
    }

    fun onLoadNextPage() {
        mPresenter.onLoadNextPage()
    }

    /**
     * get the max count set before
     */
    val maxCount: Int
        get() {
            val config = BoxingManager.instance.boxingConfig
            return config.maxCount
        }

    /**
     * successfully get result from camera in [.onActivityResult].
     * call this after other operations.
     */
    open fun onCameraActivityResult(requestCode: Int, resultCode: Int) {
        mCameraPicker.onActivityResult(requestCode, resultCode)
    }

    /**
     * successfully get result from crop in [.onActivityResult]
     */
    fun onCropActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val output = BoxingCrop.instance.onCropFinish(resultCode, data)
        val medias = ArrayList<BaseMedia>(1)
        val media = ImageMedia(System.currentTimeMillis().toString(), output!!.path)
        medias.add(media)
        onFinish(medias)
    }

    /**
     * start camera to take a photo.

     * @param activity      the caller activity.
     * *
     * @param fragment      the caller fragment, may be null.
     * *
     * @param subFolderPath the folder name in "DCIM/bili/boxing/"
     */
    fun startCamera(activity: Activity, fragment: Fragment, subFolderPath: String?) {
        try {
            if (!BoxingBuilderConfig.TESTING && ContextCompat.checkSelfPermission(getActivity(), CAMERA_PERMISSIONS[0]) != PERMISSION_GRANTED) {
                requestPermissions(CAMERA_PERMISSIONS, REQUEST_CODE_PERMISSION)
            } else {
                if (!BoxingManager.instance.boxingConfig.isVideoMode) {
                    mCameraPicker.startCamera(activity, fragment, subFolderPath)
                }
            }
        } catch (e: IllegalArgumentException) {
            onRequestPermissionError(CAMERA_PERMISSIONS, e)
        } catch (e: IllegalStateException) {
            onRequestPermissionError(CAMERA_PERMISSIONS, e)
        }

    }

    private class CameraListener internal constructor(fragment: AbsBoxingViewFragment) : CameraPickerHelper.Callback {
        private val mWr: WeakReference<AbsBoxingViewFragment> = WeakReference(fragment)

        override fun onFinish(helper: CameraPickerHelper) {
            val fragment = mWr.get() ?: return
            val file = File(helper.sourceFilePath)

            if (!file.exists()) {
                onError(helper)
                return
            }
            val cameraMedia = ImageMedia(file)
            cameraMedia.saveMediaStore(fragment.appCr)
            fragment.onCameraFinish(cameraMedia)
        }

        override fun onError(helper: CameraPickerHelper) {
            val fragment = mWr.get() ?: return
            fragment.onCameraError()
        }

    }

    companion object {
        val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private val REQUEST_CODE_PERMISSION = 233
    }
}
