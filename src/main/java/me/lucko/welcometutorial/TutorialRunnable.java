package me.lucko.welcometutorial;

import lombok.RequiredArgsConstructor;

import me.lucko.helper.Events;
import me.lucko.welcometutorial.event.TutorialCompleteEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class TutorialRunnable extends BukkitRunnable {

    private final Player player;
    private final Location returnLocation;
    private final Tutorial tutorial;
    private final List<TutorialStage> stages;

    private final AtomicInteger currentStage = new AtomicInteger(-1);
    private final AtomicInteger progress = new AtomicInteger(-1);

    @Override
    public void run() {
        player.setWalkSpeed(0.0f);
        if (!player.isOnline()) {
            player.setWalkSpeed(0.2f);
            Events.call(new TutorialCompleteEvent(player, tutorial.getName(), TutorialCompleteEvent.Reason.QUIT));
            cancel();
            return;
        }

        if (progress.get() > 0) {
            progress.decrementAndGet();
            return;
        }

        currentStage.incrementAndGet();

        if (currentStage.get() < 0 || currentStage.get() >= stages.size()) {
            player.teleport(returnLocation);
            player.setWalkSpeed(0.2f);

            Events.call(new TutorialCompleteEvent(player, tutorial.getName(), TutorialCompleteEvent.Reason.FINISHED));
            cancel();
            return;
        }

        TutorialStage stage = stages.get(currentStage.get());
        progress.set(stage.getWaitTime());

        stage.playStage(player);
    }
}
