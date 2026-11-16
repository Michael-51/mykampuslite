package com.michaelshan.mykampuslite.model;

/**
 * Interface untuk evaluasi status akademik secara polimorfik.
 * Diimplementasikan oleh Mahasiswa dan Dosen.
 */
public interface EvaluasiAkademik {
    /**
     * Menghasilkan status akademik objek berdasarkan aturan bisnis masing-masing entitas.
     *
     * @return Label status akademik (misal: "Cumlaude", "Memuaskan", "Normal", "Peringatan", "Beban Lebih").
     */
    String getStatusAkademik();
}
