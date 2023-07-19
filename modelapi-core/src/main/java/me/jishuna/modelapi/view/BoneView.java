package me.jishuna.modelapi.view;

import org.bukkit.Color;

import me.jishuna.modelapi.Bone;
import team.unnamed.creative.base.Vector3Float;

public interface BoneView {
    int DEFAULT_COLOR = 0xFFFFFF;

    Bone getBone();

    void color(Color color);

    default void color(int r, int g, int b) {
        color(Color.fromRGB(r, g, b));
    }

    default void color(int rgb) {
        color(Color.fromRGB(rgb));
    }

    default void colorDefault() {
        color(DEFAULT_COLOR);
    }

    void setPosition(Vector3Float position);

    void setRotation(Vector3Float rotation);
}
