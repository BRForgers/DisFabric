package br.com.brforgers.mods.disfabric.mixins;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import net.minecraft.SharedConstants;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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

    /**
     * @author _
     */
    @Overwrite
    public void onGameMessage(ChatMessageC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, this.player.getServerWorld());
        if (this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID));
        } else {
            this.player.updateLastActionTime();
            String string = StringUtils.normalizeSpace(packet.getChatMessage());

            for(int i = 0; i < string.length(); ++i) {
                if (!SharedConstants.isValidChar(string.charAt(i))) {
                    this.disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                    return;
                }
            }

            if (string.startsWith("/")) {
                this.executeCommand(string);
            } else {
                Text text = new TranslatableText("chat.type.text", this.player.getDisplayName(), string);
                text = ServerChatCallback.EVENT.invoker().onServerChat(this.player, string, text);
                this.server.getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, this.player.getUuid());
            }

            this.messageCooldown += 20;
            if (this.messageCooldown > 200 && !this.server.getPlayerManager().isOperator(this.player.getGameProfile())) {
                this.disconnect(new TranslatableText("disconnect.spam"));
            }

        }
    }
}
