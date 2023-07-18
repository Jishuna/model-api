package me.jishuna.modelapi.nms.v1_20_R1;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;

import me.jishuna.modelapi.Model;
import me.jishuna.modelapi.ModelEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class Utils {
    private static EntityType<MinecraftModelEntity> TEST;

    public static void registerEntityTypes() {
        TEST = register("test", EntityType.Builder.of(MinecraftModelEntity::new, MobCategory.CREATURE));
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type.build(id));
    }

    public static Field getField(Class<?> clazz, Class<?> type, int index) {
        int i = 0;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type) {
                if (index == i) {
                    field.setAccessible(true);
                    return field;
                }
                i++;
            }
        }
        return null;
    }

    public static ModelEntity create(Location location, Model model) {
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();

        MinecraftModelEntity entity = new MinecraftModelEntity(TEST, level, model);
        entity.setPos(location.getX(), location.getY(), location.getZ());
        level.addFreshEntity(entity);

        return entity.getBukkitEntity();
    }
}
