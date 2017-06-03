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

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bilibili.boxing.AbsBoxingActivity
import com.bilibili.boxing.AbsBoxingViewFragment
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing_impl.R
import java.util.*

/**
 * Default UI Activity for simplest usage, containing layout achieve [BottomSheetBehavior].
 * Only support SINGLE_IMG and VIDEO Mode.

 * @author ChenSL
 */
class BoxingBottomSheetActivity : AbsBoxingActivity(), View.OnClickListener {
    private var mBehavior: BottomSheetBehavior<FrameLayout>? = null
    private var mImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boxing_bottom_sheet)
        createToolbar()

        val bottomSheet = findViewById(R.id.content_layout) as FrameLayout
        mBehavior = BottomSheetBehavior.from(bottomSheet)
        mBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED

        mImage = findViewById(R.id.media_result) as ImageView
        mImage!!.setOnClickListener(this)
    }

    override fun onCreateBoxingView(medias: ArrayList<BaseMedia>?): AbsBoxingViewFragment {
        var fragment: BoxingBottomSheetFragment? = supportFragmentManager.findFragmentByTag(BoxingBottomSheetFragment.TAG) as BoxingBottomSheetFragment
        if (fragment == null) {
            fragment = BoxingBottomSheetFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_layout, fragment, BoxingBottomSheetFragment.TAG).commit()
        }
        return fragment
    }

    private fun createToolbar() {
        val bar = findViewById(R.id.nav_top_bar) as Toolbar
        setSupportActionBar(bar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.boxing_default_album)
        bar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun hideBottomSheet(): Boolean {
        if (mBehavior != null && mBehavior!!.state != BottomSheetBehavior.STATE_HIDDEN) {
            mBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
            return true
        }
        return false
    }

    private fun collapseBottomSheet(): Boolean {
        if (mBehavior != null && mBehavior!!.state != BottomSheetBehavior.STATE_COLLAPSED) {
            mBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }
        return false
    }

    private fun toggleBottomSheet() {
        if (mBehavior == null) {
            return
        }
        if (mBehavior!!.state == BottomSheetBehavior.STATE_HIDDEN) {
            mBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            mBehavior!!.setState(BottomSheetBehavior.STATE_HIDDEN)
        }
    }

    override fun onBackPressed() {
        if (hideBottomSheet()) {
            return
        }
        super.onBackPressed()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.media_result) {
            toggleBottomSheet()
        }
    }


    override fun onBoxingFinish(intent: Intent, medias: List<BaseMedia>?) {
        if (mImage != null && medias != null && !medias.isEmpty()) {
            val imageMedia = medias[0] as ImageMedia
            BoxingMediaLoader.instance.displayRaw(mImage!!, imageMedia.path, 1080, 720, null)
        }
        hideBottomSheet()
    }
}
