package br.com.brforgers.mods.disfabric.listeners;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.utils.Utils;
import br.com.brforgers.mods.disfabric.events.*;
import br.com.brforgers.mods.disfabric.utils.MarkdownParser;

import java.util.Optional;

import com.mashape.unirest.http.Unirest;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.json.JSONObject;

public class MinecraftEventListener {
    public void init() {
        ServerChatCallback.EVENT.register((playerEntity, rawMessage, message) -> {

            Pair<String, String> convertedPair = Utils.convertMentionsFromNames(rawMessage);
            if(DisFabric.config.isWebhookEnabled) {
                JSONObject body = new JSONObject();
                body.put("username", playerEntity.getEntityName());
                body.put("avatar_url", "https://mc-heads.net/avatar/" + playerEntity.getEntityName());
                body.put("content", convertedPair.getLeft());
                try {
                    Unirest.post(DisFabric.config.webhookURL).header("Content-Type", "application/json").body(body).asJsonAsync();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }else{
                DisFabric.textChannel.sendMessage(DisFabric.config.texts.playerMessage.replace("%playername%", playerEntity.getEntityName()).replace("%playermessage%", convertedPair.getLeft())).queue();
            }
            if (DisFabric.config.modifyChatMessages) {
                JSONObject newComponent = new JSONObject(Text.Serializer.toJson(message));
                newComponent.getJSONArray("with").put(1, convertedPair.getRight());
                String newComponentString = newComponent.toString();
                String markedDownString = MarkdownParser.parseMarkdown(newComponentString);
                if (!newComponentString.equals(markedDownString)) return Optional.of(Text.Serializer.fromJson(markedDownString));
            }
            return Optional.empty();
        });

        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            if(DisFabric.config.announceAdvancements && advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && playerEntity.getAdvancementTracker().getProgress(advancement).isDone()) {
                switch(advancement.getDisplay().getFrame()){
                    case GOAL:
                        DisFabric.textChannel.sendMessage(DisFabric.config.texts.advancementGoal.replace("%playername%", playerEntity.getEntityName()).replace("%advancement%",advancement.getDisplay().getTitle().getString())).queue();
                        break;
                    case TASK:
                        DisFabric.textChannel.sendMessage(DisFabric.config.texts.advancementTask.replace("%playername%", playerEntity.getEntityName()).replace("%advancement%",advancement.getDisplay().getTitle().getString())).queue();
                        break;
                    case CHALLENGE:
                        DisFabric.textChannel.sendMessage(DisFabric.config.texts.advancementChallenge.replace("%playername%", playerEntity.getEntityName()).replace("%advancement%",advancement.getDisplay().getTitle().getString())).queue();
                        break;
                }
            }
        });

        PlayerDeathCallback.EVENT.register((playerEntity, damageSource) -> {
            if(DisFabric.config.announceDeaths){
                DisFabric.textChannel.sendMessage(DisFabric.config.texts.deathMessage.replace("%deathmessage%",damageSource.getDeathMessage(playerEntity).getString()).replace("%playername%", playerEntity.getEntityName())).queue();
            }
        });

        PlayerJoinCallback.EVENT.register((connection, playerEntity) -> {
            if(DisFabric.config.announcePlayers){
                DisFabric.textChannel.sendMessage(DisFabric.config.texts.joinServer.replace("%playername%", playerEntity.getEntityName())).queue();
            }
        });

        PlayerLeaveCallback.EVENT.register((playerEntity) -> {
            if(DisFabric.config.announcePlayers){
                DisFabric.textChannel.sendMessage(DisFabric.config.texts.leftServer.replace("%playername%", playerEntity.getEntityName())).queue();
            }
        });
    }
}
