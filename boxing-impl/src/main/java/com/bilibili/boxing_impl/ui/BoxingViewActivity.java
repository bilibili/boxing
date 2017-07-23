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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bilibili.boxing.AbsBoxingViewActivity;
import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing.model.task.IMediaTask;
import com.bilibili.boxing_impl.BoxingResHelper;
import com.bilibili.boxing_impl.R;
import com.bilibili.boxing_impl.view.HackyViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity to show raw image by holding {@link BoxingViewFragment}.
 *
 * @author ChenSL
 */
public class BoxingViewActivity extends AbsBoxingViewActivity {
    public static final String EXTRA_TYPE_BACK = "com.bilibili.boxing_impl.ui.BoxingViewActivity.type_back";

    HackyViewPager mGallery;
    ProgressBar mProgressBar;

    private boolean mNeedEdit;
    private boolean mNeedLoading;
    private boolean mFinishLoading;
    private boolean mNeedAllCount = true;
    private int mCurrentPage;
    private int mTotalCount;
    private int mStartPos;
    private int mPos;
    private int mMaxCount;

    private String mAlbumId;
    private Toolbar mToolbar;
    private ImagesAdapter mAdapter;
    private ImageMedia mCurrentImageItem;
    private Button mOkBtn;
    private ArrayList<BaseMedia> mImages;
    private ArrayList<BaseMedia> mSelectedImages;
    private MenuItem mSelectedMenuItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boxing_view);
        createToolbar();
        initData();
        initView();
        startLoading();
    }

    private void createToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.nav_top_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initData() {
        mSelectedImages = getSelectedImages();
        mAlbumId = getAlbumId();
        mStartPos = getStartPos();
        mNeedLoading = BoxingManager.getInstance().getBoxingConfig().isNeedLoading();
        mNeedEdit = BoxingManager.getInstance().getBoxingConfig().isNeedEdit();
        mMaxCount = getMaxCount();
        mImages = new ArrayList<>();
        if (!mNeedLoading && mSelectedImages != null) {
            mImages.addAll(mSelectedImages);
        }
    }

    private void initView() {
        mAdapter = new ImagesAdapter(getSupportFragmentManager());
        mOkBtn = (Button) findViewById(R.id.image_items_ok);
        mGallery = (HackyViewPager) findViewById(R.id.pager);
        mProgressBar = (ProgressBar) findViewById(R.id.loading);
        mGallery.setAdapter(mAdapter);
        mGallery.addOnPageChangeListener(new OnPagerChangeListener());
        if (!mNeedEdit) {
            View chooseLayout = findViewById(R.id.item_choose_layout);
            chooseLayout.setVisibility(View.GONE);
        } else {
            setOkTextNumber();
            mOkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishByBackPressed(false);
                }
            });
        }
    }

    private void setOkTextNumber() {
        if (mNeedEdit) {
            int selectedSize = mSelectedImages.size();
            int size = Math.max(mSelectedImages.size(), mMaxCount);
            mOkBtn.setText(getString(R.string.boxing_image_preview_ok_fmt, String.valueOf(selectedSize)
                    , String.valueOf(size)));
            mOkBtn.setEnabled(selectedSize > 0);
        }
    }

    private void finishByBackPressed(boolean value) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA, mSelectedImages);
        intent.putExtra(EXTRA_TYPE_BACK, value);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mNeedEdit) {
            getMenuInflater().inflate(R.menu.activity_boxing_image_viewer, menu);
            mSelectedMenuItem = menu.findItem(R.id.menu_image_item_selected);
            if (mCurrentImageItem != null) {
                setMenuIcon(mCurrentImageItem.isSelected());
            } else {
                setMenuIcon(false);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_image_item_selected) {
            if (mCurrentImageItem == null) {
                return false;
            }
            if (mSelectedImages.size() >= mMaxCount && !mCurrentImageItem.isSelected()) {
                String warning = getString(R.string.boxing_max_image_over_fmt, mMaxCount);
                Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
                return true;
            }
            if (mCurrentImageItem.isSelected()) {
                cancelImage();
            } else {
                if (!mSelectedImages.contains(mCurrentImageItem)) {
                    if (mCurrentImageItem.isGifOverSize()) {
                        Toast.makeText(getApplicationContext(), R.string.boxing_gif_too_big, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    mCurrentImageItem.setSelected(true);
                    mSelectedImages.add(mCurrentImageItem);
                }
            }
            setOkTextNumber();
            setMenuIcon(mCurrentImageItem.isSelected());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cancelImage() {
        if (mSelectedImages.contains(mCurrentImageItem)) {
            mSelectedImages.remove(mCurrentImageItem);
        }
        mCurrentImageItem.setSelected(false);
    }


    private void setMenuIcon(boolean isSelected) {
        if (mNeedEdit) {
            mSelectedMenuItem.setIcon(isSelected ? BoxingResHelper.getMediaCheckedRes(): BoxingResHelper.getMediaUncheckedRes());
        }
    }

    @Override
    public void startLoading() {
        if (!mNeedLoading) {
            mCurrentImageItem = (ImageMedia) mSelectedImages.get(mStartPos);
            mToolbar.setTitle(getString(R.string.boxing_image_preview_title_fmt, String.valueOf(mStartPos + 1)
                    , String.valueOf(mSelectedImages.size())));
            mProgressBar.setVisibility(View.GONE);
            mGallery.setVisibility(View.VISIBLE);
            mAdapter.setMedias(mImages);
            if (mStartPos > 0 && mStartPos < mSelectedImages.size()) {
                mGallery.setCurrentItem(mStartPos, false);
            }
        } else {
            loadMedia(mAlbumId, mStartPos, mCurrentPage);
            mAdapter.setMedias(mImages);
        }
    }

    private void loadMedia(String albumId, int startPos, int page) {
        this.mPos = startPos;
        loadMedias(page, albumId);
    }

    @Override
    public void showMedia(@Nullable List<BaseMedia> medias, int totalCount) {
        if (medias == null || totalCount <= 0) {
            return;
        }
        mImages.addAll(medias);
        mAdapter.notifyDataSetChanged();
        checkSelectedMedia(mImages, mSelectedImages);
        setupGallery();

        if (mToolbar != null && mNeedAllCount) {
            mToolbar.setTitle(getString(R.string.boxing_image_preview_title_fmt,
                    String.valueOf(++mPos), String.valueOf(totalCount)));
            mNeedAllCount = false;
        }
        loadOtherPagesInAlbum(totalCount);
    }

    private void setupGallery() {
        int startPos = mStartPos;
        if (mGallery == null || startPos < 0) {
            return;
        }
        if (startPos < mImages.size() && !mFinishLoading) {
            mGallery.setCurrentItem(mStartPos, false);
            mCurrentImageItem = (ImageMedia) mImages.get(startPos);
            mProgressBar.setVisibility(View.GONE);
            mGallery.setVisibility(View.VISIBLE);
            mFinishLoading = true;
            invalidateOptionsMenu();
        } else if (startPos >= mImages.size()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mGallery.setVisibility(View.GONE);
        }
    }

    private void loadOtherPagesInAlbum(int totalCount) {
        mTotalCount = totalCount;
        if (mCurrentPage <= (mTotalCount / IMediaTask.PAGE_LIMIT)) {
            mCurrentPage++;
            loadMedia(mAlbumId, mStartPos, mCurrentPage);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mSelectedImages != null) {
            outState.putParcelableArrayList(Boxing.EXTRA_SELECTED_MEDIA, mSelectedImages);
        }
        outState.putString(Boxing.EXTRA_ALBUM_ID, mAlbumId);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {
        finishByBackPressed(true);
    }

    private class ImagesAdapter extends FragmentStatePagerAdapter {
        private ArrayList<BaseMedia> mMedias;

        ImagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return BoxingRawImageFragment.newInstance((ImageMedia) mMedias.get(i));
        }

        @Override
        public int getCount() {
            return mMedias == null ? 0 : mMedias.size();
        }

        public void setMedias(ArrayList<BaseMedia> medias) {
            this.mMedias = medias;
            notifyDataSetChanged();
        }
    }

    private class OnPagerChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            if (mToolbar != null && position < mImages.size()) {
                mToolbar.setTitle(getString(R.string.boxing_image_preview_title_fmt, String.valueOf(position + 1)
                        , mNeedLoading ? String.valueOf(mTotalCount) : String.valueOf(mImages.size())));
                mCurrentImageItem = (ImageMedia) mImages.get(position);
                invalidateOptionsMenu();
            }
        }
    }
}
