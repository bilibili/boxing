package com.bilibili.boxing.loader;

import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.BaseMedia;

import java.util.List;

/**
 * @author jax
 * @date 2018/1/5
 * @description filter album images list.
 */

public interface IBoxingMediaFilter {

    /**
     * filter origin albums to new list.
     *
     * @param albums origin album image.
     * @return last albumEntity list.
     */
    List<AlbumEntity> filterAlbum(List<AlbumEntity> albums);

    /**
     * filter media to new list.
     *
     * @param medias origin medias.
     * @return filter medias
     */
    List<BaseMedia> filterMedia(List<BaseMedia> medias);

}
