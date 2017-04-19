package com.bilibili.boxing.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.bilibili.boxing.demo.R;
import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.config.BoxingCropOption;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.utils.BoxingFileHelper;
import com.bilibili.boxing_impl.ui.BoxingActivity;

import java.util.List;
import java.util.Locale;

/**
 * A demo to show a Picker Activity with intent filter, which can start by other apps.
 * Get absolute path through {@link Intent#getDataString()} in {@link #onActivityResult(int, int, Intent)}.
 *
 * @author ChenSL
 */

public class IntentFilterActivity extends BoxingActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // in DCIM/bili/boxing
        String cropPath = BoxingFileHelper.getBoxingPathInDCIM();
        if (TextUtils.isEmpty(cropPath)) {
            Toast.makeText(getApplicationContext(), R.string.boxing_storage_deny, Toast.LENGTH_SHORT).show();
            return;
        }
        Uri destUri = new Uri.Builder()
                .scheme("file")
                .appendPath(cropPath)
                .appendPath(String.format(Locale.US, "%s.jpg", System.currentTimeMillis()))
                .build();
        BoxingConfig config = new BoxingConfig(BoxingConfig.Mode.SINGLE_IMG).needCamera(R.drawable.ic_boxing_camera_white).withCropOption(new BoxingCropOption(destUri));
        BoxingManager.getInstance().setBoxingConfig(config);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onBoxingFinish(Intent intent, @Nullable List<BaseMedia> medias) {
        if (medias != null && medias.size() > 0) {
            intent.setData(Uri.parse(medias.get(0).getPath()));
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, null);
        }
        finish();
    }
}


