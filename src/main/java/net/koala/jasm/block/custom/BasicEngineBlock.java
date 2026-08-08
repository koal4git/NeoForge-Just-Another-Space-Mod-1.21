package net.koala.jasm.block.custom;

import com.mojang.serialization.MapCodec;
import net.koala.jasm.component.EngineComponent;
import net.minecraft.world.level.block.Block;
public class BasicEngineBlock extends Block implements EngineComponent {

        public static final MapCodec<BasicEngineBlock> CODEC = simpleCodec(BasicEngineBlock::new);

        private final float thrust;
        private final float fuelConsumption;
        private final float specificImpulse;
        private final String fuelType;
        private final int engineTier;

        public BasicEngineBlock(Properties properties) {

            //hardcoded atm
            this(properties, 180.0f, 2.0f, 300.0f, "rocket_fuel", 1);
        }

        public BasicEngineBlock(Properties properties, float thrust, float fuelConsumption,
                                float specificImpulse, String fuelType, int engineTier) {
            super(properties);
            this.thrust = thrust;
            this.fuelConsumption = fuelConsumption;
            this.specificImpulse = specificImpulse;
            this.fuelType = fuelType;
            this.engineTier = engineTier;
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return CODEC;
        }

        @Override
        public float getThrust() {
            return thrust;
        }

        @Override
        public float getFuelConsumption() {
            return fuelConsumption;
        }

        @Override
        public float getSpecificImpulse() {
            return specificImpulse;
        }

        @Override
        public String getFuelType() {
            return fuelType;
        }

        @Override
        public int getEngineTier() {
            return engineTier;
        }
}

