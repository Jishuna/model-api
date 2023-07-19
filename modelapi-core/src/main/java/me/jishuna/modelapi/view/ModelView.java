package me.jishuna.modelapi.view;

import java.util.Collection;
import java.util.Objects;

import me.jishuna.modelapi.Model;
import me.jishuna.modelapi.animation.Animation;
import me.jishuna.modelapi.animation.AnimationController;

public interface ModelView {
    
    Model model();

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
    
    AnimationController animationController();
    
    default void playAnimation(String name, int transitionTicks) {
        Animation animation = model().animations().get(name);
        Objects.requireNonNull(animation, "Animation " + name);
        animationController().queue(animation, transitionTicks);
    }
    
    default void playAnimation(String name) {
        playAnimation(name, 0);
    }

    /**
     * Ticks animations, makes required bones pass
     * to the next animation frame
     */
    void tickAnimations();
}
