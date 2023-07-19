package me.jishuna.modelapi;

import java.util.Map;

import me.jishuna.modelapi.animation.Animation;

public record Model(String name, Map<String, Bone> bones, ModelAsset asset, Map<String, Animation> animations) {

}
