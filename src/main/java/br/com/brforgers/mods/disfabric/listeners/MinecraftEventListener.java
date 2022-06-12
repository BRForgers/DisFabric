package br.com.brforgers.mods.disfabric.listeners;

import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.utils.Utils;
import br.com.brforgers.mods.disfabric.events.*;
import br.com.brforgers.mods.disfabric.utils.MarkdownParser;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public class MinecraftEventListener {
    public void init() {
//        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
//            if (!DisFabric.stop && sender != null) {
//                Pair<String, String> convertedPair = Utils.convertMentionsFromNames(message.raw().getContent().getString());
//                if (DisFabric.config.isWebhookEnabled) {
//                    JSONObject body = new JSONObject();
//                    body.put("username", sender.getEntityName());
//                    body.put("avatar_url", "https://mc-heads.net/avatar/" + (DisFabric.config.useUUIDInsteadNickname ? sender.getUuid() : sender.getEntityName()));
//                    JSONObject allowed_mentions = new JSONObject();
//                    allowed_mentions.put("parse", new String[]{"users", "roles"});
//                    body.put("allowed_mentions", allowed_mentions);
//                    body.put("content", convertedPair.getLeft());
//                    try {
//                        Unirest.post(DisFabric.config.webhookURL).header("Content-Type", "application/json").body(body).asJsonAsync();
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                } else {
//                    DisFabric.textChannel.sendMessage(DisFabric.config.texts.playerMessage.replace("%playername%", MarkdownSanitizer.escape(sender.getEntityName())).replace("%playermessage%", convertedPair.getLeft())).queue();
//                }
//            }
//        });
//
//        ServerMessageDecoratorEvent.EVENT.register(
//                // Used to provide better compatibility across mods
//                ServerMessageDecoratorEvent.STYLING_PHASE,
//                (sender, message) -> {
//                    if (!DisFabric.stop && sender != null) {
//                        Pair<String, String> convertedPair = Utils.convertMentionsFromNames(message.getContent().toString());
//                        if (DisFabric.config.isWebhookEnabled) {
//                            JSONObject body = new JSONObject();
//                            body.put("username", sender.getEntityName());
//                            body.put("avatar_url", "https://mc-heads.net/avatar/" + (DisFabric.config.useUUIDInsteadNickname ? sender.getUuid() : sender.getEntityName()));
//                            JSONObject allowed_mentions = new JSONObject();
//                            allowed_mentions.put("parse", new String[]{"users", "roles"});
//                            body.put("allowed_mentions", allowed_mentions);
//                            body.put("content", convertedPair.getLeft());
//                            try {
//                                Unirest.post(DisFabric.config.webhookURL).header("Content-Type", "application/json").body(body).asJsonAsync();
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
//                        } else {
//                            DisFabric.textChannel.sendMessage(DisFabric.config.texts.playerMessage.replace("%playername%", MarkdownSanitizer.escape(sender.getEntityName())).replace("%playermessage%", convertedPair.getLeft())).queue();
//                        }
//                        if (DisFabric.config.modifyChatMessages) {
//                            String jsonString = Text.Serializer.toJson(message);
//                            JSONObject newComponent = new JSONObject(jsonString);
//                            newComponent.getJSONArray("with").put(1, MarkdownParser.parseMarkdown(convertedPair.getRight()));
//                            Text finalText = Text.Serializer.fromJson(newComponent.toString());
//                            return CompletableFuture.completedFuture(finalText);
//                        }
//                    }
//                    return null;
//                }
//        );

        ServerChatCallback.EVENT.register((playerEntity, rawMessage) -> {
            if (!DisFabric.stop) {
                Pair<String, String> convertedPair = Utils.convertMentionsFromNames(rawMessage);
                if (DisFabric.config.isWebhookEnabled) {
                    JSONObject body = new JSONObject();
                    body.put("username", playerEntity.getEntityName());
                    body.put("avatar_url", "https://mc-heads.net/avatar/" + (DisFabric.config.useUUIDInsteadNickname ? playerEntity.getUuid() : playerEntity.getEntityName()));
                    JSONObject allowed_mentions = new JSONObject();
                    allowed_mentions.put("parse", new String[]{"users", "roles"});
                    body.put("allowed_mentions", allowed_mentions);
                    body.put("content", convertedPair.getLeft());
                    try {
                        Unirest.post(DisFabric.config.webhookURL).header("Content-Type", "application/json").body(body).asJsonAsync();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    DisFabric.textChannel.sendMessage(DisFabric.config.texts.playerMessage.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName())).replace("%playermessage%", convertedPair.getLeft())).queue();
                }
                if (DisFabric.config.modifyChatMessages) {
                    JSONObject newComponent = new JSONObject();
                    newComponent.put("text", MarkdownParser.parseMarkdown(convertedPair.getRight()));
                    Text finalText = Text.Serializer.fromJson(newComponent.toString());
                    return Optional.ofNullable(finalText);
                }
            }
            return Optional.empty();
        });

        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            if(DisFabric.config.announceAdvancements && advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && playerEntity.getAdvancementTracker().getProgress(advancement).isDone() && !DisFabric.stop) {
                switch (advancement.getDisplay().getFrame()) {
                    case GOAL -> DisFabric.textChannel.sendMessage(DisFabric.config.texts.advancementGoal.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName())).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                    case TASK -> DisFabric.textChannel.sendMessage(DisFabric.config.texts.advancementTask.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName())).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                    case CHALLENGE -> DisFabric.textChannel.sendMessage(DisFabric.config.texts.advancementChallenge.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName())).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                }
            }
        });

        PlayerDeathCallback.EVENT.register((playerEntity, damageSource) -> {
            if(DisFabric.config.announceDeaths && !DisFabric.stop){
                DisFabric.textChannel.sendMessage(DisFabric.config.texts.deathMessage.replace("%deathmessage%",MarkdownSanitizer.escape(damageSource.getDeathMessage(playerEntity).getString())).replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName()))).queue();
            }
        });

        PlayerJoinCallback.EVENT.register((connection, playerEntity) -> {
            if(DisFabric.config.announcePlayers && !DisFabric.stop){
                DisFabric.textChannel.sendMessage(DisFabric.config.texts.joinServer.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName()))).queue();
            }
        });

        PlayerLeaveCallback.EVENT.register((playerEntity) -> {
            if(DisFabric.config.announcePlayers && !DisFabric.stop){
                DisFabric.textChannel.sendMessage(DisFabric.config.texts.leftServer.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName()))).queue();
            }
        });
    }
}
