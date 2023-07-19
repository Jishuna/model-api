package me.jishuna.modelapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipOutputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import team.unnamed.creative.base.Vector3Float;
import team.unnamed.creative.file.FileTree;

public class Utils {

    public static boolean isValid(JsonObject object, String name) {
        return object.has(name) && !object.get(name).isJsonNull();
    }

    public static Vector3Float readAsVector(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        return new Vector3Float(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    public static void writeModels(File file, Collection<Model> models) {
        try (FileTree tree = FileTree.zip(new ZipOutputStream(new FileOutputStream(file)))) {
            file.createNewFile();
            ModelWriter.resource("mynamespace").write(tree, models);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static float parseLenientFloat(JsonElement element) {
        return element.getAsJsonPrimitive().isString() ? Float.parseFloat(element.getAsString().replace(',', '.')) : element.getAsFloat();
    }
}
