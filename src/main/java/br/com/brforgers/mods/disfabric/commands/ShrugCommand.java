package br.com.brforgers.mods.disfabric.commands;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShrugCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("shrug").then(argument("message", MessageArgumentType.message()).executes(context -> {
                ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
                PlayerManager playerManager = serverCommandSource.getServer().getPlayerManager();
                MessageArgumentType.getSignedMessage(context, "message", signedMessage -> {
                    Optional<Text> eventResult = ServerChatCallback.EVENT.invoker().onServerChat(serverCommandSource.getPlayer(), signedMessage.getContent().getString() + " ¯\\_(ツ)_/¯");
                    playerManager.broadcast(signedMessage.withUnsignedContent(eventResult.orElseGet(() -> Text.of(signedMessage.getContent().getString() + " ¯\\_(ツ)_/¯"))) , serverCommandSource, MessageType.params(MessageType.CHAT, serverCommandSource));
                });
                return 1;
            }
        )));
        dispatcher.register(literal("shrug").executes(context -> {
            ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
            PlayerManager playerManager = serverCommandSource.getServer().getPlayerManager();
            String raw = "¯\\_(ツ)_/¯";
            Optional<Text> eventResult = ServerChatCallback.EVENT.invoker().onServerChat(serverCommandSource.getPlayer(), raw);
            playerManager.broadcast(SignedMessage.ofUnsigned(raw).withUnsignedContent(eventResult.orElseGet(() -> Text.of(raw))) , serverCommandSource, MessageType.params(MessageType.CHAT, serverCommandSource));
            return 1;
            }
        ));
    }
}
