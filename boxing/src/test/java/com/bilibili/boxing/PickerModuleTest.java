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

package com.bilibili.boxing;

import android.content.ContentResolver;

import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.callback.IAlbumTaskCallback;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing.model.entity.impl.VideoMedia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author ChenSL
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class PickerModuleTest {
    private BoxingManager mPickerManager;

    @Before
    public void setUp() {
        mPickerManager = BoxingManager.getInstance();
    }

    @Test
    public void testLoadImage() {
        assertNotNull(mPickerManager);
        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG));
        BoxingConfig config = mPickerManager.getBoxingConfig();
        assertNotNull(config);
        Assert.assertEquals(config.getMode(), BoxingConfig.Mode.MULTI_IMG);

        ContentResolver cr = RuntimeEnvironment.application.getContentResolver();
        assertNotNull(cr);
        mPickerManager.loadAlbum(cr, new IAlbumTaskCallback() {
            @Override
            public void postAlbumList(List<AlbumEntity> list) {
                assertNotNull(list);
            }
        });
    }

    @Test
    public void testBaseMedia() {
        BaseMedia media = new ImageMedia("233", "988");
        assertTrue(media.getId().equals("233"));
        assertTrue(media.getPath().equals("988"));
        media.setSize("99");
        assertTrue(media.getSize() == 99);
        media.setSize("&&");
        assertTrue(media.getSize() == 0);
        media.setSize("-1");
        assertTrue(media.getSize() == 0);
    }

    @Test
    public void testImageMedia() {
        ImageMedia imageMedia = new ImageMedia.Builder("233", "233").build();
        imageMedia.setPath("/");
        imageMedia.getThumbnailPath();
        String compressPath = imageMedia.getThumbnailPath();
        assertEquals(compressPath, "/");
        imageMedia.setCompressPath("111");
        String compressPath1 = imageMedia.getThumbnailPath();
        assertEquals(compressPath1, "/");

        imageMedia = new ImageMedia.Builder("233", "233").setThumbnailPath("999").build();
        String compressPath3 = imageMedia.getThumbnailPath();
        assertEquals(compressPath3, "233");

        assertEquals(imageMedia.getMimeType(), "image/jpeg");
        imageMedia.setImageType(ImageMedia.IMAGE_TYPE.GIF);
        assertEquals(imageMedia.getMimeType(), "image/gif");

    }

    @Test
    public void testVideoMedia() {
        VideoMedia videoMedia = new VideoMedia.Builder("233", "233").build();
        videoMedia.setDuration("asd");
        String result1 = videoMedia.formatTimeWithMin(0);
        assertEquals(result1, "00:00");
        String result2 = videoMedia.formatTimeWithMin(1000);
        assertEquals(result2, "00:01");
        String result3 = videoMedia.formatTimeWithMin(1000 * 36);
        assertEquals(result3, "00:36");

        String result4 = videoMedia.formatTimeWithMin(1000 * 60 * 36 + 45 * 1000);
        assertEquals(result4, "36:45");

        String result5 = videoMedia.formatTimeWithMin(1000 * 60 * 36 + 45 * 1000 + 8500);
        assertEquals(result5, "36:53");

        String result6 = videoMedia.formatTimeWithMin((long) (1000 * 60 * 102 + 1000 * 60 * 7.2 + 45 * 1000 + 8500));
        assertEquals(result6, "110:05");

        String duration = videoMedia.getDuration();
        assertEquals(duration, "0:00");
        videoMedia.setDuration("2160000");
        String duration1 = videoMedia.getDuration();
        assertEquals(duration1, "36:00");

    }
    
    @Test
    public void testSize() {
        VideoMedia videoMedia = new VideoMedia.Builder("233", "233").build();
        videoMedia.setSize("-1");
        String result = videoMedia.getSizeByUnit();
        assertEquals(result, "0K");

        videoMedia.setSize("0");
        result = videoMedia.getSizeByUnit();
        assertEquals(result, "0K");

        videoMedia.setSize("200");
        result = videoMedia.getSizeByUnit();
        assertEquals(result, "0.2K");

        videoMedia.setSize("1024");
        result = videoMedia.getSizeByUnit();
        assertEquals(result, "1.0K");

        videoMedia.setSize("1048576");
        result = videoMedia.getSizeByUnit();
        assertEquals(result, "1.0M");

        videoMedia.setSize("2048576");
        result = videoMedia.getSizeByUnit();
        assertEquals(result, "2.0M");
    }

    @Test
    public void testMaxCount() {
        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withMaxCount(10));
        BoxingConfig config = mPickerManager.getBoxingConfig();
        int count = config.getMaxCount();
        assertEquals(count, 10);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withMaxCount(0));
        config = mPickerManager.getBoxingConfig();
        count = config.getMaxCount();
        assertEquals(count, BoxingConfig.DEFAULT_SELECTED_COUNT);


    }

}

