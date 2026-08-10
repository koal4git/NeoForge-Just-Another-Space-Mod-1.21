package net.koala.jasm.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = "justanotherspace"
)
public final class SpaceTransitionHandler {

    private static final ResourceKey<Level> MOON_DIMENSION =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(
                            "justanotherspace",
                            "moon"
                    )
            );

    private static final int TRANSITION_TIME = 100;

    private SpaceTransitionHandler() {
    }


    public static void startTransition(ServerPlayer player) {
        player.getPersistentData().putInt(
                "JASM_MoonTransition",
                TRANSITION_TIME
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!player.level().dimension().equals(Level.OVERWORLD)) {
            return;
        }

        int ticks = player.getPersistentData().getInt(
                "JASM_MoonTransition"
        );

        if (ticks <= 0) {
            return;
        }

        ticks--;

        player.getPersistentData().putInt(
                "JASM_MoonTransition",
                ticks
        );

        if (ticks == 0) {
            teleportToMoon(player);
        }
    }

    private static void teleportToMoon(ServerPlayer player) {

        if (!(player.level() instanceof ServerLevel overworld)) {
            return;
        }

        ServerLevel moon =
                overworld.getServer().getLevel(MOON_DIMENSION);

        if (moon == null) {
            System.err.println(
                    "[JASM] ERROR: justanotherspace:moon does not exist!"
            );
            return;
        }

        player.teleportTo(
                moon,
                0.5D,
                100.0D,
                0.5D,
                player.getYRot(),
                player.getXRot()
        );
    }
}