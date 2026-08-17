package com.selectclass.gui;

import com.selectclass.data.ClassManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Membangun GUI chest biasa (bukan Dialog API) untuk pilih kelas.
 *
 * Sengaja pakai Inventory/Chest vanilla karena ini fitur yang sudah ada
 * sejak Minecraft versi lama dan diterjemahkan dengan baik oleh
 * ViaVersion/ViaRewind untuk client versi lama - beda dengan Dialog API
 * (menu ESC custom) yang cuma dikenali client 1.21.6+ asli.
 */
public final class ClassGuiFactory {

    private ClassGuiFactory() {
    }

    public static final Component GUI_TITLE_SELECT = Component.text("Pilih Kelas", NamedTextColor.GOLD);
    public static final Component GUI_TITLE_INFO = Component.text("Kelas Kamu", NamedTextColor.GOLD);

    private static ItemStack namedItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        List<Component> finalLore = new ArrayList<>();
        for (Component line : lore) {
            finalLore.add(line.decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(finalLore);
        item.setItemMeta(meta);
        return item;
    }

    private static Material materialFor(ClassManager.PlayerClass pc) {
        return switch (pc) {
            case RPL_A -> Material.LIME_STAINED_GLASS_PANE;
            case RPL_B -> Material.YELLOW_STAINED_GLASS_PANE;
            case RPL_C -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        };
    }

    private static NamedTextColor colorFor(ClassManager.PlayerClass pc) {
        return switch (pc) {
            case RPL_A -> NamedTextColor.GREEN;
            case RPL_B -> NamedTextColor.YELLOW;
            case RPL_C -> NamedTextColor.AQUA;
        };
    }

    /**
     * GUI utama saat pemain buka menu kelas.
     * - Belum punya kelas -> chest 9 slot, isi 3 tombol pilihan di slot 2, 4, 6.
     * - Sudah punya kelas -> chest 9 slot, isi 1 item info di slot 4 (tengah),
     *   tidak ada tombol pilihan (anti pilih ulang dari sisi tampilan).
     */
    public static Inventory buildClassMenu(ClassGuiHolder holder, ClassManager.PlayerClass current) {
        if (current == null) {
            Inventory inv = org.bukkit.Bukkit.createInventory(holder, 9, GUI_TITLE_SELECT);
            holder.setInventory(inv);

            ItemStack filler = namedItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), List.of());
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, filler);
            }

            for (ClassManager.PlayerClass pc : ClassManager.PlayerClass.values()) {
                int slot = switch (pc) {
                    case RPL_A -> 2;
                    case RPL_B -> 4;
                    case RPL_C -> 6;
                };
                inv.setItem(slot, namedItem(
                        materialFor(pc),
                        Component.text(pc.getDisplayName(), colorFor(pc), TextDecoration.BOLD),
                        List.of(
                                Component.text("Klik untuk memilih kelas ini.", NamedTextColor.GRAY),
                                Component.text("Setelah dipilih, kelas akan TERKUNCI", NamedTextColor.DARK_GRAY),
                                Component.text("dan hanya admin yang bisa mengubahnya.", NamedTextColor.DARK_GRAY)
                        )
                ));
            }
            return inv;
        }

        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 9, GUI_TITLE_INFO);
        holder.setInventory(inv);

        ItemStack filler = namedItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), List.of());
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(4, namedItem(
                materialFor(current),
                Component.text(current.getDisplayName(), colorFor(current), TextDecoration.BOLD),
                List.of(
                        Component.text("Ini kelas kamu saat ini.", NamedTextColor.GRAY),
                        Component.text("Kelas sudah terkunci.", NamedTextColor.DARK_GRAY),
                        Component.text("Hubungi admin jika ingin mengubahnya.", NamedTextColor.DARK_GRAY)
                )
        ));
        return inv;
    }
}
