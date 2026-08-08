package net.koala.jasm.block.custom;

import com.mojang.serialization.MapCodec;
import net.koala.jasm.block.entity.ControlPanelBlockEntity;
import net.koala.jasm.component.ControlComponent;
import net.koala.jasm.entity.ModEntities;
import net.koala.jasm.entity.RocketEntity;
import net.koala.jasm.structure.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

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

        ScanResult result = RocketScanner.scan((ServerLevel) level, pos);


        //switch on result. if done msesage stating ocount (added to gui in future)
        switch (result) {
            case ScanResult.Success success -> {

                RocketStructure struct = success.structure();
                RocketBlueprint blueprint = RocketBlueprint.fromStructure(struct);

                List<RelativeBlock> blocks = struct.getBlocks();

                for (RelativeBlock block : blocks) {
                    BlockPos worldPos = pos.offset(block.relPos());
                    level.removeBlock(worldPos, false);
                }

                RocketEntity rocket = new RocketEntity(ModEntities.ROCKET.get(), level);
                rocket.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                rocket.setBlueprint(blueprint);
                level.addFreshEntity(rocket);


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
