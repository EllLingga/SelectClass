package com.selectclass.commands;

import com.selectclass.data.ClassManager;
import com.selectclass.gui.ClassGuiFactory;
import com.selectclass.gui.ClassGuiHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /selectclass                          -> buka GUI chest pilih/lihat kelas milik sendiri
 * /selectclass reset <pemain>           -> KHUSUS ADMIN, kirim pesan konfirmasi (klik) untuk hapus kelas
 * /selectclass reset <pemain> confirm   -> eksekusi hapus kelas (dipicu oleh klik konfirmasi di atas)
 *
 * Didaftarkan lewat plugin.yml biasa (bukan paper-plugin.yml/bootstrapper),
 * karena GUI chest tidak butuh apa pun yang harus didaftarkan sebelum server
 * selesai menyala seperti Dialog API dulu.
 */
public class SelectClassCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ClassManager classManager;

    public SelectClassCommand(JavaPlugin plugin, ClassManager classManager) {
        this.plugin = plugin;
        this.classManager = classManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length >= 1 && args[0].equalsIgnoreCase("reset")) {
                handleReset(sender, args);
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Perintah ini hanya bisa dipakai oleh pemain di dalam game.", NamedTextColor.RED));
                return true;
            }
            openMenu(player);
        } catch (Exception ex) {
            sender.sendMessage(Component.text("Terjadi error saat memproses /selectclass. Cek console server.", NamedTextColor.RED));
            plugin.getLogger().warning("Error pada /selectclass untuk " + sender.getName() + ": " + ex);
            ex.printStackTrace();
        }
        return true;
    }

    private void openMenu(Player player) {
        ClassManager.PlayerClass current = classManager.getClass(player);
        ClassGuiHolder.Mode mode = current == null ? ClassGuiHolder.Mode.SELECT : ClassGuiHolder.Mode.INFO;
        ClassGuiHolder holder = new ClassGuiHolder(mode);
        player.openInventory(ClassGuiFactory.buildClassMenu(holder, current));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("selectclass.admin")) {
            sender.sendMessage(Component.text("Kamu tidak punya izin untuk menghapus kelas pemain.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Pakai: /selectclass reset <nama pemain>", NamedTextColor.RED));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(Component.text("Pemain '" + args[1] + "' tidak ditemukan.", NamedTextColor.RED));
            return;
        }

        ClassManager.PlayerClass current = classManager.getClass(target.getUniqueId());
        if (current == null) {
            sender.sendMessage(Component.text(
                    (target.getName() != null ? target.getName() : args[1]) + " belum memilih kelas apa pun.",
                    NamedTextColor.YELLOW));
            return;
        }

        String targetName = target.getName() != null ? target.getName() : args[1];
        boolean confirmed = args.length >= 3 && args[2].equalsIgnoreCase("confirm");

        if (!confirmed) {
            // Kirim pesan dengan tombol klik konfirmasi (chat click event bekerja
            // di semua versi client lewat ViaVersion/ViaRewind, beda dengan
            // Dialog API yang cuma dikenali client 1.21.6+ asli).
            Component confirmButton = Component.text("[Klik untuk konfirmasi hapus]", NamedTextColor.RED)
                    .clickEvent(ClickEvent.runCommand("/selectclass reset " + targetName + " confirm"))
                    .hoverEvent(Component.text("Jalankan /selectclass reset " + targetName + " confirm"));

            sender.sendMessage(Component.text(
                    "Yakin ingin menghapus kelas " + targetName + " (saat ini: " + current.getDisplayName() + ")?",
                    NamedTextColor.WHITE));
            sender.sendMessage(confirmButton);
            return;
        }

        doReset(sender, target, targetName);
    }

    private void doReset(CommandSender sender, OfflinePlayer target, String targetName) {
        classManager.clearClass(target);
        sender.sendMessage(Component.text("Kelas " + targetName + " berhasil dihapus. Pemain bisa memilih kelas baru.", NamedTextColor.GREEN));
        plugin.getLogger().info(sender.getName() + " menghapus kelas " + targetName);
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(Component.text(
                    "Kelas kamu telah dihapus oleh admin. Kamu bisa memilih kelas baru lewat /selectclass.",
                    NamedTextColor.YELLOW));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            List<String> options = new ArrayList<>(List.of("reset"));
            String partial = args.length == 1 ? args[0].toLowerCase() : "";
            return options.stream().filter(o -> o.startsWith(partial)).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
