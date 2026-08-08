package net.koala.jasm.structure;

import net.koala.jasm.component.SpacecraftComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Stream;

public class RocketStructure {


    private final List<RelativeBlock> blocks;
    private final List<RelativeComponent> components;
    private final BlockPos worldOrigin;
    private final BlockPos minBounds;
    private final BlockPos maxBounds;


    public RocketStructure(List<RelativeBlock> blocks, List<RelativeComponent> components,
                           BlockPos worldOrigin, BlockPos minBounds, BlockPos maxBounds) {
        this.blocks = List.copyOf(blocks);
        this.components = List.copyOf(components);
        this.worldOrigin = worldOrigin;
        this.minBounds = minBounds;
        this.maxBounds = maxBounds;
    }

    public List<RelativeBlock> getBlocks() { return blocks; }
    public List<RelativeComponent> getComponents() { return components; }
    public BlockPos getWorldOrigin() { return worldOrigin; }
    public BlockPos getMinBounds() { return minBounds; }
    public BlockPos getMaxBounds() { return maxBounds; }


    public int getBlockCount() {
        return blocks.size();
    }

    public int getWidth(){
        int spanX = (maxBounds.getX() - minBounds.getX() + 1);
        int spanZ = (maxBounds.getZ() - minBounds.getZ() + 1);
        return Math.max(spanX, spanZ);
    }

    public int getHeight() {
        return (maxBounds.getY() - minBounds.getY()) + 1;
    }




    //
    @SuppressWarnings("unchecked")
    public <T extends SpacecraftComponent> Stream<T> getComponentsOfType(Class<T> type) {
        return components.stream()
                .map(RelativeComponent::component)
                .filter(type::isInstance)
                .map(c -> (T) c);//safety cast

    }




}
