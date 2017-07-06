package me.lucko.welcometutorial;

import lombok.AccessLevel;
import lombok.Getter;

import com.destroystokyo.paper.Title;
import com.google.common.collect.ImmutableList;

import me.lucko.helper.utils.Color;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TutorialStage {

    @Getter(AccessLevel.NONE)
    private final Location location;
    private final String title;
    private final String subTitle;
    private final List<String> chatMessages;
    private final int waitTime;

    public TutorialStage(ConfigurationSection section) {
        String world = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        int yaw = section.getInt("yaw");
        int pitch = section.getInt("pitch");

        this.location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        this.title = Color.colorize(section.getString("title", ""));
        this.subTitle = Color.colorize(section.getString("subtitle", ""));
        this.chatMessages = ImmutableList.copyOf(section.getStringList("chat").stream().map(Color::colorize).collect(Collectors.toList()));
        this.waitTime = section.getInt("wait");
    }

    public Location getLocation() {
        return location.clone();
    }

    public void playStage(Player player) {
        player.teleport(getLocation());

        if (!title.equals("") || !subTitle.equals("")) {
            Title.Builder t = Title.builder();
            if (!title.equals("")) {
                t.title(title);
            }
            if (!subTitle.equals("")) {
                t.subtitle(subTitle);
            }

            t.stay(100);
            player.sendTitle(t.build());
        }

        for (String msg : chatMessages) {
            player.sendMessage(msg);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 0.0f);
    }
}
