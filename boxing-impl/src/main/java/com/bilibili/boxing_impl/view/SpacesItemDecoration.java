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

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * @author ChenSL
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpace;
    private int mSpanCount;
    private int mRadixX;
    private int mItemCountInLastLine;
    private int mOldItemCount = -1;

    public SpacesItemDecoration(int space) {
        this(space, 1);
    }

    public SpacesItemDecoration(int space, int spanCount) {
        this.mSpace = space;
        this.mSpanCount = spanCount;
        this.mRadixX = space / spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, final RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        final int sumCount = state.getItemCount();
        final int position = params.getViewLayoutPosition();
        final int spanSize;
        final int index;

        if (params instanceof GridLayoutManager.LayoutParams) {
            GridLayoutManager.LayoutParams gridParams = (GridLayoutManager.LayoutParams) params;
            spanSize = gridParams.getSpanSize();
            index = gridParams.getSpanIndex();

            if ((position == 0 || mOldItemCount != sumCount) && mSpanCount > 1) {
                int countInLine = 0;
                int spanIndex;

                for (int tempPosition = sumCount - mSpanCount; tempPosition < sumCount; tempPosition++) {
                    spanIndex = ((GridLayoutManager) parent.getLayoutManager()).getSpanSizeLookup().getSpanIndex(tempPosition, mSpanCount);
                    countInLine = spanIndex == 0 ? 1 : countInLine + 1;
                }
                mItemCountInLastLine = countInLine;
                if (mOldItemCount != sumCount) {
                    mOldItemCount = sumCount;
                    if (position != 0) {
                        parent.post(new Runnable() {
                            @Override
                            public void run() {
                                parent.invalidateItemDecorations();
                            }
                        });
                    }
                }
            }
        } else if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
            spanSize = ((StaggeredGridLayoutManager.LayoutParams) params).isFullSpan() ? mSpanCount : 1;
            index = ((StaggeredGridLayoutManager.LayoutParams) params).getSpanIndex();
        } else {
            spanSize = 1;
            index = 0;
        }

        if (spanSize < 1 || index < 0 || spanSize > mSpanCount) {
            return;
        }

        outRect.left = mSpace - mRadixX * index;
        outRect.right = mRadixX + mRadixX * (index + spanSize - 1);

        if (mSpanCount == 1 && position == sumCount - 1) {
            outRect.bottom = mSpace;
        } else if (position >= sumCount - mItemCountInLastLine && position < sumCount) {
            outRect.bottom = mSpace;
        }
        outRect.top = mSpace;
    }
}