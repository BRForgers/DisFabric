package br.com.brforgers.mods.disfabric;

import br.com.brforgers.mods.disfabric.commands.ShrugCommand;
import br.com.brforgers.mods.disfabric.listeners.DiscordEventListener;
import br.com.brforgers.mods.disfabric.listeners.MinecraftEventListener;
import kong.unirest.Unirest;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Collections;

public class DisFabric implements DedicatedServerModInitializer {

    public static final String MOD_ID = "disfabric";
    public static Logger logger = LogManager.getLogger(MOD_ID);
    public static Configuration config;
    public static JDA jda;
    public static GuildMessageChannel textChannel;
    public static boolean stop = false;

    @Override
    public void onInitializeServer() {
        AutoConfig.register(Configuration.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(Configuration.class).getConfig();
        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(config.botToken).setHttpClient(new OkHttpClient.Builder()
                            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                            .build())
                    .addEventListeners(new DiscordEventListener())
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT);
            if(config.membersIntents){
                jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL);
            }
            DisFabric.jda = jdaBuilder.build();
            DisFabric.jda.awaitReady();
            DisFabric.textChannel = (GuildMessageChannel) DisFabric.jda.getGuildChannelById(config.channelId);
        } catch (InvalidTokenException ex) {
            jda = null;
            DisFabric.logger.error("Unable to login!", ex);
        } catch (InterruptedException ex) {
            jda = null;
            DisFabric.logger.error(ex);
        }
        if(jda != null) {
            if(!config.botGameStatus.isEmpty())
                jda.getPresence().setActivity(Activity.playing(config.botGameStatus));
            ServerLifecycleEvents.SERVER_STARTED.register((server) -> textChannel.sendMessage(DisFabric.config.texts.serverStarted).queue());
            ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
                stop = true;
                textChannel.sendMessage(DisFabric.config.texts.serverStopped).queue();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Unirest.shutDown();
                DisFabric.jda.shutdown();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            new MinecraftEventListener().init();
        }
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (environment.dedicated) {
                ShrugCommand.register(dispatcher);
            }
        });
    }
}
