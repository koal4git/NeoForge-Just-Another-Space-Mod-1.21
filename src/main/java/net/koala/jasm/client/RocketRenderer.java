package net.koala.jasm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.koala.jasm.entity.RocketEntity;
import net.koala.jasm.structure.RelativeBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class RocketRenderer extends EntityRenderer<RocketEntity> {


    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity rocketEntity) {
        return TextureAtlas.LOCATION_BLOCKS; // placeholder
    }

    @Override
    public void render(RocketEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        //super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);


        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();


        for (RelativeBlock block : entity.getBlueprint().getBlocks()) {
            poseStack.pushPose();

            poseStack.translate(block.relPos().getX(), block.relPos().getY(), block.relPos().getZ());
            dispatcher.renderSingleBlock(block.state(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }
    }
}
