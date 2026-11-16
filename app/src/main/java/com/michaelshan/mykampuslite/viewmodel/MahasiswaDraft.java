package com.michaelshan.mykampuslite.viewmodel;

/**
 * Menyimpan draft input form Mahasiswa di memori ViewModel.
 * Menjamin data input sementara pengguna tidak hilang saat terjadi rotasi layar.
 */
public class MahasiswaDraft {
    private long id = 0;
    private String nama = "";
    private String email = "";
    private String nim = "";
    private String jurusan = "";
    private String ipk = "";
    private boolean isEditing = false;

    public MahasiswaDraft() {}

    public MahasiswaDraft(long id, String nama, String email, String nim, String jurusan, String ipk, boolean isEditing) {
        this.id = id;
        this.nama = nama != null ? nama : "";
        this.email = email != null ? email : "";
        this.nim = nim != null ? nim : "";
        this.jurusan = jurusan != null ? jurusan : "";
        this.ipk = ipk != null ? ipk : "";
        this.isEditing = isEditing;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public String getJurusan() {
        return jurusan;
    }

    public void setJurusan(String jurusan) {
        this.jurusan = jurusan;
    }

    public String getIpk() {
        return ipk;
    }

    public void setIpk(String ipk) {
        this.ipk = ipk;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public void clear() {
        id = 0;
        nama = "";
        email = "";
        nim = "";
        jurusan = "";
        ipk = "";
        isEditing = false;
    }
}
