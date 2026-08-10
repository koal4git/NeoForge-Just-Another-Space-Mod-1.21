package net.koala.jasm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        modid = "justanotherspace",
        value = Dist.CLIENT
)
public final class SpaceTransitionHandler {

    private static int ticksRemaining = -1;

    private static final ResourceKey<Level> EARTH_DIMENSION =
            Level.OVERWORLD;

    private static final ResourceKey<Level> MOON_DIMENSION =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(
                            "justanotherspace",
                            "moon"
                    )
            );

    private SpaceTransitionHandler() {
    }


    public static void startTransition() {
        ticksRemaining = 100;
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {

        if (ticksRemaining < 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            ticksRemaining = -1;
            return;
        }


        if (!minecraft.level.dimension().equals(EARTH_DIMENSION)) {
            ticksRemaining = -1;
            return;
        }

        ticksRemaining--;

        if (ticksRemaining <= 0) {
            ticksRemaining = -1;


            return;
        }
    }

    public static int getTicksRemaining() {
        return ticksRemaining;
    }

    public static boolean isTransitioning() {
        return ticksRemaining >= 0;
    }
}