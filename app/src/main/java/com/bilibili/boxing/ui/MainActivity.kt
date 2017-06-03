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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View

import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.demo.R
import com.bilibili.boxing.impl.BoxingFrescoLoader
import com.bilibili.boxing.impl.BoxingGlideLoader
import com.bilibili.boxing.impl.BoxingPicassoLoader
import com.bilibili.boxing.loader.IBoxingMediaLoader
import com.bilibili.boxing.model.config.BoxingConfig

/**
 * @author ChenSL
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createToolbar()
        findViewById(R.id.first_btn).setOnClickListener(this)
        findViewById(R.id.second_btn).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.first_btn -> {
                val intent1 = Intent(this, FirstActivity::class.java)
                startActivity(intent1)
            }
            R.id.second_btn -> {
                val singleImgConfig = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
                Boxing.of(singleImgConfig).withIntent(this, SecondActivity::class.java).start(this)
            }
            else -> {
            }
        }
    }

    private fun createToolbar() {
        val bar = findViewById(R.id.nav_top_bar) as Toolbar
        setSupportActionBar(bar)
        supportActionBar?.setTitle(R.string.boxing_app_name)
        bar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val loader: IBoxingMediaLoader
        when (id) {
            R.id.menu_fresco -> loader = BoxingFrescoLoader(this)
            R.id.menu_glide -> loader = BoxingGlideLoader()
            R.id.menu_picasso -> loader = BoxingPicassoLoader()
            else -> loader = BoxingFrescoLoader(this)
        }
        BoxingMediaLoader.instance.init(loader)
        return super.onOptionsItemSelected(item)
    }
}
