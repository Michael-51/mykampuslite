package com.michaelshan.mykampuslite.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import java.util.Objects;

/**
 * Entitas Mahasiswa mewakili data mahasiswa yang tersimpan di tabel database "mahasiswa".
 * Mewarisi Person dan mengimplementasikan EvaluasiAkademik secara polimorfik.
 * Memiliki indeks unik pada kolom NIM di tingkat database untuk mencegah duplikasi.
 */
@Entity(
    tableName = "mahasiswa",
    indices = {@Index(value = {"nim"}, unique = true)}
)
public class Mahasiswa extends Person implements EvaluasiAkademik {

    @ColumnInfo(name = "nim")
    private String nim;

    @ColumnInfo(name = "jurusan")
    private String jurusan;

    @ColumnInfo(name = "ipk")
    private double ipk;

    public Mahasiswa() {
        super();
    }

    @Ignore
    public Mahasiswa(String nama, String email, String nim, String jurusan, double ipk) {
        super(nama, email);
        setNim(nim);
        setJurusan(jurusan);
        setIpk(ipk);
    }

    @Ignore
    public Mahasiswa(long id, long createdAt, long updatedAt, String nama, String email, String nim, String jurusan, double ipk) {
        super(id, createdAt, updatedAt, nama, email);
        setNim(nim);
        setJurusan(jurusan);
        setIpk(ipk);
    }

    @Override
    public String getRole() {
        return "Mahasiswa";
    }

    /**
     * Menghitung status akademik mahasiswa berdasarkan nilai IPK secara polimorfik:
     * - IPK > 3.5 : "Cumlaude"
     * - IPK > 3.0 : "Memuaskan"
     * - IPK < 2.0 : "Peringatan"
     * - Rentang 2.0 sampai 3.0 : "Normal"
     *
     * @return Label evaluasi akademik mahasiswa
     */
    @Override
    public String getStatusAkademik() {
        if (ipk > 3.5) {
            return "Cumlaude";
        } else if (ipk > 3.0) {
            return "Memuaskan";
        } else if (ipk < 2.0) {
            return "Peringatan";
        } else {
            return "Normal";
        }
    }

    public String getNim() {
        return nim;
    }

    /**
     * Memvalidasi dan menyimpan NIM.
     * Aturan: Wajib tepat 10 digit angka tanpa karakter lain.
     *
     * @param nim Nomor Induk Mahasiswa
     * @throws IllegalArgumentException Jika NIM null atau bukan tepat 10 digit angka
     */
    public void setNim(String nim) {
        if (nim == null || !nim.matches("^\\d{10}$")) {
            throw new IllegalArgumentException("NIM harus tepat 10 digit angka");
        }
        this.nim = nim;
    }

    public String getJurusan() {
        return jurusan;
    }

    public void setJurusan(String jurusan) {
        if (jurusan == null || jurusan.trim().isEmpty()) {
            throw new IllegalArgumentException("Jurusan tidak boleh kosong");
        }
        this.jurusan = jurusan.trim();
    }

    public double getIpk() {
        return ipk;
    }

    /**
     * Memvalidasi dan menyimpan IPK.
     * Aturan: Nilai harus berada dalam rentang 0.0 sampai 4.0 inklusif.
     *
     * @param ipk Indeks Prestasi Kumulatif
     * @throws IllegalArgumentException Jika IPK < 0.0 atau > 4.0
     */
    public void setIpk(double ipk) {
        if (Double.isNaN(ipk) || ipk < 0.0 || ipk > 4.0) {
            throw new IllegalArgumentException("IPK harus berada pada rentang 0.0 sampai 4.0");
        }
        this.ipk = ipk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mahasiswa mahasiswa = (Mahasiswa) o;
        if (getId() != 0 && mahasiswa.getId() != 0) {
            return getId() == mahasiswa.getId();
        }
        return Objects.equals(nim, mahasiswa.nim);
    }

    @Override
    public int hashCode() {
        if (getId() != 0) {
            return Objects.hash(getId());
        }
        return Objects.hash(nim);
    }
}
