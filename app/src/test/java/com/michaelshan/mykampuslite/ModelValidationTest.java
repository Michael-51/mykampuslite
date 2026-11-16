package com.michaelshan.mykampuslite;

import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Mahasiswa;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Pengujian unit untuk enkapsulasi dan aturan validasi pada Model (TC02, TC03, TC04, TC05, TC07, TC08).
 * Memastikan bahwa aturan bisnis dijalankan di level Model sebelum menyentuh lapisan database.
 */
public class ModelValidationTest {

    private Mahasiswa mahasiswa;
    private Dosen dosen;

    @Before
    public void setUp() {
        mahasiswa = new Mahasiswa();
        dosen = new Dosen();
    }

    // ==========================================
    // TC02: Nama kosong atau hanya spasi
    // ==========================================

    @Test
    public void tc02_setNama_null_throwsException() {
        try {
            mahasiswa.setNama(null);
            fail("Seharusnya melempar IllegalArgumentException untuk nama null");
        } catch (IllegalArgumentException e) {
            assertEquals("Nama tidak boleh kosong dan minimal 3 karakter", e.getMessage());
        }
    }

    @Test
    public void tc02_setNama_empty_throwsException() {
        try {
            mahasiswa.setNama("");
            fail("Seharusnya melempar IllegalArgumentException untuk nama string kosong");
        } catch (IllegalArgumentException e) {
            assertEquals("Nama tidak boleh kosong dan minimal 3 karakter", e.getMessage());
        }
    }

    @Test
    public void tc02_setNama_whitespaceOnly_throwsException() {
        try {
            mahasiswa.setNama("     ");
            fail("Seharusnya melempar IllegalArgumentException untuk nama hanya spasi");
        } catch (IllegalArgumentException e) {
            assertEquals("Nama tidak boleh kosong dan minimal 3 karakter", e.getMessage());
        }
    }

    // ==========================================
    // TC03: Nama kurang dari 3 karakter
    // ==========================================

    @Test
    public void tc03_setNama_lessThan3Chars_throwsException() {
        try {
            mahasiswa.setNama("Al");
            fail("Seharusnya melempar IllegalArgumentException untuk nama < 3 karakter");
        } catch (IllegalArgumentException e) {
            assertEquals("Nama tidak boleh kosong dan minimal 3 karakter", e.getMessage());
        }

        try {
            dosen.setNama(" B ");
            fail("Seharusnya melempar IllegalArgumentException untuk nama ter-trim < 3 karakter");
        } catch (IllegalArgumentException e) {
            assertEquals("Nama tidak boleh kosong dan minimal 3 karakter", e.getMessage());
        }
    }

    @Test
    public void tc03_setNama_validLength_success() {
        mahasiswa.setNama("Ali");
        assertEquals("Ali", mahasiswa.getNama());

        mahasiswa.setNama("  Budi Santoso  ");
        assertEquals("Budi Santoso", mahasiswa.getNama());
    }

    // ==========================================
    // TC04: NIM berisi huruf
    // ==========================================

    @Test
    public void tc04_setNim_containsLetters_throwsException() {
        try {
            mahasiswa.setNim("211011400A");
            fail("Seharusnya melempar IllegalArgumentException untuk NIM berisi huruf");
        } catch (IllegalArgumentException e) {
            assertEquals("NIM harus tepat 10 digit angka", e.getMessage());
        }

        try {
            mahasiswa.setNim("ABCDEFGHIJ");
            fail("Seharusnya melempar IllegalArgumentException untuk NIM semua huruf");
        } catch (IllegalArgumentException e) {
            assertEquals("NIM harus tepat 10 digit angka", e.getMessage());
        }
    }

    // ==========================================
    // TC05: NIM bukan tepat 10 digit
    // ==========================================

    @Test
    public void tc05_setNim_tooShort_throwsException() {
        try {
            mahasiswa.setNim("123456789"); // 9 digit
            fail("Seharusnya melempar IllegalArgumentException untuk NIM < 10 digit");
        } catch (IllegalArgumentException e) {
            assertEquals("NIM harus tepat 10 digit angka", e.getMessage());
        }
    }

    @Test
    public void tc05_setNim_tooLong_throwsException() {
        try {
            mahasiswa.setNim("12345678901"); // 11 digit
            fail("Seharusnya melempar IllegalArgumentException untuk NIM > 10 digit");
        } catch (IllegalArgumentException e) {
            assertEquals("NIM harus tepat 10 digit angka", e.getMessage());
        }
    }

    @Test
    public void tc05_setNim_exactly10Digits_success() {
        mahasiswa.setNim("2110114001");
        assertEquals("2110114001", mahasiswa.getNim());
    }

    // ==========================================
    // TC07: IPK di bawah 0.0 atau di atas 4.0
    // ==========================================

    @Test
    public void tc07_setIpk_negative_throwsException() {
        try {
            mahasiswa.setIpk(-0.01);
            fail("Seharusnya melempar IllegalArgumentException untuk IPK negatif");
        } catch (IllegalArgumentException e) {
            assertEquals("IPK harus berada pada rentang 0.0 sampai 4.0", e.getMessage());
        }
    }

    @Test
    public void tc07_setIpk_above4_throwsException() {
        try {
            mahasiswa.setIpk(4.01);
            fail("Seharusnya melempar IllegalArgumentException untuk IPK > 4.0");
        } catch (IllegalArgumentException e) {
            assertEquals("IPK harus berada pada rentang 0.0 sampai 4.0", e.getMessage());
        }

        try {
            mahasiswa.setIpk(5.0);
            fail("Seharusnya melempar IllegalArgumentException untuk IPK 5.0");
        } catch (IllegalArgumentException e) {
            assertEquals("IPK harus berada pada rentang 0.0 sampai 4.0", e.getMessage());
        }
    }

    // ==========================================
    // TC08: IPK batas rentang inklusif (0.0 dan 4.0)
    // ==========================================

    @Test
    public void tc08_setIpk_boundaryValues_success() {
        mahasiswa.setIpk(0.0);
        assertEquals(0.0, mahasiswa.getIpk(), 0.001);

        mahasiswa.setIpk(4.0);
        assertEquals(4.0, mahasiswa.getIpk(), 0.001);

        mahasiswa.setIpk(3.75);
        assertEquals(3.75, mahasiswa.getIpk(), 0.001);
    }

    // ==========================================
    // Validasi Entitas Dosen
    // ==========================================

    @Test
    public void testDosen_setNidn_validation() {
        try {
            dosen.setNidn("123");
            fail("Seharusnya melempar exception untuk NIDN bukan 10 digit");
        } catch (IllegalArgumentException e) {
            assertEquals("NIDN harus tepat 10 digit angka", e.getMessage());
        }

        dosen.setNidn("0415088001");
        assertEquals("0415088001", dosen.getNidn());
    }

    @Test
    public void testDosen_setMataKuliah_validation() {
        try {
            dosen.setMataKuliahDiampu("   ");
            fail("Seharusnya melempar exception untuk mata kuliah kosong");
        } catch (IllegalArgumentException e) {
            assertEquals("Mata kuliah diampu tidak boleh kosong", e.getMessage());
        }

        dosen.setMataKuliahDiampu("Pemrograman Berorientasi Objek");
        assertEquals("Pemrograman Berorientasi Objek", dosen.getMataKuliahDiampu());
    }
}
