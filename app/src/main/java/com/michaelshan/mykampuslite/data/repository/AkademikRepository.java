package com.michaelshan.mykampuslite.data.repository;

import androidx.lifecycle.LiveData;

import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Mahasiswa;

import java.util.List;

/**
 * Abstraksi repository akademik sesuai prinsip Abstraction dan Dependency Inversion (DIP).
 * Menentukan kontrak operasi data Mahasiswa dan Dosen tanpa mengekspos detail database/DAO.
 * ViewModel hanya bergantung pada interface ini.
 */
public interface AkademikRepository {

    // === Operasi Mahasiswa ===
    LiveData<List<Mahasiswa>> getAllMahasiswa();
    LiveData<List<Mahasiswa>> searchMahasiswa(String query);
    void insertMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Long> callback);
    void updateMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Integer> callback);
    void deleteMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Integer> callback);

    // === Operasi Dosen ===
    LiveData<List<Dosen>> getAllDosen();
    LiveData<List<Dosen>> searchDosen(String query);
    void insertDosen(Dosen dosen, RepositoryCallback<Long> callback);
    void updateDosen(Dosen dosen, RepositoryCallback<Integer> callback);
    void deleteDosen(Dosen dosen, RepositoryCallback<Integer> callback);
}
