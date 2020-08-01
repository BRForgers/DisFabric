package br.com.brforgers.mods.disfabric.utils;

import br.com.brforgers.mods.disfabric.DisFabric;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DiscordCommandOutput implements CommandOutput {

    StringBuilder outputString = new StringBuilder();
    Thread outputThread = null;
    long lastOutputMillis = 0;

    @Override
    public void sendSystemMessage(Text message, UUID senderUuid) {
        String messageString = message.getString();
        DisFabric.logger.info(messageString);
        long currentOutputMillis = System.currentTimeMillis();
        if((outputString.length() + messageString.length()) > 2000) {
            DisFabric.textChannel.sendMessage(outputString).queue();
        }else{
            outputString.append("> ").append(messageString).append("\n");
        }
        if((currentOutputMillis - lastOutputMillis) > 50) {
            outputThread = new Thread(() -> new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    DisFabric.textChannel.sendMessage(outputString).queue();
                    outputString = new StringBuilder();
                }
            }, 51));
            outputThread.start();
        }
        lastOutputMillis = currentOutputMillis;
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return true;
    }

    @Override
    public boolean shouldTrackOutput() {
        return true;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }
}
