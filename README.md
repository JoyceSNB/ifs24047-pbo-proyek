# 🌸 Katalog Bunga — Aplikasi Web Manajemen Bunga

Aplikasi web berbasis **Spring Boot** untuk mengelola katalog bunga. Dibuat sebagai proyek individu mata kuliah **Pemrograman Berorientasi Objek (PBO)** di **Institut Teknologi Del**.

---
## 📸 Tampilan Aplikasi

| Daftar | Login | Dashboard |
|---|---|---|
| ![Daftar](screenshots/Daftar.png) | ![Login](screenshots/Login.png) | ![Katalog](screenshots/Katalog.png) |

| Tambah Bunga | Statistik | Logout |
|---|---|---|
| ![Tambah](screenshots/Tambah%20Bunga.png) | ![Statistik](screenshots/Statistik.png) | ![Logout](screenshots/Logout.png) |
---

## ✨ Fitur

- 🔐 **Autentikasi** — Registrasi dan login pengguna dengan manajemen sesi
- 🌺 **Koleksi Bunga** — Tambah dan kelola data bunga (nama, spesies, harga, stok, foto, deskripsi)
- 📊 **Dashboard Statistik** — Visualisasi proporsi stok dan bunga terlaris
- 🖼️ **Unggah Foto** — Upload foto bunga saat menambah data baru
- 🚪 **Konfirmasi Logout** — Alur keluar sesi yang aman

---

## 🛠️ Teknologi yang Digunakan

| Lapisan | Teknologi |
|---|---|
| Backend | Java, Spring Boot |
| Frontend | HTML, CSS |
| Build Tool | Maven |
| Pengujian | JUnit, JaCoCo (code coverage) |
| CI/CD | GitHub Actions |

---

## 🚀 Cara Menjalankan

### Prasyarat
- Java 17+
- Maven

### Langkah Instalasi

```bash
# Clone repositori
git clone https://github.com/JoyceSNB/ifs24047-pbo-proyek.git
cd ifs24047-pbo-proyek

# Install dependensi
mvn clean install

# Jalankan aplikasi
mvn spring-boot:run
```

Buka browser dan akses **http://localhost:8080**

---

## 🧪 Menjalankan Pengujian

```bash
# Jalankan pengujian dan laporan coverage
mvn clean test

# Buka laporan JaCoCo
# Windows:
mvn clean test; start target\site\jacoco\index.html

# Mac:
mvn clean test && open target/site/jacoco/index.html

# Linux:
mvn clean test && xdg-open target/site/jacoco/index.html
```

---

## 📂 Struktur Proyek

## 📂 Struktur Proyek

```
IFS24047-PBO-PROYEK/
├── src/
│   ├── main/
│   │   ├── java/org/delcom/app/
│   │   │   ├── entities/        # Model data (User, Flower, dsb)
│   │   │   ├── interceptors/    # Auth interceptor
│   │   │   ├── repositories/    # Akses database
│   │   │   └── services/        # Logika bisnis
│   │   └── resources/           # Template HTML, aset statis
│   └── test/
│       └── java/org/delcom/app/
│           ├── entities/        # Unit test entity
│           ├── interceptors/    # Unit test interceptor
│           ├── repositories/    # Unit test repository
│           └── services/        # Unit test service
├── screenshots/                 # Tampilan aplikasi
├── uploads/                     # File foto bunga
├── README.md
└── pom.xml
```

---

## 👩‍💻 Pembuat

**Joyce Stephanie**  
Institut Teknologi Del — Teknik Informatika

---

## 📝 Lisensi

Proyek ini dibuat untuk keperluan **pendidikan** sebagai bagian dari mata kuliah PBO.

# 🌸 Katalog Bunga — Aplikasi Web Manajemen Bunga

![Java CI](https://github.com/JoyceSNB/ifs24047-pbo-proyek/actions/workflows/maven.yml/badge.svg)