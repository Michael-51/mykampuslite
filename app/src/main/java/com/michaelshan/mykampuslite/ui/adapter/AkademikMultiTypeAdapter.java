package com.michaelshan.mykampuslite.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.michaelshan.mykampuslite.R;
import com.michaelshan.mykampuslite.databinding.ItemDosenBinding;
import com.michaelshan.mykampuslite.databinding.ItemMahasiswaBinding;
import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.EvaluasiAkademik;
import com.michaelshan.mykampuslite.model.Mahasiswa;
import com.michaelshan.mykampuslite.model.Person;

import java.util.Locale;
import java.util.Objects;

/**
 * Satu ListAdapter terpadu yang mampu menampilkan dua tipe entitas (Mahasiswa dan Dosen)
 * melalui prinsip polymorphism, ListAdapter, dan DiffUtil.ItemCallback (Bonus +10).
 *
 * Logika evaluasi status akademik dipanggil langsung via antarmuka EvaluasiAkademik
 * tanpa menduplikasi aturan bisnis ke dalam adapter.
 */
public class AkademikMultiTypeAdapter extends ListAdapter<Person, RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_MAHASISWA = 1;
    public static final int VIEW_TYPE_DOSEN = 2;

    public interface OnItemActionListener {
        void onEdit(Person item);
        void onDelete(Person item);
    }

    private final OnItemActionListener actionListener;

    public AkademikMultiTypeAdapter(OnItemActionListener actionListener) {
        super(DIFF_CALLBACK);
        this.actionListener = actionListener;
    }

    private static final DiffUtil.ItemCallback<Person> DIFF_CALLBACK = new DiffUtil.ItemCallback<Person>() {
        @Override
        public boolean areItemsTheSame(@NonNull Person oldItem, @NonNull Person newItem) {
            if (!oldItem.getClass().equals(newItem.getClass())) {
                return false;
            }
            if (oldItem.getId() != 0 && newItem.getId() != 0) {
                return oldItem.getId() == newItem.getId();
            }
            if (oldItem instanceof Mahasiswa && newItem instanceof Mahasiswa) {
                return Objects.equals(((Mahasiswa) oldItem).getNim(), ((Mahasiswa) newItem).getNim());
            }
            if (oldItem instanceof Dosen && newItem instanceof Dosen) {
                return Objects.equals(((Dosen) oldItem).getNidn(), ((Dosen) newItem).getNidn());
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Person oldItem, @NonNull Person newItem) {
            // Memanfaatkan equals() pada Model yang diimplementasikan untuk perbandingan akurat
            return oldItem.equals(newItem)
                    && Objects.equals(oldItem.getNama(), newItem.getNama())
                    && Objects.equals(oldItem.getEmail(), newItem.getEmail());
        }
    };

    @Override
    public int getItemViewType(int position) {
        Person item = getItem(position);
        if (item instanceof Mahasiswa) {
            return VIEW_TYPE_MAHASISWA;
        } else if (item instanceof Dosen) {
            return VIEW_TYPE_DOSEN;
        }
        throw new IllegalArgumentException("Tipe item tidak didukung: " + item.getClass().getName());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_MAHASISWA) {
            ItemMahasiswaBinding binding = ItemMahasiswaBinding.inflate(inflater, parent, false);
            return new MahasiswaViewHolder(binding);
        } else if (viewType == VIEW_TYPE_DOSEN) {
            ItemDosenBinding binding = ItemDosenBinding.inflate(inflater, parent, false);
            return new DosenViewHolder(binding);
        }
        throw new IllegalArgumentException("Unknown viewType: " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Person item = getItem(position);
        if (holder instanceof MahasiswaViewHolder && item instanceof Mahasiswa) {
            ((MahasiswaViewHolder) holder).bind((Mahasiswa) item, actionListener);
        } else if (holder instanceof DosenViewHolder && item instanceof Dosen) {
            ((DosenViewHolder) holder).bind((Dosen) item, actionListener);
        }
    }

    // ==========================================
    // VIEW HOLDER: MAHASISWA
    // ==========================================

    public static class MahasiswaViewHolder extends RecyclerView.ViewHolder {
        private final ItemMahasiswaBinding binding;

        public MahasiswaViewHolder(ItemMahasiswaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Mahasiswa mahasiswa, OnItemActionListener listener) {
            binding.tvNama.setText(mahasiswa.getNama());
            binding.tvEmail.setText(mahasiswa.getEmail() != null && !mahasiswa.getEmail().isEmpty() ? mahasiswa.getEmail() : "-");
            binding.tvNim.setText("NIM: " + mahasiswa.getNim());
            binding.tvJurusan.setText("Jurusan: " + mahasiswa.getJurusan());
            binding.tvIpk.setText(String.format(Locale.US, "IPK: %.2f", mahasiswa.getIpk()));

            // Panggilan Evaluasi Polimorfik tanpa if-else IPK di Adapter
            if (mahasiswa instanceof EvaluasiAkademik) {
                String status = ((EvaluasiAkademik) mahasiswa).getStatusAkademik();
                binding.tvStatusAkademik.setText("Status: " + status);
            }

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(mahasiswa);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(mahasiswa);
            });
        }
    }

    // ==========================================
    // VIEW HOLDER: DOSEN
    // ==========================================

    public static class DosenViewHolder extends RecyclerView.ViewHolder {
        private final ItemDosenBinding binding;

        public DosenViewHolder(ItemDosenBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Dosen dosen, OnItemActionListener listener) {
            binding.tvNama.setText(dosen.getNama());
            binding.tvEmail.setText(dosen.getEmail() != null && !dosen.getEmail().isEmpty() ? dosen.getEmail() : "-");
            binding.tvNidn.setText("NIDN: " + dosen.getNidn());
            binding.tvMataKuliah.setText("Mata Kuliah: " + dosen.getMataKuliahDiampu());
            binding.tvJumlahMatkul.setText("Jumlah: " + dosen.getJumlahMataKuliah() + " Mata Kuliah");

            // Panggilan Evaluasi Polimorfik tanpa cabang di Adapter
            if (dosen instanceof EvaluasiAkademik) {
                String status = ((EvaluasiAkademik) dosen).getStatusAkademik();
                binding.tvStatusAkademik.setText("Status: " + status);
            }

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(dosen);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(dosen);
            });
        }
    }
}
