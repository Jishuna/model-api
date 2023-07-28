package me.jishuna.modelapi.nms.v1_20_R1;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

import me.jishuna.modelapi.Bone;
import me.jishuna.modelapi.Model;
import me.jishuna.modelapi.Vectors;
import me.jishuna.modelapi.animation.AnimationController;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import team.unnamed.creative.base.Vector3Float;

public class MinecraftModelEntity extends PathfinderMob {
    private final Model model;
    private final ImmutableMap<String, BoneEntity> bones;
    private final AnimationController animationController;
    private final CraftModelEntity bukkitEntity;
    private final ModelEntityTracker tracker;
    private AttributeMap attributes;

    public float lastBodyRotation;

    float rotate = 0;
    int frame = 0;

    int index;

    public MinecraftModelEntity(EntityType<? extends PathfinderMob> type, Level world, Model model) {
        super(type, world);
        this.setNoGravity(true);

        this.model = model;
        this.bukkitEntity = new CraftModelEntity(super.level().getCraftServer(), this);
        this.animationController = AnimationController.create(bukkitEntity);

        this.bones = instantiateBones();
        this.tracker = new ModelEntityTracker(this);

        setPos(0.0, 0.0, 0.0);

        this.animationController.queue(this.model.animations().get("walk"));
    }

    public MinecraftModelEntity(EntityType<? extends PathfinderMob> type, Level world) {
        this(type, world, null);
    }

    private ImmutableMap<String, BoneEntity> instantiateBones() {
        ImmutableMap.Builder<String, BoneEntity> bones = ImmutableMap.builder();

        for (Bone bone : model.bones().values()) {
            instantiateBone(bone, Vector3Float.ZERO, bones);
        }
        return bones.build();
    }

    private void instantiateBone(Bone bone, Vector3Float parentPosition, ImmutableMap.Builder<String, BoneEntity> into) {
        Vector3Float position = bone.position();// .add(parentPosition);
        BoneEntity entity = new BoneEntity(this, bone, index++);
        entity.setPosition(position);
        into.put(bone.name(), entity);

        for (Bone child : bone.children().values()) {
            instantiateBone(child, position, into);
        }
    }

    @Override
    protected void registerGoals() {
//        this.goalSelector.addGoal(0, new FloatGoal(this));
//        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
//        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25D, Ingredient.of(Items.EMERALD), false));
//        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
//        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
//        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public AttributeMap getAttributes() {
        if (this.attributes == null) {
            this.attributes = new AttributeMap(Cow.createAttributes().build());
        }
        return this.attributes;
    }

    @SuppressWarnings("resource")
    @Override
    public void tick() {
        ServerLevel level = ((ServerLevel) this.level());
        TrackedEntity tracked = level.getChunkSource().chunkMap.entityMap.get(super.getId());

        if (tracked == null) {
            return;
        }

        tracker.tick(tracked.seenBy);
        // if (frame++ % 10 == 0) {
        this.animationController.tick(Math.toRadians(super.getYRot()));
        // }
        super.tick();
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);

        if (model != null) { // model is null when setPos is called by the Entity constructor
            for (Bone bone : model.bones().values()) {
                teleportBoneAndChildren(0, bone, Vector3Float.ZERO);
            }
        }
    }

    @Override
    public void setYRot(float yaw) {
        if (model != null) {
            double radians = Math.toRadians(yaw);
            for (var bone : model.bones().values()) {
                teleportBoneAndChildren(radians, bone, Vector3Float.ZERO);
            }
        }
    }

    private void teleportBoneAndChildren(double yawRadians, Bone bone, Vector3Float parentPosition) {
        // location computing
        var position = bone.position();// .add(parentPosition);
        var rotatedPosition = Vectors.rotateAroundY(position, yawRadians);

        var entity = bones.get(bone.name());
        Objects.requireNonNull(entity, "Unknown bone");
        entity.setPosition(rotatedPosition);

        for (var child : bone.children().values()) {
            teleportBoneAndChildren(yawRadians, child, position);
        }
    }

    public AnimationController animationController() {
        return animationController;
    }

    public Model model() {
        return model;
    }

    @Override
    public CraftModelEntity getBukkitEntity() {
        return bukkitEntity;
    }

    public Map<String, BoneEntity> getBones() {
        return bones;
    }

}
