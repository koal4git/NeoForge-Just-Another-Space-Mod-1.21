package net.koala.jasm;

import net.koala.jasm.block.ModBlocks;
import net.koala.jasm.block.entity.ModBlockEntities;
import net.koala.jasm.client.ModKeyMappings;
import net.koala.jasm.client.SpaceDimensionEffects;
import net.koala.jasm.client.SpaceSkyRenderer;
import net.koala.jasm.entity.RocketEntity;
import net.koala.jasm.entity.client.ChairRenderer;
import net.koala.jasm.entity.client.RocketRenderer;
import net.koala.jasm.entity.ModEntities;
import net.koala.jasm.fluid.ModFluidTypes;
import net.koala.jasm.fluid.ModFluids;
import net.koala.jasm.item.ModCreativeModeTabs;
import net.koala.jasm.item.ModItems;
import net.koala.jasm.network.AscendInputPayload;
import net.koala.jasm.util.ModSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static net.koala.jasm.client.SpaceSkyRenderEvent.MOON_DIMENSION;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(JasMod.MOD_ID)
public class JasMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "justanotherspace";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public JasMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);



        NeoForge.EVENT_BUS.register(this);

        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        ModEntities.register(modEventBus);



        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onRegisterPayloads);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModSetup::init);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(AscendInputPayload.TYPE, AscendInputPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    System.out.println("[JASM DEBUG] payload received, held=" + payload.held()
                            + " vehicle=" + (context.player() != null ? context.player().getVehicle() : "no player"));
                    if (context.player() instanceof ServerPlayer serverPlayer
                            && serverPlayer.getVehicle() instanceof RocketEntity rocket) {
                        rocket.setAscendInputHeld(payload.held());
                    }
                })
        );
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts

    }



    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (!(event.getEntityBeingMounted() instanceof RocketEntity rocket)) {
            return;
        }
        if (event.isMounting()) {
            return; // only gate dismounts, mounting is always fine
        }
        if (!rocket.canExit()) {
            event.setCanceled(true);
        }
    }



    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = JasMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {

        private static RegisterDimensionSpecialEffectsEvent event;

        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.ROCKET.get(), RocketRenderer::new);
            event.registerEntityRenderer(ModEntities.CHAIR_ENTITY.get(), ChairRenderer::new);
        }

        @SubscribeEvent
        static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(ModKeyMappings.ASCEND);
        }

        @SubscribeEvent
        static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
            ClientModEvents.event = event;
            event.register(
                    ResourceLocation.fromNamespaceAndPath(
                            JasMod.MOD_ID,
                            "space"
                    ),
                    new SpaceDimensionEffects()
            );
        }
    }

    @EventBusSubscriber(modid = JasMod.MOD_ID, value = Dist.CLIENT)
    static class ClientGameEvents {

        private static boolean lastAscendState = false;

        @SubscribeEvent
        static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }

            boolean riding = mc.player.getVehicle() instanceof RocketEntity;
            boolean holding = riding && ModKeyMappings.ASCEND.isDown();

            if (holding != lastAscendState) {
                lastAscendState = holding;
                PacketDistributor.sendToServer(new AscendInputPayload(holding));
            }
        }

        @SubscribeEvent
        static void onCalculateCameraDistance(CalculateDetachedCameraDistanceEvent event) {
            Entity cameraEntity = event.getCamera().getEntity();

            if (cameraEntity.getVehicle() instanceof RocketEntity rocket) {
                float baseDistance = event.getDistance();
                float extraForSize = rocket.getCameraRadius() * 1.5f;
                event.setDistance(baseDistance + extraForSize);
            }
        }

        @SubscribeEvent
        static void onRenderLevel(RenderLevelStageEvent event) {

            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();

            if (mc.level == null) {
                return;
            }

            if (!mc.level.dimension().equals(MOON_DIMENSION)) {
                return;
            }

            SpaceSkyRenderer.render(
                    event.getPoseStack()
            );
        }
    }
}
