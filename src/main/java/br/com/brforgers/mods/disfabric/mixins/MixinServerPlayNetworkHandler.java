package br.com.brforgers.mods.disfabric.mixins;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    @Final
    @Shadow private MinecraftServer server;
    @Shadow public ServerPlayerEntity player;
    @Shadow private int messageCooldown;
    @Shadow public abstract void sendPacket(Packet<?> packet);
    @Shadow public abstract void disconnect(Text reason);
    @Shadow protected abstract void executeCommand(String input);

//    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
//    private void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci){
//        if (this.player.getClientChatVisibility() != ChatVisibility.HIDDEN) {
//            String string = StringUtils.normalizeSpace(packet.getChatMessage());
//            if (!string.startsWith("/")) {
//                this.player.updateLastActionTime();
//                Text text = new TranslatableText("chat.type.text", this.player.getDisplayName(), string);
//                ServerChatCallback.EVENT.invoker().onServerChat(this.player, string, text);
//            }
//        }
//    }
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"), method = "onGameMessage", cancellable = true)
    private void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String message = StringUtils.normalizeSpace(packet.getChatMessage());
        Text text = new TranslatableText("chat.type.text", this.player.getDisplayName(), message);
        Optional<Text> eventResult = ServerChatCallback.EVENT.invoker().onServerChat(this.player, message, text);
        if (eventResult.isPresent()) {
            this.server.getPlayerManager().broadcastChatMessage(eventResult.get(), MessageType.CHAT, this.player.getUuid());
            ci.cancel();
        }
    }
}
