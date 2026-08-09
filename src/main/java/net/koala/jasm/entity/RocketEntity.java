package net.koala.jasm.entity;

import net.koala.jasm.structure.RelativeBlock;
import net.koala.jasm.structure.RocketBlueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RocketEntity extends Entity {

    private final Map<UUID, Vec3> seatAssignments = new HashMap<>();
    private boolean canExit = false;
    private boolean hasPassengers = false;

    private BlockPos originOffset = BlockPos.ZERO;

    private static final EntityDataAccessor<CompoundTag> DATA_BLUEPRINT =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private static final EntityDataAccessor<Boolean> DATA_CAN_EXIT =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<CompoundTag> DATA_SEATS =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private RocketBlueprint blueprint = new RocketBlueprint(List.of(), BlockPos.ZERO, BlockPos.ZERO);

    private float renderWidth = 1.0f;
    private float renderHeight = 1.0f;

    public RocketEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLUEPRINT, new CompoundTag());
        builder.define(DATA_CAN_EXIT, false);
        builder.define(DATA_SEATS, new CompoundTag());
    }

    public void assignSeat(UUID playerId, Vec3 offset) {
        seatAssignments.put(playerId, offset);
        syncSeats();
    }

    private void syncSeats() {
        if (this.level().isClientSide()) {
            return;
        }
        CompoundTag root = new CompoundTag();
        for (Map.Entry<UUID, Vec3> entry : seatAssignments.entrySet()) {
            CompoundTag seat = new CompoundTag();
            seat.putDouble("x", entry.getValue().x);
            seat.putDouble("y", entry.getValue().y);
            seat.putDouble("z", entry.getValue().z);
            root.put(entry.getKey().toString(), seat);
        }
        this.entityData.set(DATA_SEATS, root);
    }

    public void setOriginOffset(BlockPos offset) {
        this.originOffset = offset;
    }

    public boolean canExit() {
        return canExit;
    }

    public void setCanExit(boolean value) {
        this.canExit = value;

        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_CAN_EXIT, value);
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        Vec3 seatOffset = seatAssignments.getOrDefault(passenger.getUUID(), Vec3.ZERO);
        double x = this.getX() + seatOffset.x;
        double y = this.getY() + seatOffset.y;
        double z = this.getZ() + seatOffset.z;
        callback.accept(passenger, x, y, z);
    }

    public void setBlueprint(RocketBlueprint blueprint) {
        this.blueprint = blueprint;
        applyDimensionsFromBlueprint();

        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_BLUEPRINT, blueprint.toNbt(this.registryAccess()));
        }
    }

    private void applyDimensionsFromBlueprint() {
        int widthX = blueprint.getMaxBounds().getX() - blueprint.getMinBounds().getX() + 1;
        int widthZ = blueprint.getMaxBounds().getZ() - blueprint.getMinBounds().getZ() + 1;
        int height = blueprint.getMaxBounds().getY() - blueprint.getMinBounds().getY() + 1;

        this.renderWidth = Math.max(1, Math.max(widthX, widthZ));
        this.renderHeight = Math.max(1, height);
        this.refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (key.equals(DATA_BLUEPRINT)) {
            CompoundTag tag = this.entityData.get(DATA_BLUEPRINT);
            if (!tag.isEmpty()) {
                this.blueprint = RocketBlueprint.fromNbt(tag, this.registryAccess());
                applyDimensionsFromBlueprint();
            }
        }

        if (key.equals(DATA_CAN_EXIT)) {
            this.canExit = this.entityData.get(DATA_CAN_EXIT);
        }

        if (key.equals(DATA_SEATS)) {
            CompoundTag root = this.entityData.get(DATA_SEATS);
            seatAssignments.clear();
            for (String uuidKey : root.getAllKeys()) {
                CompoundTag seat = root.getCompound(uuidKey);
                Vec3 offset = new Vec3(seat.getDouble("x"), seat.getDouble("y"), seat.getDouble("z"));
                seatAssignments.put(UUID.fromString(uuidKey), offset);
            }
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.renderWidth, this.renderHeight);
    }

    public void updateDimensions() {
        this.refreshDimensions();
    }

    public RocketBlueprint getBlueprint() {
        return blueprint;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        CompoundTag blueprintTag = tag.getCompound("Blueprint");
        if (!blueprintTag.isEmpty()) {
            this.blueprint = RocketBlueprint.fromNbt(blueprintTag, this.registryAccess());
            applyDimensionsFromBlueprint();
            this.entityData.set(DATA_BLUEPRINT, blueprintTag);
        }

        this.originOffset = new BlockPos(tag.getInt("OriginOffsetX"), tag.getInt("OriginOffsetY"), tag.getInt("OriginOffsetZ"));
        this.canExit = tag.getBoolean("CanExit");
        this.entityData.set(DATA_CAN_EXIT, this.canExit);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("Blueprint", blueprint.toNbt(this.registryAccess()));

        tag.putInt("OriginOffsetX", originOffset.getX());
        tag.putInt("OriginOffsetY", originOffset.getY());
        tag.putInt("OriginOffsetZ", originOffset.getZ());
        tag.putBoolean("CanExit", canExit);
    }

    @Override
    public void tick() {
        super.tick();

        boolean isMoving = this.getDeltaMovement().lengthSqr() > 1.0E-4;
        boolean shouldAllowExit = !isMoving;
        if (shouldAllowExit != this.canExit) {
            setCanExit(shouldAllowExit);
        }

        if (!this.level().isClientSide() && canExit && hasPassengers && getPassengers().isEmpty()) {
            disassemble();
        }

        hasPassengers = !getPassengers().isEmpty();
    }

    private void disassemble() {
        ServerLevel level = (ServerLevel) this.level();
        BlockPos origin = this.blockPosition().subtract(originOffset);

        for (RelativeBlock block : blueprint.getBlocks()) {
            BlockPos worldPos = origin.offset(block.relPos());
            level.setBlock(worldPos, block.state(), 2);

            if (block.blockEntityData() != null) {
                BlockEntity be = level.getBlockEntity(worldPos);
                if (be != null) {
                    be.loadWithComponents(block.blockEntityData(), this.registryAccess());
                }
            }
        }

        for (RelativeBlock block : blueprint.getBlocks()) {
            BlockPos worldPos = origin.offset(block.relPos());
            level.updateNeighborsAt(worldPos, block.state().getBlock());
        }

        this.discard();
    }
}