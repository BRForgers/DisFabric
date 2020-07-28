package br.com.brforgers.mods.disfabric.listeners;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.Utils;
import br.com.brforgers.mods.disfabric.events.*;
import com.mashape.unirest.http.Unirest;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Pair;
import org.json.JSONObject;

public class MinecraftEventListener {
    public void init() {
        ServerChatCallback.EVENT.register((playerEntity, rawMessage, message) -> {
            Pair<String, String> convertedPair = Utils.convertMentionsFromNames(rawMessage);
            JSONObject body = new JSONObject();
            body.put("username", playerEntity.getEntityName());
            body.put("avatar_url", "https://mc-heads.net/avatar/" + playerEntity.getEntityName());
            body.put("content", convertedPair.getLeft());
            try {
                Unirest.post(DisFabric.config.webhookURL).header("Content-Type", "application/json").body(body).asJsonAsync();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            JSONObject newComponent = new JSONObject(LiteralText.Serializer.toJson(message));
            DisFabric.logger.warn(newComponent.toString());
            newComponent.getJSONArray("with").put(1, convertedPair.getRight());
            DisFabric.logger.warn(newComponent.toString());
            return LiteralText.Serializer.fromJson(newComponent.toString());
        });

        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            if(advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && playerEntity.getAdvancementTracker().getProgress(advancement).isDone()) {
                DisFabric.textChannel.sendMessage(playerEntity.getEntityName()+" has made the advancement **["+advancement.getDisplay().getTitle().getString()+"]**").queue();
            }
        });

        PlayerDeathCallback.EVENT.register((playerEntity, damageSource) -> DisFabric.textChannel.sendMessage("**"+damageSource.getDeathMessage(playerEntity).getString()+"**").queue());

        PlayerJoinCallback.EVENT.register((connection, playerEntity) -> DisFabric.textChannel.sendMessage("**" + playerEntity.getEntityName() + " joined the server!**").queue());

        PlayerLeaveCallback.EVENT.register((playerEntity) -> DisFabric.textChannel.sendMessage("**" + playerEntity.getEntityName() + " left the server!**").queue());
    }
}
