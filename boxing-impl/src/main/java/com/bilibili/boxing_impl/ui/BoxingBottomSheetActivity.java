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

package com.bilibili.boxing_impl.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bilibili.boxing.AbsBoxingActivity;
import com.bilibili.boxing.AbsBoxingViewFragment;
import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing_impl.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Default UI Activity for simplest usage, containing layout achieve {@link BottomSheetBehavior}.
 * Only support SINGLE_IMG and VIDEO Mode.
 *
 * @author ChenSL
 */
public class BoxingBottomSheetActivity extends AbsBoxingActivity implements View.OnClickListener {
    private BottomSheetBehavior<FrameLayout> mBehavior;
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boxing_bottom_sheet);
        createToolbar();

        FrameLayout bottomSheet = (FrameLayout) findViewById(R.id.content_layout);
        mBehavior = BottomSheetBehavior.from(bottomSheet);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        mImage = (ImageView) findViewById(R.id.media_result);
        mImage.setOnClickListener(this);
    }

    @NonNull
    @Override
    public AbsBoxingViewFragment onCreateBoxingView(ArrayList<BaseMedia> medias) {
        BoxingBottomSheetFragment fragment = (BoxingBottomSheetFragment) getSupportFragmentManager().findFragmentByTag(BoxingBottomSheetFragment.TAG);
        if (fragment == null) {
            fragment = BoxingBottomSheetFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_layout, fragment, BoxingBottomSheetFragment.TAG).commit();
        }
        return fragment;
    }

    private void createToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.nav_top_bar);
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.boxing_default_album);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private boolean hideBottomSheet() {
        if (mBehavior != null && mBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return true;
        }
        return false;
    }

    private boolean collapseBottomSheet() {
        if (mBehavior != null && mBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        }
        return false;
    }

    private void toggleBottomSheet() {
        if (mBehavior == null) {
            return;
        }
        if (mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void onBackPressed() {
        if (hideBottomSheet()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.media_result) {
            toggleBottomSheet();
        }
    }


    @Override
    public void onBoxingFinish(Intent intent, @Nullable List<BaseMedia> medias) {
        if (mImage != null && medias != null && !medias.isEmpty()) {
            ImageMedia imageMedia = (ImageMedia) medias.get(0);
            BoxingMediaLoader.getInstance().displayRaw(mImage, imageMedia.getPath(), 1080, 720, null);
        }
        hideBottomSheet();
    }
}
