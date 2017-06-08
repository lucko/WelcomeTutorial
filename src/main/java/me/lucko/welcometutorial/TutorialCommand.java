package me.lucko.welcometutorial;

import lombok.RequiredArgsConstructor;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.lucko.helper.metadata.Metadata;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TutorialCommand implements CommandExecutor {
    private final TutorialPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.isOp()) {
            plugin.reloadConfig();
            plugin.msg(sender, "Config reloaded.");
            return true;
        }

        if (!(sender instanceof Player)) {
            plugin.msg(sender, "You need to be a player to do this.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("welcometutorial.use")) {
            plugin.msg(sender, "No permission.");
            return true;
        }

        if (Metadata.provideForPlayer(player).has(TutorialPlugin.VANISHED_KEY)) {
            plugin.msg(sender, "You are already in a tutorial.");
            return true;
        }

        if (args.length == 0) {
            plugin.msg(player, "Usage: /tutorial <name>");
            plugin.msg(player, "Available Tutorials: " + plugin.getTutorials().keySet().stream().collect(Collectors.joining("&7, &f")));
            return true;
        }

        String tutorialName = args[0];
        Tutorial tutorial = plugin.getTutorials().get(tutorialName.toLowerCase());

        if (tutorial == null) {
            plugin.msg(player, "That tutorial does not exist.");
            plugin.msg(player, "Available Tutorials: " + plugin.getTutorials().keySet().stream().collect(Collectors.joining("&7, &f")));
            return true;
        }

        regioncheck:
        if (!tutorial.getRequiredRegion().equals("")) {
            ApplicableRegionSet set = WGBukkit.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
            for (ProtectedRegion r : set.getRegions()) {
                if (r.getId().equalsIgnoreCase(tutorial.getRequiredRegion())) {
                    break regioncheck;
                }
            }

            plugin.msg(player, "You cannot start that tutorial in this region.");
            return true;
        }

        plugin.vanishPlayer(player);
        new TutorialRunnable(player, player.getLocation().clone(), tutorial, tutorial.getStages()).runTaskTimer(plugin, 10L, 1L);
        return true;
    }
}
