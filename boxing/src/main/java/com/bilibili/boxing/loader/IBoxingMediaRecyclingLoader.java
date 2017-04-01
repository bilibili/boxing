package com.bilibili.boxing.loader;

import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Defines the additional methods to recycle the resources associated with the destroyed media objects.
 */
public interface IBoxingMediaRecyclingLoader extends IBoxingMediaLoader {
    /**
     * Called when the image view containing the thumbnail is no longer valid (e.g. `onViewRecycled` in the recycler view has been triggered).
     * <p>
     * <b>Note:</b> If you are using `Glide` then call `Glide.clear(img)` here.
     * </p>
     *
     * @param img     The {@link ImageView} which holds the thumbnail.
     * @param absPath The absolute path to the thumbnail image.
     */
    void recycleThumbnail(@NonNull ImageView img, @NonNull String absPath);

    /**
     * Called when the image view containing the raw image is no longer valid (e.g. the fragment has been destroyed).
     * <p>
     * <b>Note:</b> If you are using `Glide` then call `Glide.clear(img)` here.
     * </p>
     *
     * @param img     The {@link ImageView} which holds the raw (big) image resource.
     * @param absPath The absolute path to the image resource.
     */
    void recycleRaw(@NonNull ImageView img, @NonNull String absPath);
}
