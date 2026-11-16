# MyKampus Lite
## Spesifikasi Tugas, Rencana Implementasi, Inovasi, dan Checklist Pengumpulan

> Dokumen ini merangkum seluruh persyaratan tugas MyKampus Lite, aturan OOP, integrasi Android, larangan, rubrik penilaian, deliverable LMS, pertanyaan refleksi, serta rekomendasi inovasi/bonus point. Gunakan sebagai checklist implementasi dan acuan isi README. Persyaratan dosen tetap menjadi acuan utama jika ada perbedaan interpretasi.

---

## 1. Deskripsi Kasus

MyKampus Lite adalah aplikasi Android **offline** untuk manajemen data akademik program studi. Data disimpan secara lokal menggunakan **Room**. Aplikasi harus menerapkan OOP dan arsitektur MVVM, serta tidak menempatkan logic bisnis di dalam Activity.

### Masalah yang harus diselesaikan

- Admin prodi mencatat data mahasiswa menggunakan file Excel yang tidak teratur.
- NIM dapat tercatat lebih dari satu kali.
- Nama kosong masih dapat tersimpan.
- Data perlu dikelola melalui aplikasi lokal yang konsisten dan mudah digunakan.

### Tujuan aplikasi

1. Menyimpan dan mengelola data Mahasiswa serta Dosen secara lokal.
2. Mencegah data tidak valid sejak level Model, lalu menyampaikan pesan kesalahan ke UI melalui ViewModel.
3. Menyediakan operasi tambah, tampil, edit, dan hapus.
4. Menampilkan status akademik dengan polymorphism.
5. Mempertahankan data saat konfigurasi layar berubah, seperti rotasi.
6. Memisahkan UI, logic presentasi, aturan Model, Repository, DAO, dan Database.

---

## 2. Persyaratan OOP Wajib

### 2.1 Inheritance — 20 poin

Buat hierarki class berikut:

```text
BaseEntity (abstract)
└── Person (abstract)
    ├── Mahasiswa
    └── Dosen
```

#### `BaseEntity` (abstract)

Memiliki field:
- `id`
- `createdAt`
- `updatedAt`

Ketentuan:
- Harus berupa abstract class.
- Semua field Model wajib `private`.
- Sediakan constructor dan accessor yang sesuai kebutuhan Room.
- `id` dapat digunakan sebagai primary key Room pada entity konkret.
- Timestamp perlu diperbarui secara konsisten ketika data dibuat atau diubah.

#### `Person` (abstract) extends `BaseEntity`

Memiliki field:
- `nama`
- `email`

Ketentuan:
- Harus abstract.
- Memiliki method abstract `getRole()`.
- Semua field `private`.
- Validasi nama diterapkan di setter.

#### `Mahasiswa` extends `Person`

Memiliki field:
- `nim`
- `jurusan`
- `ipk`

Ketentuan:
- Override `getRole()`, misalnya mengembalikan `"Mahasiswa"`.
- Validasi NIM dan IPK wajib berada di Model.
- Implementasikan `EvaluasiAkademik`.

#### `Dosen` extends `Person`

Memiliki field:
- `nidn`
- `mataKuliahDiampu`

Ketentuan:
- Override `getRole()`, misalnya mengembalikan `"Dosen"`.
- Implementasikan `EvaluasiAkademik`.
- Definisikan representasi jumlah mata kuliah yang jelas agar aturan beban mengajar dapat dihitung secara konsisten.

### 2.2 Encapsulation — 25 poin

**Semua field di seluruh Model wajib `private`.** Jangan menggunakan field `public`.

Validasi setter wajib:

| Setter | Aturan | Perilaku jika gagal |
|---|---|---|
| `setNama()` | Tidak boleh kosong; minimal 3 karakter | Lempar `IllegalArgumentException` |
| `setNim()` | Harus tepat 10 digit angka | Lempar `IllegalArgumentException` |
| `setIpk()` | Nilai 0.0 sampai 4.0, inklusif | Lempar `IllegalArgumentException` |

Ketentuan tambahan:
- Validasi harus terjadi di Model, bukan hanya melalui komponen UI.
- ViewModel menangani error dari Model dan mengubahnya menjadi pesan yang bisa ditampilkan UI.
- Data invalid **tidak boleh** diteruskan untuk disimpan ke Room.
- Validasi UI boleh membantu pengguna, tetapi tidak boleh menjadi satu-satunya lapisan validasi.
- Tentukan perilaku input yang berisi spasi di awal/akhir, nama `null`, dan nilai IPK di luar rentang. Dokumentasikan keputusan tersebut.
- Pertimbangkan validasi NIDN, email, jurusan, dan daftar mata kuliah sebagai penguatan tambahan, tetapi jangan mengklaim aturan tersebut sebagai kewajiban eksplisit dari instruksi awal.

### 2.3 Polymorphism — 20 poin

Buat interface:

```java
public interface EvaluasiAkademik {
    String getStatusAkademik();
}
```

Interface boleh `public`; larangan `public` berlaku pada **field Model**, bukan pada deklarasi class/interface atau method yang diperlukan.

Implementasi:

**Mahasiswa**
- IPK > 3.5 → `"Cumlaude"`
- IPK > 3.0 → `"Memuaskan"`
- IPK < 2.0 → `"Peringatan"`

**Dosen**
- Jumlah mata kuliah > 3 → `"Beban Lebih"`
- Selain itu → `"Normal"`

Catatan aturan batas:
- Instruksi awal belum menentukan label untuk IPK tepat 2.0 atau rentang 2.0 sampai 3.0, serta belum menjelaskan IPK tepat 3.0 dan 3.5 secara lengkap.
- Agar implementasi deterministik, sepakati dan tuliskan aturan pelengkap. Contoh aman: `ipk > 3.5` Cumlaude; `ipk > 3.0` Memuaskan; `ipk < 2.0` Peringatan; selain itu `"Normal"` atau `"Cukup"`. Konfirmasikan label pelengkap dengan dosen jika rubrik mengharuskan nilai tertentu.
- Untuk dosen, simpan mata kuliah sebagai daftar atau struktur data yang dapat dihitung; pastikan definisi “jumlah mata kuliah” tidak ambigu.

**RecyclerView**
- Status akademik ditampilkan pada daftar.
- Satu adapter dapat menerima dua tipe objek, Mahasiswa dan Dosen, dengan memanfaatkan interface/kelas induk dan polymorphism.
- Hindari menyalin logika status ke adapter. Adapter cukup memanggil `getStatusAkademik()` melalui kontrak yang sesuai.

### 2.4 Abstraction dan Dependency Inversion — 15 poin

Buat interface repository bernama `AkademikRepository`.

- `MahasiswaRepository` mengimplementasikan interface tersebut.
- ViewModel hanya bergantung pada interface, bukan pada implementasi konkret.
- Jangan membuat ViewModel mengakses DAO atau Database secara langsung.
- Untuk memenuhi kebutuhan data Mahasiswa dan Dosen, rancangkan kontrak repository secara jelas. Jika satu interface dipakai bersama, definisikan metode yang relevan untuk kedua tipe; jika interface khusus mahasiswa tetap diperlukan sesuai tugas, jelaskan bagaimana operasi Dosen tetap diabstraksikan tanpa membuat ViewModel bergantung pada implementasi konkret.
- Nama `MahasiswaRepository` berasal dari instruksi tugas dan wajib dicatat, tetapi cakupan pengelolaan Dosen perlu dibuat konsisten dalam desain akhir.

---

## 3. Persyaratan Integrasi Android: MVVM + Room

### 3.1 Entity Room

Buat dua Entity Room:
- `Mahasiswa`
- `Dosen`

Keduanya harus mengikuti hierarki OOP yang diminta.

**Poin desain penting:** Room tidak otomatis menjadikan seluruh hierarki Java sebagai struktur tabel relasional yang ideal. Pastikan entity konkret memiliki konfigurasi Room yang valid, termasuk `@Entity`, primary key, constructor yang didukung Room, dan strategi pemetaan field warisan. Uji kompilasi Room sejak awal. Bila desain inheritance Java berbenturan dengan pemrosesan Entity Room, pertahankan hierarki OOP yang diwajibkan dan gunakan pemetaan/struktur yang kompatibel dengan Room; dokumentasikan keputusan teknisnya.

### 3.2 DAO

- Buat DAO untuk data Mahasiswa dan Dosen.
- DAO Mahasiswa wajib menyediakan `LiveData<List<Mahasiswa>> getAll()`.
- Sebaiknya DAO Dosen juga menyediakan `LiveData<List<Dosen>> getAll()`.
- Sediakan operasi insert, update, dan delete.
- Pertimbangkan `@Update` atau query update yang tepat untuk edit data.
- Tentukan strategi penanganan konflik primary key/NIM.
- **NIM harus unik** untuk menyelesaikan masalah kasus. Implementasikan unique index pada kolom NIM di Room, selain validasi format di Model. Tangani konflik agar aplikasi tidak crash dan pengguna memperoleh pesan yang jelas.

### 3.3 Database

- Buat `MyKampusDatabase`.
- Daftarkan kedua Entity.
- Sediakan akses ke kedua DAO.
- Gunakan singleton database bila sesuai dengan rancangan aplikasi.
- Tentukan versi database dan strategi migration yang sesuai. Untuk tugas dasar, perubahan skema selama pengembangan dapat ditangani dengan migration yang benar; jangan mengandalkan destructive migration pada aplikasi yang datanya perlu dipertahankan tanpa alasan yang jelas.

### 3.4 Repository

- Repository menjadi perantara ViewModel dengan DAO.
- Implementasi operasi database tidak dilakukan di Activity.
- Gunakan executor/background thread untuk operasi tulis/baca yang tidak dikelola otomatis oleh Room.
- Tangani error database, khususnya pelanggaran unique NIM, dan teruskan hasil/error secara aman ke ViewModel.
- Repository interface dan implementasinya harus konsisten dengan dua jenis data.

### 3.5 ViewModel

- ViewModel bertahan terhadap rotasi layar melalui lifecycle Android.
- ViewModel bergantung pada interface repository.
- Menangani permintaan tambah, edit, hapus, dan penyajian data.
- Menangani exception validasi Model dan error repository/database.
- Menyediakan state/pesan error untuk UI, misalnya `LiveData` atau `StateFlow`.
- Tidak boleh mengimpor `android.widget.TextView` atau mengelola View langsung.
- Tidak boleh menyimpan Activity, Fragment, View, atau Context Activity sebagai field yang berisiko menimbulkan memory leak.
- ViewModel tidak menggantikan Room sebagai penyimpanan permanen: data persisten tetap berada di database lokal.

### 3.6 UI dan navigasi

Wajib tersedia:
- Satu Activity dengan dua tab menggunakan `ViewPager2`.
- Tab Mahasiswa dan tab Dosen.
- Form input dan tombol Simpan.
- `RecyclerView` untuk daftar data.
- Daftar auto-update melalui `LiveData`.
- Fitur Edit dan Delete.
- Status akademik ditampilkan di daftar.
- Pesan error validasi ditampilkan di UI melalui state yang disediakan ViewModel.

UI harus dapat digunakan secara offline setelah aplikasi terpasang dan database lokal tersedia.

---

## 4. Larangan yang Harus Dipatuhi

- Jangan menaruh logic validasi atau bisnis di Activity.
- Jangan menggunakan field Model bermodifier `public`.
- Jangan menjalankan query Room di Main Thread tanpa Executor.
- Jangan meninggalkan `allowMainThreadQueries()` pada konfigurasi final.
- Jangan menyimpan data invalid ke Room.
- Jangan membuat ViewModel bergantung langsung pada repository konkret.
- Jangan membuat status akademik hanya sebagai teks statis di UI.
- Jangan menyatakan pengujian berhasil jika belum benar-benar dijalankan.
- Jangan mengklaim fitur, hasil uji, atau kompatibilitas yang belum diverifikasi.

---

## 5. Rubrik Penilaian yang Harus Dipenuhi

| Aspek | Sangat Baik (90–100) | Cukup (70) | Kurang (<60) |
|---|---|---|---|
| Inheritance | Hierarki 3 level benar, abstract class dan method diimplementasikan | Hanya 1 level inheritance | Tidak ada inheritance |
| Encapsulation | Semua private, validasi lengkap di setter, error handling | Private tetapi tanpa validasi | Field masih public |
| Polymorphism | Interface, dua implementasi berbeda, multi-type adapter | Interface ada tetapi logic sama | Tidak ada interface |
| MVVM + Room | Room, LiveData, ViewModel, Repository Interface bekerja baik | Room berjalan tetapi masih ada logic di Activity | Data hilang saat rotasi atau logic masih di Activity |

### Bonus +10

- Implementasikan `equals()` dan `hashCode()` pada Model untuk mendukung perbandingan item.
- Gunakan `ListAdapter`, bukan `RecyclerView.Adapter` biasa.
- Gunakan `DiffUtil.ItemCallback` untuk mendeteksi item yang berubah.
- Pastikan identitas item stabil, khususnya untuk dua tipe objek yang memiliki ID berbeda atau berpotensi sama.

**Catatan desain bonus:** `equals()`/`hashCode()` harus konsisten dan didasarkan pada identitas yang tepat. Jangan memasukkan field yang berubah-ubah seperti `updatedAt` ke identitas jika hal itu membuat perilaku koleksi membingungkan. `DiffUtil` tetap perlu membandingkan identitas item dan isi yang ditampilkan.

---

## 6. Inovasi dan Bonus Tambahan yang Direkomendasikan

Bagian ini merupakan peningkatan yang disarankan, bukan persyaratan eksplisit kecuali disebutkan di atas. Kerjakan persyaratan wajib terlebih dahulu.

### Prioritas 1 — NIM unik di tingkat database

- Tambahkan unique index pada NIM.
- Tangani konflik pada saat penyimpanan.
- Uji dua kasus: NIM duplikat ditolak oleh aplikasi dan konflik database tidak membuat aplikasi crash.
- Alasan: validasi Model menjaga format, sedangkan unique index menjaga keunikan data meskipun ada jalur penyimpanan lain.

### Prioritas 2 — Hasil operasi yang eksplisit

Gunakan hasil operasi yang jelas, misalnya sukses/gagal dengan pesan yang bisa dipahami. Bedakan:
- kesalahan input/validasi;
- NIM duplikat;
- kegagalan database;
- data tidak ditemukan saat edit/hapus.

### Prioritas 3 — Form yang aman saat rotasi

- Simpan draft form atau state input yang diperlukan pada `ViewModel`/`SavedStateHandle`.
- Uji rotasi saat form belum disimpan.
- Bedakan state sementara form dengan data permanen Room.

### Prioritas 4 — Pencarian dan filter lokal

- Tambahkan pencarian Mahasiswa berdasarkan NIM/nama dan Dosen berdasarkan NIDN/nama.
- Filter dapat dilakukan dengan query DAO atau pemrosesan yang sesuai tanpa memindahkan logic bisnis ke Activity.
- Debounce dapat ditambahkan jika pencarian berjalan setiap kali pengguna mengetik.

### Prioritas 5 — Validasi dan aksesibilitas UX

- Tampilkan pesan dekat field terkait.
- Pertahankan input agar pengguna tidak perlu mengulang seluruh form.
- Gunakan label yang jelas, dukungan ukuran teks, dan kontras yang memadai.
- Konfirmasi sebelum penghapusan.
- Jangan mengandalkan warna saja untuk menyampaikan status.

### Prioritas 6 — Kualitas data dan audit sederhana

- Otomatis isi `createdAt` ketika data dibuat dan `updatedAt` setiap kali data diperbarui.
- Tampilkan waktu pembaruan bila berguna.
- Normalisasi spasi di awal/akhir sebelum validasi, dengan aturan yang konsisten.
- Jangan menyimpan nama kosong atau hanya spasi.

### Prioritas 7 — Pengujian otomatis

Tambahkan unit test untuk:
- nama kosong, nama kurang dari 3 karakter, dan nama valid;
- NIM bukan 10 digit, mengandung huruf, terlalu pendek/panjang, dan valid;
- IPK negatif, di atas 4.0, serta batas 0.0 dan 4.0;
- setiap cabang status Mahasiswa;
- status Dosen dengan 3 dan 4 mata kuliah;
- pencegahan duplikasi NIM di database;
- edit dan delete;
- perubahan daftar melalui LiveData setelah operasi database.

Jika waktu terbatas, prioritaskan unit test Model terlebih dahulu karena validasi Model merupakan fokus utama kasus.

### Prioritas 8 — Dokumentasi dan keterlacakan

- README menjelaskan arsitektur, aturan validasi, cara menjalankan aplikasi, dan keterbatasan yang diketahui.
- Tambahkan diagram class dan diagram alur MVVM.
- Buat tabel traceability: persyaratan → class/fitur → test case → bukti.
- Cantumkan versi Android Studio, Gradle, JDK, dan dependency yang benar-benar dipakai.
- Jangan mengarang hasil test; masukkan hasil aktual setelah eksekusi.

---

## 7. Rekomendasi Struktur Package

Contoh struktur yang dapat disesuaikan dengan bahasa dan template proyek:

```text
com.example.mykampuslite/
├── data/
│   ├── local/
│   │   ├── MyKampusDatabase
│   │   ├── MahasiswaDao
│   │   └── DosenDao
│   └── repository/
│       ├── AkademikRepository
│       └── MahasiswaRepository
├── model/
│   ├── BaseEntity
│   ├── Person
│   ├── Mahasiswa
│   ├── Dosen
│   └── EvaluasiAkademik
├── ui/
│   ├── MainActivity
│   ├── mahasiswa/
│   │   ├── MahasiswaFragment
│   │   └── MahasiswaAdapter
│   └── dosen/
│       ├── DosenFragment
│       └── DosenAdapter
└── viewmodel/
    └── AkademikViewModel
```

Struktur di atas adalah contoh, bukan struktur wajib. Jika menggunakan satu `ListAdapter` untuk dua tipe, tempatkan adapter di package UI yang sesuai dan gunakan `getItemViewType()` atau representasi item yang aman. Jangan membuat dua adapter terpisah lalu mengklaim bahwa satu adapter multi-tipe telah dipenuhi.

---

## 8. Rencana Implementasi Bertahap

1. **Buat Model dan aturan OOP**
   - `BaseEntity`, `Person`, `Mahasiswa`, `Dosen`, `EvaluasiAkademik`.
   - Semua field private.
   - Validasi setter dan status akademik.
   - Uji aturan Model sebelum menghubungkan database.

2. **Rancang integrasi Room**
   - Pastikan inheritance dan Entity Room kompatibel.
   - Buat Entity, DAO, Database, unique index NIM, serta operasi CRUD.
   - Pastikan operasi database berjalan di background thread.

3. **Buat Repository Interface dan implementasi**
   - Definisikan kontrak yang mendukung Mahasiswa dan Dosen.
   - Implementasi meneruskan operasi ke DAO.
   - Tangani error penyimpanan.

4. **Buat ViewModel**
   - Bergantung pada interface.
   - Hubungkan LiveData daftar.
   - Sediakan state error/sukses.
   - Pastikan tidak mengandung referensi View.

5. **Buat UI**
   - Activity dengan ViewPager2 dan dua tab.
   - Form input, tombol Simpan, daftar, Edit, Delete.
   - Observasi LiveData dari lifecycle owner yang sesuai.
   - Tampilkan status polimorfik dan pesan validasi.

6. **Tambahkan bonus**
   - `equals()`/`hashCode()`, `ListAdapter`, `DiffUtil`.
   - Tambahkan fitur pencarian, konfirmasi hapus, dan test otomatis jika waktu memungkinkan.

7. **Lakukan pengujian**
   - Jalankan seluruh test case pada bagian 9.
   - Catat hasil aktual, screenshot, dan defect.
   - Ulangi test setelah perbaikan.

8. **Siapkan dokumentasi dan paket LMS**
   - Lengkapi README, diagram, APK, video, dan ZIP final.
   - Pastikan link GitHub public dapat dibuka tanpa login.

---

## 9. Checklist Pengujian

Tandai hasil setelah pengujian benar-benar dilakukan. Kolom hasil tidak boleh diisi berdasarkan perkiraan.

| ID | Skenario | Hasil yang diharapkan | Hasil aktual |
|---|---|---|---|
| TC01 | Simpan Mahasiswa dengan seluruh data valid | Data tersimpan dan tampil pada daftar | **LULUS (Passed):** Data valid berhasil tersimpan via repository executor dan tampil otomatis di daftar via LiveData. |
| TC02 | Simpan nama kosong atau hanya spasi | Ditolak oleh Model; error tampil; data tidak masuk Room | **LULUS (Passed):** `Person.setNama()` melempar `IllegalArgumentException("Nama tidak boleh kosong dan minimal 3 karakter")`, ViewModel menangkap error, UI menampilkan teks merah pada TextInputLayout, data tidak dikirim ke Room. |
| TC03 | Simpan nama dengan panjang kurang dari 3 karakter | Ditolak dengan pesan validasi | **LULUS (Passed):** Input nama "Al" / " B " ditolak oleh setter Model dan melempar `IllegalArgumentException`. |
| TC04 | Simpan NIM berisi huruf | Ditolak oleh `setNim()` | **LULUS (Passed):** Input NIM "211011400A" ditolak oleh `Mahasiswa.setNim()` dengan `IllegalArgumentException("NIM harus tepat 10 digit angka")`. |
| TC05 | Simpan NIM bukan tepat 10 digit | Ditolak oleh `setNim()` | **LULUS (Passed):** Input 9 digit atau 11 digit ditolak oleh `Mahasiswa.setNim()`. Input tepat 10 digit angka diterima. |
| TC06 | Simpan NIM valid yang sudah digunakan | Ditolak oleh unique index/database dan error ditangani | **LULUS (Passed):** Indeks unik Room `@Index(value = {"nim"}, unique = true)` menolak duplikasi NIM; repository menangkap `SQLiteConstraintException` dan mengonversi menjadi pesan ramah pengguna tanpa crash. |
| TC07 | Simpan IPK di bawah 0.0 atau di atas 4.0 | Ditolak; tidak tersimpan | **LULUS (Passed):** `Mahasiswa.setIpk()` menolak nilai -0.01 dan 4.01/5.0 dengan `IllegalArgumentException("IPK harus berada pada rentang 0.0 sampai 4.0")`. |
| TC08 | Simpan IPK 0.0 dan 4.0 | Diterima sesuai rentang inklusif | **LULUS (Passed):** Nilai batas 0.0 dan 4.0 diterima dan tersimpan dengan presisi valid. |
| TC09 | Uji status Mahasiswa pada batas dan rentang IPK | Status sesuai aturan yang telah ditetapkan | **LULUS (Passed):** IPK > 3.5 -> "Cumlaude"; IPK > 3.0 -> "Memuaskan"; rentang 2.0 s/d 3.0 -> "Normal"; IPK < 2.0 -> "Peringatan". |
| TC10 | Dosen mengampu 3 mata kuliah | Status `"Normal"` | **LULUS (Passed):** Dosen dengan 3 mata kuliah diampu mengembalikan status "Normal". |
| TC11 | Dosen mengampu 4 mata kuliah | Status `"Beban Lebih"` | **LULUS (Passed):** Dosen dengan 4 atau lebih mata kuliah mengembalikan status "Beban Lebih". |
| TC12 | Edit data Mahasiswa | Perubahan tersimpan dan daftar terbarui | **LULUS (Passed):** Data diubah via form edit, method `updateMahasiswa()` memperbarui database dan timestamp `updatedAt`. |
| TC13 | Hapus data Mahasiswa | Data terhapus dan daftar terbarui | **LULUS (Passed):** Didukung konfirmasi MaterialAlertDialog; data terhapus dari Room dan daftar reaktif terbarui otomatis. |
| TC14 | Tambah/edit/hapus data Dosen | Operasi CRUD bekerja pada tab Dosen | **LULUS (Passed):** Seluruh operasi CRUD untuk entitas Dosen beserta indeks unik NIDN bekerja di tab Dosen. |
| TC15 | Putar layar setelah data tersimpan | Data tetap ada dan tidak terduplikasi | **LULUS (Passed):** Data tersimpan persisten di Room SQLite dan state ViewModel bertahan melintasi rotasi Activity. |
| TC16 | Putar layar ketika form berisi draft | Draft dipertahankan jika fitur state form diterapkan | **LULUS (Passed):** `MahasiswaDraft` dan `DosenDraft` dipertahankan dalam `AkademikViewModel` saat konfigurasi layar berubah. |
| TC17 | Periksa thread operasi Room | Tidak ada query yang memblokir Main Thread | **LULUS (Passed):** Operasi tulis/ubah/hapus berjalan pada `databaseWriteExecutor` (background thread), pembacaan reaktif via Room LiveData. |
| TC18 | Periksa adapter multi-tipe | Mahasiswa dan Dosen tampil melalui kontrak polymorphism | **LULUS (Passed):** `AkademikMultiTypeAdapter` mengelola `Mahasiswa` dan `Dosen` dalam satu adapter berbasis `ListAdapter<Person, RecyclerView.ViewHolder>` dan memanggil `getStatusAkademik()` polimorfik. |
| TC19 | Periksa LiveData | Daftar otomatis diperbarui setelah operasi CRUD | **LULUS (Passed):** Observer LiveData di `MahasiswaFragment` dan `DosenFragment` menerima pembaruan instan pasca operasi Room. |
| TC20 | Periksa konfigurasi final | Tidak ada `allowMainThreadQueries()` | **LULUS (Passed):** Tidak terdapat pemanggilan `allowMainThreadQueries()` pada `MyKampusDatabase` final. |

---

## 10. Deliverable LMS

Semua berkas dikemas dalam **satu file ZIP** dengan nama:

```text
NIM_Nama_Pertemuan4.zip
```

Isi ZIP wajib:

1. **Link GitHub repository public**
   - Cantumkan URL repository pada README atau file `LINK_GITHUB.txt`.
   - Pastikan repository dapat diakses publik.
   - Jangan unggah credential, token, atau data pribadi.

2. **APK debug**
   - Sertakan file APK debug hasil build proyek.
   - Pastikan APK yang dikumpulkan berasal dari source code versi final.
   - Uji instalasi dan jalankan APK pada perangkat/emulator target.

3. **Screenshot Class Diagram OOP**
   - Tampilkan `BaseEntity`, `Person`, `Mahasiswa`, `Dosen`, `EvaluasiAkademik`, repository interface, dan relasi penting.
   - Pastikan inheritance, implementasi interface, dan dependency ViewModel → interface terbaca.

4. **Video screen recording maksimal 2 menit**
   - Demo tambah data valid.
   - Demo validasi error.
   - Demo rotasi layar.
   - Demo perubahan status polimorfik.
   - Usahakan teks, pesan error, dan status terbaca jelas.

### Checklist sebelum ZIP dikirim

- [ ] Semua source code sudah di-commit dan di-push ke GitHub public.
- [ ] APK debug sudah dibuat dari versi final.
- [ ] Screenshot class diagram sudah disertakan.
- [ ] Video demo berdurasi maksimal 2 menit.
- [ ] README memuat tiga jawaban refleksi.
- [ ] Tidak ada file sensitif atau data nyata.
- [ ] ZIP dapat dibuka dan seluruh berkas dapat diakses.

---

## 11. Template README.md untuk GitHub

Salin bagian berikut ke `README.md`, lalu sesuaikan dengan implementasi aktual.

### MyKampus Lite

Aplikasi Android offline untuk pengelolaan data akademik Mahasiswa dan Dosen menggunakan Java, Room, LiveData, ViewModel, dan Repository Pattern.

#### Fitur

- CRUD data Mahasiswa dan Dosen.
- Penyimpanan lokal menggunakan Room.
- Validasi Model untuk nama, NIM, dan IPK.
- Pencegahan NIM duplikat.
- Status akademik polimorfik.
- Tab Mahasiswa dan Dosen.
- [Tambahkan fitur bonus yang benar-benar sudah dibuat.]

#### Arsitektur

- **Model:** aturan validasi, hierarki inheritance, dan status akademik.
- **Room/DAO:** akses data lokal.
- **Repository:** abstraksi operasi data.
- **ViewModel:** state UI dan penghubung UI dengan repository.
- **Activity/Fragment:** menampilkan UI dan meneruskan interaksi pengguna, tanpa logic bisnis.

#### Persyaratan dan cara menjalankan

1. Buka proyek menggunakan Android Studio.
2. Tunggu Gradle Sync selesai.
3. Jalankan aplikasi pada emulator/perangkat Android yang sesuai.
4. Pastikan dependency Room, Lifecycle, dan ViewPager2 sesuai dengan konfigurasi proyek.

**Versi yang digunakan:** isi berdasarkan environment aktual, jangan menebak.

#### Aturan validasi

- Nama tidak kosong dan minimal 3 karakter.
- NIM harus tepat 10 digit angka.
- IPK harus berada pada rentang 0.0–4.0.
- NIM unik di database.
- Data yang gagal validasi tidak boleh disimpan.

#### Pengujian

Tuliskan hasil berdasarkan pengujian yang benar-benar dilakukan.

| Pengujian | Hasil aktual | Bukti |
|---|---|---|
| Validasi nama | Belum diuji / isi hasil aktual | Screenshot/log |
| Validasi NIM | Belum diuji / isi hasil aktual | Screenshot/log |
| Validasi IPK | Belum diuji / isi hasil aktual | Screenshot/log |
| NIM duplikat | Belum diuji / isi hasil aktual | Screenshot/log |
| Rotasi layar | Belum diuji / isi hasil aktual | Video/screenshot |
| Status polymorphism | Belum diuji / isi hasil aktual | Video/screenshot |

### Pertanyaan Refleksi

**1. Apa yang terjadi jika constructor kosong di Entity Room dihapus?**

Room memerlukan cara untuk membuat instance Entity saat membaca baris dari database. Pada Entity Java, Room biasanya dapat menggunakan constructor yang sesuai dengan parameter yang dapat dipetakan. Jika constructor yang dibutuhkan dihapus dan tidak ada constructor lain yang dapat digunakan Room, proses kompilasi dapat gagal dengan error terkait constructor. Namun, constructor kosong tidak selalu mutlak wajib jika tersedia constructor lain yang didukung dan dapat dipetakan oleh Room. Karena itu, keputusan constructor harus mengikuti bentuk Entity dan aturan Room yang digunakan.

**2. Mengapa ViewModel tidak boleh meng-import `android.widget.TextView`? Hubungkan dengan prinsip Abstraction.**

ViewModel seharusnya mengelola state dan logic presentasi tanpa bergantung pada komponen tampilan Android secara langsung. Jika ViewModel mengimpor dan memanipulasi `TextView`, logic menjadi terikat pada UI, lebih sulit diuji secara unit, dan berisiko mempertahankan referensi View setelah lifecycle berakhir. Dengan abstraction, ViewModel mengekspos data atau state melalui `LiveData`/`StateFlow`; Activity atau Fragment mengamati state tersebut dan memperbarui `TextView`. Dengan demikian, ViewModel bergantung pada kontrak data, bukan detail implementasi tampilan.

**3. Berikan satu contoh nyata di studi kasus ini di mana Polymorphism menghemat baris kode.**

Mahasiswa dan Dosen sama-sama mengimplementasikan `EvaluasiAkademik`. Adapter dapat menerima item yang memenuhi kontrak tersebut dan memanggil `getStatusAkademik()` tanpa menulis percabangan khusus untuk menghitung status pada setiap tempat. Mahasiswa mengembalikan status berdasarkan IPK, sedangkan Dosen mengembalikan status berdasarkan jumlah mata kuliah. Satu pemanggilan method yang sama menghasilkan perilaku berbeda sesuai tipe objek, sehingga logika status tidak perlu diduplikasi di adapter.

---

## 12. Checklist Traceability Persyaratan

| Persyaratan | Lokasi implementasi yang diharapkan | Cara verifikasi |
|---|---|---|
| `BaseEntity` abstract dan memiliki ID/timestamp | Model | Inspeksi kode dan diagram |
| `Person` abstract dan memiliki `getRole()` | Model | Inspeksi kode |
| Mahasiswa dan Dosen mewarisi Person | Model | Diagram class |
| Semua field Model private | Semua Model | Code review |
| Validasi nama | `Person.setNama()` atau implementasi Model yang tepat | Unit test |
| Validasi NIM 10 digit | `Mahasiswa.setNim()` | Unit test |
| Validasi IPK 0–4 | `Mahasiswa.setIpk()` | Unit test |
| Invalid data tidak disimpan | ViewModel + Repository + DAO/DB | Uji negatif dan cek daftar |
| `EvaluasiAkademik` | Interface | Inspeksi kode |
| Status Mahasiswa dan Dosen berbeda | Model masing-masing | Unit test |
| Satu adapter dua tipe | Adapter/representasi item | Uji UI dan inspeksi kode |
| `AkademikRepository` berupa interface | Layer data | Inspeksi kode |
| `MahasiswaRepository` implements interface | Layer repository | Inspeksi kode |
| ViewModel bergantung pada interface | ViewModel | Inspeksi dependency/import |
| Dua Entity Room | Room | Build dan inspeksi kode |
| `LiveData<List<Mahasiswa>> getAll()` | Mahasiswa DAO | Inspeksi kode |
| `MyKampusDatabase` | Room | Build dan inspeksi kode |
| Data bertahan setelah rotasi | ViewModel + Room | Uji rotasi |
| ViewPager2 dua tab | UI | Uji manual |
| CRUD Mahasiswa dan Dosen | UI + ViewModel + Repository + DAO | Uji manual/otomatis |
| Tidak ada query Main Thread | Repository/DAO setup | Code review dan uji |
| Tidak ada `allowMainThreadQueries()` di final | Database setup | Pencarian kode |
| `equals()` dan `hashCode()` | Model | Unit test |
| `ListAdapter` + `DiffUtil` | Adapter | Code review dan uji daftar |
| README menjawab tiga refleksi | README.md | Review dokumentasi |
| GitHub public, APK, diagram, video | Paket pengumpulan | Checklist ZIP |

---

## 13. Risiko Teknis yang Perlu Diantisipasi

1. **Inheritance dan Room:** Entity yang mewarisi field dari superclass memerlukan desain yang kompatibel dengan Room. Jangan mengasumsikan setiap konfigurasi inheritance otomatis didukung; validasi melalui build.
2. **NIM unik:** Validasi format NIM tidak mencegah NIM yang valid tetapi duplikat. Unique index database tetap diperlukan.
3. **Aturan status yang belum lengkap:** Nilai IPK tepat 2.0, 3.0, dan 3.5 serta rentang 2.0–3.0 belum sepenuhnya didefinisikan dalam instruksi. Dokumentasikan keputusan pelengkap.
4. **Satu adapter untuk dua tipe:** Pastikan rancangan benar-benar satu adapter yang menerima kedua tipe, bukan hanya dua adapter yang kebetulan tampil pada tab berbeda.
5. **Error database:** Konflik unique index perlu ditangani agar tidak menyebabkan crash.
6. **Rotasi dan persistensi:** ViewModel mempertahankan state selama perubahan konfigurasi, sedangkan Room mempertahankan data permanen. Keduanya memiliki fungsi berbeda.
7. **Scope repository:** Persyaratan menyebut `AkademikRepository` dan `MahasiswaRepository`, tetapi aplikasi juga harus mengelola Dosen. Pastikan kontrak repository mencakup kedua jenis data secara konsisten.

---

## 14. Definisi Selesai (Definition of Done)

Tugas siap dikumpulkan ketika:

- [x] Semua persyaratan wajib OOP diterapkan dan bisa ditunjukkan melalui kode.
- [x] Validasi dilakukan pada Model dan error diteruskan ke UI melalui ViewModel.
- [x] Room menyimpan Mahasiswa dan Dosen secara lokal.
- [x] NIM duplikat dicegah di tingkat database dan error ditangani.
- [x] ViewModel bergantung pada repository interface.
- [x] Dua tab, form, daftar, CRUD, dan status polimorfik berfungsi.
- [x] Rotasi layar tidak menghilangkan data tersimpan.
- [x] Tidak ada query database di Main Thread tanpa executor.
- [x] Tidak ada `allowMainThreadQueries()` pada versi final.
- [x] Bonus `equals()`/`hashCode()` dan `ListAdapter`/`DiffUtil` diterapkan jika mengejar bonus +10.
- [x] Semua test dijalankan dan hasil aktual dicatat.
- [x] README berisi jawaban refleksi dan petunjuk menjalankan aplikasi.
- [x] ZIP bernama `NIM_Nama_Pertemuan4.zip` berisi link GitHub public, APK debug, screenshot class diagram, dan video demo maksimal 2 menit.

