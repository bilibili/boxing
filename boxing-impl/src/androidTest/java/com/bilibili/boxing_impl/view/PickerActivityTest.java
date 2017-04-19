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
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.config.BoxingCropOption;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing_impl.R;
import com.bilibili.boxing_impl.ui.BoxingActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Locale;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author ChenSL
 */
@RunWith(AndroidJUnit4.class)
public class PickerActivityTest {
    private Context mContext;

    @Before
    public void setup() {
        BoxingManager.getInstance().setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG));
        mContext = InstrumentationRegistry.getContext();
    }

    @Test
    public void testCreateVideoIntent() {
        Intent intent = Boxing.of(new BoxingConfig(BoxingConfig.Mode.VIDEO))
                .withIntent(mContext, BoxingActivity.class).getIntent();
        assertNotNull(intent);
        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        assertNotNull(config);
        assertEquals(config.getMode(), BoxingConfig.Mode.VIDEO);
        assertEquals(config.isNeedCamera(), false);
        BoxingCropOption cropOptions = config.getCropOption();
        assertEquals(cropOptions, null);
        ArrayList<BaseMedia> list = intent.getParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA);
        assertEquals(null, list);
    }

    @Test
    public void testCreateSingleImageIntent() {
        Intent intent = Boxing.of(new BoxingConfig(BoxingConfig.Mode.SINGLE_IMG).needCamera(R.drawable.ic_boxing_broken_image))
                .withIntent(mContext, BoxingActivity.class).getIntent();
        assertNotNull(intent);
        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        assertNotNull(config);
        assertEquals(config.getMode(), BoxingConfig.Mode.SINGLE_IMG);
        assertEquals(config.isNeedCamera(), true);
        BoxingCropOption cropOptions = config.getCropOption();
        assertEquals(cropOptions, null);
        ArrayList<BaseMedia> list = intent.getParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA);
        assertEquals(list, null);
    }

    @Test
    public void testCreateSingleImageWithCropIntent() {
        Uri destUri = new Uri.Builder()
                .scheme("file")
                .appendPath("test")
                .appendPath(String.format(Locale.US, "%s.jpg", System.currentTimeMillis()))
                .build();
        Intent intent = Boxing.of(new BoxingConfig(BoxingConfig.Mode.SINGLE_IMG).withCropOption(new BoxingCropOption(destUri)))
                .withIntent(mContext, BoxingActivity.class).getIntent();
        assertNotNull(intent);
        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        assertNotNull(config);
        assertEquals(config.getMode(), BoxingConfig.Mode.SINGLE_IMG);
        assertEquals(config.isNeedCamera(), false);
        BoxingCropOption cropOptions = config.getCropOption();
        assertNotNull(cropOptions);
        ArrayList<BaseMedia> list = intent.getParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA);
        assertEquals(list, null);
    }

    @Test
    public void testCreateMultiImageIntent() {
        Intent intent = Boxing.of(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needGif())
                .withIntent(mContext, BoxingActivity.class).getIntent();
        assertNotNull(intent);
        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        assertNotNull(config);
        assertEquals(config.getMode(), BoxingConfig.Mode.MULTI_IMG);
        assertEquals(config.isNeedCamera(), false);
        BoxingCropOption cropOptions = config.getCropOption();
        assertNull(cropOptions);
        ArrayList<BaseMedia> list = intent.getParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA);
        assertEquals(list, null);

        Intent intent1 = Boxing.of(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needGif()).
                withIntent(mContext, BoxingActivity.class, new ArrayList<ImageMedia>()).getIntent();
        assertNotNull(intent1);
        ArrayList<BaseMedia> list1 = intent.getParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA);
        assertEquals(list1, null);

        ArrayList<ImageMedia> medias = new ArrayList<>();
        medias.add(new ImageMedia("test", "test"));
        Intent intent2 = Boxing.of(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needGif()).
                withIntent(mContext, BoxingActivity.class, medias).getIntent();
        assertNotNull(intent2);
        ArrayList<BaseMedia> list2 = intent2.getParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA);
        assertEquals(list2, medias);
    }

    @Test
    public void testCreateCustomIntent() {
        BoxingConfig pickerConfig = new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needCamera(R.drawable.ic_boxing_broken_image);
        Uri destUri = new Uri.Builder()
                .scheme("file")
                .appendPath("test")
                .appendPath(String.format(Locale.US, "%s.jpg", System.currentTimeMillis()))
                .build();
        BoxingCropOption cropOptions = new BoxingCropOption(destUri);
        ArrayList<ImageMedia> medias = new ArrayList<>();
        medias.add(new ImageMedia("test", "test"));
        medias.add(new ImageMedia("test1", "test1"));
        pickerConfig.withCropOption(cropOptions);

        Intent intent = Boxing.of(pickerConfig).withIntent(mContext, BoxingActivity.class, medias).getIntent();
        assertNotNull(intent);

        BoxingConfig config = BoxingManager.getInstance().getBoxingConfig();
        assertNotNull(config);
        assertEquals(config.getMode(), BoxingConfig.Mode.MULTI_IMG);
        assertEquals(config.isNeedCamera(), true);
        assertEquals(config.isNeedGif(), false);
        BoxingCropOption cropOptionsResult = pickerConfig.getCropOption();
        assertEquals(cropOptions, cropOptionsResult);
        ArrayList<BaseMedia> list = intent.getParcelableArrayListExtra(Boxing.EXTRA_SELECTED_MEDIA);
        assertEquals(list, medias);

    }


}
