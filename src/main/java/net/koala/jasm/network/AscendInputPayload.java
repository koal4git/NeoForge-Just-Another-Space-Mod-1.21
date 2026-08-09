package net.koala.jasm.network;

import io.netty.buffer.ByteBuf;
import net.koala.jasm.JasMod;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AscendInputPayload(boolean held) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AscendInputPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "ascend_input"));

    public static final StreamCodec<ByteBuf, AscendInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, AscendInputPayload::held,
                    AscendInputPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}