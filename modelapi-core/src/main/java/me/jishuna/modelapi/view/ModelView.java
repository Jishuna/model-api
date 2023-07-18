package me.jishuna.modelapi.view;

import java.util.Collection;

public interface ModelView {

    default void color(int r, int g, int b) {
        for (BoneView bone : getBones()) {
            bone.color(r, g, b);
        }
    }

    default void color(int rgb) {
        for (BoneView bone : getBones()) {
            bone.color(rgb);
        }
    }

    default void colorizeDefault() {
        color(BoneView.DEFAULT_COLOR);
    }

    Collection<? extends BoneView> getBones();

    BoneView getBone(String name);
}
