package me.jishuna.modelapi;

import java.util.Map;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import team.unnamed.creative.base.Vector3Float;

public record Bone(String name, Vector3Float position, Vector3Float rotation, Map<String, Bone> children, int customModelData) {

    public Vector3f positionJOML() {
        return new Vector3f(position.x(), position.y(), position.z());
    }

    public Quaternionf rotationJOML() {
        Quaternion quat = Quaternion.fromEuler(this.rotation);
        return new Quaternionf(quat.x, quat.y, quat.z, quat.w);
    }
}
