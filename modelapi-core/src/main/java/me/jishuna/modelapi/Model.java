package me.jishuna.modelapi;

import java.util.Map;

public record Model(String name, Map<String, Bone> bones, ModelAsset asset) {

}
