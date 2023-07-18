package me.jishuna.modelapi.nms.v1_20_R1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;

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

            if (this.entity.yBodyRot != this.entity.lastBodyRotation) {
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
            bone.startRiding(this.entity, true);
            bone.show(list::add);
        }

        list.add(new ClientboundSetPassengersPacket(this.entity));
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
