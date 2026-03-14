package ichttt.mods.firstaid.common.network;

import com.google.gson.JsonObject;
import ichttt.mods.firstaid.FirstAid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MessageSyncServerConfig implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageSyncServerConfig> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "sync_server_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSyncServerConfig> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32767),
            message -> message.payload,
            MessageSyncServerConfig::new);

    private final String payload;

    private MessageSyncServerConfig(String payload) {
        this.payload = payload;
    }

    public MessageSyncServerConfig(JsonObject payload) {
        this(payload.toString());
    }

    public String payload() {
        return payload;
    }

    @Override
    public CustomPacketPayload.Type<MessageSyncServerConfig> type() {
        return TYPE;
    }

}
