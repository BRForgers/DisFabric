package br.com.brforgers.mods.disfabric.mixins;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
//import net.minecraft.text.TranslatableText;
//import net.minecraft.server.filter.TextStream.Message;
import java.util.Optional;

import net.minecraft.text.TranslatableTextContent;
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

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/server/PlayerManager.broadcast (Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"), method = "handleDecoratedMessage", cancellable = true)
    private void handleMessage(SignedMessage message, CallbackInfo ci) {
        String string = message.getContent().getString();
        String msg = StringUtils.normalizeSpace(string);
        Optional<Text> eventResult = ServerChatCallback.EVENT.invoker().onServerChat(this.player, msg);
        if (eventResult.isPresent()) {
            this.server.getPlayerManager().broadcast(message.withUnsignedContent(eventResult.get()), this.player, MessageType.params(MessageType.CHAT, this.player));
            ci.cancel();
        }
    }
}
