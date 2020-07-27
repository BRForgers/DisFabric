package br.com.brforgers.mods.disfabric;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Member;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

public class Utils {

    public static Pair<String, String> convertMentionsFromNames(String message) {

        if (!message.contains("@")) return new Pair<>(message, message);

        List<String> messageList = Arrays.asList(message.split("@[\\S]+"));
        if(messageList.size() == 0) {
            messageList = new ArrayList<>();
            messageList.add("");
        }

        StringBuilder discordString = new StringBuilder(), mcString = new StringBuilder();
        Pattern pattern = Pattern.compile("@[\\S]+");
        Matcher matcher = pattern.matcher(message);

        int x = 0;
        while(matcher.find()) {
            Member member = null;
            for (Member m : DisFabric.textChannel.getMembers()) {
                String name = matcher.group().substring(1);
                if (m.getUser().getName().toLowerCase().equals(name.toLowerCase()) || (m.getNickname() != null && m.getNickname().toLowerCase().equals(name.toLowerCase()))) {
                    member = m;
                }
            }
            if (member == null) {
                discordString.append(messageList.get(x)).append(matcher.group());
                mcString.append(messageList.get(x)).append(matcher.group());
            } else {
                discordString.append(messageList.get(x)).append(member.getAsMention());
                mcString.append(messageList.get(x)).append(Formatting.YELLOW.toString()).append("@").append(member.getEffectiveName()).append(Formatting.WHITE.toString());
            }
            x++;
        }
        if(x < messageList.size()) {
            discordString.append(messageList.get(x));
            mcString.append(messageList.get(x));
        }
        return new Pair<>(discordString.toString(), mcString.toString());
    }

    public static Formatting getTextFormattingByColor(Color color) {
        if(color == null) return Formatting.BLUE;

        HashMap<Formatting, Color> mcColors = new HashMap<>();
        mcColors.put(Formatting.BLACK, new Color(0x000000));
        mcColors.put(Formatting.DARK_BLUE, new Color(0x0000AA));
        mcColors.put(Formatting.DARK_GREEN, new Color(0x00AA00));
        mcColors.put(Formatting.DARK_AQUA, new Color(0x00AAAA));
        mcColors.put(Formatting.DARK_RED, new Color(0xAA0000));
        mcColors.put(Formatting.DARK_PURPLE, new Color(0xAA00AA));
        mcColors.put(Formatting.GOLD, new Color(0xFFAA00).brighter());
        mcColors.put(Formatting.GRAY, new Color(0xAAAAAA));
        mcColors.put(Formatting.DARK_GRAY, new Color(0x555555));
        mcColors.put(Formatting.BLUE, new Color(0x5555ff).brighter());
        mcColors.put(Formatting.GREEN, new Color(0x55ff55).brighter());
        mcColors.put(Formatting.AQUA, new Color(0x55ffff).brighter());
        mcColors.put(Formatting.RED, new Color(0xff5555).brighter());
        mcColors.put(Formatting.LIGHT_PURPLE, new Color(0xff55ff).brighter().brighter());
        mcColors.put(Formatting.YELLOW, new Color(0xffff55).brighter());
        mcColors.put(Formatting.WHITE, new Color(0xffffff));

        HashMap<Integer, Formatting>  distances = new HashMap<>();
        for(Map.Entry<Formatting, Color> colorr: mcColors.entrySet()){
            int distance = Math.abs(color.getRed() - colorr.getValue().getRed()) +
                    Math.abs(color.getGreen() - colorr.getValue().getGreen()) +
                    Math.abs(color.getBlue() - colorr.getValue().getBlue());

            distances.put(distance, colorr.getKey());
        }

        Integer[] dis = distances.keySet().toArray(new Integer[distances.size()]);
        Arrays.sort(dis);

        return distances.get(dis[0]);
    }

    public static long mean(long[] values) {
        long sum = 0L;
        for (long v : values) {
            sum += v;
        }
        return sum / values.length;
    }

}
