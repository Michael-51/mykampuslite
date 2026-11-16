package com.michaelshan.mykampuslite.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.michaelshan.mykampuslite.model.Dosen;

import java.util.List;

/**
 * Data Access Object untuk tabel Dosen.
 * Menyediakan operasi CRUD serta query reaktif berbasis LiveData.
 */
@Dao
public interface DosenDao {

    @Query("SELECT * FROM dosen ORDER BY id DESC")
    LiveData<List<Dosen>> getAll();

    @Query("SELECT * FROM dosen WHERE nama LIKE '%' || :query || '%' OR nidn LIKE '%' || :query || '%' OR mata_kuliah_diampu LIKE '%' || :query || '%' ORDER BY id DESC")
    LiveData<List<Dosen>> searchDosen(String query);

    @Query("SELECT * FROM dosen WHERE id = :id LIMIT 1")
    Dosen getById(long id);

    @Query("SELECT * FROM dosen WHERE nidn = :nidn LIMIT 1")
    Dosen getByNidn(String nidn);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Dosen dosen);

    @Update
    int update(Dosen dosen);

    @Delete
    int delete(Dosen dosen);
}
