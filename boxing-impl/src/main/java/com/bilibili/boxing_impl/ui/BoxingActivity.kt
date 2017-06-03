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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.bilibili.boxing.AbsBoxingActivity
import com.bilibili.boxing.AbsBoxingViewFragment
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing_impl.R
import java.util.*

/**
 * Default UI Activity for simplest usage.
 * A simple subclass of [AbsBoxingActivity]. Holding a [AbsBoxingViewFragment] to display medias.
 */
open class BoxingActivity : AbsBoxingActivity() {
    lateinit private var mPickerFragment: BoxingViewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boxing)
        createToolbar()
        setTitleTxt(boxingConfig)
    }

    override fun onCreateBoxingView(medias: ArrayList<BaseMedia>?): AbsBoxingViewFragment {
        val temp = supportFragmentManager.findFragmentByTag(BoxingViewFragment.TAG) as BoxingViewFragment?
        if (temp != null) {
            mPickerFragment = temp
        } else {
            mPickerFragment = BoxingViewFragment.newInstance().setSelectedBundle(medias) as BoxingViewFragment
        }
        supportFragmentManager.beginTransaction().replace(R.id.content_layout, mPickerFragment, BoxingViewFragment.TAG).commit()
        return mPickerFragment
    }

    private fun createToolbar() {
        val bar = findViewById(R.id.nav_top_bar) as Toolbar
        setSupportActionBar(bar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        bar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setTitleTxt(config: BoxingConfig?) {
        val titleTxt = findViewById(R.id.pick_album_txt) as TextView
        if (config?.mode === BoxingConfig.Mode.VIDEO) {
            titleTxt.setText(R.string.boxing_video_title)
            titleTxt.setCompoundDrawables(null, null, null, null)
            return
        }
        mPickerFragment.setTitleTxt(titleTxt)
    }

    override fun onBoxingFinish(intent: Intent, medias: List<BaseMedia>?) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
