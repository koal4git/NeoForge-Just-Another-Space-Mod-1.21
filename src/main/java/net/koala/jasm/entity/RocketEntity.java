package net.koala.jasm.entity;

import net.koala.jasm.structure.RocketBlueprint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class RocketEntity extends Entity {

    private RocketBlueprint blueprint = new RocketBlueprint(List.of(), BlockPos.ZERO, BlockPos.ZERO);
    private float renderWidth = 1.0f;
    private float renderHeight = 1.0f;
    public RocketEntity(EntityType<?> type, Level level) {
        super(type, level);
    }


    public void setBlueprint(RocketBlueprint blueprint) {
        this.blueprint = blueprint;

        int widthX = (blueprint.getMaxBounds().getX() - blueprint.getMinBounds().getX() + 1);
        int widthZ = (blueprint.getMaxBounds().getZ() - blueprint.getMinBounds().getZ() + 1);


        this.renderWidth =Math.max(widthX, widthZ);

        this.renderHeight = blueprint.getMaxBounds().getY() - blueprint.getMinBounds().getY() + 1;
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(renderWidth, renderHeight);
    }

    public RocketBlueprint getBlueprint() {
        return blueprint;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        //no network field stuff atm
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.blueprint = RocketBlueprint.fromNbt(compoundTag.getCompound("Blueprint"), this.registryAccess());


    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("Blueprint", blueprint.toNbt(this.registryAccess()));
    }

    @Override
    public void tick() {
        super.tick();

        //physicsw ill go here
    }
}
