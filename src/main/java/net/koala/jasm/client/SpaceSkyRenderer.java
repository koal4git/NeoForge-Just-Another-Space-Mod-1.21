package net.koala.jasm.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class SpaceSkyRenderer {

    private static final float DISTANCE = 100.0F;

    private static final float EARTH_SIZE = 18.0F;
    private static final float MOON_SIZE = 5.0F;

    private static final float EARTH_YAW = 35.0F;
    private static final float EARTH_PITCH = 20.0F;

    private static final float MOON_YAW = -55.0F;
    private static final float MOON_PITCH = -10.0F;

    private SpaceSkyRenderer() {
    }

    public static void render(PoseStack poseStack) {

        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        renderCelestialBody(
                poseStack,
                SpaceDimensionEffects.EARTH_TEXTURE,
                EARTH_SIZE,
                EARTH_YAW,
                EARTH_PITCH
        );

        renderCelestialBody(
                poseStack,
                SpaceDimensionEffects.MOON_TEXTURE,
                MOON_SIZE,
                MOON_YAW,
                MOON_PITCH
        );

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private static void renderCelestialBody(
            PoseStack poseStack,
            ResourceLocation texture,
            float size,
            float yaw,
            float pitch
    ) {
        poseStack.pushPose();

        poseStack.translate(0.0F, 0.0F, DISTANCE);

        poseStack.mulPose(
                com.mojang.math.Axis.YP.rotationDegrees(yaw)
        );

        poseStack.mulPose(
                com.mojang.math.Axis.XP.rotationDegrees(pitch)
        );

        RenderSystem.setShaderTexture(0, texture);

        Matrix4f matrix = poseStack.last().pose();

        Tesselator tesselator = Tesselator.getInstance();

        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR
        );

        float half = size / 2.0F;

        buffer.addVertex(matrix, -half, -half, 0.0F)
                .setUv(0.0F, 1.0F)
                .setColor(255, 255, 255, 255);

        buffer.addVertex(matrix, half, -half, 0.0F)
                .setUv(1.0F, 1.0F)
                .setColor(255, 255, 255, 255);

        buffer.addVertex(matrix, half, half, 0.0F)
                .setUv(1.0F, 0.0F)
                .setColor(255, 255, 255, 255);

        buffer.addVertex(matrix, -half, half, 0.0F)
                .setUv(0.0F, 0.0F)
                .setColor(255, 255, 255, 255);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();
    }
}