package net.koala.jasm.structure;

import net.koala.jasm.Config;
import net.koala.jasm.component.ModComponents;
import net.koala.jasm.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public final class RocketScanner {


    //just so player cant kill a server by right-clicking panel next to entire base
    private static final int SAFETY_HARD_CAP = 50000;

    private RocketScanner() {}

    public static ScanResult scan(ServerLevel level, BlockPos startPos) {

        Set<BlockPos> platformTouchPositions = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        List<RelativeBlock> blocks = new ArrayList<>();
        List<RelativeComponent> components = new ArrayList<>();

        queue.add(startPos);

        //running bounds. start centered on the origin block itself
        int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;

        int maxBlocksConfig = Config.MAX_ROCKET_BLOCKS.get();   // -1 means unlimited
        int maxWidthConfig = Config.MAX_ROCKET_WIDTH.get();
        int maxHeightConfig = Config.MAX_ROCKET_HEIGHT.get();

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            if (visited.contains(pos)) continue;
            visited.add(pos);

            if (level.isEmptyBlock(pos)) continue; // skip air



            // relpos = pos-starpos and shi
            BlockPos relPos = pos.subtract(startPos);

            BlockState state = level.getBlockState(pos);
            BlockEntity be = level.getBlockEntity(pos);
            var nbt = be != null ? be.saveWithoutMetadata(level.registryAccess()) : null;
            if (state.is(ModTags.Blocks.LAUNCH_PAD)) {
                platformTouchPositions.add(pos);
                continue;
            }


            blocks.add(new RelativeBlock(relPos, state, nbt));

            //ask modcomponents is thi s a engine/tank/control? ect
            ModComponents.get(state, be).ifPresent(component ->
                    components.add(new RelativeComponent(relPos, component)));


            //update minx,miny ect
            minX = Math.min(minX, relPos.getX());
            minY = Math.min(minY, relPos.getY());
            minZ = Math.min(minZ, relPos.getZ());
            maxY = Math.max(maxY, relPos.getY());
            maxX = Math.max(maxX, relPos.getX());
            maxZ = Math.max(maxZ, relPos.getZ());


            //early exit checks (must be in loop not after)

            if (blocks.size() > SAFETY_HARD_CAP) {
                return new ScanResult.Failure(ScanResult.Reason.TOO_MANY_BLOCKS, "safety limit ");
            }
            if (maxBlocksConfig >= 0 && blocks.size() > maxBlocksConfig) {
                return new ScanResult.Failure(ScanResult.Reason.TOO_MANY_BLOCKS, "max " + maxBlocksConfig);
            }

            if (maxWidthConfig > 0 && (maxX - minX) + 1 > maxWidthConfig) {
                return new ScanResult.Failure(ScanResult.Reason.TOO_WIDE, "wide " + maxWidthConfig);
            }

            if (maxWidthConfig > 0 && (maxZ - minZ) + 1 > maxWidthConfig) {
                return new ScanResult.Failure(ScanResult.Reason.TOO_WIDE, "wide " + maxWidthConfig);
            }

            if (maxHeightConfig > 0 && (maxY - minY) + 1 > maxHeightConfig) {
                return new ScanResult.Failure(ScanResult.Reason.TOO_TALL, "Tall " + maxHeightConfig);
            }




            //queue 6 neighbors up
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }

        }

        if (blocks.isEmpty()) {
            return new ScanResult.Failure(ScanResult.Reason.NO_BLOCKS_FOUND, "");
        }

        BlockPos minBounds = new BlockPos(minX, minY, minZ);
        BlockPos maxBounds = new BlockPos(maxX, maxY, maxZ);

        RocketStructure struct = new RocketStructure(blocks, components, startPos, minBounds, maxBounds);

        if (platformTouchPositions.isEmpty()) {
            return new ScanResult.Failure(ScanResult.Reason.NOT_ON_PLATFORM, "");
        }

        int platformTopY = platformTouchPositions.iterator().next().getY();

        for (RelativeBlock block : blocks) {
            int blockWorldY = block.relPos().getY() + startPos.getY();
            if (blockWorldY <= platformTopY) {
                return new ScanResult.Failure(ScanResult.Reason.BELOW_PLATFORM, "");
            }
        }

        return new ScanResult.Success(struct);

    }
}
