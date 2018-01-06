package com.bilibili.boxing;

import com.bilibili.boxing.loader.IBoxingMediaFilter;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.BaseMedia;

import java.util.List;

/**
 * @author jax
 * @date 2018/1/5
 * @description
 */

public enum BoxingMediaFilter {
    INSTANCE;

    private IBoxingMediaFilter filter;

    public void init(IBoxingMediaFilter filter) {
        this.filter = filter;
    }

    public List<AlbumEntity> startFilter(List<AlbumEntity> entities) {
        return filter == null ? entities : filter.filterAlbum(entities);
    }

    public List<BaseMedia> filterMedia(List<BaseMedia> medias) {
        return filter == null ? medias : filter.filterMedia(medias);
    }

}
