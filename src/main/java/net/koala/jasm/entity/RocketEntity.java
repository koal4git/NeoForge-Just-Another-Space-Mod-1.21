package net.koala.jasm.entity;

import net.koala.jasm.JasMod;
import net.koala.jasm.structure.RelativeBlock;
import net.koala.jasm.structure.RocketBlueprint;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class RocketEntity extends Entity {


    private double launchX;
    private double launchY;
    private double launchZ;
    private boolean hasSavedLaunch = false;


    private final Map<UUID, Vec3> seatAssignments = new HashMap<>();
    private boolean canExit = false;
    private String destinationDim = "";

    private static final double ASCEND_ACCELERATION = 0.02;
    private static final double MAX_ASCEND_SPEED = 0.6;
    private double verticalSpeed = 0.0;

    private static double DESCEND_ACCELERATION = 0.02;
    private static final double MAX_DESCEND_SPEED = -0.6;


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

    private static final double TRANSIT_ALTITUDE = 350.0;
    private boolean inTransit = false;

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

    @Override
    public boolean isControlledByLocalInstance() {
        return false;
    }



    //THIS CANNOT BE DELETED THIUS MAKES THE PHYSICS WORK
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 seatOffset = seatAssignments.getOrDefault(passenger.getUUID(), Vec3.ZERO);
        return new Vec3(this.getX() + seatOffset.x, this.getY() + seatOffset.y, this.getZ() + seatOffset.z);
    }

    private void handleFlightInput() {
        if (ascendInputHeld) {
            verticalSpeed = Math.min(verticalSpeed + ASCEND_ACCELERATION, MAX_ASCEND_SPEED);
        } else {
            verticalSpeed = Math.max(verticalSpeed - DESCEND_ACCELERATION, MAX_DESCEND_SPEED);
        }

        Vec3 movement = new Vec3(0, verticalSpeed, 0);
        this.move(MoverType.SELF, movement);
        this.setDeltaMovement(movement);

        if (this.verticalCollision) {
            verticalSpeed = 0.0;
        }
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
        this.ticks = tag.getDouble("FlightTicks");
        this.destinationDim = tag.getString("DestinationDim");
        this.launchX = tag.getDouble("LaunchX");
        this.launchY = tag.getDouble("LaunchY");
        this.launchZ = tag.getDouble("LaunchZ");
        this.hasSavedLaunch = tag.getBoolean("HasSavedLaunch");

        if (tag.contains("SeatAssignments")) {
            CompoundTag seatsTag = tag.getCompound("SeatAssignments");
            this.seatAssignments.clear();
            for (String uuidKey : seatsTag.getAllKeys()) {
                CompoundTag seat = seatsTag.getCompound(uuidKey);
                Vec3 offset = new Vec3(seat.getDouble("x"), seat.getDouble("y"), seat.getDouble("z"));
                this.seatAssignments.put(UUID.fromString(uuidKey), offset);
            }
            syncSeats();
        }


    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("Blueprint", blueprint.toNbt(this.registryAccess()));
        tag.putInt("OriginOffsetX", originOffset.getX());
        tag.putInt("OriginOffsetY", originOffset.getY());
        tag.putInt("OriginOffsetZ", originOffset.getZ());
        tag.putBoolean("CanExit", canExit);
        tag.putDouble("FlightTicks", this.ticks);
        tag.putString("DestinationDim", this.destinationDim);
        tag.putDouble("LaunchX", this.launchX);
        tag.putDouble("LaunchY", this.launchY);
        tag.putDouble("LaunchZ", this.launchZ);
        tag.putBoolean("HasSavedLaunch", this.hasSavedLaunch);

        CompoundTag seatsTag = new CompoundTag();
        for (Map.Entry<UUID, Vec3> entry : seatAssignments.entrySet()) {
            CompoundTag seat = new CompoundTag();
            seat.putDouble("x", entry.getValue().x);
            seat.putDouble("y", entry.getValue().y);
            seat.putDouble("z", entry.getValue().z);
            seatsTag.put(entry.getKey().toString(), seat);
        }
        tag.put("SeatAssignments", seatsTag);

    }

    public double ticks = 0.0;

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            handleFlightInput();
        }

        if (!this.level().isClientSide() && !hasSavedLaunch) {
            this.launchX = this.getX();
            this.launchY = this.getY();
            this.launchZ = this.getZ();
            this.hasSavedLaunch = true;
        }

        boolean isMoving = Math.abs(verticalSpeed) > 0.01;
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

        ticks = ticks + 1;
        changeDimension();

    }

    private void changeDimension() {
        if (this.level().isClientSide()) return;

        ResourceKey<Level> overworld = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"));
        ResourceKey<Level> etm = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "earth_to_moon"));
        ResourceKey<Level> moon = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "moon"));

        ResourceKey<Level> current = this.level().dimension();

        if (current == overworld && this.getY() >= TRANSIT_ALTITUDE) {
            this.destinationDim = "moon";
            this.ticks = 0;
            enterTransitDimension(etm);
        }

        else if (current == moon && this.getY() >= TRANSIT_ALTITUDE) {
            this.destinationDim = "overworld";
            this.ticks = 0;
            enterTransitDimension(etm);
        }

        else if (current == etm) {
            DESCEND_ACCELERATION = 0.0f;

            if (this.ticks > 140) {
                DESCEND_ACCELERATION = 0.02f;
                this.ticks = 0;

                if (this.destinationDim.equals("moon")) {
                    enterTransitDimension(moon);
                } else if (this.destinationDim.equals("overworld")) {
                    enterTransitDimension(overworld);
                }
            }
        }
    }

    private void enterTransitDimension(ResourceKey<Level> transitKey) {
        ServerLevel currentLevel = (ServerLevel) this.level();

        ServerLevel transitLevel = currentLevel.getServer().getLevel(transitKey);

        if (transitLevel == null) {
            return;
        }

        inTransit = true;

        List<Entity> passengers = new ArrayList<>(this.getPassengers());

        for (Entity passenger : passengers) {
            passenger.stopRiding();
        }

        String targetDimName = transitLevel.dimension().location().getPath();

        double spawnX;
        double spawnZ;

        if (targetDimName.equals("moon")) {
            spawnX = this.launchX * 5.0;
            spawnZ = this.launchZ * 5.0;
        }
        else if (targetDimName.equals("overworld")) {
            spawnX = this.launchX / 5;
            spawnZ = this.launchZ / 5;
        }
        else {
            spawnX = this.getX();
            spawnZ = this.getZ();
        }
        double spawnY = 200.0;

        Vec3 rocketVelocity = this.getDeltaMovement();

        DimensionTransition transition = new DimensionTransition(
                transitLevel,
                new Vec3(spawnX, spawnY, spawnZ),
                rocketVelocity,
                this.getYRot(),
                this.getXRot(),
                DimensionTransition.DO_NOTHING
        );

        Entity movedEntity = this.changeDimension(transition);

        if (!(movedEntity instanceof RocketEntity newRocket)) {
            inTransit = false;
            return;
        }

        newRocket.seatAssignments.clear();
        newRocket.seatAssignments.putAll(this.seatAssignments);
        newRocket.syncSeats();

        newRocket.setPos(spawnX, spawnY, spawnZ);
        newRocket.setDeltaMovement(rocketVelocity);

        newRocket.launchX = this.launchX;
        newRocket.launchY = this.launchY;
        newRocket.launchZ = this.launchZ;
        newRocket.hasSavedLaunch = this.hasSavedLaunch;
        newRocket.destinationDim = this.destinationDim;
        newRocket.ticks = this.ticks;
        newRocket.setPos(spawnX, spawnY, spawnZ);
        newRocket.setDeltaMovement(rocketVelocity);
        newRocket.setYRot(this.getYRot());
        newRocket.setXRot(this.getXRot());
        newRocket.inTransit = true;

        for (Entity passenger : passengers) {
            if (passenger instanceof ServerPlayer serverPlayer) {
                serverPlayer.teleportTo(
                        transitLevel,
                        spawnX,
                        spawnY,
                        spawnZ,
                        Set.of(),
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot()
                );

                passenger.startRiding(newRocket, true);
            } else {
                DimensionTransition passengerTransition = new DimensionTransition(
                        transitLevel,
                        new Vec3(spawnX, spawnY, spawnZ),
                        passenger.getDeltaMovement(),
                        passenger.getYRot(),
                        passenger.getXRot(),
                        DimensionTransition.DO_NOTHING
                );

                Entity movedPassenger = passenger.changeDimension(passengerTransition);

                if (movedPassenger != null) {
                    movedPassenger.startRiding(newRocket, true);
                }
            }
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
