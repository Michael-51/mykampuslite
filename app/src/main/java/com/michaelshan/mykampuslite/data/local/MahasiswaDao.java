package com.michaelshan.mykampuslite.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.michaelshan.mykampuslite.model.Mahasiswa;

import java.util.List;

/**
 * Data Access Object untuk tabel Mahasiswa.
 * Menyediakan operasi CRUD serta query reaktif berbasis LiveData.
 */
@Dao
public interface MahasiswaDao {

    @Query("SELECT * FROM mahasiswa ORDER BY id DESC")
    LiveData<List<Mahasiswa>> getAll();

    @Query("SELECT * FROM mahasiswa WHERE nama LIKE '%' || :query || '%' OR nim LIKE '%' || :query || '%' OR jurusan LIKE '%' || :query || '%' ORDER BY id DESC")
    LiveData<List<Mahasiswa>> searchMahasiswa(String query);

    @Query("SELECT * FROM mahasiswa WHERE id = :id LIMIT 1")
    Mahasiswa getById(long id);

    @Query("SELECT * FROM mahasiswa WHERE nim = :nim LIMIT 1")
    Mahasiswa getByNim(String nim);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Mahasiswa mahasiswa);

    @Update
    int update(Mahasiswa mahasiswa);

    @Delete
    int delete(Mahasiswa mahasiswa);
}
