package br.com.brforgers.mods.disfabric;

import br.com.brforgers.mods.disfabric.commands.ShrugCommand;
import br.com.brforgers.mods.disfabric.listeners.DiscordEventListener;
import br.com.brforgers.mods.disfabric.listeners.MinecraftEventListener;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Objects;

public class DisFabric implements DedicatedServerModInitializer {

    public static final String MOD_ID = "disfabric";
    public static Logger logger = LogManager.getLogger(MOD_ID);
    public static Configuration config;
    public static JDA jda;
    public static TextChannel textChannel;

    @Override
    public void onInitializeServer() {
        AutoConfig.register(Configuration.class, GsonConfigSerializer::new);
        logger.info("DisFabric >>>>>>>>>>>>>>>>>>>>>>>>> All others discord integrations mods");
        config = AutoConfig.getConfigHolder(Configuration.class).getConfig();
        try {
            DisFabric.jda = JDABuilder.createLight(config.botToken)
                    .addEventListeners(new DiscordEventListener())
                    .build();
            DisFabric.jda.awaitReady();
            DisFabric.textChannel = DisFabric.jda.getTextChannelById(config.channelId);
        } catch (LoginException ex) {
            jda = null;
            DisFabric.logger.error("Unable to login!", ex);
        } catch (InterruptedException ex) {
            jda = null;
            DisFabric.logger.error(ex);
        }
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            if(jda != null) {
                Objects.requireNonNull(DisFabric.jda.getTextChannelById(config.channelId)).sendMessage("**Server started!**").queue();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            if(jda != null) {
                Objects.requireNonNull(DisFabric.jda.getTextChannelById(config.channelId)).sendMessage("**Server stopped!**").queue();
                DisFabric.jda.shutdown();
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (dedicated) {
                ShrugCommand.register(dispatcher);
            }
        });
        new MinecraftEventListener().init();
    }
}