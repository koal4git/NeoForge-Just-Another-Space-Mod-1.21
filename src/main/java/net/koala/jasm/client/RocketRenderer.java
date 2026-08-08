package net.koala.jasm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.koala.jasm.entity.RocketEntity;
import net.koala.jasm.structure.RelativeBlock;
import net.koala.jasm.structure.RocketBlueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;

public class RocketRenderer extends EntityRenderer<RocketEntity> {

    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity rocketEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(RocketEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        RocketBlueprint blueprint = entity.getBlueprint();
        if (blueprint != null && !blueprint.getBlocks().isEmpty()) {

            BlockPos min = blueprint.getMinBounds();
            BlockPos max = blueprint.getMaxBounds();

            poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

            float offsetX = -((min.getX() + max.getX()) / 2.0f + 0.5f);
            float offsetZ = -((min.getZ() + max.getZ()) / 2.0f + 0.5f);
            float offsetY = -min.getY();

            poseStack.translate(offsetX, offsetY, offsetZ);

            // 4. Render all the static blocks
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            for (RelativeBlock block : blueprint.getBlocks()) {
                poseStack.pushPose();

                poseStack.translate(block.relPos().getX(), block.relPos().getY(), block.relPos().getZ());

                blockRenderer.renderSingleBlock(block.state(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            }
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}