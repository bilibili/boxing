package com.bilibili.boxing;

import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.config.BoxingConfig;
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

        mPickerManager.setBoxingConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withMediaPlaceHolderRes(R.drawable.ic_boxing_broken_image));
        config = mPickerManager.getBoxingConfig();
        assertEquals(config.getAlbumPlaceHolderRes(), R.drawable.ic_boxing_broken_image);
    }
}
