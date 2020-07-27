package br.com.brforgers.mods.disfabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerLeaveCallback {
    Event<PlayerLeaveCallback> EVENT = EventFactory.createArrayBacked(PlayerLeaveCallback.class, callbacks -> playerEntity -> {
        for (PlayerLeaveCallback callback : callbacks) {
            callback.onLeave(playerEntity);
        }
    });

    void onLeave(ServerPlayerEntity playerEntity);
}
