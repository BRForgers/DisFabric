package br.com.brforgers.mods.disfabric.commands;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.text.Text;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShrugCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("shrug").then(argument("message", MessageArgumentType.message()).executes(context -> {
                MessageArgumentType.SignedMessage signedMessage = MessageArgumentType.getSignedMessage(context, "message");
                ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
                PlayerManager playerManager = serverCommandSource.getServer().getPlayerManager();
                signedMessage.decorate(serverCommandSource).thenAcceptAsync((decoratedMessage) -> {
                    Optional<Text> eventResult = ServerChatCallback.EVENT.invoker().onServerChat(serverCommandSource.getPlayer(), decoratedMessage.raw().getContent().getString() + " ¯\\_(ツ)_/¯");
                    playerManager.broadcast(FilteredMessage.permitted(decoratedMessage.raw().withUnsigned(eventResult.orElseGet(() -> Text.of(decoratedMessage.raw().getContent().getString() + " ¯\\_(ツ)_/¯")))) , serverCommandSource, MessageType.CHAT);
                }, serverCommandSource.getServer());
                return 1;
            }
        )));
        dispatcher.register(literal("shrug").executes(context -> {
            ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
            PlayerManager playerManager = serverCommandSource.getServer().getPlayerManager();
            Optional<Text> eventResult = ServerChatCallback.EVENT.invoker().onServerChat(serverCommandSource.getPlayer(), "¯\\_(ツ)_/¯");
            playerManager.broadcast(FilteredMessage.permitted(SignedMessage.of(eventResult.orElseGet(() -> Text.of("¯\\_(ツ)_/¯")))) , serverCommandSource, MessageType.CHAT);
            return 1;
            }
        ));
    }
}
