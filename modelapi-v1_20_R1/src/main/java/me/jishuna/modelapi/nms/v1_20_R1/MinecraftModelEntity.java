package me.jishuna.modelapi.nms.v1_20_R1;

import java.util.Map;
import java.util.Objects;

import org.joml.Vector3f;

import com.google.common.collect.ImmutableMap;

import me.jishuna.modelapi.Bone;
import me.jishuna.modelapi.Model;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class MinecraftModelEntity extends PathfinderMob {
    private final Model model;
    private final ImmutableMap<String, BoneEntity> bones;
    private final CraftModelEntity bukkitEntity;
    private final ModelEntityTracker tracker;
    private AttributeMap attributes;

    public float lastBodyRotation;
    
    float rotate = 0;

    public MinecraftModelEntity(EntityType<? extends PathfinderMob> type, Level world, Model model) {
        super(type, world);
        this.model = model;
        this.bukkitEntity = new CraftModelEntity(super.level().getCraftServer(), this);

        this.bones = instantiateBones();
        this.tracker = new ModelEntityTracker(this);

        setPos(0.0, 0.0, 0.0);
    }

    public MinecraftModelEntity(EntityType<? extends PathfinderMob> type, Level world) {
        this(type, world, null);
    }

    private ImmutableMap<String, BoneEntity> instantiateBones() {
        ImmutableMap.Builder<String, BoneEntity> bones = ImmutableMap.builder();

        for (Bone bone : model.bones().values()) {
            instantiateBone(bone, new Vector3f(), bones, 0);
        }
        return bones.build();
    }

    private void instantiateBone(Bone bone, Vector3f parentPosition, ImmutableMap.Builder<String, BoneEntity> into, int index) {
        Vector3f position = bone.positionJOML().add(parentPosition);
        BoneEntity entity = new BoneEntity(this, bone, index++);
        entity.setPosition(position);
        System.out.println("Entity pos: " + entity.position());
        into.put(bone.name(), entity);

        for (Bone child : bone.children().values()) {
            instantiateBone(child, position, into, index);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25D, Ingredient.of(Items.EMERALD), false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public AttributeMap getAttributes() {
        if (this.attributes == null) {
            this.attributes = new AttributeMap(Cow.createAttributes().build());
        }
        return this.attributes;
    }

    @Override
    public void tick() {
        ServerLevel level = ((ServerLevel) this.level());
        TrackedEntity tracked = level.getChunkSource().chunkMap.entityMap.get(super.getId());

        if (tracked == null) {
            return;
        }

        tracker.tick(tracked.seenBy);
        testRotate();
        super.tick();
    }

    public void testRotate() {
        rotate = (rotate + 1) % 180;
        Vector3f vector = new Vector3f(rotate, 0, 0);
        for (BoneEntity entity : this.bones.values()) {
            entity.setRotation(vector);
        }
    }

    @Override
    public void setYRot(float yaw) {
        super.setYRot(yaw);
        if (model != null) {
            for (var bone : model.bones().values()) {
                var entity = bones.get(bone.name());
                Objects.requireNonNull(entity, "Unknown bone");
                entity.setYRot(yaw);
            }
        }
    }

    @Override
    public CraftModelEntity getBukkitEntity() {
        return bukkitEntity;
    }

    public Map<String, BoneEntity> getBones() {
        return bones;
    }

}
