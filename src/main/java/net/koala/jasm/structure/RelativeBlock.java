package net.koala.jasm.structure;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public record RelativeBlock(BlockPos relPos, BlockState state, CompoundTag blockEntityData) {

    public BlockPos getRelPos() {
        return this.relPos;
    }

    public BlockState getBlockState() {
        return this.state;
    }

    public CompoundTag getBlockEntityData() {
        return this.blockEntityData;
    }


}
