package br.com.brforgers.mods.disfabric;

import br.com.brforgers.mods.disfabric.commands.ShrugCommand;
import br.com.brforgers.mods.disfabric.listeners.DiscordEventListener;
import br.com.brforgers.mods.disfabric.listeners.MinecraftEventListener;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;

public class DisFabric implements DedicatedServerModInitializer {

    public static final String MOD_ID = "disfabric";
    public static Logger logger = LogManager.getLogger(MOD_ID);
    public static Configuration config;
    public static JDA jda;
    public static TextChannel textChannel;

    public static boolean stop = false;

    @Override
    public void onInitializeServer() {
        AutoConfig.register(Configuration.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(Configuration.class).getConfig();
        try {
            if(config.membersIntents){
                DisFabric.jda = JDABuilder.createDefault(config.botToken)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(new DiscordEventListener())
                    .build();
            } else {
                DisFabric.jda = JDABuilder.createDefault(config.botToken)
                    .addEventListeners(new DiscordEventListener())
                    .build();
            }
            DisFabric.jda.awaitReady();
            DisFabric.textChannel = DisFabric.jda.getTextChannelById(config.channelId);
        } catch (LoginException ex) {
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
                //logger.error(stop);
                textChannel.sendMessage(DisFabric.config.texts.serverStopped).queue();
                DisFabric.jda.shutdown();
                OkHttpClient client = jda.getHttpClient();
                client.connectionPool().evictAll();
                client.dispatcher().executorService().shutdown();
            });
            //ServerLifecycleEvents.SERVER_STOPPED.register((server) -> DisFabric.jda.shutdownNow());
            new MinecraftEventListener().init();
        }
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (dedicated) {
                ShrugCommand.register(dispatcher);
            }
        });
    }
}
