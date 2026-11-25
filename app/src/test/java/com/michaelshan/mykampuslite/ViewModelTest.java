package com.michaelshan.mykampuslite;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.michaelshan.mykampuslite.data.repository.AkademikRepository;
import com.michaelshan.mykampuslite.data.repository.RepositoryCallback;
import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Mahasiswa;
import com.michaelshan.mykampuslite.viewmodel.AkademikViewModel;
import com.michaelshan.mykampuslite.viewmodel.FormErrorState;
import com.michaelshan.mykampuslite.viewmodel.MahasiswaDraft;
import com.michaelshan.mykampuslite.viewmodel.OperationResult;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Pengujian unit untuk ViewModel dan interaksinya dengan antarmuka Repository (TC16, TC17, Abstraction).
 */
public class ViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FakeAkademikRepository fakeRepository;
    private AkademikViewModel viewModel;

    @Before
    public void setUp() {
        fakeRepository = new FakeAkademikRepository();
        viewModel = new AkademikViewModel(fakeRepository);
    }

    // ==========================================
    // TC16: Draft form dipertahankan saat rotasi
    // ==========================================

    @Test
    public void tc16_rotationDraftPreservation_mahasiswa() {
        // Pengguna mengetik form tetapi belum menekan simpan
        viewModel.saveMahasiswa(0, "Ahmad Subandi", "ahmad@kampus.id", "211011400", "Informatika", "3.75", false);

        // Validasi NIM gagal (9 digit), data tersimpan di draft untuk rotasi
        MahasiswaDraft draft = viewModel.getMahasiswaDraft();
        assertEquals("Ahmad Subandi", draft.getNama());
        assertEquals("211011400", draft.getNim());
        assertEquals("3.75", draft.getIpk());
        assertFalse(draft.isEditing());

        // Pastikan repositori TIDAK dipanggil saat validasi gagal
        assertEquals(0, fakeRepository.insertedMahasiswaList.size());
    }

    @Test
    public void tc16_rotationDraftPreservation_editMode() {
        viewModel.saveMahasiswa(42L, "Citra Dewi", "citra@k.id", "2110114001", "Sistem Informasi", "3.90", true);

        // Operasi berhasil dan masuk repositori
        assertEquals(1, fakeRepository.updatedMahasiswaList.size());
    }

    // ==========================================
    // Validasi Model menolak data invalid di ViewModel
    // ==========================================

    @Test
    public void testViewModel_saveMahasiswa_invalidData_setsFormErrors() {
        // Nama < 3 karakter, NIM bukan 10 digit, IPK > 4.0
        viewModel.saveMahasiswa(0, "Al", "al@k.id", "123", "Teknik", "4.5", false);

        FormErrorState errorState = viewModel.getMahasiswaFormErrors().getValue();
        assertNotNull(errorState);
        assertTrue(errorState.hasErrors());
        assertNotNull(errorState.getNamaError());
        assertNotNull(errorState.getIdentifierError());
        assertNotNull(errorState.getIpkError());

        // Memastikan tidak diteruskan ke database
        assertEquals(0, fakeRepository.insertedMahasiswaList.size());

        OperationResult result = viewModel.getOperationResult().getValue();
        assertNotNull(result);
        assertEquals(OperationResult.Status.ERROR, result.getStatus());
    }

    @Test
    public void testViewModel_saveMahasiswa_validData_callsRepository() {
        viewModel.saveMahasiswa(0, "Muhammad Fatih", "fatih@k.id", "2110114001", "Teknik Informatika", "3.85", false);

        FormErrorState errorState = viewModel.getMahasiswaFormErrors().getValue();
        assertNotNull(errorState);
        assertFalse(errorState.hasErrors());

        assertEquals(1, fakeRepository.insertedMahasiswaList.size());
        assertEquals("2110114001", fakeRepository.insertedMahasiswaList.get(0).getNim());
    }

    @Test
    public void testViewModel_saveDosen_validData_emitsDosenResultWithoutTouchingMahasiswaResult() {
        List<OperationResult> mahasiswaResults = new ArrayList<>();
        List<OperationResult> dosenResults = new ArrayList<>();
        viewModel.getMahasiswaOperationResult().observeForever(mahasiswaResults::add);
        viewModel.getDosenOperationResult().observeForever(dosenResults::add);

        viewModel.saveDosen(0, "Dr. Hendra", "hendra@k.id", "0415088001", "PBO, Basis Data", false);

        assertEquals(1, fakeRepository.insertedDosenList.size());
        assertEquals(0, mahasiswaResults.size());
        assertEquals(1, dosenResults.size());
        assertTrue(dosenResults.get(0).isSuccess());
        assertEquals("INSERT", dosenResults.get(0).getAction());
    }

    @Test
    public void testViewModel_updateDosen_emitsDosenUpdateSuccess() {
        List<OperationResult> dosenResults = new ArrayList<>();
        viewModel.getDosenOperationResult().observeForever(dosenResults::add);

        viewModel.saveDosen(5L, "Dr. Hendra", "hendra@k.id", "0415088001", "PBO, Algoritma", true);

        assertEquals(1, fakeRepository.updatedDosenList.size());
        assertEquals(5L, fakeRepository.updatedDosenList.get(0).getId());
        assertEquals(1, dosenResults.size());
        assertTrue(dosenResults.get(0).isSuccess());
        assertEquals("UPDATE", dosenResults.get(0).getAction());
    }

    // ==========================================
    // Fake Repository untuk isolasi pengujian ViewModel
    // ==========================================

    private static class FakeAkademikRepository implements AkademikRepository {

        final List<Mahasiswa> insertedMahasiswaList = new ArrayList<>();
        final List<Mahasiswa> updatedMahasiswaList = new ArrayList<>();
        final List<Mahasiswa> deletedMahasiswaList = new ArrayList<>();

        final List<Dosen> insertedDosenList = new ArrayList<>();
        final List<Dosen> updatedDosenList = new ArrayList<>();
        final List<Dosen> deletedDosenList = new ArrayList<>();

        private final MutableLiveData<List<Mahasiswa>> mahasiswaLiveData = new MutableLiveData<>(new ArrayList<>());
        private final MutableLiveData<List<Dosen>> dosenLiveData = new MutableLiveData<>(new ArrayList<>());

        @Override
        public LiveData<List<Mahasiswa>> getAllMahasiswa() {
            return mahasiswaLiveData;
        }

        @Override
        public LiveData<List<Mahasiswa>> searchMahasiswa(String query) {
            return mahasiswaLiveData;
        }

        @Override
        public void insertMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Long> callback) {
            insertedMahasiswaList.add(mahasiswa);
            if (callback != null) callback.onSuccess(1L);
        }

        @Override
        public void updateMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Integer> callback) {
            updatedMahasiswaList.add(mahasiswa);
            if (callback != null) callback.onSuccess(1);
        }

        @Override
        public void deleteMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Integer> callback) {
            deletedMahasiswaList.add(mahasiswa);
            if (callback != null) callback.onSuccess(1);
        }

        @Override
        public LiveData<List<Dosen>> getAllDosen() {
            return dosenLiveData;
        }

        @Override
        public LiveData<List<Dosen>> searchDosen(String query) {
            return dosenLiveData;
        }

        @Override
        public void insertDosen(Dosen dosen, RepositoryCallback<Long> callback) {
            insertedDosenList.add(dosen);
            if (callback != null) callback.onSuccess(1L);
        }

        @Override
        public void updateDosen(Dosen dosen, RepositoryCallback<Integer> callback) {
            updatedDosenList.add(dosen);
            if (callback != null) callback.onSuccess(1);
        }

        @Override
        public void deleteDosen(Dosen dosen, RepositoryCallback<Integer> callback) {
            deletedDosenList.add(dosen);
            if (callback != null) callback.onSuccess(1);
        }
    }
}
