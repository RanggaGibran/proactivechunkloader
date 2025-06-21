# ProactiveChunkLoader

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.1-green.svg)
![PaperMC](https://img.shields.io/badge/server-PaperMC-yellow.svg)

ProactiveChunkLoader adalah plugin Minecraft untuk server PaperMC yang secara cerdas dan proaktif memuat (pre-generates) chunk di sekitar pemain yang sedang bergerak, dengan tujuan untuk mengurangi atau menghilangkan lag spike (penurunan TPS) yang terjadi ketika pemain memasuki area dunia yang belum pernah dijelajahi.

## Fitur

- **Pemuat Chunk Proaktif** - Memuat chunk di depan pemain berdasarkan arah gerakan mereka
- **Prioritas Cerdas** - Mengutamakan chunk yang paling mungkin dimasuki pemain
- **Prediksi Arah Adaptif** - Memprediksi arah pergerakan pemain berdasarkan riwayat dan kecepatan
- **Pengaturan Performa Cerdas** - Menyesuaikan tingkat pemuatan chunk berdasarkan TPS server
- **Struktur "Cone" Yang Dapat Dikonfigurasi** - Mengatur berapa banyak chunk yang dimuat ke arah samping dari arah gerakan
- **Statistik Performa** - Melacak metrik pemuatan chunk untuk analisis dan pemantauan
- **Perintah Lengkap** - Untuk melihat info, statistik, dan memuat ulang konfigurasi

## Perbedaan dengan Plugin Chunky

Berbeda dengan plugin pre-generator chunk massal seperti Chunky, ProactiveChunkLoader:

- Bekerja secara real-time saat pemain bergerak
- Hanya memuat chunk yang benar-benar dibutuhkan berdasarkan arah pergerakan pemain
- Menggunakan lebih sedikit resources server karena lebih selektif
- Memprioritaskan chunk berdasarkan kemungkinan pemain akan mengunjunginya

## Instalasi

1. Unduh file `.jar` terbaru dari [Releases](https://github.com/yourusername/ProactiveChunkLoader/releases) atau [SpigotMC](https://www.spigotmc.org/resources/proactivechunkloader.12345/)
2. Letakkan file `.jar` di folder `plugins` server Anda
3. Mulai ulang server atau muat ulang plugin dengan `/reload confirm`
4. Sesuaikan konfigurasi di `plugins/ProactiveChunkLoader/config.yml` jika diperlukan
5. Gunakan perintah `/pcl` untuk memastikan plugin berfungsi dengan benar

## Perintah

| Perintah | Deskripsi | Izin |
|----------|-----------|------|
| `/pcl help` | Menampilkan bantuan | `proactivechunkloader.command` |
| `/pcl info` | Menampilkan informasi plugin | `proactivechunkloader.command` |
| `/pcl stats` | Menampilkan statistik performa | `proactivechunkloader.stats` |
| `/pcl reload` | Memuat ulang konfigurasi | `proactivechunkloader.admin` |

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

## Izin

| Izin | Deskripsi | Default |
|------|-----------|---------|
| `proactivechunkloader.command` | Akses ke perintah dasar | `true` |
| `proactivechunkloader.stats` | Melihat statistik performa | `op` |
| `proactivechunkloader.admin` | Akses administratif (reload) | `op` |

## Metrik dan Statistik

Plugin ini menyediakan metrik komprehensif melalui perintah `/pcl stats`:

- Total chunk yang telah dimuat
- Jumlah chunk yang dimuat dalam menit terakhir
- Ukuran antrian saat ini
- Waktu pemuatan rata-rata
- TPS server saat ini
- Pengaturan pemuatan aktif saat ini

## Tips Performa

1. Mulai dengan pengaturan default dan sesuaikan berdasarkan kebutuhan.
2. Jika server mengalami lag, kurangi `max-chunks-per-tick` atau tingkatkan `minimum-tps`.
3. Untuk server dengan banyak pemain, fitur `adaptive-tps-scaling` sangat membantu menyeimbangkan pemuatan chunk dengan performa server.
4. Fitur `adaptive-cone-prediction` sangat membantu untuk pemain yang bergerak cepat (seperti saat terbang atau menggunakan kendaraan cepat).
5. Aktifkan `debug` hanya saat diperlukan karena dapat membanjiri konsol dengan pesan.

## Untuk Developer

Plugin ini menggunakan Maven sebagai build tool. Untuk mengkompilasi:

```bash
mvn clean package
```

File jar akan dihasilkan di folder `target`.

## Dependensi

- PaperMC API 1.21.1+
- Java 17+

## Kontribusi

Kontribusi sangat diterima! Jika Anda ingin berkontribusi:

1. Fork repositori
2. Buat branch fitur (`git checkout -b fitur-baru`)
3. Commit perubahan Anda (`git commit -am 'Menambahkan fitur baru'`)
4. Push ke branch (`git push origin fitur-baru`)
5. Buat Pull Request baru

## Support

Jika Anda mengalami masalah atau memiliki pertanyaan:

- Buat [Issue](https://github.com/yourusername/ProactiveChunkLoader/issues) di GitHub
- Posting di [thread diskusi SpigotMC](https://www.spigotmc.org/threads/proactivechunkloader.12345/)

## Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

## Author

Dibuat oleh [rnggagib](https://github.com/ranggagibran)

---

Jika Anda menganggap plugin ini berguna, pertimbangkan untuk [memberikan donasi](https://link-to-your-donation-page) atau memberikan rating di [SpigotMC](https://www.spigotmc.org/resources/proactivechunkloader.12345/).
