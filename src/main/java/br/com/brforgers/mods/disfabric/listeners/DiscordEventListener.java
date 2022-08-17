package br.com.brforgers.mods.disfabric.listeners;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.utils.DiscordCommandOutput;
import br.com.brforgers.mods.disfabric.utils.MarkdownParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.ParseResults;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DiscordEventListener extends ListenerAdapter {

    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        MinecraftServer server = getServer();
        if(e.getAuthor() != e.getJDA().getSelfUser() && !e.getAuthor().isBot() && e.getChannel().getId().equals(DisFabric.config.channelId) && server != null) {
            if(e.getMessage().getContentRaw().startsWith("!console") && Arrays.asList(DisFabric.config.adminsIds).contains(e.getAuthor().getId())) {
                String command = e.getMessage().getContentRaw().replace("!console ", "");

                ServerCommandSource source = getDiscordCommandSource();
                ParseResults<ServerCommandSource> results = server.getCommandManager().getDispatcher().parse(command, source);

                server.getCommandManager().execute(results, command);

            } else if(e.getMessage().getContentRaw().startsWith("!online")) {
                List<ServerPlayerEntity> onlinePlayers = server.getPlayerManager().getPlayerList();
                StringBuilder playerList = new StringBuilder("```\n=============== Online Players (" + onlinePlayers.size() + ") ===============\n");
                for (ServerPlayerEntity player : onlinePlayers) {
                    playerList.append("\n").append(player.getEntityName());
                }
                playerList.append("```");
                e.getChannel().sendMessage(playerList.toString()).queue();

            } else if (e.getMessage().getContentRaw().startsWith("!tps")) {
//                StringBuilder tpss = new StringBuilder("```\n============== TPS per loaded dimension ==============\n");
//                for (Integer id : server.DimensionManager.getIDs()) {
//                    double worldTickTime = Utils.mean(server.tick.worldTickTimes.get(id)) * 1.0E-6D;
//                    double worldTPS = Math.min(1000.0 / worldTickTime, 20);
//                    tpss.append("\n").append(DimensionManager.getProviderType(id).getName()).append(" (").append(id).append("): ").append(worldTPS).append("\n");
//                }
//                tpss.append("Server TPS: ");
                StringBuilder tpss = new StringBuilder("Server TPS: ");
                double serverTickTime = MathHelper.average(server.lastTickLengths) * 1.0E-6D;
                tpss.append(Math.min(1000.0 / serverTickTime, 20));
//                tpss.append("```");
                e.getChannel().sendMessage(tpss.toString()).queue();

            } else if(e.getMessage().getContentRaw().startsWith("!help")){
                String help = """
                        ```
                        =============== Commands ===============

                        !online: list server online players
                        !tps: shows loaded dimensions tps´s
                        !console <command>: executes commands in the server console (admins only)
                        ```""";
                e.getChannel().sendMessage(help).queue();

            } else {
                MutableText discord = Text.literal(DisFabric.config.texts.coloredText.replace("%discordname%", Objects.requireNonNull(e.getMember()).getEffectiveName()).replace("%message%",e.getMessage().getContentDisplay().replace("§", DisFabric.config.texts.removeVanillaFormattingFromDiscord ? "&" : "§").replace("\n", DisFabric.config.texts.removeLineBreakFromDiscord ? " " : "\n") + ((e.getMessage().getAttachments().size() > 0) ? " <att>" : "") + ((e.getMessage().getEmbeds().size() > 0) ? " <embed>" : "")));
                discord.setStyle(discord.getStyle().withColor(TextColor.fromRgb(Objects.requireNonNull(e.getMember()).getColorRaw())));
                MutableText msg = Text.literal(DisFabric.config.texts.colorlessText.replace("%discordname%", Objects.requireNonNull(e.getMember()).getEffectiveName()).replace("%message%", MarkdownParser.parseMarkdown(e.getMessage().getContentDisplay().replace("§", DisFabric.config.texts.removeVanillaFormattingFromDiscord ? "&" : "§").replace("\n", DisFabric.config.texts.removeLineBreakFromDiscord ? " " : "\n") + ((e.getMessage().getAttachments().size() > 0) ? " <att>" : "") + ((e.getMessage().getEmbeds().size() > 0) ? " <embed>" : ""))));
                msg.setStyle(msg.getStyle().withColor(TextColor.fromFormatting(Formatting.WHITE)));
                server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(Text.literal("").append(discord).append(msg),false));
            }
        }

    }

    public ServerCommandSource getDiscordCommandSource(){
        ServerWorld serverWorld = Objects.requireNonNull(getServer()).getOverworld();
        return new ServerCommandSource(new DiscordCommandOutput(), serverWorld == null ? Vec3d.ZERO : Vec3d.of(serverWorld.getSpawnPos()), Vec2f.ZERO, serverWorld, 4, "Discord", Text.literal("Discord"), getServer(), null);
    }

    private MinecraftServer getServer(){
        @SuppressWarnings("deprecation")
        Object gameInstance = FabricLoader.getInstance().getGameInstance();
        if (gameInstance instanceof MinecraftServer) {
            return (MinecraftServer) gameInstance;
        }else {
            return null;
        }
    }
}