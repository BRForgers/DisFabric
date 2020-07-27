package br.com.brforgers.mods.disfabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerJoinCallback {
    Event<PlayerJoinCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinCallback.class, callbacks -> (connection, playerEntity) -> {
        for (PlayerJoinCallback callback : callbacks) {
            callback.onJoin(connection, playerEntity);
        }
    });

    void onJoin(ClientConnection connection, ServerPlayerEntity playerEntity);
}
