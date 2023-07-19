package me.jishuna.modelapi.nms.v1_20_R1;

import java.util.function.Consumer;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.math.Transformation;

import me.jishuna.modelapi.Bone;
import me.jishuna.modelapi.Quaternion;
import me.jishuna.modelapi.view.BoneView;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import team.unnamed.creative.base.Vector3Float;

public class BoneEntity extends ItemDisplay implements BoneView {
    private final MinecraftModelEntity parent;
    private final Bone bone;

    public long lastPx, lastPy, lastPz;
    public boolean dirtyColor;

    public BoneEntity(MinecraftModelEntity parent, Bone bone, int index) {
        super(EntityType.ITEM_DISPLAY, parent.level());
        this.parent = parent;
        this.bone = bone;

        initialize(index);
    }

    private void initialize(int index) {
        ItemStack item = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.setColor(Color.WHITE);
        meta.setCustomModelData(this.bone.customModelData());
        item.setItemMeta(meta);

        setItemStack(CraftItemStack.asNMSCopy(item));

        super.setItemTransform(ItemDisplayContext.HEAD);

        Transformation traansformation = new Transformation(this.bone.positionJOML(), new Quaternionf(), new Vector3f(1, 1, 1), new Quaternionf());
        super.setTransformation(traansformation);
    }

    void show(Consumer<Packet<ClientGamePacketListener>> sender) {
        sender.accept(new ClientboundAddEntityPacket(this));
        sender.accept(new ClientboundSetEntityDataPacket(super.getId(), super.getEntityData().getNonDefaultValues()));
    }

    @Override
    public Bone getBone() {
        return this.bone;
    }

    @Override
    public void color(Color color) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        boolean success = super.startRiding(entity, force);

        if (success) {
            setPosition(Vector3Float.ZERO);
        }
        return success;
    }

    @Override
    public void setPosition(Vector3Float position) {
        Vec3 root = this.parent.position();
        super.setPos(root.x, root.y, root.z);
    }

    @Override
    public void setRotation(Vector3Float rotation) {
        Quaternion quat = Quaternion.fromEuler(rotation);
        Quaternionf rotationQ = new Quaternionf(quat.x, quat.y, quat.z, quat.w);
        Transformation current = Display.createTransformation(super.getEntityData());

        Vector3f translation = bone.positionJOML();
        translation.rotate(rotationQ);

        Transformation traansformation = new Transformation(translation, rotationQ, current.getScale(), current.getRightRotation());
        super.setTransformation(traansformation);
    }

}
