package com.selectclass;

import com.selectclass.commands.SelectClassCommand;
import com.selectclass.data.ClassManager;
import com.selectclass.gui.ClassGuiListener;
import com.selectclass.listeners.ChatListener;
import com.selectclass.placeholder.SelectClassExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SelectClassPlugin extends JavaPlugin implements Listener {

    private ClassManager classManager;

    @Override
    public void onEnable() {
        this.classManager = new ClassManager(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new ClassGuiListener(classManager), this);
        pm.registerEvents(new ChatListener(classManager), this);

        // GUI chest biasa + command lewat plugin.yml, jadi tidak perlu
        // paper-plugin.yml/bootstrapper lagi seperti versi Dialog API dulu.
        SelectClassCommand command = new SelectClassCommand(this, classManager);
        PluginCommand pluginCommand = getCommand("selectclass");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        } else {
            getLogger().severe("Command 'selectclass' tidak ditemukan di plugin.yml!");
        }

        // Integrasi DiscordSRV: didaftarkan lewat PlaceholderAPI (kalau ada),
        // supaya %selectclass_class%/%selectclass_tag% bisa dipakai di format
        // pesan Minecraft->Discord DiscordSRV (config.yml -> aktifkan
        // "PlaceholderAPI: true", lalu pakai placeholder itu di
        // "MinecraftToDiscord" > "ChatChannel"/format).
        if (pm.getPlugin("PlaceholderAPI") != null) {
            new SelectClassExpansion(classManager).register();
            getLogger().info("Placeholder %selectclass_class% / %selectclass_tag% terdaftar ke PlaceholderAPI (dipakai DiscordSRV).");
        } else {
            getLogger().warning("PlaceholderAPI tidak ditemukan - kelas TIDAK akan muncul di DiscordSRV. "
                    + "Install PlaceholderAPI, lalu aktifkan 'PlaceholderAPI: true' di config.yml DiscordSRV "
                    + "dan tambahkan %selectclass_tag% ke format MinecraftToDiscord.");
        }

        // Terapkan ulang suffix tab list untuk pemain yang sudah online saat
        // plugin di-reload (mis. /reload atau restart cepat plugin-nya saja).
        for (Player player : Bukkit.getOnlinePlayers()) {
            ClassManager.PlayerClass playerClass = classManager.getClass(player);
            if (playerClass != null) {
                classManager.applyTabSuffix(player, playerClass);
            }
        }

        getLogger().info("SelectClassPlugin aktif (versi chest GUI, kompatibel ViaVersion/ViaRewind). Buka menu lewat /selectclass.");
    }

    @Override
    public void onDisable() {
        if (classManager != null) {
            classManager.save();
        }
        getLogger().info("SelectClassPlugin dinonaktifkan.");
    }

    /**
     * Memastikan suffix tab list pemain yang sudah pernah memilih kelas
     * tetap muncul dengan benar setiap kali mereka join. Tidak ada GUI yang
     * otomatis terbuka di sini - pemain harus membuka sendiri lewat /selectclass.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ClassManager.PlayerClass playerClass = classManager.getClass(player);
        if (playerClass != null) {
            classManager.applyTabSuffix(player, playerClass);
        }
    }

    public ClassManager getClassManager() {
        return classManager;
    }
}
