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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.presenter.PickerPresenter;
import com.bilibili.boxing_impl.R;
import com.bilibili.boxing_impl.ui.BoxingViewFragment;

/**
 * Created by ChenSL on 2017/1/10.
 */

public class TestBlankActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boxing);
        BoxingMediaLoader.getInstance().init(new IBoxingMediaLoader() {

            @Override
            public void displayThumbnail(@NonNull ImageView img, @NonNull String absPath, int width, int height) {

            }

            @Override
            public void displayRaw(@NonNull ImageView img, @NonNull String absPath, int width, int height,  IBoxingCallback callback) {

            }
        });
        BoxingViewFragment fragment = BoxingViewFragment.newInstance();
        fragment.setTitleTxt((TextView) findViewById(R.id.pick_album_txt));
        fragment.setPresenter(new PickerPresenter(fragment));
        fragment.setPickerConfig(new BoxingConfig(BoxingConfig.Mode.MULTI_IMG));

        final FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().add(R.id.content_layout, fragment).commit();
    }
}
