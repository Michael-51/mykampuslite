package com.michaelshan.mykampuslite;

import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.EvaluasiAkademik;
import com.michaelshan.mykampuslite.model.Mahasiswa;
import com.michaelshan.mykampuslite.model.Person;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Pengujian unit untuk prinsip Polymorphism (TC09, TC10, TC11, TC18).
 * Menguji interface EvaluasiAkademik dan method getStatusAkademik() pada Mahasiswa dan Dosen,
 * serta verifikasi kontrak polimorfik multi-tipe.
 */
public class PolymorphismTest {

    // ==========================================
    // TC09: Status Mahasiswa pada rentang dan batas IPK
    // ==========================================

    @Test
    public void tc09_mahasiswa_cumlaude() {
        Mahasiswa m1 = new Mahasiswa("Ahmad", "ahmad@kampus.id", "2110114001", "Informatika", 3.51);
        Mahasiswa m2 = new Mahasiswa("Budi", "budi@kampus.id", "2110114002", "Informatika", 3.85);
        Mahasiswa m3 = new Mahasiswa("Citra", "citra@kampus.id", "2110114003", "Informatika", 4.00);

        assertEquals("Cumlaude", m1.getStatusAkademik());
        assertEquals("Cumlaude", m2.getStatusAkademik());
        assertEquals("Cumlaude", m3.getStatusAkademik());
    }

    @Test
    public void tc09_mahasiswa_memuaskan() {
        Mahasiswa m1 = new Mahasiswa("Dedi", "dedi@kampus.id", "2110114004", "Sistem Informasi", 3.01);
        Mahasiswa m2 = new Mahasiswa("Eka", "eka@kampus.id", "2110114005", "Sistem Informasi", 3.25);
        Mahasiswa m3 = new Mahasiswa("Fani", "fani@kampus.id", "2110114006", "Sistem Informasi", 3.50);

        assertEquals("Memuaskan", m1.getStatusAkademik());
        assertEquals("Memuaskan", m2.getStatusAkademik());
        assertEquals("Memuaskan", m3.getStatusAkademik());
    }

    @Test
    public void tc09_mahasiswa_normal() {
        Mahasiswa m1 = new Mahasiswa("Gita", "gita@kampus.id", "2110114007", "Teknik Elektro", 2.00);
        Mahasiswa m2 = new Mahasiswa("Hadi", "hadi@kampus.id", "2110114008", "Teknik Elektro", 2.75);
        Mahasiswa m3 = new Mahasiswa("Indah", "indah@kampus.id", "2110114009", "Teknik Elektro", 3.00);

        assertEquals("Normal", m1.getStatusAkademik());
        assertEquals("Normal", m2.getStatusAkademik());
        assertEquals("Normal", m3.getStatusAkademik());
    }

    @Test
    public void tc09_mahasiswa_peringatan() {
        Mahasiswa m1 = new Mahasiswa("Joko", "joko@kampus.id", "2110114010", "Manajemen", 0.00);
        Mahasiswa m2 = new Mahasiswa("Kiki", "kiki@kampus.id", "2110114011", "Manajemen", 1.50);
        Mahasiswa m3 = new Mahasiswa("Lina", "lina@kampus.id", "2110114012", "Manajemen", 1.99);

        assertEquals("Peringatan", m1.getStatusAkademik());
        assertEquals("Peringatan", m2.getStatusAkademik());
        assertEquals("Peringatan", m3.getStatusAkademik());
    }

    // ==========================================
    // TC10 & TC11: Status Dosen Beban Mengajar
    // ==========================================

    @Test
    public void tc10_dosen_3MataKuliah_statusNormal() {
        Dosen d1 = new Dosen("Dr. Maya", "maya@kampus.id", "0415088001", "PBO, Algoritma, Basis Data");
        assertEquals(3, d1.getJumlahMataKuliah());
        assertEquals("Normal", d1.getStatusAkademik());

        Dosen d2 = new Dosen("Dr. Naufal", "naufal@kampus.id", "0415088002", "PBO");
        assertEquals(1, d2.getJumlahMataKuliah());
        assertEquals("Normal", d2.getStatusAkademik());
    }

    @Test
    public void tc11_dosen_4MataKuliah_statusBebanLebih() {
        Dosen d1 = new Dosen("Prof. Oki", "oki@kampus.id", "0415088003", "PBO, Algoritma, Basis Data, Jaringan Komputer");
        assertEquals(4, d1.getJumlahMataKuliah());
        assertEquals("Beban Lebih", d1.getStatusAkademik());

        Dosen d2 = new Dosen("Prof. Putri", "putri@kampus.id", "0415088004", "A, B, C, D, E");
        assertEquals(5, d2.getJumlahMataKuliah());
        assertEquals("Beban Lebih", d2.getStatusAkademik());
    }

    // ==========================================
    // TC18: Polimorfisme Multi-Tipe Melalui EvaluasiAkademik dan Person
    // ==========================================

    @Test
    public void tc18_polymorphicList_evaluationWithoutTypeChecking() {
        List<Person> daftarPerson = new ArrayList<>();

        Mahasiswa m = new Mahasiswa("Rian", "rian@kampus.id", "2110114020", "Informatika", 3.9);
        Dosen d = new Dosen("Dr. Sinta", "sinta@kampus.id", "0415088010", "Matematika, Fisika, Kimia, Biologi");

        daftarPerson.add(m);
        daftarPerson.add(d);

        // Uji pemanggilan method getRole() secara polimorfik
        assertEquals("Mahasiswa", daftarPerson.get(0).getRole());
        assertEquals("Dosen", daftarPerson.get(1).getRole());

        // Uji pemanggilan getStatusAkademik() via EvaluasiAkademik tanpa if-else manual
        List<EvaluasiAkademik> evaluasis = new ArrayList<>();
        evaluasis.add(m);
        evaluasis.add(d);

        assertEquals("Cumlaude", evaluasis.get(0).getStatusAkademik());
        assertEquals("Beban Lebih", evaluasis.get(1).getStatusAkademik());
    }
}
