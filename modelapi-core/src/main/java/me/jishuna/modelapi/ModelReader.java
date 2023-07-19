package me.jishuna.modelapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.jishuna.modelapi.animation.Animation;
import team.unnamed.creative.base.Axis3D;
import team.unnamed.creative.base.CubeFace;
import team.unnamed.creative.base.Vector3Float;
import team.unnamed.creative.base.Vector4Float;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.model.ElementFace;
import team.unnamed.creative.model.ElementRotation;

public class ModelReader {
    private static int MODEL_DATA = 43653;

    private final File file;

    public ModelReader(File file) {
        this.file = file;
    }

    public Model read() {
        JsonObject json;
        try (InputStream stream = new FileInputStream(this.file); Reader reader = new InputStreamReader(stream)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException ex) {
            // panic
            return null;
        }

        String name = json.get("name").getAsString();

        JsonObject resolution = json.getAsJsonObject("resolution");
        int textureWidth = resolution.get("width").getAsInt();
        int textureHeight = resolution.get("height").getAsInt();

        Map<String, Bone> bones = new LinkedHashMap<>();
        Map<String, Animation> animations = new LinkedHashMap<>();
        Map<String, Writable> textures = new HashMap<>();
        Map<Integer, String> textureMapping = new HashMap<>();
        Map<String, BoneAsset> boneAssets = new LinkedHashMap<>();

        try {
            readTextures(json, textures, textureMapping);
            readElementArray(json, bones, boneAssets, textureWidth, textureHeight);
            AnimationReader.readAnimations(json, animations);

            return new Model(name, bones, new ModelAsset(name, textures, textureMapping, boneAssets), animations);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void readElementArray(JsonObject json, Map<String, Bone> bones, Map<String, BoneAsset> boneAssets, int width, int height) {
        Map<String, Cube> cubeMap = new HashMap<>();

        for (JsonElement cubeElement : json.getAsJsonArray("elements")) {
            JsonObject cubeJson = cubeElement.getAsJsonObject();

            Vector3Float pivot = Utils.readAsVector(cubeJson.get("origin"));

            Vector3Float from = Utils.readAsVector(cubeJson.get("from"));
            Vector3Float to = Utils.readAsVector(cubeJson.get("to"));

            System.out.println("From: " + from);
            System.out.println("To: " + to);

            // Vector3Float origin = new Vector3Float(-to.x(), from.y(), from.z());
            // to = origin.add(to.subtract(from));

            System.out.println("From: " + from);
            System.out.println("To: " + to);

            Vector3Float rotation = Utils.isValid(cubeJson, "rotation") ? Utils.readAsVector(cubeJson.get("rotation")) : Vector3Float.ZERO;

            if (getRotationCount(rotation) > 1) {
                // error
                continue;
            }

            float x = rotation.x();
            float y = rotation.y();
            float z = rotation.z();

            Axis3D axis;
            float angle;

            if (x != 0) {
                axis = Axis3D.X;
                angle = x;
            } else if (y != 0) {
                axis = Axis3D.Y;
                angle = y;
            } else {
                axis = Axis3D.Z;
                angle = z;
            }

            Map<CubeFace, ElementFace> faces = new HashMap<>();

            for (Map.Entry<String, JsonElement> faceEntry : cubeJson.getAsJsonObject("faces").entrySet()) {

                CubeFace face = CubeFace.valueOf(faceEntry.getKey().toUpperCase(Locale.ROOT));
                JsonObject faceJson = faceEntry.getValue().getAsJsonObject();

                int texture = Utils.isValid(faceJson, "texture") ? faceJson.get("texture").getAsInt() : -1;
                JsonArray uvJson = faceJson.get("uv").getAsJsonArray();

                Vector4Float uv = new Vector4Float(uvJson.get(0).getAsFloat() / width, uvJson.get(1).getAsFloat() / height, uvJson.get(2).getAsFloat() / width, uvJson.get(3).getAsFloat() / height);
                faces.put(face, ElementFace.builder().uv(uv).texture("#" + texture).tintIndex(0).build());
            }

            String id = cubeJson.get("uuid").getAsString();
            cubeMap.put(id, new Cube(from, to, ElementRotation.of(pivot, axis, angle, ElementRotation.DEFAULT_RESCALE), faces));

            for (JsonElement element : json.get("outliner").getAsJsonArray()) {
                if (element.isJsonObject()) {
                    createBone(Vector3Float.ZERO, cubeMap, element.getAsJsonObject(), bones, boneAssets);
                }
            }
        }
    }

    private void createBone(Vector3Float parentPosition, Map<String, Cube> cubeMap, JsonObject json, Map<String, Bone> siblings, Map<String, BoneAsset> siblingAssets) {
        String name = json.get("name").getAsString();

        Vector3Float unitAbsolutePosition = Utils.readAsVector(json.get("origin"));
        Vector3Float rotation = Utils.isValid(json, "rotation") ? Utils.readAsVector(json.get("rotation")) : Vector3Float.ZERO;

        Vector3Float absolutePosition = unitAbsolutePosition.divide(16f, 16f, 16f);
        Vector3Float position = absolutePosition;

        List<Cube> cubes = new ArrayList<>();
        Map<String, Bone> children = new LinkedHashMap<>();
        Map<String, BoneAsset> childrenAssets = new LinkedHashMap<>();

        for (JsonElement childElement : json.get("children").getAsJsonArray()) {
            if (childElement.isJsonObject()) {
                createBone(absolutePosition, cubeMap, childElement.getAsJsonObject(), children, childrenAssets);
            } else if (childElement.isJsonPrimitive() && childElement.getAsJsonPrimitive().isString()) {
                String cubeId = childElement.getAsString();
                Cube cube = cubeMap.get(cubeId);

                if (cube == null) {
                    // throw new IOException("Bone " + name + " contains " + "an invalid cube id: '"
                    // + cubeId + "', not present in " + "the 'elements' section");
                } else {
                    cubes.add(cube);
                }
            }
        }
        BoneAsset asset = new BoneAsset(name, unitAbsolutePosition, MODEL_DATA++, Vector3Float.ZERO, test(cubes, unitAbsolutePosition.multiply(1, -1, 1)), false, childrenAssets);

        siblings.put(name, new Bone(name, position, rotation, children, asset.customModelData()));
        siblingAssets.put(name, asset);
    }

    private List<Cube> test(List<Cube> cubes, Vector3Float unitAbsolutePosition) {
        List<Cube> newCubes = new ArrayList<>();
        for (Cube cube : cubes) {
            Vector3Float from = cube.from();
            Vector3Float to = cube.to();

            ElementRotation rotation = cube.rotation();
            Vector3Float rotationOrigin = rotation.origin();
            rotationOrigin = rotationOrigin.add(8 + unitAbsolutePosition.x(), unitAbsolutePosition.y(), 8 + unitAbsolutePosition.z());

            from = from.add(8 + unitAbsolutePosition.x(), unitAbsolutePosition.y(), 8 + unitAbsolutePosition.z());
            to = to.add(8 + unitAbsolutePosition.x(), unitAbsolutePosition.y(), 8 + unitAbsolutePosition.z());

            Cube newCube = new Cube(from, to, rotation.origin(rotationOrigin), cube.faces());
            newCubes.add(newCube);
        }
        return newCubes;
    }

    private static int getRotationCount(Vector3Float vector) {
        int count = 0;

        if (vector.x() != 0) {
            count++;
        }

        if (vector.y() != 0) {
            count++;
        }

        if (vector.z() != 0) {
            count++;
        }
        return count;
    }

    private static final String BASE_64_PREFIX = "data:image/png;base64,";

    public static void readTextures(JsonObject json, Map<String, Writable> textures, Map<Integer, String> textureMappings) throws IOException {

        JsonArray texturesJson = json.get("textures").getAsJsonArray();

        for (int index = 0; index < texturesJson.size(); index++) {

            JsonObject textureJson = texturesJson.get(index).getAsJsonObject();
            String name = textureJson.get("name").getAsString();
            String source = textureJson.get("source").getAsString();

            if (!(source.startsWith(BASE_64_PREFIX))) {
                throw new IOException("Model doesn't contains a valid" + " texture source. Not Base64");
            }

            String base64Source = source.substring(BASE_64_PREFIX.length());

            // remove PNG extension
            if (name.endsWith(".png")) {
                name = name.substring(0, name.length() - ".png".length());
            }

            // map to index
            textureMappings.put(index, name);
            textures.put(name, Writable.bytes(Base64.getDecoder().decode(base64Source)));
        }
    }
}
