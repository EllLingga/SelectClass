package com.selectclass.gui;

import com.selectclass.data.ClassManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Menangani klik di GUI chest pilih-kelas.
 *
 * PENEGAKAN KUNCI: sama seperti versi Dialog sebelumnya, setiap klik SELALU
 * dicek ulang ke ClassManager (bukan percaya tampilan chest yang mungkin
 * sudah usang di client - misal 2 chest terbuka dari 2 device/sesi). Jadi
 * "sekali pilih, terkunci" ditegakkan di backend, bukan cuma disembunyikan
 * di tampilan.
 */
public class ClassGuiListener implements Listener {

    private final ClassManager classManager;

    public ClassGuiListener(ClassManager classManager) {
        this.classManager = classManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder rawHolder = event.getInventory().getHolder();
        if (!(rawHolder instanceof ClassGuiHolder holder)) {
            return;
        }

        // Selalu batalkan supaya pemain tidak bisa mengambil item apa pun
        // dari GUI ini, termasuk klik di inventory pemain sendiri saat
        // GUI ini yang sedang terbuka (top inventory).
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (holder.getMode() != ClassGuiHolder.Mode.SELECT) {
            // Mode INFO: tidak ada aksi apa pun saat diklik.
            return;
        }
        // Hanya proses klik di inventory atas (GUI-nya), bukan inventory pemain.
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        ClassManager.PlayerClass selected = slotToClass(event.getRawSlot());
        if (selected == null) {
            return;
        }

        boolean success = classManager.setClassIfAbsent(player, selected);
        player.closeInventory();

        if (!success) {
            ClassManager.PlayerClass current = classManager.getClass(player);
            player.sendMessage(Component.text(
                    "Kamu sudah memilih kelas " + (current != null ? current.getDisplayName() : "-")
                            + " sebelumnya. Hanya admin yang bisa mengubahnya.",
                    NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text(
                "Kamu telah memilih kelas " + selected.getDisplayName() + ".",
                NamedTextColor.GREEN));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ClassGuiHolder) {
            event.setCancelled(true);
        }
    }

    private ClassManager.PlayerClass slotToClass(int slot) {
        return switch (slot) {
            case 2 -> ClassManager.PlayerClass.RPL_A;
            case 4 -> ClassManager.PlayerClass.RPL_B;
            case 6 -> ClassManager.PlayerClass.RPL_C;
            default -> null;
        };
    }
}
