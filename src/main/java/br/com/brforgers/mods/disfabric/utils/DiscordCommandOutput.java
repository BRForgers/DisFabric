package br.com.brforgers.mods.disfabric.utils;

import br.com.brforgers.mods.disfabric.DisFabric;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;

import java.util.UUID;

public class DiscordCommandOutput implements CommandOutput {
    @Override
    public void sendSystemMessage(Text message, UUID senderUuid) {
        DisFabric.logger.info(message.getString());
        DisFabric.textChannel.sendMessage("> " + message.getString()).queue();
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
        return false;
    }
}
