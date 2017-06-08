package me.lucko.welcometutorial;

import lombok.Getter;

import me.lucko.helper.Events;
import me.lucko.helper.Scheduler;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import me.lucko.helper.terminable.CompositeTerminable;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.utils.Color;
import me.lucko.welcometutorial.event.TutorialCompleteEvent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Getter
@Plugin(name = "WelcomeTutorial", depends = @PluginDependency("WorldGuard"))
public class TutorialPlugin extends ExtendedJavaPlugin implements TutorialApi, CompositeTerminable {
    public static final MetadataKey<Boolean> VANISHED_KEY = MetadataKey.createBooleanKey("wt-vanished");

    private String prefix;
    private final Map<String, Tutorial> tutorials = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void onEnable() {
        loadConfig();

        bindTerminable(this);
        registerCommand(new TutorialCommand(this), "tutorial");
        provideService(TutorialApi.class, this);
    }

    public void reloadConfig() {
        this.tutorials.clear();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = loadConfig("config.yml");
        this.prefix = Color.colorize(config.getString("prefix", "&7[&bTutorial&7] &f"));

        Set<String> tutorials = config.getKeys(false);
        for (String tutorialId : tutorials) {
            if (tutorialId.equalsIgnoreCase("prefix")) {
                continue;
            }

            ConfigurationSection tutorialSection = config.getConfigurationSection(tutorialId);
            String name = Color.colorize(tutorialSection.getString("name", tutorialId));
            String requiredRegion = tutorialSection.getString("required-region", "");

            Tutorial tutorial = new Tutorial(name, requiredRegion);

            ConfigurationSection locationsSection = tutorialSection.getConfigurationSection("locations");
            Set<String> locationKeys = locationsSection.getKeys(false);

            for (String locationKey : locationKeys) {
                ConfigurationSection locationSection = locationsSection.getConfigurationSection(locationKey);
                tutorial.addStage(new TutorialStage(locationSection));
            }

            this.tutorials.put(ChatColor.stripColor(Color.colorize(name.toLowerCase())), tutorial);
        }
    }

    public void vanishPlayer(Player p) {
        Metadata.provideForPlayer(p).put(VANISHED_KEY, true);
        for (Player other : getServer().getOnlinePlayers()) {
            if (other.getUniqueId().equals(p.getUniqueId())) {
                continue;
            }

            other.hidePlayer(p);
        }
    }

    public void unvanishPlayer(Player p) {
        Metadata.provideForPlayer(p).remove(VANISHED_KEY);
        for (Player other : getServer().getOnlinePlayers()) {
            if (other.getUniqueId().equals(p.getUniqueId())) {
                continue;
            }

            other.showPlayer(p);
        }
    }

    @Override
    public void bind(Consumer<Terminable> consumer) {
        Events.subscribe(TutorialCompleteEvent.class)
                .handler(e -> Scheduler.runLaterSync(() -> unvanishPlayer(e.getPlayer()), 2L))
                .register(consumer);

        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    for (Player vanished : Metadata.lookupPlayersWithKey(VANISHED_KEY).keySet()) {
                        e.getPlayer().hidePlayer(vanished);
                    }

                    if (e.getPlayer().getWalkSpeed() < 0.2f) {
                        e.getPlayer().setWalkSpeed(0.2f);
                    }
                })
                .register(consumer);

        Events.subscribe(PlayerQuitEvent.class)
                .filter(Events.DEFAULT_FILTERS.playerHasMetadata(VANISHED_KEY))
                .handler(e -> unvanishPlayer(e.getPlayer()))
                .register(consumer);

        Events.subscribe(PlayerCommandPreprocessEvent.class)
                .filter(Events.DEFAULT_FILTERS.playerHasMetadata(VANISHED_KEY))
                .handler(e -> {
                    msg(e.getPlayer(), "You cannot use commands whilst in a tutorial!");
                    e.setCancelled(true);
                })
                .register(consumer);

        Events.subscribe(AsyncPlayerChatEvent.class)
                .handler(e -> e.getRecipients().removeIf(p -> Metadata.provideForPlayer(p).has(VANISHED_KEY)))
                .register(consumer);

        Events.subscribe(EntityDamageEvent.class)
                .filter(e -> e.getEntity() instanceof Player)
                .filter(e -> Metadata.provideForEntity(e.getEntity()).has(VANISHED_KEY))
                .handler(e -> e.setCancelled(true))
                .register(consumer);

        Events.subscribe(PlayerMoveEvent.class)
                .filter(e -> !(e instanceof PlayerTeleportEvent))
                .filter(Events.DEFAULT_FILTERS.ignoreSameBlockAndY())
                .filter(Events.DEFAULT_FILTERS.playerHasMetadata(VANISHED_KEY))
                .handler(e -> e.setCancelled(true))
                .register(consumer);
    }

    public void msg(CommandSender sender, String msg) {
        sender.sendMessage(prefix + Color.colorize(msg));
    }

    @Override
    public boolean isInTutorial(Player player) {
        return Metadata.provideForPlayer(player).has(VANISHED_KEY);
    }
}
