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

import android.app.Activity;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing.utils.CameraPickerHelper;
import com.bilibili.boxing.utils.CompressTask;
import com.bilibili.boxing.utils.BoxingFileHelper;
import com.bilibili.boxing.utils.ImageCompressor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;


/**
 * @author ChenSL
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
@PrepareOnlyThisForTest(Environment.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class PickerUtilTest {
    @Mock
    private CameraPickerHelper mHelper;

    @Captor
    private ArgumentCaptor<CameraPickerHelper.Callback> mCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence charSequence = (CharSequence) invocation.getArguments()[0];
                return !(charSequence != null && charSequence.length() > 0);
            }
        });
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void testCompressTask() {
        ImageCompressor illegalCompressor = new ImageCompressor(new File("///"));
        ImageMedia media = new ImageMedia("123", "44");
        ImageCompressor compressor = new ImageCompressor(new File("src/main/res/"));
        ImageMedia media1 = new ImageMedia("1223", "../boxing/boxing-impl/src/main/res/drawable-hdpi/ic_boxing_broken_image.png");
        media1.setSize("233");

        boolean result1 = CompressTask.compress(null, null, 0);
        assertTrue(!result1);
        result1 = CompressTask.compress(null, media, 0);
        assertTrue(!result1);
        result1 = CompressTask.compress(illegalCompressor, media, 0);
        assertTrue(!result1);
        result1= CompressTask.compress(illegalCompressor, media, 1000);
        assertTrue(!result1);

        result1 = CompressTask.compress(compressor, media1, 1000);
        assertTrue(result1);

    }

    @Test
    public void testFileHelper() throws ExecutionException, InterruptedException {
        boolean nullFile = BoxingFileHelper.createFile(null);
        assertTrue(!nullFile);

        boolean hasFile = BoxingFileHelper.createFile("/");
        assertTrue(hasFile);

    }

    @Test
    public void testCacheDir() {
        String nullFile = BoxingFileHelper.getCacheDir(null);
        assertTrue(nullFile == null);
    }

    @Test
    public void testGetExternalDCIM() {
        PowerMockito.mockStatic(Environment.class);
        String file = BoxingFileHelper.getExternalDCIM(BoxingFileHelper.DEFAULT_SUB_DIR);
        assertNull(file);

        PowerMockito.when(Environment.getExternalStorageState()).thenReturn(Environment.MEDIA_MOUNTED);
        file = BoxingFileHelper.getExternalDCIM(BoxingFileHelper.DEFAULT_SUB_DIR);
        assertNull(file);

        PowerMockito.when(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))
                .thenReturn(new File("DCIM"));
        file = BoxingFileHelper.getExternalDCIM(BoxingFileHelper.DEFAULT_SUB_DIR);
        assertNotNull(file);
    }

    @Test
    public void testCameraHelper() {
        CameraPickerHelper helper = new CameraPickerHelper(null);
        boolean fail = helper.onActivityResult(0, 0);
        assertFalse(fail);

        fail = helper.onActivityResult(CameraPickerHelper.REQ_CODE_CAMERA, 0);
        assertFalse(fail);

        fail = helper.onActivityResult(0, Activity.RESULT_OK);
        assertFalse(fail);

        boolean suc = helper.onActivityResult(CameraPickerHelper.REQ_CODE_CAMERA, Activity.RESULT_OK);
        Assert.assertTrue(suc);
    }
}
