# ProactiveChunkLoader

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/ranggagibran/proactivechunkloader/releases)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![PaperMC](https://img.shields.io/badge/server-PaperMC-yellow.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

**English** | [Bahasa Indonesia](#plugin-description-in-bahasa-indonesia)

ProactiveChunkLoader is a Minecraft plugin for PaperMC servers that intelligently and proactively loads chunks around moving players, with the aim of reducing or eliminating lag spikes that occur when players enter unexplored areas of the world.

## Plugin Description in Bahasa Indonesia

ProactiveChunkLoader adalah plugin Minecraft untuk server PaperMC yang secara cerdas dan proaktif memuat (pre-generates) chunk di sekitar pemain yang sedang bergerak, dengan tujuan untuk mengurangi atau menghilangkan lag spike (penurunan TPS) yang terjadi ketika pemain memasuki area dunia yang belum pernah dijelajahi.

## Features

- **Proactive Chunk Loading** - Loads chunks ahead of players based on their movement direction
- **Smart Prioritization** - Prioritizes chunks that are most likely to be entered by players
- **Adaptive Direction Prediction** - Predicts player movement direction based on history and velocity
- **Intelligent Performance Adjustment** - Adjusts chunk loading rate based on server TPS
- **Configurable "Cone" Structure** - Controls how many chunks are loaded to the sides of the movement direction
- **Performance Statistics** - Tracks chunk loading metrics for analysis and monitoring
- **Comprehensive Commands** - For viewing info, statistics, and reloading configuration

## Fitur

- **Pemuat Chunk Proaktif** - Memuat chunk di depan pemain berdasarkan arah gerakan mereka
- **Prioritas Cerdas** - Mengutamakan chunk yang paling mungkin dimasuki pemain
- **Prediksi Arah Adaptif** - Memprediksi arah pergerakan pemain berdasarkan riwayat dan kecepatan
- **Pengaturan Performa Cerdas** - Menyesuaikan tingkat pemuatan chunk berdasarkan TPS server
- **Struktur "Cone" Yang Dapat Dikonfigurasi** - Mengatur berapa banyak chunk yang dimuat ke arah samping dari arah gerakan
- **Statistik Performa** - Melacak metrik pemuatan chunk untuk analisis dan pemantauan
- **Perintah Lengkap** - Untuk melihat info, statistik, dan memuat ulang konfigurasi

## Differences from Chunky Plugin

Unlike mass chunk pre-generator plugins like Chunky, ProactiveChunkLoader:

- Works in real-time as players move
- Only loads chunks that are actually needed based on player movement direction
- Uses fewer server resources because it's more selective
- Prioritizes chunks based on the likelihood of players visiting them

## Perbedaan dengan Plugin Chunky

Berbeda dengan plugin pre-generator chunk massal seperti Chunky, ProactiveChunkLoader:

- Bekerja secara real-time saat pemain bergerak
- Hanya memuat chunk yang benar-benar dibutuhkan berdasarkan arah pergerakan pemain
- Menggunakan lebih sedikit resources server karena lebih selektif
- Memprioritaskan chunk berdasarkan kemungkinan pemain akan mengunjunginya

## Installation

1. Download the latest `.jar` file from [Releases](https://github.com/ranggagibran/proactivechunkloader/releases) or [SpigotMC](https://www.spigotmc.org/resources/proactivechunkloader.xxxxx/)
2. Place the `.jar` file in your server's `plugins` folder
3. Restart the server or reload plugins with `/reload confirm`
4. Adjust the configuration in `plugins/ProactiveChunkLoader/config.yml` if needed
5. Use the `/pcl` command to ensure the plugin is working properly

## Instalasi

1. Unduh file `.jar` terbaru dari [Releases](https://github.com/ranggagibran/proactivechunkloader/releases) atau [SpigotMC](https://www.spigotmc.org/resources/proactivechunkloader.xxxxx/)
2. Letakkan file `.jar` di folder `plugins` server Anda
3. Mulai ulang server atau muat ulang plugin dengan `/reload confirm`
4. Sesuaikan konfigurasi di `plugins/ProactiveChunkLoader/config.yml` jika diperlukan
5. Gunakan perintah `/pcl` untuk memastikan plugin berfungsi dengan benar

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pcl help` | Display help | `proactivechunkloader.command` |
| `/pcl info` | Display plugin information | `proactivechunkloader.command` |
| `/pcl stats` | Display performance statistics | `proactivechunkloader.stats` |
| `/pcl reload` | Reload configuration | `proactivechunkloader.admin` |

## Perintah

| Perintah | Deskripsi | Izin |
|----------|-----------|------|
| `/pcl help` | Menampilkan bantuan | `proactivechunkloader.command` |
| `/pcl info` | Menampilkan informasi plugin | `proactivechunkloader.command` |
| `/pcl stats` | Menampilkan statistik performa | `proactivechunkloader.stats` |
| `/pcl reload` | Memuat ulang konfigurasi | `proactivechunkloader.admin` |

## Configuration

This plugin provides various configuration options that can be adjusted in `config.yml`:

## Konfigurasi

Plugin ini menyediakan berbagai opsi konfigurasi yang dapat disesuaikan di `config.yml`:

```yaml
# ProactiveChunkLoader Configuration
# Enhanced version with advanced chunk loading and adaptive performance

# Maximum chunks to load per task execution
max-chunks-per-tick: 1

# How many chunks beyond view distance to preload
frontier-distance:
  min: 1  # Start preloading from (view-distance + min) chunks away
  max: 3  # Preload up to (view-distance + max) chunks away

# Width of the preloading "cone" in each direction (1 = 3x3, 2 = 5x5)
frontier-width: 1

# Debug mode (enable for verbose logging)
debug: false

# Performance settings
performance:
  # Adjust chunk loading based on server TPS
  adaptive-tps-scaling: true
  
  # Minimum TPS threshold before reducing chunk loading rate
  minimum-tps: 18.0

# Advanced features
advanced:
  # Enable adaptive cone prediction based on player movement
  adaptive-cone-prediction: true
  
  # Number of movement records to keep per player for prediction
  player-history-size: 10
  
  # How much player speed influences chunk loading priority (higher = more influence)
  speed-influence-factor: 1.0
  
  # Enable extra detailed logging (performance impact)
  extra-detailed-logging: false
```

### Configuration Explanation

- **max-chunks-per-tick** - Maximum number of chunks to load per tick. Higher values = faster loading but can impact performance.
- **frontier-distance**
  - **min** - Minimum distance outside the player's view-distance to start loading chunks
  - **max** - Maximum distance outside the player's view-distance to load chunks
- **frontier-width** - Width of the loading "cone". The higher the value, the wider the area loaded around the movement direction.
- **debug** - Enable for more detailed logging.
- **performance.adaptive-tps-scaling** - Automatically adjust chunk loading rate based on server TPS.
- **performance.minimum-tps** - TPS threshold at which the plugin starts reducing chunk loading.
- **advanced.adaptive-cone-prediction** - Use player movement history to predict direction of movement.
- **advanced.player-history-size** - Number of movement records stored for each player.
- **advanced.speed-influence-factor** - How much player speed influences chunk loading priority.
- **advanced.extra-detailed-logging** - Enable for very detailed logging (may affect performance).

### Penjelasan Konfigurasi

- **max-chunks-per-tick** - Jumlah maksimum chunk yang dimuat per tick. Nilai lebih tinggi = pemuatan lebih cepat tetapi bisa berdampak pada performa.
- **frontier-distance**
  - **min** - Jarak minimum di luar view-distance pemain untuk mulai memuat chunk
  - **max** - Jarak maksimum di luar view-distance pemain untuk memuat chunk
- **frontier-width** - Lebar "cone" pemuatan. Semakin tinggi nilai, semakin lebar area yang dimuat di sekitar arah pergerakan.
- **debug** - Aktifkan untuk logging lebih detail.
- **performance.adaptive-tps-scaling** - Secara otomatis menyesuaikan tingkat pemuatan chunk berdasarkan TPS server.
- **performance.minimum-tps** - Batas TPS di mana plugin mulai mengurangi pemuatan chunk.
- **advanced.adaptive-cone-prediction** - Menggunakan riwayat pergerakan pemain untuk memprediksi arah gerakan.
- **advanced.player-history-size** - Jumlah catatan pergerakan yang disimpan untuk setiap pemain.
- **advanced.speed-influence-factor** - Seberapa besar kecepatan pemain memengaruhi prioritas pemuatan chunk.
- **advanced.extra-detailed-logging** - Aktifkan untuk logging sangat detail (dapat memengaruhi performa).

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `proactivechunkloader.command` | Access to basic commands | `true` |
| `proactivechunkloader.stats` | View performance statistics | `op` |
| `proactivechunkloader.admin` | Administrative access (reload) | `op` |

## Izin

| Izin | Deskripsi | Default |
|------|-----------|---------|
| `proactivechunkloader.command` | Akses ke perintah dasar | `true` |
| `proactivechunkloader.stats` | Melihat statistik performa | `op` |
| `proactivechunkloader.admin` | Akses administratif (reload) | `op` |

## Metrics and Statistics

This plugin provides comprehensive metrics through the `/pcl stats` command:

- Total chunks loaded
- Number of chunks loaded in the last minute
- Current queue size
- Average loading time
- Current server TPS
- Current active loading settings

## Metrik dan Statistik

Plugin ini menyediakan metrik komprehensif melalui perintah `/pcl stats`:

- Total chunk yang telah dimuat
- Jumlah chunk yang dimuat dalam menit terakhir
- Ukuran antrian saat ini
- Waktu pemuatan rata-rata
- TPS server saat ini
- Pengaturan pemuatan aktif saat ini

## Performance Tips

1. Start with the default settings and adjust based on your needs.
2. If your server experiences lag, reduce `max-chunks-per-tick` or increase `minimum-tps`.
3. For servers with many players, the `adaptive-tps-scaling` feature helps balance chunk loading with server performance.
4. The `adaptive-cone-prediction` feature is especially helpful for players moving quickly (such as when flying or using fast vehicles).
5. Enable `debug` only when needed as it can flood the console with messages.

## Tips Performa

1. Mulai dengan pengaturan default dan sesuaikan berdasarkan kebutuhan.
2. Jika server mengalami lag, kurangi `max-chunks-per-tick` atau tingkatkan `minimum-tps`.
3. Untuk server dengan banyak pemain, fitur `adaptive-tps-scaling` sangat membantu menyeimbangkan pemuatan chunk dengan performa server.
4. Fitur `adaptive-cone-prediction` sangat membantu untuk pemain yang bergerak cepat (seperti saat terbang atau menggunakan kendaraan cepat).
5. Aktifkan `debug` hanya saat diperlukan karena dapat membanjiri konsol dengan pesan.

## For Developers

This plugin uses Maven as the build tool. To compile:

```bash
mvn clean package
```

The jar file will be generated in the `target` folder.

## Dependencies

- PaperMC API 1.21.1+
- Java 17+

## Untuk Developer

Plugin ini menggunakan Maven sebagai build tool. Untuk mengkompilasi:

```bash
mvn clean package
```

File jar akan dihasilkan di folder `target`.

## Dependensi

- PaperMC API 1.21.1+
- Java 17+

## Contributing

Contributions are welcome! If you'd like to contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin new-feature`)
5. Create a new Pull Request

## Kontribusi

Kontribusi sangat diterima! Jika Anda ingin berkontribusi:

1. Fork repositori
2. Buat branch fitur (`git checkout -b fitur-baru`)
3. Commit perubahan Anda (`git commit -am 'Menambahkan fitur baru'`)
4. Push ke branch (`git push origin fitur-baru`)
5. Buat Pull Request baru

## Support

If you experience issues or have questions:

- Create an [Issue](https://github.com/ranggagibran/proactivechunkloader/issues) on GitHub

## Support

Jika Anda mengalami masalah atau memiliki pertanyaan:

- Buat [Issue](https://github.com/ranggagibran/proactivechunkloader/issues) di GitHub

## License

This project is licensed under the [MIT License](LICENSE).

## Author

Created by [rnggagib](https://github.com/ranggagibran)

---

## Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

## Author

Dibuat oleh [rnggagib](https://github.com/ranggagibran)

---
