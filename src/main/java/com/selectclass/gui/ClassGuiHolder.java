package com.selectclass.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Penanda supaya listener bisa tahu sebuah Inventory yang dibuka pemain
 * adalah GUI pilih-kelas milik plugin ini (bukan chest/inventory lain),
 * jadi klik di dalamnya bisa ditangani dengan aman tanpa salah tangkap
 * inventory GUI plugin lain.
 */
public class ClassGuiHolder implements InventoryHolder {

    public enum Mode {
        /** Pemain belum punya kelas -> tampilkan 3 pilihan yang bisa diklik. */
        SELECT,
        /** Pemain sudah punya kelas -> tampilkan info saja, tidak bisa diklik ulang. */
        INFO
    }

    private final Mode mode;
    private Inventory inventory;

    public ClassGuiHolder(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
