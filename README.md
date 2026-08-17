# SelectClassPlugin — Pilih Kelas lewat GUI Chest (Paper 26.1.2)

Plugin untuk memilih kelas (**RPL A**, **RPL B**, **RPL C**) lewat **GUI
chest biasa** — bukan Dialog API (menu ESC custom). Dialog API cuma
dikenali client vanilla **1.21.6+ asli**, jadi kalau ada pemain yang connect
lewat **ViaVersion** (versi lebih baru/lama dari server) atau **ViaRewind**
(versi sangat lama, mis. 1.8–1.15), mereka tidak akan melihat tombolnya sama
sekali. GUI chest sudah didukung Minecraft sejak sangat lama, jadi
diterjemahkan dengan sempurna oleh ViaVersion/ViaRewind ke semua versi client.

## Cara pakai di game

- Ketik **`/selectclass`** → terbuka chest kecil berisi 3 pilihan: **RPL A**,
  **RPL B**, **RPL C**. Klik salah satu untuk memilih.
- **Tidak ada yang terbuka otomatis** saat join — pemain harus mengetik
  `/selectclass` sendiri.
- Kalau sudah punya kelas, `/selectclass` menampilkan chest info (1 item di
  tengah) yang menunjukkan kelasmu saat ini — **tidak ada tombol pilihan
  lagi**, jadi tidak bisa pilih ulang.

## Aturan kunci (sesuai permintaan)

1. Begitu pemain klik salah satu kelas, pilihan itu **langsung terkunci**.
2. Kalau pemain sudah punya kelas lalu buka `/selectclass` lagi, dia hanya
   melihat info kelasnya — tombol pilihan disembunyikan.
3. Backend selalu mengecek ulang ke data tersimpan sebelum mengunci kelas
   (bukan cuma percaya tampilan chest di client) — jadi anti-bug/exploit
   kalau ada 2 chest kebuka dari 2 sesi berbeda.
4. **Hanya admin** (permission `selectclass.admin`, default: op) yang bisa
   menghapus kelas seorang pemain:
   ```
   /selectclass reset <nama_pemain>
   ```
   Server akan membalas dengan pesan chat berisi tombol **[Klik untuk
   konfirmasi hapus]** (pakai chat click-event, kompatibel semua versi
   client) supaya tidak kepencet tidak sengaja. Setelah dihapus, pemain itu
   bisa memilih kelas baru dari awal.

## Fitur lain

- Kelas otomatis muncul di **chat**: `NamaPemain [RPL C]: pesan`
- Kelas otomatis muncul di **tab list**: `NamaPemain [RPL C]`
- Data tersimpan permanen di `plugins/SelectClassPlugin/classdata.yml`

## 🔗 Integrasi DiscordSRV (nama kelas muncul di Discord)

Plugin ini **tidak** menempel langsung ke internal DiscordSRV (API-nya
sering berubah antar versi). Sebagai gantinya, plugin ini mendaftarkan
placeholder ke **PlaceholderAPI**, dan DiscordSRV sendiri sudah punya
dukungan bawaan untuk membaca placeholder PlaceholderAPI di format pesannya.
Ini cara paling stabil lintas versi DiscordSRV.

Placeholder yang tersedia setelah PlaceholderAPI terpasang:

| Placeholder             | Hasil                              |
|--------------------------|-------------------------------------|
| `%selectclass_class%`    | `RPL C` (kosong kalau belum pilih) |
| `%selectclass_tag%`      | `[RPL C]` (kosong kalau belum pilih) |

### Langkah setup

1. Pasang **PlaceholderAPI** di server (kalau belum ada):
   `/papi ecloud download PlaceholderAPI` lalu `/papi reload` — atau download
   manual dari SpigotMC/Modrinth dan taruh di `plugins/`.
2. Restart/reload server. Di console akan muncul log:
   `Placeholder %selectclass_class% / %selectclass_tag% terdaftar ke PlaceholderAPI`.
3. Buka `plugins/DiscordSRV/config.yml`, cari opsi **`PlaceholderAPI`** dan
   pastikan bernilai `true`.
4. Masih di `config.yml` DiscordSRV, cari format pesan Minecraft→Discord,
   biasanya di bagian **`MinecraftToDiscord`** atau per-channel (tergantung
   versi DiscordSRV kamu, cari baris yang mengandung `%message%` dan
   `%username%`). Tambahkan `%selectclass_tag%` di depannya, contoh:
   ```yaml
   ChatChannel: "%selectclass_tag% **%username%**: %message%"
   ```
5. `/discordsrv reload` (atau restart server).

> Kalau PlaceholderAPI **tidak** dipasang, plugin ini tetap berjalan normal
> (chest GUI, chat tag, tab list semua tetap jalan) — cuma bagian Discord
> yang tidak akan menampilkan kelas. Ada peringatan soal ini di console log
> saat plugin nyala.

## Build

Butuh **Java 21** dan **Maven**.

```bash
cd selectclass-plugin
mvn clean package
```

Hasil jar: `target/SelectClassPlugin.jar` → taruh di folder `plugins/`
server Paper kamu.

### Build otomatis lewat GitHub

Repo ini sudah dilengkapi GitHub Actions (`.github/workflows/build.yml`).
Setiap push ke branch `main`, GitHub otomatis `mvn clean package` dan
menyediakan jar hasil build di tab **Actions → run terbaru → Artifacts**.

```bash
git init
git add .
git commit -m "Initial commit: SelectClassPlugin (chest GUI)"
git branch -M main
git remote add origin https://github.com/USERNAME/NAMA-REPO.git
git push -u origin main
```

### Versi server (26.1.2)

Di `pom.xml`:
```xml
<paper.version>[26.1.2.build,)</paper.version>
```
Otomatis ambil build terbaru untuk 26.1.2. Kalau nanti pindah drop versi
lain, ganti angka ini di `pom.xml` dan `api-version` di `plugin.yml`.

Plugin ini murni pakai Inventory/Chat API standar Bukkit/Paper (tidak ada
fitur "Experimental"), jadi aman dipakai berdampingan dengan **ViaVersion**
dan **ViaRewind** untuk menerima client versi lain dari server.

## Instalasi

1. Server **Paper** (atau fork turunannya seperti Purpur) versi 26.1.2 atau
   yang kompatibel.
2. Copy `SelectClassPlugin.jar` ke folder `plugins/`.
3. *(Opsional, untuk fitur Discord)* Pasang **PlaceholderAPI**, aktifkan
   `PlaceholderAPI: true` di config DiscordSRV, lihat bagian
   [Integrasi DiscordSRV](#-integrasi-discordsrv-nama-kelas-muncul-di-discord)
   di atas.
4. Restart server.

## Command & Permission

| Command                                       | Keterangan                                             | Permission              |
|-----------------------------------------------|----------------------------------------------------------|---------------------------|
| `/selectclass`                                | Buka GUI chest kelas (pilih jika belum, lihat jika sudah) | `selectclass.use` (default: semua pemain) |
| `/selectclass reset <pemain>`                 | Kirim konfirmasi hapus kelas pemain (admin)               | `selectclass.admin` (default: op) |
| `/selectclass reset <pemain> confirm`         | Eksekusi hapus kelas (dipicu tombol konfirmasi di chat)   | `selectclass.admin` (default: op) |

## Struktur Folder Project

```
selectclass-plugin/
├── pom.xml
├── README.md
├── .github/workflows/build.yml
└── src/main/
    ├── resources/
    │   └── plugin.yml                    (descriptor biasa, tanpa bootstrapper)
    └── java/com/selectclass/
        ├── SelectClassPlugin.java        (main class, daftar command & listener)
        ├── data/ClassManager.java        (data, kunci pemilihan, tab suffix)
        ├── gui/ClassGuiHolder.java       (penanda inventory milik plugin ini)
        ├── gui/ClassGuiFactory.java      (bangun chest GUI pilih/lihat kelas)
        ├── gui/ClassGuiListener.java     (tangani klik di chest GUI)
        ├── listeners/ChatListener.java   (suffix kelas di chat)
        ├── commands/SelectClassCommand.java (/selectclass)
        └── placeholder/SelectClassExpansion.java (placeholder untuk DiscordSRV via PlaceholderAPI)
```

## Kenapa pindah dari Dialog API ke chest GUI?

Dialog API (menu ESC custom) itu fitur vanilla Minecraft yang baru ada sejak
1.21.6/1.21.7, dan render tombolnya dilakukan sepenuhnya oleh **client**
berdasarkan versi protokolnya. ViaVersion/ViaRewind menerjemahkan
packet-packet gameplay antar versi, tapi UI native seperti Dialog tidak bisa
"dibuatkan mundur" untuk client versi lama — kalau client belum kenal fitur
itu, tombolnya ya tidak akan pernah muncul di layar mereka, apa pun yang
dilakukan Via*. Chest/inventory GUI sebaliknya sudah jadi bagian protokol
Minecraft sejak sangat lama, jadi kompatibel ke hampir semua versi client
yang didukung ViaVersion/ViaRewind. Konsekuensinya: tidak perlu lagi
`paper-plugin.yml` + `PluginBootstrap` + datapack bundel — semuanya diganti
`plugin.yml` biasa dan listener `InventoryClickEvent` standar.
