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

package com.bilibili.boxing_impl.view;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing.model.entity.impl.VideoMedia;
import com.bilibili.boxing_impl.BoxingResHelper;
import com.bilibili.boxing_impl.R;
import com.bilibili.boxing_impl.WindowManagerHelper;


/**
 * A media layout for {@link android.support.v7.widget.RecyclerView} item, including image and video <br/>
 *
 * @author ChenSL
 */
public class MediaItemLayout extends FrameLayout {
    private static final int BIG_IMG_SIZE = 5 * 1024 * 1024;

    private ImageView mCheckImg;
    private View mVideoLayout;
    private View mFontLayout;
    private ImageView mCoverImg;
    private ScreenType mScreenType;

    private enum ScreenType {
        SMALL(100), NORMAL(180), LARGE(320);
        int value;

        ScreenType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public MediaItemLayout(Context context) {
        this(context, null, 0);
    }

    public MediaItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_boxing_media_item, this, true);
        mCoverImg = (ImageView) view.findViewById(R.id.media_item);
        mCheckImg = (ImageView) view.findViewById(R.id.media_item_check);
        mVideoLayout = view.findViewById(R.id.video_layout);
        mFontLayout = view.findViewById(R.id.media_font_layout);
        mScreenType = getScreenType(context);
        setImageRect(context);
    }

    private void setImageRect(Context context) {
        int screenHeight = WindowManagerHelper.getScreenHeight(context);
        int screenWidth = WindowManagerHelper.getScreenWidth(context);
        int width = 100;
        if (screenHeight != 0 && screenWidth != 0) {
            width = (screenWidth - getResources().getDimensionPixelOffset(R.dimen.boxing_media_margin) * 4) / 3;
        }
        mCoverImg.getLayoutParams().width = width;
        mCoverImg.getLayoutParams().height = width;
        mFontLayout.getLayoutParams().width = width;
        mFontLayout.getLayoutParams().height = width;
    }

    private ScreenType getScreenType(Context context) {
        int type = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        ScreenType result;
        switch (type) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                result = ScreenType.SMALL;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                result = ScreenType.NORMAL;
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                result = ScreenType.LARGE;
                break;
            default:
                result = ScreenType.NORMAL;
                break;
        }
        return result;
    }

    public void setImageRes(@DrawableRes int imageRes) {
        if (mCoverImg != null) {
            mCoverImg.setImageResource(imageRes);
        }
    }

    public void setMedia(BaseMedia media) {
        if (media instanceof ImageMedia) {
            mVideoLayout.setVisibility(GONE);
            setCover(((ImageMedia) media).getThumbnailPath());
        } else if (media instanceof VideoMedia) {
            mVideoLayout.setVisibility(VISIBLE);
            VideoMedia videoMedia = (VideoMedia) media;
            TextView durationTxt = ((TextView) mVideoLayout.findViewById(R.id.video_duration_txt));
            durationTxt.setText(videoMedia.getDuration());
            durationTxt.setCompoundDrawablesWithIntrinsicBounds(BoxingManager.getInstance().getBoxingConfig().getVideoDurationRes(), 0, 0, 0);
            ((TextView) mVideoLayout.findViewById(R.id.video_size_txt)).setText(videoMedia.getSizeByUnit());
            setCover(videoMedia.getPath());
        }
    }

    private void setCover(@NonNull String path) {
        if (mCoverImg == null || TextUtils.isEmpty(path)) {
            return;
        }
        mCoverImg.setTag(R.string.boxing_app_name, path);
        BoxingMediaLoader.getInstance().displayThumbnail(mCoverImg, path, mScreenType.getValue(), mScreenType.getValue());
    }

    @SuppressWarnings("deprecation")
    public void setChecked(boolean isChecked) {
        if (isChecked) {
            mFontLayout.setVisibility(View.VISIBLE);
            mCheckImg.setImageDrawable(getResources().getDrawable(BoxingResHelper.getMediaCheckedRes()));
        } else {
            mFontLayout.setVisibility(View.GONE);
            mCheckImg.setImageDrawable(getResources().getDrawable(BoxingResHelper.getMediaUncheckedRes()));
        }
    }

}
