package com.michaelshan.mykampuslite.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Mahasiswa;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database utama aplikasi MyKampus Lite menggunakan Room.
 * Mengimplementasikan Singleton Pattern untuk memastikan satu koneksi database bersama.
 * Tidak menggunakan allowMainThreadQueries() untuk menjaga kestabilan UI Thread.
 */
@Database(
    entities = {Mahasiswa.class, Dosen.class},
    version = 1,
    exportSchema = false
)
public abstract class MyKampusDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "mykampus_database";
    private static volatile MyKampusDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    /**
     * Executor untuk operasi database background (tulis, ubah, hapus).
     */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract MahasiswaDao mahasiswaDao();
    public abstract DosenDao dosenDao();

    /**
     * Mengambil instance singleton database Room.
     *
     * @param context Application context
     * @return Instance MyKampusDatabase
     */
    public static MyKampusDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (MyKampusDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MyKampusDatabase.class,
                            DATABASE_NAME
                    )
                    // fallbackToDestructiveMigration tidak diaktifkan demi integritas data offline
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
