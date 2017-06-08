package me.lucko.welcometutorial.event;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class TutorialCompleteEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Getter
    private final Reason reason;

    @Getter
    private final String name;

    public TutorialCompleteEvent(Player who, String name, Reason reason) {
        super(who);
        this.reason = reason;
        this.name = name;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public enum Reason {
        QUIT,
        FINISHED
    }
}
