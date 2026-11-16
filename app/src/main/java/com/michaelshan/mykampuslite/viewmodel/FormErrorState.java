package com.michaelshan.mykampuslite.viewmodel;

/**
 * Menyimpan status pesan kesalahan per-field input form,
 * sehingga UI dapat menampilkan pesan error tepat di bawah kolom yang bersangkutan.
 */
public class FormErrorState {

    private String namaError;
    private String identifierError; // NIM untuk mahasiswa, NIDN untuk dosen
    private String jurusanError;
    private String ipkError;
    private String mataKuliahError;
    private String emailError;
    private String generalError;

    public FormErrorState() {}

    public String getNamaError() {
        return namaError;
    }

    public void setNamaError(String namaError) {
        this.namaError = namaError;
    }

    public String getIdentifierError() {
        return identifierError;
    }

    public void setIdentifierError(String identifierError) {
        this.identifierError = identifierError;
    }

    public String getJurusanError() {
        return jurusanError;
    }

    public void setJurusanError(String jurusanError) {
        this.jurusanError = jurusanError;
    }

    public String getIpkError() {
        return ipkError;
    }

    public void setIpkError(String ipkError) {
        this.ipkError = ipkError;
    }

    public String getMataKuliahError() {
        return mataKuliahError;
    }

    public void setMataKuliahError(String mataKuliahError) {
        this.mataKuliahError = mataKuliahError;
    }

    public String getEmailError() {
        return emailError;
    }

    public void setEmailError(String emailError) {
        this.emailError = emailError;
    }

    public String getGeneralError() {
        return generalError;
    }

    public void setGeneralError(String generalError) {
        this.generalError = generalError;
    }

    public boolean hasErrors() {
        return namaError != null || identifierError != null || jurusanError != null
                || ipkError != null || mataKuliahError != null || emailError != null || generalError != null;
    }

    public void clear() {
        namaError = null;
        identifierError = null;
        jurusanError = null;
        ipkError = null;
        mataKuliahError = null;
        emailError = null;
        generalError = null;
    }
}
