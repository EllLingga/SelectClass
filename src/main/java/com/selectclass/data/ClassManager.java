package com.selectclass.data;

import com.selectclass.SelectClassPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Menyimpan kelas (RPL A/B/C) tiap pemain, dan menerapkan suffix di tab list.
 *
 * ATURAN KUNCI (sesuai permintaan): sekali seorang pemain sudah memilih kelas,
 * dia TIDAK BISA memilih lagi. Satu-satunya cara mengubah/menghapusnya adalah
 * admin lewat command "/selectclass reset <pemain>". Class ini adalah satu-
 * satunya tempat yang menulis data kelas, jadi aturan ini ditegakkan di sini
 * (lihat setClass) - dipanggil dari listener klik & command, keduanya sudah
 * mengecek hasClass() dulu sebelum memanggil, tapi validasi juga dijaga di
 * level manager supaya tidak ada jalur lain yang bisa menembusnya.
 */
public class ClassManager {

    public enum PlayerClass {
        RPL_A("RPL A"),
        RPL_B("RPL B"),
        RPL_C("RPL C");

        private final String displayName;

        PlayerClass(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final SelectClassPlugin plugin;
    private final Map<UUID, PlayerClass> playerClasses = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public ClassManager(SelectClassPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "classdata.yml");
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Gagal membuat file classdata.yml", e);
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        playerClasses.clear();
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String className = dataConfig.getString(key);
                if (className != null) {
                    playerClasses.put(uuid, PlayerClass.valueOf(className));
                }
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Entry tidak valid di classdata.yml, dilewati: " + key);
            }
        }
    }

    public void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Gagal menyimpan classdata.yml", e);
        }
    }

    public PlayerClass getClass(UUID uuid) {
        return playerClasses.get(uuid);
    }

    public PlayerClass getClass(Player player) {
        return getClass(player.getUniqueId());
    }

    public boolean hasClass(UUID uuid) {
        return playerClasses.containsKey(uuid);
    }

    public boolean hasClass(Player player) {
        return hasClass(player.getUniqueId());
    }

    /**
     * Menetapkan kelas seorang pemain. Mengembalikan false (dan tidak melakukan
     * apa-apa) jika pemain itu SUDAH punya kelas - inilah penegakan aturan
     * "sekali pilih, terkunci". Dipakai untuk pemilihan awal oleh pemain sendiri.
     */
    public boolean setClassIfAbsent(Player player, PlayerClass playerClass) {
        if (hasClass(player)) {
            return false;
        }
        forceSetClass(player.getUniqueId(), playerClass);
        applyTabSuffix(player, playerClass);
        return true;
    }

    /**
     * Dipakai KHUSUS oleh alur admin (command reset lalu set ulang jika perlu).
     * Tidak melakukan pengecekan "sudah punya kelas", karena memang dipakai
     * untuk mengubah data secara sengaja oleh admin.
     */
    public void forceSetClass(UUID uuid, PlayerClass playerClass) {
        playerClasses.put(uuid, playerClass);
        dataConfig.set(uuid.toString(), playerClass.name());
        save();
    }

    /** Dipanggil admin lewat "/selectclass reset <pemain>". */
    public void clearClass(OfflinePlayer target) {
        UUID uuid = target.getUniqueId();
        playerClasses.remove(uuid);
        dataConfig.set(uuid.toString(), null);
        save();
        if (target.isOnline() && target.getPlayer() != null) {
            removeTabSuffix(target.getPlayer());
        }
    }

    private String teamNameFor(PlayerClass pc) {
        String name = "sc_" + pc.name();
        return name.length() > 16 ? name.substring(0, 16) : name;
    }

    public void applyTabSuffix(Player player, PlayerClass playerClass) {
        Scoreboard board = player.getServer().getScoreboardManager().getMainScoreboard();

        for (PlayerClass pc : PlayerClass.values()) {
            Team otherTeam = board.getTeam(teamNameFor(pc));
            if (otherTeam != null) {
                otherTeam.removeEntry(player.getName());
            }
        }

        String teamName = teamNameFor(playerClass);
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }
        team.suffix(Component.text(" [" + playerClass.getDisplayName() + "]", NamedTextColor.GRAY));

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    public void removeTabSuffix(Player player) {
        Scoreboard board = player.getServer().getScoreboardManager().getMainScoreboard();
        for (PlayerClass pc : PlayerClass.values()) {
            Team team = board.getTeam(teamNameFor(pc));
            if (team != null) {
                team.removeEntry(player.getName());
            }
        }
    }
}
