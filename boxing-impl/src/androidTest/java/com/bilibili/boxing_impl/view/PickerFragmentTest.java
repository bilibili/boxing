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

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.WindowManager;

import com.bilibili.boxing_impl.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author ChenSL
 */
@RunWith(AndroidJUnit4.class)
public class PickerFragmentTest {
    @Rule
    public ActivityTestRule<TestBlankActivity> mRule = new ActivityTestRule<>(TestBlankActivity.class);

    @Before
    public void setup() throws Throwable {
        // espresso need the screen on
        final Activity activity = mRule.getActivity();
        mRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                KeyguardManager km = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
                KeyguardManager.KeyguardLock lock = km.newKeyguardLock(Context.KEYGUARD_SERVICE);
                lock.disableKeyguard();

                //turn the screen on
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            }
        });
    }

    @Test
    public void showMedia() {
        ViewInteraction container = onView(withId(R.id.container));
        container.check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                assertNotNull(view);
                assertTrue(view.getVisibility() == View.VISIBLE);
            }
        });
        ViewInteraction empty = onView(withId(R.id.empty_txt));
        empty.check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                assertNotNull(view);
            }
        });

    }

}