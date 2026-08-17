package com.selectclass.placeholder;

import com.selectclass.data.ClassManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Menyediakan placeholder PlaceholderAPI supaya nama kelas pemain bisa
 * dipakai di plugin lain yang mendukung PlaceholderAPI - termasuk
 * DiscordSRV, yang membaca placeholder ini di format pesan
 * Minecraft->Discord kalau opsi "PlaceholderAPI: true" diaktifkan di
 * config.yml DiscordSRV.
 *
 * Placeholder yang tersedia:
 *   %selectclass_class%      -> "RPL A" / "RPL B" / "RPL C" / "" (kosong kalau belum pilih)
 *   %selectclass_tag%        -> "[RPL A]" dst, atau "" kalau belum pilih
 */
public class SelectClassExpansion extends PlaceholderExpansion {

    private final ClassManager classManager;

    public SelectClassExpansion(ClassManager classManager) {
        this.classManager = classManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "selectclass";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SelectClassPlugin";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    /**
     * Tetap aktif walau /papi reload, supaya tidak perlu didaftarkan ulang manual.
     */
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        ClassManager.PlayerClass playerClass = classManager.getClass(player.getUniqueId());

        return switch (params.toLowerCase()) {
            case "class" -> playerClass != null ? playerClass.getDisplayName() : "";
            case "tag" -> playerClass != null ? "[" + playerClass.getDisplayName() + "]" : "";
            default -> null;
        };
    }
}
