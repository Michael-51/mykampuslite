package com.michaelshan.mykampuslite;

import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Mahasiswa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Pengujian unit untuk implementasi equals() dan hashCode() (Bonus +10).
 * Memastikan stabilitas identitas objek untuk mendukung DiffUtil.ItemCallback pada ListAdapter.
 */
public class ModelEqualsAndHashCodeTest {

    @Test
    public void testMahasiswa_equalsAndHashCode_byId() {
        Mahasiswa m1 = new Mahasiswa(1L, 1000L, 1000L, "Fatih", "f@k.id", "2110114001", "IF", 3.8);
        Mahasiswa m2 = new Mahasiswa(1L, 2000L, 2500L, "Fatih", "f@k.id", "2110114001", "IF", 3.8);
        Mahasiswa m3 = new Mahasiswa(2L, 1000L, 1000L, "Fatih", "f@k.id", "2110114001", "IF", 3.8);

        // m1 dan m2 memiliki ID sama -> harus equal dan memiliki hashCode sama
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());

        // m1 dan m3 memiliki ID berbeda -> tidak equal
        assertNotEquals(m1, m3);
    }

    @Test
    public void testMahasiswa_equalsAndHashCode_byNim_whenIdZero() {
        Mahasiswa m1 = new Mahasiswa("Ali", "ali@k.id", "2110114001", "IF", 3.5);
        Mahasiswa m2 = new Mahasiswa("Ali", "ali@k.id", "2110114001", "IF", 3.5);
        Mahasiswa m3 = new Mahasiswa("Ali", "ali@k.id", "2110114002", "IF", 3.5);

        // Belum disimpan ke database (id == 0) -> fallback perbandingan ke NIM unik
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());

        assertNotEquals(m1, m3);
    }

    @Test
    public void testDosen_equalsAndHashCode_byIdAndNidn() {
        Dosen d1 = new Dosen(10L, 1000L, 1000L, "Pak Hendra", "h@k.id", "0415088001", "PBO");
        Dosen d2 = new Dosen(10L, 2000L, 2000L, "Pak Hendra", "h@k.id", "0415088001", "PBO");
        Dosen d3 = new Dosen(11L, 1000L, 1000L, "Pak Hendra", "h@k.id", "0415088001", "PBO");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
    }

    @Test
    public void testDifferentTypes_notEqual() {
        Mahasiswa m = new Mahasiswa("User", "u@k.id", "2110114001", "IF", 3.5);
        Dosen d = new Dosen("User", "u@k.id", "2110114001", "PBO");

        // Objek bertipe beda tidak boleh dianggap sama
        assertNotEquals(m, d);
    }
}
