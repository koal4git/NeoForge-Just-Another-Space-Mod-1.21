package net.koala.jasm.component;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public final class ModComponents {


    private static final Map<Block, BiFunction<BlockState, BlockEntity, SpacecraftComponent>> REGISTRY = new HashMap<>();

    public ModComponents() {}

    public static void register(Block block, BiFunction<BlockState, BlockEntity, SpacecraftComponent> factory) {
        REGISTRY.put(block, factory);
    }

    // for components that dont need blockstate or block entity to contstruct
    public static void register(Block block, SpacecraftComponent staticComponent) {
        REGISTRY.put(block, (state, be) -> staticComponent);
    }

    public static Optional<SpacecraftComponent> get(BlockState state, BlockEntity blockEntity) {
        BiFunction<BlockState, BlockEntity, SpacecraftComponent> factory = REGISTRY.get(state.getBlock());
        if (factory == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(factory.apply(state, blockEntity));
    }

}
