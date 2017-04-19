package com.bilibili.boxing;

import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing_impl.BoxingResHelper;
import com.bilibili.boxing_impl.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * @author ChenSL
 */
@RunWith(JUnit4.class)
public class ConfigTest {
    private BoxingManager mPickerManager;

    @Before
    public void setUp() {
        mPickerManager = BoxingManager.getInstance();
    }

    @Test
    public void testPlaceHolder() {
        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG));
        BoxingConfig config = mPickerManager.getBoxingConfig();
        assertEquals(config.getMediaPlaceHolderRes(), 0);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withMediaPlaceHolderRes(R.drawable.ic_boxing_broken_image));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getMediaPlaceHolderRes(), R.drawable.ic_boxing_broken_image);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getAlbumPlaceHolderRes(), 0);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withAlbumPlaceHolderRes(R.drawable.ic_boxing_broken_image));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getAlbumPlaceHolderRes(), R.drawable.ic_boxing_broken_image);
    }

    @Test
    public void testVideoDuration() {
        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.VIDEO));
        BoxingConfig config = mPickerManager.getBoxingConfig();
        assertEquals(config.getVideoDurationRes(), 0);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withVideoDurationRes(R.drawable.ic_boxing_broken_image));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getVideoDurationRes(), R.drawable.ic_boxing_broken_image);
    }

    @Test
    public void testImageSelectionRes() {
        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG));
        BoxingConfig config = mPickerManager.getBoxingConfig();
        assertEquals(config.getMediaCheckedRes(), 0);
        assertEquals(BoxingResHelper.getMediaCheckedRes(), R.drawable.ic_boxing_checked);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withMediaCheckedRes(R.drawable.ic_boxing_broken_image));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getMediaCheckedRes(), R.drawable.ic_boxing_broken_image);
        assertEquals(BoxingResHelper.getMediaCheckedRes(), R.drawable.ic_boxing_broken_image);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getMediaUnCheckedRes(), 0);
        assertEquals(BoxingResHelper.getMediaUncheckedRes(), R.drawable.shape_boxing_unchecked);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withMediaUncheckedRes(R.drawable.ic_boxing_broken_image));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getMediaUnCheckedRes(), R.drawable.ic_boxing_broken_image);
        assertEquals(BoxingResHelper.getMediaUncheckedRes(), R.drawable.ic_boxing_broken_image);
    }

    @Test
    public void testCameraRes() {
        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG));
        BoxingConfig config = mPickerManager.getBoxingConfig();
        assertEquals(config.getCameraRes(), 0);
        assertEquals(BoxingResHelper.getCameraRes(), 0);

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needCamera(R.drawable.ic_boxing_broken_image));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getCameraRes(), R.drawable.ic_boxing_broken_image);
        assertEquals(BoxingResHelper.getCameraRes(), R.drawable.ic_boxing_broken_image);
    }
}
