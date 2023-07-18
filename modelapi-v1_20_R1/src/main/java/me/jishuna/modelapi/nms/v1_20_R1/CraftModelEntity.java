package me.jishuna.modelapi.nms.v1_20_R1;

import java.util.Collection;

import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftMob;
import org.bukkit.entity.EntityType;

import me.jishuna.modelapi.ModelEntity;
import me.jishuna.modelapi.view.BoneView;

public class CraftModelEntity extends CraftMob implements ModelEntity {

    public CraftModelEntity(CraftServer server, MinecraftModelEntity entity) {
        super(server, entity);
    }

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
    }

    @Override
    public MinecraftModelEntity getHandle() {
        return (MinecraftModelEntity) super.getHandle();
    }

    @Override
    public Collection<? extends BoneView> getBones() {
        return getHandle().getBones().values();
    }

    @Override
    public BoneView getBone(String name) {
        return getHandle().getBones().get(name);
    }

}
