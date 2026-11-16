package com.michaelshan.mykampuslite.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import java.util.Objects;

/**
 * Entitas Dosen mewakili data tenaga pengajar yang tersimpan di tabel database "dosen".
 * Mewarisi Person dan mengimplementasikan EvaluasiAkademik secara polimorfik.
 * Memiliki indeks unik pada kolom NIDN di tingkat database untuk mencegah duplikasi.
 */
@Entity(
    tableName = "dosen",
    indices = {@Index(value = {"nidn"}, unique = true)}
)
public class Dosen extends Person implements EvaluasiAkademik {

    @ColumnInfo(name = "nidn")
    private String nidn;

    @ColumnInfo(name = "mata_kuliah_diampu")
    private String mataKuliahDiampu;

    public Dosen() {
        super();
    }

    @Ignore
    public Dosen(String nama, String email, String nidn, String mataKuliahDiampu) {
        super(nama, email);
        setNidn(nidn);
        setMataKuliahDiampu(mataKuliahDiampu);
    }

    @Ignore
    public Dosen(long id, long createdAt, long updatedAt, String nama, String email, String nidn, String mataKuliahDiampu) {
        super(id, createdAt, updatedAt, nama, email);
        setNidn(nidn);
        setMataKuliahDiampu(mataKuliahDiampu);
    }

    @Override
    public String getRole() {
        return "Dosen";
    }

    /**
     * Menghitung jumlah mata kuliah yang diampu dari teks yang dipisahkan koma/titik koma/baris baru.
     *
     * @return Jumlah mata kuliah yang valid (tidak kosong)
     */
    public int getJumlahMataKuliah() {
        if (mataKuliahDiampu == null || mataKuliahDiampu.trim().isEmpty()) {
            return 0;
        }
        String[] parts = mataKuliahDiampu.split("[,;\\n]+");
        int count = 0;
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Menghitung status akademik beban kerja dosen secara polimorfik:
     * - Jumlah mata kuliah > 3 : "Beban Lebih"
     * - Jumlah mata kuliah <= 3 : "Normal"
     *
     * @return Label evaluasi akademik dosen
     */
    @Override
    public String getStatusAkademik() {
        if (getJumlahMataKuliah() > 3) {
            return "Beban Lebih";
        } else {
            return "Normal";
        }
    }

    public String getNidn() {
        return nidn;
    }

    /**
     * Memvalidasi dan menyimpan NIDN.
     * Aturan: Wajib tepat 10 digit angka.
     *
     * @param nidn Nomor Induk Dosen Nasional
     * @throws IllegalArgumentException Jika NIDN null atau bukan tepat 10 digit angka
     */
    public void setNidn(String nidn) {
        if (nidn == null || !nidn.matches("^\\d{10}$")) {
            throw new IllegalArgumentException("NIDN harus tepat 10 digit angka");
        }
        this.nidn = nidn;
    }

    public String getMataKuliahDiampu() {
        return mataKuliahDiampu;
    }

    public void setMataKuliahDiampu(String mataKuliahDiampu) {
        if (mataKuliahDiampu == null || mataKuliahDiampu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mata kuliah diampu tidak boleh kosong");
        }
        this.mataKuliahDiampu = mataKuliahDiampu.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dosen dosen = (Dosen) o;
        if (getId() != 0 && dosen.getId() != 0) {
            return getId() == dosen.getId();
        }
        return Objects.equals(nidn, dosen.nidn);
    }

    @Override
    public int hashCode() {
        if (getId() != 0) {
            return Objects.hash(getId());
        }
        return Objects.hash(nidn);
    }
}
