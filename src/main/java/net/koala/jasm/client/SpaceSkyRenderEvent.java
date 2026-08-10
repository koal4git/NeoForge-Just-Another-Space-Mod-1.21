package net.koala.jasm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static net.koala.jasm.JasMod.MOD_ID;

@EventBusSubscriber(
        modid = MOD_ID,
        value = Dist.CLIENT
)
public final class SpaceSkyRenderEvent {

    public static final ResourceLocation MOON_DIMENSION =
            ResourceLocation.fromNamespaceAndPath(
                    MOD_ID,
                    "moon"
            );

    private SpaceSkyRenderEvent() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;

        if (level == null) {
            return;
        }

        if (!level.dimension().location().equals(MOON_DIMENSION)) {
            return;
        }

        SpaceSkyRenderer.render(
                event.getPoseStack()
        );
    }
}