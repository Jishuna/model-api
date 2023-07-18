package me.jishuna.modelapi;

import java.util.Map;

import team.unnamed.creative.base.CubeFace;
import team.unnamed.creative.base.Vector3Float;
import team.unnamed.creative.model.ElementFace;
import team.unnamed.creative.model.ElementRotation;

public record Cube(Vector3Float from, Vector3Float to, ElementRotation rotation, Map<CubeFace, ElementFace> faces) {
}
