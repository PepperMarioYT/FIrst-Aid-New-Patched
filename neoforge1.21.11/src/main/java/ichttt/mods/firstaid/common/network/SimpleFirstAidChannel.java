package ichttt.mods.firstaid.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class SimpleFirstAidChannel {

    public void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(MessageApplyHealingItem.TYPE, MessageApplyHealingItem.STREAM_CODEC, MessageApplyHealingItem.Handler::onMessage);
        registrar.playToServer(MessageClientRequest.TYPE, MessageClientRequest.STREAM_CODEC, MessageClientRequest.Handler::onMessage);
    }

    public <T> void registerMessage(int id, Class<T> type, Object encoder, Object decoder, Object handler) {
    }

    public void send(Object target, Object message) {
    }

    public void sendToServer(CustomPacketPayload message) {
        ClientPacketDistributor.sendToServer(message);
    }
}
