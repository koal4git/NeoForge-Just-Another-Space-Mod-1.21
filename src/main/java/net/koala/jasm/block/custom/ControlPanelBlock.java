package net.koala.jasm.block.custom;

import com.mojang.serialization.MapCodec;
import net.koala.jasm.block.entity.ControlPanelBlockEntity;
import net.koala.jasm.component.ControlComponent;
import net.koala.jasm.structure.RocketScanner;
import net.koala.jasm.structure.RocketStructure;
import net.koala.jasm.structure.ScanResult;
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
                RocketStructure structure = success.structure();
                player.sendSystemMessage(Component.literal(
                        "Blocks: " + structure.getBlockCount() +
                                " | Components: " + structure.getComponents().size()));
            }
            case ScanResult.Failure failure -> {
                player.sendSystemMessage(Component.literal(
                        "Scan failed: " + failure.reason() + " (" + failure.detail() + ")"));
            }
        }

        return InteractionResult.SUCCESS;
    }
}
