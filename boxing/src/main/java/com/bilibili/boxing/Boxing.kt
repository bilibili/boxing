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

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.Fragment
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.presenter.PickerPresenter

/**
 * An entry for [AbsBoxingActivity] and [AbsBoxingViewFragment].<br></br>
 * 1.call [.of] to pick a mode.<br></br>
 * 2.to use [AbsBoxingActivity] + [AbsBoxingViewFragment] combination,
 * call [.withIntent] to make a intent and [.start] to start a new Activity.<br></br>
 * to use [AbsBoxingViewFragment] only, just call [.setupFragment].<br></br>
 * 3 4.to get result from a new Activity, call [.getResult] in [Activity.onActivityResult].

 * @author ChenSL
 */
class Boxing private constructor(config: BoxingConfig) {

    /**
     * get the intent build by boxing after call [.withIntent].
     */
    val intent: Intent

    init {
        BoxingManager.instance.boxingConfig = config
        this.intent = Intent()
    }

    /**
     * [Intent.setClass] with input medias.
     */
    @JvmOverloads fun withIntent(context: Context, cls: Class<*>, selectedMedias: ArrayList<out BaseMedia>? = null): Boxing {
        intent.setClass(context, cls)
        if (selectedMedias != null && !selectedMedias.isEmpty()) {
            intent.putExtra(EXTRA_SELECTED_MEDIA, selectedMedias)
        }
        return this
    }

    /**
     * use to start image viewer.

     * @param medias selected medias.
     * *
     * @param pos    the start position.
     */
    fun withIntent(context: Context, cls: Class<*>, medias: ArrayList<out BaseMedia>, pos: Int): Boxing {
        withIntent(context, cls, medias, pos, "")
        return this
    }


    /**
     * use to start image viewer.

     * @param medias  selected medias.
     * *
     * @param pos     the start position.
     * *
     * @param albumId the specify album id.
     */
    fun withIntent(context: Context, cls: Class<*>, medias: ArrayList<out BaseMedia>?, pos: Int, albumId: String?): Boxing {
        intent.setClass(context, cls)
        if (medias != null && !medias.isEmpty()) {
            intent.putExtra(EXTRA_SELECTED_MEDIA, medias)
        }
        if (pos >= 0) {
            intent.putExtra(EXTRA_START_POS, pos)
        }
        if (albumId != null) {
            intent.putExtra(EXTRA_ALBUM_ID, albumId)
        }
        return this
    }


    /**
     * same as [Activity.startActivity]
     */
    fun start(activity: Activity) {
        activity.startActivity(intent)
    }

    /**
     * use to start raw image viewer.

     * @param viewMode [BoxingConfig.ViewMode]
     */
    fun start(activity: Activity, viewMode: BoxingConfig.ViewMode) {
        BoxingManager.instance.boxingConfig.withViewer(viewMode)
        activity.startActivity(intent)
    }

    /**
     * same as [Activity.startActivityForResult]
     */
    fun start(activity: Activity, requestCode: Int) {
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * same as [Fragment.startActivityForResult]
     */
    fun start(fragment: Fragment, requestCode: Int) {
        fragment.startActivityForResult(intent, requestCode)
    }

    /**
     * use to start raw image viewer.

     * @param viewMode [BoxingConfig.ViewMode]
     */
    fun start(fragment: Fragment, requestCode: Int, viewMode: BoxingConfig.ViewMode) {
        BoxingManager.instance.boxingConfig.withViewer(viewMode)
        fragment.startActivityForResult(intent, requestCode)
    }

    /**
     * same as [android.app.Fragment.startActivityForResult]
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun start(fragment: android.app.Fragment, requestCode: Int) {
        fragment.startActivityForResult(intent, requestCode)
    }

    /**
     * use to start raw image viewer.

     * @param viewMode [BoxingConfig.ViewMode]
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun start(fragment: android.app.Fragment, requestCode: Int, viewMode: BoxingConfig.ViewMode) {
        BoxingManager.instance.boxingConfig.withViewer(viewMode)
        fragment.startActivityForResult(intent, requestCode)
    }

    /**
     * set up a subclass of [AbsBoxingViewFragment] without a [AbsBoxingActivity].

     * @param fragment         subclass of [AbsBoxingViewFragment]
     * *
     * @param onFinishListener a listener fo media result
     */
    fun setupFragment(fragment: AbsBoxingViewFragment, onFinishListener: OnBoxingFinishListener) {
        fragment.setPresenter(PickerPresenter(fragment))
        fragment.setOnFinishListener(onFinishListener)
    }

    /**
     * work with a subclass of [AbsBoxingViewFragment] without a [AbsBoxingActivity].
     */
    interface OnBoxingFinishListener {

        /**
         * live with [com.bilibili.boxing.presenter.PickerContract.View.onFinish]

         * @param medias the selection of medias.
         */
        fun onBoxingFinish(intent: Intent, medias: List<BaseMedia>?)
    }

    companion object {
        val EXTRA_SELECTED_MEDIA = "com.bilibili.boxing.Boxing.selected_media"
        val EXTRA_ALBUM_ID = "com.bilibili.boxing.Boxing.album_id"

        internal val EXTRA_CONFIG = "com.bilibili.boxing.Boxing.config"
        internal val EXTRA_RESULT = "com.bilibili.boxing.Boxing.result"
        internal val EXTRA_START_POS = "com.bilibili.boxing.Boxing.start_pos"

        /**
         * get the media result.
         */
        fun getResult(data: Intent?): ArrayList<BaseMedia> {
            if (data != null) {
                return data.getParcelableArrayListExtra<BaseMedia>(EXTRA_RESULT)
            }
            return ArrayList()
        }

        /**
         * call [.of] first to specify the mode otherwise [BoxingConfig.Mode.MULTI_IMG] is used.<br></br>
         */
        fun get(): Boxing {
            var config = BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needGif()
            BoxingManager.instance.boxingConfig = config
            return Boxing(config)
        }

        /**
         * create a boxing entry.

         * @param config [BoxingConfig]
         */
        fun of(config: BoxingConfig): Boxing {
            return Boxing(config)
        }

        /**
         * create a boxing entry.

         * @param mode [BoxingConfig.Mode]
         */
        fun of(mode: BoxingConfig.Mode): Boxing {
            return Boxing(BoxingConfig(mode))
        }

        /**
         * create a boxing entry. use [BoxingConfig.Mode.MULTI_IMG].
         */
        fun of(): Boxing {
            val config = BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needGif()
            return Boxing(config)
        }
    }

}
