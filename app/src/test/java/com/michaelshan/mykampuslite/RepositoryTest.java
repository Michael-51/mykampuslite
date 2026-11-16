package com.michaelshan.mykampuslite;

import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.michaelshan.mykampuslite.data.local.DosenDao;
import com.michaelshan.mykampuslite.data.local.MahasiswaDao;
import com.michaelshan.mykampuslite.data.repository.MahasiswaRepository;
import com.michaelshan.mykampuslite.data.repository.RepositoryCallback;
import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Mahasiswa;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Pengujian unit untuk MahasiswaRepository dan penanganan pelanggaran indeks unik Room (TC06, TC12, TC13, TC14, TC17).
 */
public class RepositoryTest {

    private FakeMahasiswaDao fakeMahasiswaDao;
    private FakeDosenDao fakeDosenDao;
    private ExecutorService directExecutor;
    private MahasiswaRepository repository;

    @Before
    public void setUp() {
        fakeMahasiswaDao = new FakeMahasiswaDao();
        fakeDosenDao = new FakeDosenDao();
        // Executor langsung untuk pengujian deterministik
        directExecutor = Executors.newSingleThreadExecutor();
        repository = new MahasiswaRepository(fakeMahasiswaDao, fakeDosenDao, directExecutor);
    }

    // ==========================================
    // TC06: Pencegahan Duplikasi NIM di Database
    // ==========================================

    @Test
    public void tc06_duplicateNim_throwsHandledConstraintException() throws InterruptedException {
        Mahasiswa m1 = new Mahasiswa("Ahmad", "ahmad@k.id", "2110114001", "Informatika", 3.8);
        fakeMahasiswaDao.shouldThrowConstraintExceptionOnInsert = true;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> caughtException = new AtomicReference<>();

        repository.insertMahasiswa(m1, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                latch.countDown();
            }

            @Override
            public void onError(Exception exception) {
                caughtException.set(exception);
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertNotNull(caughtException.get());
        assertTrue(caughtException.get().getMessage().contains("sudah terdaftar di database"));
        assertTrue(caughtException.get().getMessage().contains("2110114001"));
    }

    @Test
    public void testRepository_insertMahasiswa_setsTimestamps() throws InterruptedException {
        Mahasiswa m = new Mahasiswa("Budi", "budi@k.id", "2110114002", "Sistem Informasi", 3.5);
        m.setCreatedAt(0);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> resultId = new AtomicReference<>();

        repository.insertMahasiswa(m, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                resultId.set(result);
                latch.countDown();
            }

            @Override
            public void onError(Exception exception) {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertNotNull(resultId.get());
        assertTrue(m.getCreatedAt() > 0);
        assertTrue(m.getUpdatedAt() > 0);
    }

    @Test
    public void tc14_dosenDuplicateNidn_handledGracefully() throws InterruptedException {
        Dosen d = new Dosen("Dr. Maya", "maya@k.id", "0415088001", "PBO");
        fakeDosenDao.shouldThrowConstraintException = true;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> caughtException = new AtomicReference<>();

        repository.insertDosen(d, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                latch.countDown();
            }

            @Override
            public void onError(Exception exception) {
                caughtException.set(exception);
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertNotNull(caughtException.get());
        assertTrue(caughtException.get().getMessage().contains("NIDN '0415088001' sudah terdaftar"));
    }

    // ==========================================
    // STUB DAO UNTUK TESTING REPOSITORY
    // ==========================================

    private static class FakeMahasiswaDao implements MahasiswaDao {
        boolean shouldThrowConstraintExceptionOnInsert = false;
        final List<Mahasiswa> list = new ArrayList<>();

        @Override
        public LiveData<List<Mahasiswa>> getAll() {
            return new MutableLiveData<>(list);
        }

        @Override
        public LiveData<List<Mahasiswa>> searchMahasiswa(String query) {
            return new MutableLiveData<>(list);
        }

        @Override
        public Mahasiswa getById(long id) {
            return null;
        }

        @Override
        public Mahasiswa getByNim(String nim) {
            return null;
        }

        @Override
        public long insert(Mahasiswa mahasiswa) {
            if (shouldThrowConstraintExceptionOnInsert) {
                throw new SQLiteConstraintException("UNIQUE constraint failed: mahasiswa.nim");
            }
            list.add(mahasiswa);
            return list.size();
        }

        @Override
        public int update(Mahasiswa mahasiswa) {
            return 1;
        }

        @Override
        public int delete(Mahasiswa mahasiswa) {
            list.remove(mahasiswa);
            return 1;
        }
    }

    private static class FakeDosenDao implements DosenDao {
        boolean shouldThrowConstraintException = false;
        final List<Dosen> list = new ArrayList<>();

        @Override
        public LiveData<List<Dosen>> getAll() {
            return new MutableLiveData<>(list);
        }

        @Override
        public LiveData<List<Dosen>> searchDosen(String query) {
            return new MutableLiveData<>(list);
        }

        @Override
        public Dosen getById(long id) {
            return null;
        }

        @Override
        public Dosen getByNidn(String nidn) {
            return null;
        }

        @Override
        public long insert(Dosen dosen) {
            if (shouldThrowConstraintException) {
                throw new SQLiteConstraintException("UNIQUE constraint failed: dosen.nidn");
            }
            list.add(dosen);
            return list.size();
        }

        @Override
        public int update(Dosen dosen) {
            return 1;
        }

        @Override
        public int delete(Dosen dosen) {
            list.remove(dosen);
            return 1;
        }
    }
}
