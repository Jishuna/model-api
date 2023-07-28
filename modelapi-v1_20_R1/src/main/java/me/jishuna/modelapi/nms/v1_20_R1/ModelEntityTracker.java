package me.jishuna.modelapi.nms.v1_20_R1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Particle;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ModelEntityTracker {
    private final Set<ServerPlayer> tracking = new HashSet<>();
    private final MinecraftModelEntity entity;

    public ModelEntityTracker(MinecraftModelEntity entity) {
        this.entity = entity;
    }

    public void tick(Set<ServerPlayerConnection> players) {
        removeOld(players);
        addNew(players);

        sendChanges();
    }

    private void sendChanges() {
        for (BoneEntity bone : entity.getBones().values()) {
            sendDirtyEntityData(bone);

            bone.getBukkitEntity().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, bone.getX(), bone.getY(), bone.getZ(), 1, 0, 0, 0, 0);

            Vec3 position = bone.position();
            long x = bone.getPositionCodec().encodeX(position);
            long y = bone.getPositionCodec().encodeY(position);
            long z = bone.getPositionCodec().encodeZ(position);

            boolean move = x != 0 || y != 0 || z != 0;
            boolean rotate = this.entity.yBodyRot != this.entity.lastBodyRotation;

            if (move && rotate) {
                byte yaw = (byte) Mth.floor(this.entity.yBodyRot * 256.0F / 360.0F);
                boolean big = x < Short.MIN_VALUE || x > Short.MAX_VALUE || y < Short.MIN_VALUE || y > Short.MAX_VALUE || z < Short.MIN_VALUE || z > Short.MAX_VALUE;

                if (big) {
                    sendToAll(new ClientboundTeleportEntityPacket(bone));
                    sendToAll(new ClientboundMoveEntityPacket.Rot(bone.getId(), yaw, (byte) 0, bone.onGround));
                } else {
                    sendToAll(new ClientboundMoveEntityPacket.PosRot(bone.getId(), (short) x, (short) y, (short) z, yaw, (byte) 0, entity.onGround()));
                }

                bone.getPositionCodec().setBase(position);
            } else if (move) {
                boolean big = x < Short.MIN_VALUE || x > Short.MAX_VALUE || y < Short.MIN_VALUE || y > Short.MAX_VALUE || z < Short.MIN_VALUE || z > Short.MAX_VALUE;

                if (big) {
                    sendToAll(new ClientboundTeleportEntityPacket(bone));
                } else {
                    sendToAll(new ClientboundMoveEntityPacket.Pos(bone.getId(), (short) x, (short) y, (short) z, entity.onGround()));
                }

                bone.getPositionCodec().setBase(position);
            } else if (rotate) {
                byte yaw = (byte) Mth.floor(this.entity.yBodyRot * 256.0F / 360.0F);
                sendToAll(new ClientboundMoveEntityPacket.Rot(bone.getId(), yaw, (byte) 0, bone.onGround));
            }
        }
        this.entity.lastBodyRotation = this.entity.yBodyRot;
    }

    private void addNew(Set<ServerPlayerConnection> players) {
        for (ServerPlayerConnection connection : players) {
            ServerPlayer player = connection.getPlayer();

            if (!tracking.contains(player)) {
                addTracker(player);
                this.tracking.add(player);
            }
        }
    }

    private void removeOld(Set<ServerPlayerConnection> players) {
        Iterator<ServerPlayer> iterator = this.tracking.iterator();
        while (iterator.hasNext()) {
            ServerPlayer player = iterator.next();
            ServerPlayerConnection connection = player.connection;

            if (!players.contains(connection)) {
                removeTracker(player);
                iterator.remove();
            }
        }
    }

    private void addTracker(ServerPlayer player) {
        if (this.entity.isRemoved()) {
            return;
        }
        List<Packet<ClientGamePacketListener>> list = new ArrayList<>();

        for (BoneEntity bone : entity.getBones().values()) {
            // bone.startRiding(this.entity, true);
            bone.show(list::add);
        }

        // list.add(new ClientboundSetPassengersPacket(this.entity));
        player.connection.send(new ClientboundBundlePacket(list));
    }

    private void removeTracker(ServerPlayer player) {
        Collection<BoneEntity> bones = entity.getBones().values();
        int[] ids = new int[bones.size()];
        int i = 0;
        for (BoneEntity bone : bones) {
            ids[i++] = bone.getId();
        }

        player.connection.send(new ClientboundRemoveEntitiesPacket(ids));
    }

    private void sendDirtyEntityData(BoneEntity bone) {
        SynchedEntityData data = bone.getEntityData();
        if (data.isDirty()) {
            sendToAll(new ClientboundSetEntityDataPacket(bone.getId(), data.getNonDefaultValues()));
        }
    }

    private void sendToAll(Packet<?> packet) {
        this.tracking.forEach(player -> player.connection.send(packet));
    }
}
