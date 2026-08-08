package net.koala.jasm.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RocketBlueprint {

    private final List<RelativeBlock> blocks;
    private final BlockPos minBounds;
    private final BlockPos maxBounds;

    public RocketBlueprint(List<RelativeBlock> blocks, BlockPos minBounds, BlockPos maxBounds) {
        this.blocks = List.copyOf(blocks);
        this.minBounds = minBounds;
        this.maxBounds = maxBounds;
    }

    public static RocketBlueprint fromStructure(RocketStructure struct) {
        return new RocketBlueprint(struct.getBlocks(), struct.getMinBounds(), struct.getMaxBounds());
    }

    public List<RelativeBlock> getBlocks() {
        return blocks;
    }

    public BlockPos getMinBounds() {
        return minBounds;
    }

    public BlockPos getMaxBounds() {
        return maxBounds;
    }

    public CompoundTag toNbt(HolderLookup.Provider registries) {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();

        for (RelativeBlock block : blocks) {
            CompoundTag entry = new CompoundTag();

            entry.put("pos", NbtUtils.writeBlockPos(block.relPos()));
            entry.put("state", NbtUtils.writeBlockState(block.state()));

            if (block.blockEntityData() != null) {
                entry.put("be", block.blockEntityData());
            }

            list.add(entry);
        }

        root.put("Blocks", list);
        root.put("MinBounds", NbtUtils.writeBlockPos(minBounds));
        root.put("MaxBounds", NbtUtils.writeBlockPos(maxBounds));
        return root;
    }

    public static RocketBlueprint fromNbt(CompoundTag root, HolderLookup.Provider registries) {
        List<RelativeBlock> blocks = new ArrayList<>();
        ListTag list = root.getList("Blocks", ListTag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);

            BlockPos relPos = NbtUtils.readBlockPos(entry, "pos").orElseThrow();
            BlockState state = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), entry.getCompound("state"));
            CompoundTag beData = entry.contains("be") ? entry.getCompound("be") : null;

            blocks.add(new RelativeBlock(relPos, state, beData));
        }

        BlockPos minBounds = NbtUtils.readBlockPos(root, "MinBounds").orElse(BlockPos.ZERO);
        BlockPos maxBounds = NbtUtils.readBlockPos(root, "MaxBounds").orElse(BlockPos.ZERO);

        return new RocketBlueprint(blocks, minBounds, maxBounds);
    }
}