package com.michaelshan.mykampuslite.model;

import androidx.room.ColumnInfo;

/**
 * Person merupakan kelas abstrak perantara yang memodelkan individu di lingkungan kampus.
 * Mewarisi BaseEntity dan mendefinisikan method abstrak getRole().
 * Seluruh field bersifat private dengan validasi ketat pada setter.
 */
public abstract class Person extends BaseEntity {

    @ColumnInfo(name = "nama")
    private String nama;

    @ColumnInfo(name = "email")
    private String email;

    public Person() {
        super();
    }

    public Person(long id, long createdAt, long updatedAt, String nama, String email) {
        super(id, createdAt, updatedAt);
        setNama(nama);
        setEmail(email);
    }

    public Person(String nama, String email) {
        super();
        setNama(nama);
        setEmail(email);
    }

    /**
     * Mengembalikan peran dari individu (misal: "Mahasiswa" atau "Dosen").
     *
     * @return Nama peran
     */
    public abstract String getRole();

    public String getNama() {
        return nama;
    }

    /**
     * Memvalidasi dan menyimpan nama.
     * Aturan: Tidak boleh kosong/null, minimal 3 karakter (setelah membuang spasi depan/belakang).
     *
     * @param nama Nama yang akan divalidasi
     * @throws IllegalArgumentException Jika nama null, kosong, atau kurang dari 3 karakter
     */
    public void setNama(String nama) {
        if (nama == null || nama.trim().length() < 3) {
            throw new IllegalArgumentException("Nama tidak boleh kosong dan minimal 3 karakter");
        }
        this.nama = nama.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }
}
