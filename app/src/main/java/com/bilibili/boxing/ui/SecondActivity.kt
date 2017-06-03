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

package com.bilibili.boxing.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bilibili.boxing.AbsBoxingActivity
import com.bilibili.boxing.AbsBoxingViewFragment
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.demo.R
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.presenter.PickerPresenter
import com.bilibili.boxing_impl.ui.BoxingBottomSheetFragment
import com.bilibili.boxing_impl.ui.BoxingViewActivity
import java.util.*

/**
 * to show use [AbsBoxingViewFragment] without [AbsBoxingActivity].
 * use [Boxing.setupFragment] to set a fragment.

 * @author ChenSL
 */
class SecondActivity : AppCompatActivity(), View.OnClickListener {
    lateinit private var mInsideBottomSheet: FrameLayout
    lateinit private var mResultImg: ImageView
    private var mMedia: BaseMedia? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        createToolbar()
        findViewById(R.id.inside_bs_btn).setOnClickListener(this)
        mResultImg = findViewById(R.id.media_result) as ImageView
        mResultImg.setOnClickListener(this)
        mInsideBottomSheet = findViewById(R.id.content_layout) as FrameLayout
        var fragment: BoxingBottomSheetFragment? = supportFragmentManager.findFragmentByTag(BoxingBottomSheetFragment.TAG) as BoxingBottomSheetFragment
        if (fragment == null) {
            fragment = BoxingBottomSheetFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_layout, fragment, BoxingBottomSheetFragment.TAG).commit()

            val singleImgConfig = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
                    .withMediaPlaceHolderRes(R.drawable.ic_boxing_default_image).withAlbumPlaceHolderRes(R.drawable.ic_boxing_default_image)
            Boxing.of(singleImgConfig).setupFragment(fragment, object : Boxing.OnBoxingFinishListener {

                override fun onBoxingFinish(intent: Intent, medias: List<BaseMedia>?) {
                    val behavior = BottomSheetBehavior.from(mInsideBottomSheet)
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    if (medias != null && medias.isNotEmpty()) {
                        mMedia = medias[0]
                        val media = mMedia
                        val path = media?.path
                        BoxingMediaLoader.instance.displayRaw(mResultImg, path!!, 1080, 720, null)
                    }
                }
            })
        } else {
            fragment.setPresenter(PickerPresenter(fragment))
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.inside_bs_btn -> showFragment()
            R.id.media_result -> {
                if (mMedia == null) {
                    return
                }
                val medias = ArrayList<BaseMedia>(1)
                medias.add(mMedia!!)
                Boxing.get().withIntent(this, BoxingViewActivity::class.java, medias).start(this, BoxingConfig.ViewMode.PREVIEW)
            }
        }
    }

    private fun showFragment() {
        val behavior = BottomSheetBehavior.from(mInsideBottomSheet)
        if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        } else {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        }
    }


    private fun createToolbar() {
        val bar = findViewById(R.id.nav_top_bar) as Toolbar
        setSupportActionBar(bar)
        supportActionBar!!.setTitle(R.string.second_demo_title)
        bar.setNavigationOnClickListener { onBackPressed() }
    }

}
