package net.koala.jasm.entity;

import net.koala.jasm.structure.RocketBlueprint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

import java.util.List;

public class RocketEntity extends Entity {

    // TODO: EntityDataAccessor<CompoundTag>, defined via
    // SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.COMPOUND_TAG)
    private static final EntityDataAccessor<CompoundTag> DATA_BLUEPRINT = /* fill in */ null;

    private RocketBlueprint blueprint = new RocketBlueprint(List.of(), BlockPos.ZERO, BlockPos.ZERO);
    private float renderWidth = 1.0f;
    private float renderHeight = 1.0f;

    public RocketEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // TODO: builder.define(DATA_BLUEPRINT, new CompoundTag());
    }

    public void setBlueprint(RocketBlueprint blueprint) {
        this.blueprint = blueprint;
        applyDimensionsFromBlueprint();

        // TODO: only the server should push data — client receives it via sync, not by
        // calling this method itself. Guard with: if (!this.level().isClientSide()) { ... }
        // Inside: this.entityData.set(DATA_BLUEPRINT, blueprint.toNbt(this.registryAccess()));
    }

    private void applyDimensionsFromBlueprint() {
        int widthX = blueprint.getMaxBounds().getX() - blueprint.getMinBounds().getX() + 1;
        int widthZ = blueprint.getMaxBounds().getZ() - blueprint.getMinBounds().getZ() + 1;
        this.renderWidth = Math.max(widthX, widthZ);
        this.renderHeight = blueprint.getMaxBounds().getY() - blueprint.getMinBounds().getY() + 1;
        this.refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (key.equals(DATA_BLUEPRINT)) {
            CompoundTag tag = this.entityData.get(DATA_BLUEPRINT);

            if (!tag.isEmpty()) {
                this.blueprint = RocketBlueprint.fromNbt(
                        tag,
                        this.registryAccess()
                );

                applyDimensionsFromBlueprint();
            }
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(renderWidth, renderHeight);
    }

    public RocketBlueprint getBlueprint() {
        return blueprint;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.blueprint = RocketBlueprint.fromNbt(tag.getCompound("Blueprint"), this.registryAccess());
        applyDimensionsFromBlueprint();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("Blueprint", blueprint.toNbt(this.registryAccess()));
    }

    @Override
    public void tick() {
        super.tick();
    }
}