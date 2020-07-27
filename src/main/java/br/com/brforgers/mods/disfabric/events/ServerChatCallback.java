package br.com.brforgers.mods.disfabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface ServerChatCallback {
    Event<ServerChatCallback> EVENT = EventFactory.createArrayBacked(ServerChatCallback.class, callbacks -> (playerEntity, rawMessage, message) -> {
        Text msg = message;
        for (ServerChatCallback callback : callbacks) {
            msg = callback.onServerChat(playerEntity, rawMessage, message);
        }
        return msg;
    });

    Text onServerChat(ServerPlayerEntity playerEntity, String rawMessage, Text message);
}
