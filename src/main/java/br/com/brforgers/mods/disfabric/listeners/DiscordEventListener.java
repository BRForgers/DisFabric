package br.com.brforgers.mods.disfabric.listeners;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.Utils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DiscordEventListener extends ListenerAdapter {

    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        @SuppressWarnings("deprecation")
        Object gameInstance = FabricLoader.getInstance().getGameInstance();
        MinecraftServer server = null;
        if (gameInstance instanceof MinecraftServer) {
            server = (MinecraftServer) gameInstance;
        }
        if(e.getAuthor() != e.getJDA().getSelfUser() && !e.getAuthor().isBot() && e.getChannel().getId().equals(DisFabric.config.channelId) && server != null) {
            if(e.getMessage().getContentRaw().startsWith("!console") && Arrays.asList(DisFabric.config.adminsIds).contains(e.getAuthor().getId())) {
                String command = e.getMessage().getContentRaw().replace("!console ", "");
                server.getCommandManager().execute(server.getCommandSource(), command);

            }else if(e.getMessage().getContentRaw().startsWith("!online")) {
                List<ServerPlayerEntity> onlinePlayers = server.getPlayerManager().getPlayerList();
                StringBuilder playerList = new StringBuilder("```\n=============== Online Players (" + onlinePlayers.size() + ") ===============\n");
                for (ServerPlayerEntity player : onlinePlayers) {
                    playerList.append("\n").append(player.getEntityName());
                }
                playerList.append("```");
                e.getChannel().sendMessage(playerList.toString()).queue();

            }else if (e.getMessage().getContentRaw().startsWith("!tps")) {
                StringBuilder tpss = new StringBuilder("```\n============= TPS per loaded dimension ==============\n");
//                for (Integer id : server.DimensionManager.getIDs()) {
//                    double worldTickTime = Utils.mean(server.tick.worldTickTimes.get(id)) * 1.0E-6D;
//                    double worldTPS = Math.min(1000.0 / worldTickTime, 20);
//                    tpss.append("\n").append(DimensionManager.getProviderType(id).getName()).append(" (").append(id).append("): ").append(worldTPS).append("\n");
//                }
                tpss.append("//TODO\n");
                tpss.append("Server TPS: ");
                double serverTickTime = Utils.mean(server.lastTickLengths) * 1.0E-6D;
                tpss.append(Math.min(1000.0 / serverTickTime, 20));
                tpss.append("```");
                e.getChannel().sendMessage(tpss.toString()).queue();

            }else if(e.getMessage().getContentRaw().startsWith("!help")){
                String help = "```\n" + "=============== Commands ==============\n" +
                        "\n" + "!online: list server online players" +
                        "\n" + "!tps: shows loaded dimensions tps´s" +
                        "\n" + "!console <command>: executes commands in the server console (admins only)\n```";
                e.getChannel().sendMessage(help).queue();

            }else {
                LiteralText discord = new LiteralText("[Discord] ");
                discord.setStyle(discord.getStyle().withColor(TextColor.fromRgb(Objects.requireNonNull(e.getMember()).getColorRaw())));
                LiteralText msg = new LiteralText(" <" + e.getMember().getEffectiveName() + "> " + e.getMessage().getContentDisplay() + ((e.getMessage().getAttachments().size() > 0) ? "<att>" : "") + ((e.getMessage().getEmbeds().size() > 0) ? "<embed>" : ""));
                msg.setStyle(msg.getStyle().withColor(TextColor.fromFormatting(Formatting.WHITE)));
                server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(new LiteralText("").append(discord).append(msg),false));
            }
        }

    }
}