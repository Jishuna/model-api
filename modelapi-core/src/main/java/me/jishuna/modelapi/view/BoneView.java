package me.jishuna.modelapi.view;

import org.joml.Vector3f;
import me.jishuna.modelapi.Bone;
import org.bukkit.Color;

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

    void setPosition(Vector3f position);

    void setRotation(Vector3f rotation);
}
