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

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.presenter.PickerContract
import com.bilibili.boxing.presenter.PickerPresenter
import java.util.*

/**
 * A abstract class to connect [com.bilibili.boxing.presenter.PickerContract.View] and [com.bilibili.boxing.presenter.PickerContract.Presenter].
 * one job has to be done. override [.onCreateBoxingView] to create a subclass for [AbsBoxingViewFragment].

 * @author ChenSL
 */
abstract class AbsBoxingActivity : AppCompatActivity(), Boxing.OnBoxingFinishListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = onCreateBoxingView(getSelectedMedias(intent))
        val pickerConfig = BoxingManager.instance.boxingConfig
        view.setPresenter(PickerPresenter(view))
        view.setPickerConfig(pickerConfig)
        Boxing.get().setupFragment(view, this)
    }

    private fun getSelectedMedias(intent: Intent): ArrayList<BaseMedia>? {
        return intent.getParcelableArrayListExtra<BaseMedia>(Boxing.EXTRA_SELECTED_MEDIA)
    }

    val boxingConfig: BoxingConfig?
        get() = BoxingManager.instance.boxingConfig

    /**
     * create a [PickerContract.View] attaching to
     * [PickerContract.Presenter],call in [.onCreate]
     */
    abstract fun onCreateBoxingView(medias: ArrayList<BaseMedia>?): AbsBoxingViewFragment

}
