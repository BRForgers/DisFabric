package br.com.brforgers.mods.disfabric;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = DisFabric.MOD_ID)
public class Configuration implements ConfigData {
    @ConfigEntry.Category(value = "Discord")
    public String botToken = "";
    @ConfigEntry.Category(value = "Discord")
    public String webhookURL = "";
    @ConfigEntry.Category(value = "Discord")
    public String[] adminsIds = {""};
    @ConfigEntry.Category(value = "Discord")
    public String channelId = "";
    @ConfigEntry.Category(value = "Discord")
    public boolean membersIntents = false;
}
