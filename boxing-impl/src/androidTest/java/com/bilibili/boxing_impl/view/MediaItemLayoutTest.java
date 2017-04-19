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

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.impl.VideoMedia;
import com.bilibili.boxing_impl.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author ChenSL
 */
@RunWith(AndroidJUnit4.class)
public class MediaItemLayoutTest {

    @Before
    public void setup() {
        BoxingMediaLoader.getInstance().init(new IBoxingMediaLoader() {
            @Override
            public void displayThumbnail(@NonNull ImageView img, @NonNull String absPath, int width, int height) {

            }

            @Override
            public void displayRaw(@NonNull ImageView img, @NonNull String absPath, int width, int height, IBoxingCallback callback) {

            }
        });
    }

    @Test
    public void testMediaItemLayout() {
        MediaItemLayout layout = (MediaItemLayout) LayoutInflater.from(InstrumentationRegistry.getContext()).inflate(R.layout.layout_boxing_recycleview_item, null, false);
        assertNotNull(layout);

        BoxingManager.getInstance().setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.VIDEO).withVideoDurationRes(R.drawable.ic_boxing_broken_image));
        VideoMedia videoMedia = new VideoMedia.Builder("233", "233").build();
        layout.setMedia(videoMedia);
        View videoLayout = layout.findViewById(R.id.video_layout);
        assertNotNull(videoLayout);
        assertTrue(videoLayout.getVisibility() == View.VISIBLE);

        layout.setChecked(true);
        View fontLayout = layout.findViewById(R.id.media_font_layout);
        assertNotNull(fontLayout);
        assertTrue(fontLayout.getVisibility() == View.VISIBLE);
        ImageView checkImg = (ImageView) layout.findViewById(R.id.media_item_check);
        assertNotNull(checkImg);
        assertTrue(checkImg.getVisibility() == View.VISIBLE);

        layout.setChecked(false);
        assertNotNull(fontLayout);
        assertTrue(fontLayout.getVisibility() == View.GONE);
    }
}