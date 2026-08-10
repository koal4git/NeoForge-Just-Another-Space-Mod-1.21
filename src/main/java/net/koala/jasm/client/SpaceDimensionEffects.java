package net.koala.jasm.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class SpaceDimensionEffects extends DimensionSpecialEffects {

    public static final ResourceLocation EARTH_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "justanotherspace",
                    "textures/environment/earth.png"
            );

    public static final ResourceLocation MOON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "justanotherspace",
                    "textures/environment/moon.png"
            );

    public SpaceDimensionEffects() {
        super(
                Float.NaN,
                false,
                SkyType.NONE,
                false,
                true
        );
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(
            Vec3 fogColor,
            float brightness
    ) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }
}