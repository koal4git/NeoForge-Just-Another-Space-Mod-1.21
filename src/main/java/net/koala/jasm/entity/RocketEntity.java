package net.koala.jasm.entity;

import net.koala.jasm.structure.RelativeBlock;
import net.koala.jasm.structure.RocketBlueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RocketEntity extends Entity {

    private final Map<UUID, BlockPos> seatAssignments = new HashMap<>();
    private boolean canExit = false;
    private boolean hasPassengers = false;

    private BlockPos originOffset = BlockPos.ZERO;

    private static final EntityDataAccessor<CompoundTag> DATA_BLUEPRINT =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private static final EntityDataAccessor<Boolean> DATA_CAN_EXIT =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);

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
    }

    public void assignSeat(UUID playerId, BlockPos relPos) {
        seatAssignments.put(playerId, relPos);
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
        BlockPos rawOffset = seatAssignments.getOrDefault(passenger.getUUID(), BlockPos.ZERO);
        BlockPos seatOffset = rawOffset.subtract(originOffset);
        double x = this.getX() + seatOffset.getX() + 0.5;
        double y = this.getY() + seatOffset.getY();
        double z = this.getZ() + seatOffset.getZ() + 0.5;
        callback.accept(passenger, x, y, z);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && !this.level().isClientSide()) {
            this.setCanExit(!this.canExit);
            player.sendSystemMessage(Component.literal("Rocket canExit = " + this.canExit));
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
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
            level.setBlock(worldPos, block.state(), 3);

            if (block.blockEntityData() != null) {
                BlockEntity be = level.getBlockEntity(worldPos);
                if (be != null) {
                    be.loadWithComponents(block.blockEntityData(), this.registryAccess());
                }
            }
        }

        this.discard();
    }
}