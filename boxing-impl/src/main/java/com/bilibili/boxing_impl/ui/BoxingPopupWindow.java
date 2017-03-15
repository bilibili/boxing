package com.bilibili.boxing_impl.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.bilibili.boxing_impl.R;
import com.bilibili.boxing_impl.adapter.BoxingAlbumAdapter;
import com.bilibili.boxing_impl.view.SpacesItemDecoration;

public class BoxingPopupWindow extends PopupWindow {
    private RecyclerView recyclerView;
    private View anchorView;
    private View shadow;

    public BoxingPopupWindow(Context context, BoxingAlbumAdapter adapter, View anchor) {
        super(context);

        View view = View.inflate(context, R.layout.layout_album, null);
        anchorView = anchor;

        shadow = view.findViewById(R.id.album_shadow);
        shadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0));
        setAnimationStyle(0);

        recyclerView = (RecyclerView) view.findViewById(R.id.album_recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new SpacesItemDecoration(2, 1));
        recyclerView.setAdapter(adapter);
        setContentView(view);
    }

    @Override
    public void dismiss() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator alpha1 = ObjectAnimator.ofFloat(recyclerView, "alpha", 1, 0);
        ObjectAnimator alpha2 = ObjectAnimator.ofFloat(shadow, "alpha", 1, 0);
        set.playTogether(alpha1, alpha2);
        set.setDuration(500);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                BoxingPopupWindow.super.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        set.start();
    }

    public void show() {
        showAsDropDown(anchorView, 0, 0);
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator alpha1 = ObjectAnimator.ofFloat(recyclerView, "alpha", 0, 1);
        ObjectAnimator alpha2 = ObjectAnimator.ofFloat(shadow, "alpha", 0, 1);
        set.playTogether(alpha1, alpha2);
        set.setDuration(500);
        set.start();
    }
}
