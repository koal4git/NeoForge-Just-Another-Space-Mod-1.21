package net.koala.jasm.entity;

import net.koala.jasm.structure.RelativeBlock;
import net.koala.jasm.structure.RocketBlueprint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
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

    private static final double ASCEND_ACCELERATION = 0.02;
    private static final double MAX_ASCEND_SPEED = 0.6;
    private static final double IDLE_DAMPING = 0.9;

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

    private boolean ascendInputHeld = false;
    private boolean hadPassengersPrev = false;

    public RocketEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
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
        if (this.level().isClientSide()) return;
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

    public float getCameraRadius() {
        return Math.max(renderWidth, renderHeight) / 2.0f;
    }

    public RocketBlueprint getBlueprint() {
        return blueprint;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof LivingEntity living ? living : null;
    }

    public void onClientInput(UUID playerId, boolean held) {
        LivingEntity controller = getControllingPassenger();
        if (controller != null && controller.getUUID().equals(playerId)) {
            this.ascendInputHeld = held;
            System.out.println("[JASM DEBUG] payload received, held=" + held + " vehicle=" + this);
        }
    }

    public void setAscendInputHeld(boolean held) {
        this.ascendInputHeld = held;
    }

    private void handleFlightInput() {
        Vec3 motion = this.getDeltaMovement();
        System.out.println("Flight before: ascendHeld=" + ascendInputHeld + " motionY(before)=" + motion.y + " passengers=" + this.getPassengers().size());
        double newY;
        if (ascendInputHeld) {
            if (this.onGround()) {
                this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
            }
            newY = Math.min(motion.y + ASCEND_ACCELERATION, MAX_ASCEND_SPEED);
        } else {
            newY = motion.y * IDLE_DAMPING;
            if (Math.abs(newY) < 0.005) newY = 0.0;
        }
        this.setDeltaMovement(motion.x, newY, motion.z);
        this.hasImpulse = true;
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 after = this.getDeltaMovement();
        System.out.println("Flight after: motionY(after)=" + after.y + " onGround=" + this.onGround() + " verticalCollision=" + this.verticalCollision);
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
        this.setNoGravity(true);
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
        if (!this.level().isClientSide()) {
            handleFlightInput();
        }
        boolean isMoving = this.getDeltaMovement().lengthSqr() > 1.0E-4;
        boolean shouldAllowExit = !isMoving;
        if (shouldAllowExit != this.canExit) {
            setCanExit(shouldAllowExit);
        }
        boolean hasPassengersNow = !getPassengers().isEmpty();
        if (!this.level().isClientSide() && hadPassengersPrev && !hasPassengersNow && this.canExit) {
            disassemble();
        }
        this.hadPassengersPrev = hasPassengersNow;
        if (getControllingPassenger() == null) {
            ascendInputHeld = false;
        }
    }

    private void disassemble() {
        if (!(this.level() instanceof ServerLevel)) return;
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
