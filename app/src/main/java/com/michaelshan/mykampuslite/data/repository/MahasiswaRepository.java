package com.michaelshan.mykampuslite.data.repository;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.michaelshan.mykampuslite.data.local.DosenDao;
import com.michaelshan.mykampuslite.data.local.MahasiswaDao;
import com.michaelshan.mykampuslite.data.local.MyKampusDatabase;
import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Mahasiswa;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Implementasi konkret dari AkademikRepository.
 * Bertanggung jawab mengeksekusi operasi database di thread background via Executor,
 * menangani SQLiteConstraintException untuk keunikan NIM/NIDN,
 * serta meneruskan hasil ke thread utama melalui RepositoryCallback.
 */
public class MahasiswaRepository implements AkademikRepository {

    private final MahasiswaDao mahasiswaDao;
    private final DosenDao dosenDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public MahasiswaRepository(MahasiswaDao mahasiswaDao, DosenDao dosenDao, ExecutorService executor, Handler mainHandler) {
        this.mahasiswaDao = mahasiswaDao;
        this.dosenDao = dosenDao;
        this.executor = executor != null ? executor : MyKampusDatabase.databaseWriteExecutor;
        this.mainHandler = mainHandler;
    }

    public MahasiswaRepository(MahasiswaDao mahasiswaDao, DosenDao dosenDao, ExecutorService executor) {
        this(mahasiswaDao, dosenDao, executor, createDefaultHandler());
    }

    public MahasiswaRepository(MyKampusDatabase database) {
        this(database.mahasiswaDao(), database.dosenDao(), MyKampusDatabase.databaseWriteExecutor, createDefaultHandler());
    }

    private static Handler createDefaultHandler() {
        try {
            if (Looper.getMainLooper() != null) {
                return new Handler(Looper.getMainLooper());
            }
        } catch (Throwable ignored) {
            // JVM Unit test environment fallback
        }
        return null;
    }

    private void dispatchCallback(Runnable runnable) {
        if (mainHandler != null) {
            mainHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    // ==========================================
    // OPERASI MAHASISWA
    // ==========================================

    @Override
    public LiveData<List<Mahasiswa>> getAllMahasiswa() {
        return mahasiswaDao.getAll();
    }

    @Override
    public LiveData<List<Mahasiswa>> searchMahasiswa(String query) {
        return mahasiswaDao.searchMahasiswa(query);
    }

    @Override
    public void insertMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Long> callback) {
        executor.execute(() -> {
            try {
                // Pastikan timestamp terisi
                if (mahasiswa.getCreatedAt() == 0) {
                    long now = System.currentTimeMillis();
                    mahasiswa.setCreatedAt(now);
                    mahasiswa.setUpdatedAt(now);
                }
                long id = mahasiswaDao.insert(mahasiswa);
                if (callback != null) {
                    dispatchCallback(() -> callback.onSuccess(id));
                }
            } catch (SQLiteConstraintException e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(
                            new IllegalStateException("NIM '" + mahasiswa.getNim() + "' sudah terdaftar di database! Gunakan NIM yang berbeda.", e)
                    ));
                }
            } catch (Exception e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(e));
                }
            }
        });
    }

    @Override
    public void updateMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                mahasiswa.markUpdated();
                int rows = mahasiswaDao.update(mahasiswa);
                if (callback != null) {
                    dispatchCallback(() -> callback.onSuccess(rows));
                }
            } catch (SQLiteConstraintException e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(
                            new IllegalStateException("NIM '" + mahasiswa.getNim() + "' bertabrakan dengan data mahasiswa lain!", e)
                    ));
                }
            } catch (Exception e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(e));
                }
            }
        });
    }

    @Override
    public void deleteMahasiswa(Mahasiswa mahasiswa, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                int rows = mahasiswaDao.delete(mahasiswa);
                if (callback != null) {
                    dispatchCallback(() -> callback.onSuccess(rows));
                }
            } catch (Exception e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(e));
                }
            }
        });
    }

    // ==========================================
    // OPERASI DOSEN
    // ==========================================

    @Override
    public LiveData<List<Dosen>> getAllDosen() {
        return dosenDao.getAll();
    }

    @Override
    public LiveData<List<Dosen>> searchDosen(String query) {
        return dosenDao.searchDosen(query);
    }

    @Override
    public void insertDosen(Dosen dosen, RepositoryCallback<Long> callback) {
        executor.execute(() -> {
            try {
                if (dosen.getCreatedAt() == 0) {
                    long now = System.currentTimeMillis();
                    dosen.setCreatedAt(now);
                    dosen.setUpdatedAt(now);
                }
                long id = dosenDao.insert(dosen);
                if (callback != null) {
                    dispatchCallback(() -> callback.onSuccess(id));
                }
            } catch (SQLiteConstraintException e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(
                            new IllegalStateException("NIDN '" + dosen.getNidn() + "' sudah terdaftar di database! Gunakan NIDN yang berbeda.", e)
                    ));
                }
            } catch (Exception e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(e));
                }
            }
        });
    }

    @Override
    public void updateDosen(Dosen dosen, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                dosen.markUpdated();
                int rows = dosenDao.update(dosen);
                if (callback != null) {
                    dispatchCallback(() -> callback.onSuccess(rows));
                }
            } catch (SQLiteConstraintException e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(
                            new IllegalStateException("NIDN '" + dosen.getNidn() + "' bertabrakan dengan data dosen lain!", e)
                    ));
                }
            } catch (Exception e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(e));
                }
            }
        });
    }

    @Override
    public void deleteDosen(Dosen dosen, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                int rows = dosenDao.delete(dosen);
                if (callback != null) {
                    dispatchCallback(() -> callback.onSuccess(rows));
                }
            } catch (Exception e) {
                if (callback != null) {
                    dispatchCallback(() -> callback.onError(e));
                }
            }
        });
    }
}
