package net.koala.jasm.block.custom;

import com.mojang.serialization.MapCodec;
import net.koala.jasm.block.entity.ControlPanelBlockEntity;
import net.koala.jasm.component.ControlComponent;
import net.koala.jasm.entity.ModEntities;
import net.koala.jasm.entity.RocketEntity;
import net.koala.jasm.entity.custom.ChairEntity;
import net.koala.jasm.structure.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ControlPanelBlock extends BaseEntityBlock implements ControlComponent {

    public static final MapCodec<ControlPanelBlock> CODEC = simpleCodec(ControlPanelBlock::new);

    public ControlPanelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControlPanelBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player.getVehicle() instanceof ChairEntity)) {
            player.sendSystemMessage(Component.literal("You must be seated to use the control panel."));
            return InteractionResult.SUCCESS;
        }

        ScanResult result = RocketScanner.scan((ServerLevel) level, pos);

        switch (result) {
            case ScanResult.Success success -> {

                RocketStructure struct = success.structure();

                // find chairs and capture riders + real world seat centers before removing chairs
                List<Player> pendingRiders = new ArrayList<>();
                List<Vec3> pendingSeatWorldCenters = new ArrayList<>();

                for (RelativeBlock block : struct.getBlocks()) {
                    if (block.state().getBlock() instanceof ChairBlock) {
                        BlockPos worldPos = pos.offset(block.relPos());
                        Vec3 seatWorldCenter = new Vec3(worldPos.getX() + 0.5, worldPos.getY(), worldPos.getZ() + 0.5);

                        List<ChairEntity> chairs = level.getEntitiesOfClass(ChairEntity.class, new AABB(worldPos));

                        for (ChairEntity chair : chairs) {
                            for (Entity rider : List.copyOf(chair.getPassengers())) {
                                if (rider instanceof Player ridingPlayer) {
                                    pendingRiders.add(ridingPlayer);
                                    pendingSeatWorldCenters.add(seatWorldCenter);
                                }
                            }
                            chair.discard();
                        }
                    }
                }

                RocketBlueprint blueprint = RocketBlueprint.fromStructure(struct);

                List<BlockPos> removedPositions = new ArrayList<>();
                for (RelativeBlock block : struct.getBlocks()) {
                    BlockPos worldPos = pos.offset(block.relPos());
                    level.removeBlockEntity(worldPos);
                    level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), 2);
                    removedPositions.add(worldPos);
                }
                for (BlockPos worldPos : removedPositions) {
                    level.updateNeighborsAt(worldPos, Blocks.AIR);
                }

                BlockPos min = blueprint.getMinBounds();
                BlockPos max = blueprint.getMaxBounds();

                double spawnX = pos.getX() + ((min.getX() + max.getX()) / 2.0) + 0.5;
                double spawnY = pos.getY() + min.getY();
                double spawnZ = pos.getZ() + ((min.getZ() + max.getZ()) / 2.0) + 0.5;

                RocketEntity rocket = new RocketEntity(ModEntities.ROCKET.get(), level);
                rocket.setPos(spawnX, spawnY, spawnZ);

                // remember how far spawn drifted from the control panel, for disassemble() later
                BlockPos originOffset = rocket.blockPosition().subtract(pos);
                rocket.setOriginOffset(originOffset);

                rocket.setBlueprint(blueprint);
                level.addFreshEntity(rocket);

                // remount riders at their real seat offset relative to the rocket's actual spawn point
                Vec3 rocketSpawnPos = new Vec3(spawnX, spawnY, spawnZ);
                for (int i = 0; i < pendingRiders.size(); i++) {
                    Player ridingPlayer = pendingRiders.get(i);
                    Vec3 seatOffset = pendingSeatWorldCenters.get(i).subtract(rocketSpawnPos);

                    rocket.assignSeat(ridingPlayer.getUUID(), seatOffset);
                    ridingPlayer.startRiding(rocket, true);
                }

                player.sendSystemMessage(Component.literal(
                        "Blocks: " + struct.getBlockCount() +
                                " | Components: " + struct.getComponents().size()));
            }
            case ScanResult.Failure failure -> {
                player.sendSystemMessage(Component.literal(
                        "Scan failed: " + failure.reason() + " (" + failure.detail() + ")"));
            }
        }

        return InteractionResult.SUCCESS;
    }
}