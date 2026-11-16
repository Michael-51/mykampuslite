package com.michaelshan.mykampuslite.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.michaelshan.mykampuslite.data.repository.AkademikRepository;
import com.michaelshan.mykampuslite.data.repository.RepositoryCallback;
import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Mahasiswa;

import java.util.List;

/**
 * ViewModel utama aplikasi sesuai arsitektur MVVM.
 * Bergantung sepenuhnya pada interface AkademikRepository (Dependency Inversion),
 * bukan pada implementasi database atau DAO konkret.
 *
 * Menjamin isolasi View dengan tidak mengimpor komponen android.widget.* atau Context,
 * serta mempertahankan draft data form saat terjadi konfigurasi ulang layar (rotasi).
 */
public class AkademikViewModel extends ViewModel {

    private final AkademikRepository repository;

    // Search query states
    private final MutableLiveData<String> mahasiswaSearchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> dosenSearchQuery = new MutableLiveData<>("");

    // MediatorLiveData untuk daftar data yang merespons query pencarian secara dinamis
    private final MediatorLiveData<List<Mahasiswa>> mahasiswaList = new MediatorLiveData<>();
    private final MediatorLiveData<List<Dosen>> dosenList = new MediatorLiveData<>();

    private LiveData<List<Mahasiswa>> currentMahasiswaSource = null;
    private LiveData<List<Dosen>> currentDosenSource = null;

    // Error states per-form
    private final MutableLiveData<FormErrorState> mahasiswaFormErrors = new MutableLiveData<>(new FormErrorState());
    private final MutableLiveData<FormErrorState> dosenFormErrors = new MutableLiveData<>(new FormErrorState());

    // Single-event operation result untuk Toast/Snackbar
    private final SingleLiveEvent<OperationResult> operationResult = new SingleLiveEvent<>();

    // Draft state untuk rotasi layar
    private final MahasiswaDraft mahasiswaDraft = new MahasiswaDraft();
    private final DosenDraft dosenDraft = new DosenDraft();

    public AkademikViewModel(@NonNull AkademikRepository repository) {
        this.repository = repository;

        setupMahasiswaMediator();
        setupDosenMediator();
    }

    private void setupMahasiswaMediator() {
        mahasiswaList.addSource(mahasiswaSearchQuery, query -> {
            if (currentMahasiswaSource != null) {
                mahasiswaList.removeSource(currentMahasiswaSource);
            }
            if (query == null || query.trim().isEmpty()) {
                currentMahasiswaSource = repository.getAllMahasiswa();
            } else {
                currentMahasiswaSource = repository.searchMahasiswa(query.trim());
            }
            mahasiswaList.addSource(currentMahasiswaSource, mahasiswaList::setValue);
        });
    }

    private void setupDosenMediator() {
        dosenList.addSource(dosenSearchQuery, query -> {
            if (currentDosenSource != null) {
                dosenList.removeSource(currentDosenSource);
            }
            if (query == null || query.trim().isEmpty()) {
                currentDosenSource = repository.getAllDosen();
            } else {
                currentDosenSource = repository.searchDosen(query.trim());
            }
            dosenList.addSource(currentDosenSource, dosenList::setValue);
        });
    }

    // ==========================================
    // GETTERS LIVE DATA & STATE
    // ==========================================

    public LiveData<List<Mahasiswa>> getMahasiswaList() {
        return mahasiswaList;
    }

    public LiveData<List<Dosen>> getDosenList() {
        return dosenList;
    }

    public LiveData<FormErrorState> getMahasiswaFormErrors() {
        return mahasiswaFormErrors;
    }

    public LiveData<FormErrorState> getDosenFormErrors() {
        return dosenFormErrors;
    }

    public SingleLiveEvent<OperationResult> getOperationResult() {
        return operationResult;
    }

    public MahasiswaDraft getMahasiswaDraft() {
        return mahasiswaDraft;
    }

    public DosenDraft getDosenDraft() {
        return dosenDraft;
    }

    // ==========================================
    // PENCARIAN
    // ==========================================

    public void setMahasiswaSearchQuery(String query) {
        mahasiswaSearchQuery.setValue(query);
    }

    public void setDosenSearchQuery(String query) {
        dosenSearchQuery.setValue(query);
    }

    // ==========================================
    // LOGIKA BISNIS & VALIDASI MAHASISWA
    // ==========================================

    /**
     * Memvalidasi input melalui kelas Model Mahasiswa dan menyimpannya ke database via repository.
     * Jika terjadi pelanggaran validasi, pesan kesalahan dipetakan ke field form tanpa meneruskan ke database.
     */
    public void saveMahasiswa(long id, String nama, String email, String nim, String jurusan, String ipkText, boolean isEdit) {
        // Simpan ke draft terlebih dahulu untuk proteksi rotasi
        mahasiswaDraft.setId(id);
        mahasiswaDraft.setNama(nama);
        mahasiswaDraft.setEmail(email);
        mahasiswaDraft.setNim(nim);
        mahasiswaDraft.setJurusan(jurusan);
        mahasiswaDraft.setIpk(ipkText);
        mahasiswaDraft.setEditing(isEdit);

        FormErrorState errorState = new FormErrorState();
        Mahasiswa mahasiswa = new Mahasiswa();

        // 1. Validasi Nama di Model
        try {
            mahasiswa.setNama(nama);
        } catch (IllegalArgumentException e) {
            errorState.setNamaError(e.getMessage());
        }

        // 2. Email (opsional atau format)
        mahasiswa.setEmail(email);

        // 3. Validasi NIM di Model (harus tepat 10 digit angka)
        try {
            mahasiswa.setNim(nim);
        } catch (IllegalArgumentException e) {
            errorState.setIdentifierError(e.getMessage());
        }

        // 4. Validasi Jurusan di Model
        try {
            mahasiswa.setJurusan(jurusan);
        } catch (IllegalArgumentException e) {
            errorState.setJurusanError(e.getMessage());
        }

        // 5. Validasi IPK di Model (0.0 sampai 4.0)
        if (ipkText == null || ipkText.trim().isEmpty()) {
            errorState.setIpkError("IPK wajib diisi (rentang 0.0 - 4.0)");
        } else {
            try {
                double parsedIpk = Double.parseDouble(ipkText.trim());
                mahasiswa.setIpk(parsedIpk);
            } catch (NumberFormatException e) {
                errorState.setIpkError("Format IPK tidak valid. Gunakan angka desimal (misal: 3.75)");
            } catch (IllegalArgumentException e) {
                errorState.setIpkError(e.getMessage());
            }
        }

        // Jika terdapat error validasi Model, jangan simpan ke database!
        if (errorState.hasErrors()) {
            mahasiswaFormErrors.setValue(errorState);
            operationResult.setValue(OperationResult.error(
                    "Gagal menyimpan: Periksa data yang ditandai merah.",
                    "Mahasiswa",
                    isEdit ? "UPDATE" : "INSERT"
            ));
            return;
        }

        // Bersihkan pesan error form jika validasi Model lolos
        mahasiswaFormErrors.setValue(new FormErrorState());

        if (isEdit && id > 0) {
            mahasiswa.setId(id);
            repository.updateMahasiswa(mahasiswa, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    mahasiswaDraft.clear();
                    operationResult.setValue(OperationResult.success(
                            "Data mahasiswa berhasil diperbarui.",
                            "Mahasiswa",
                            "UPDATE"
                    ));
                }

                @Override
                public void onError(Exception exception) {
                    FormErrorState dbError = new FormErrorState();
                    if (exception.getMessage() != null && exception.getMessage().contains("NIM")) {
                        dbError.setIdentifierError(exception.getMessage());
                    }
                    mahasiswaFormErrors.setValue(dbError);
                    operationResult.setValue(OperationResult.error(
                            exception.getMessage(),
                            "Mahasiswa",
                            "UPDATE"
                    ));
                }
            });
        } else {
            repository.insertMahasiswa(mahasiswa, new RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    mahasiswaDraft.clear();
                    operationResult.setValue(OperationResult.success(
                            "Mahasiswa baru berhasil disimpan.",
                            "Mahasiswa",
                            "INSERT"
                    ));
                }

                @Override
                public void onError(Exception exception) {
                    FormErrorState dbError = new FormErrorState();
                    if (exception.getMessage() != null && exception.getMessage().contains("NIM")) {
                        dbError.setIdentifierError(exception.getMessage());
                    }
                    mahasiswaFormErrors.setValue(dbError);
                    operationResult.setValue(OperationResult.error(
                            exception.getMessage(),
                            "Mahasiswa",
                            "INSERT"
                    ));
                }
            });
        }
    }

    public void deleteMahasiswa(Mahasiswa mahasiswa) {
        if (mahasiswa == null) return;
        repository.deleteMahasiswa(mahasiswa, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                operationResult.setValue(OperationResult.success(
                        "Data mahasiswa '" + mahasiswa.getNama() + "' berhasil dihapus.",
                        "Mahasiswa",
                        "DELETE"
                ));
            }

            @Override
            public void onError(Exception exception) {
                operationResult.setValue(OperationResult.error(
                        "Gagal menghapus mahasiswa: " + exception.getMessage(),
                        "Mahasiswa",
                        "DELETE"
                ));
            }
        });
    }

    // ==========================================
    // LOGIKA BISNIS & VALIDASI DOSEN
    // ==========================================

    public void saveDosen(long id, String nama, String email, String nidn, String mataKuliah, boolean isEdit) {
        dosenDraft.setId(id);
        dosenDraft.setNama(nama);
        dosenDraft.setEmail(email);
        dosenDraft.setNidn(nidn);
        dosenDraft.setMataKuliahDiampu(mataKuliah);
        dosenDraft.setEditing(isEdit);

        FormErrorState errorState = new FormErrorState();
        Dosen dosen = new Dosen();

        // 1. Validasi Nama di Model
        try {
            dosen.setNama(nama);
        } catch (IllegalArgumentException e) {
            errorState.setNamaError(e.getMessage());
        }

        // 2. Email
        dosen.setEmail(email);

        // 3. Validasi NIDN di Model
        try {
            dosen.setNidn(nidn);
        } catch (IllegalArgumentException e) {
            errorState.setIdentifierError(e.getMessage());
        }

        // 4. Validasi Mata Kuliah di Model
        try {
            dosen.setMataKuliahDiampu(mataKuliah);
        } catch (IllegalArgumentException e) {
            errorState.setMataKuliahError(e.getMessage());
        }

        if (errorState.hasErrors()) {
            dosenFormErrors.setValue(errorState);
            operationResult.setValue(OperationResult.error(
                    "Gagal menyimpan: Periksa data yang ditandai merah.",
                    "Dosen",
                    isEdit ? "UPDATE" : "INSERT"
            ));
            return;
        }

        dosenFormErrors.setValue(new FormErrorState());

        if (isEdit && id > 0) {
            dosen.setId(id);
            repository.updateDosen(dosen, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    dosenDraft.clear();
                    operationResult.setValue(OperationResult.success(
                            "Data dosen berhasil diperbarui.",
                            "Dosen",
                            "UPDATE"
                    ));
                }

                @Override
                public void onError(Exception exception) {
                    FormErrorState dbError = new FormErrorState();
                    if (exception.getMessage() != null && exception.getMessage().contains("NIDN")) {
                        dbError.setIdentifierError(exception.getMessage());
                    }
                    dosenFormErrors.setValue(dbError);
                    operationResult.setValue(OperationResult.error(
                            exception.getMessage(),
                            "Dosen",
                            "UPDATE"
                    ));
                }
            });
        } else {
            repository.insertDosen(dosen, new RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    dosenDraft.clear();
                    operationResult.setValue(OperationResult.success(
                            "Dosen baru berhasil disimpan.",
                            "Dosen",
                            "INSERT"
                    ));
                }

                @Override
                public void onError(Exception exception) {
                    FormErrorState dbError = new FormErrorState();
                    if (exception.getMessage() != null && exception.getMessage().contains("NIDN")) {
                        dbError.setIdentifierError(exception.getMessage());
                    }
                    dosenFormErrors.setValue(dbError);
                    operationResult.setValue(OperationResult.error(
                            exception.getMessage(),
                            "Dosen",
                            "INSERT"
                    ));
                }
            });
        }
    }

    public void deleteDosen(Dosen dosen) {
        if (dosen == null) return;
        repository.deleteDosen(dosen, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                operationResult.setValue(OperationResult.success(
                        "Data dosen '" + dosen.getNama() + "' berhasil dihapus.",
                        "Dosen",
                        "DELETE"
                ));
            }

            @Override
            public void onError(Exception exception) {
                operationResult.setValue(OperationResult.error(
                        "Gagal menghapus dosen: " + exception.getMessage(),
                        "Dosen",
                        "DELETE"
                ));
            }
        });
    }

    public void resetMahasiswaFormErrors() {
        mahasiswaFormErrors.setValue(new FormErrorState());
    }

    public void resetDosenFormErrors() {
        dosenFormErrors.setValue(new FormErrorState());
    }
}
