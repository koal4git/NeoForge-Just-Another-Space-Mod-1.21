package net.koala.jasm.component;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface EngineComponent {


    public float getThrust() ;

    public float getFuelConsumption();

    public float getSpecificImpulse();

    public String getFuelType();

    public int getEngineTier();


    //thrust direction reltive to block direction
    default Direction getFacing(BlockState state) {
        return Direction.DOWN;
    }
}
