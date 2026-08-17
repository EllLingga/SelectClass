package com.selectclass.listeners;

import com.selectclass.data.ClassManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Menambahkan tag kelas (mis. [RPL C]) di belakang nama pemain saat chat.
 */
public class ChatListener implements Listener {

    private final ClassManager classManager;

    public ChatListener(ClassManager classManager) {
        this.classManager = classManager;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        ClassManager.PlayerClass playerClass = classManager.getClass(player);

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component name = Component.text(source.getName(), NamedTextColor.WHITE);
            Component classTag = playerClass != null
                    ? Component.text(" [" + playerClass.getDisplayName() + "]", NamedTextColor.GRAY)
                    : Component.empty();
            return name.append(classTag)
                    .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                    .append(message);
        });
    }
}
