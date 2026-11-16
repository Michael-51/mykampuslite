package com.michaelshan.mykampuslite.viewmodel;

/**
 * Menyimpan draft input form Dosen di memori ViewModel.
 * Menjamin data input sementara pengguna tidak hilang saat terjadi rotasi layar.
 */
public class DosenDraft {
    private long id = 0;
    private String nama = "";
    private String email = "";
    private String nidn = "";
    private String mataKuliahDiampu = "";
    private boolean isEditing = false;

    public DosenDraft() {}

    public DosenDraft(long id, String nama, String email, String nidn, String mataKuliahDiampu, boolean isEditing) {
        this.id = id;
        this.nama = nama != null ? nama : "";
        this.email = email != null ? email : "";
        this.nidn = nidn != null ? nidn : "";
        this.mataKuliahDiampu = mataKuliahDiampu != null ? mataKuliahDiampu : "";
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

    public String getNidn() {
        return nidn;
    }

    public void setNidn(String nidn) {
        this.nidn = nidn;
    }

    public String getMataKuliahDiampu() {
        return mataKuliahDiampu;
    }

    public void setMataKuliahDiampu(String mataKuliahDiampu) {
        this.mataKuliahDiampu = mataKuliahDiampu;
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
        nidn = "";
        mataKuliahDiampu = "";
        isEditing = false;
    }
}
