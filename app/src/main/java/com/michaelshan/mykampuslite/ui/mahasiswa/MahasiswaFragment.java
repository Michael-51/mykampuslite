package com.michaelshan.mykampuslite.ui.mahasiswa;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.michaelshan.mykampuslite.R;
import com.michaelshan.mykampuslite.databinding.FragmentMahasiswaBinding;
import com.michaelshan.mykampuslite.model.Mahasiswa;
import com.michaelshan.mykampuslite.model.Person;
import com.michaelshan.mykampuslite.ui.adapter.AkademikMultiTypeAdapter;
import com.michaelshan.mykampuslite.viewmodel.AkademikViewModel;
import com.michaelshan.mykampuslite.viewmodel.AkademikViewModelFactory;
import com.michaelshan.mykampuslite.viewmodel.FormErrorState;
import com.michaelshan.mykampuslite.viewmodel.MahasiswaDraft;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Fragment untuk pengelolaan data Mahasiswa.
 * Mengamati LiveData dari AkademikViewModel tanpa menyimpan logika bisnis atau akses database.
 * Mendukung fitur pencarian dinamis, validasi visual, proteksi rotasi draft form, dan konfirmasi hapus.
 */
public class MahasiswaFragment extends Fragment implements AkademikMultiTypeAdapter.OnItemActionListener {

    private FragmentMahasiswaBinding binding;
    private AkademikViewModel viewModel;
    private AkademikMultiTypeAdapter adapter;

    private boolean isEditing = false;
    private long editingId = 0;
    private boolean suppressDraftSync = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMahasiswaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi ViewModel melalui factory yang menyuntikkan Repository interface
        AkademikViewModelFactory factory = AkademikViewModelFactory.createDefault(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(AkademikViewModel.class);

        setupRecyclerView();
        setupFormListeners();
        setupSearchListener();
        observeViewModel();
        restoreDraftState();
    }

    private void setupRecyclerView() {
        adapter = new AkademikMultiTypeAdapter(this);
        binding.rvMahasiswa.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMahasiswa.setAdapter(adapter);
    }

    private void setupFormListeners() {
        binding.btnSimpan.setOnClickListener(v -> {
            String nama = getText(binding.etNama);
            String email = getText(binding.etEmail);
            String nim = getText(binding.etNim);
            String jurusan = getText(binding.etJurusan);
            String ipk = getText(binding.etIpk);

            viewModel.saveMahasiswa(editingId, nama, email, nim, jurusan, ipk, isEditing);
        });

        binding.btnBatal.setOnClickListener(v -> resetForm());

        // Sinkronisasi input form ke ViewModel draft untuk keamanan saat rotasi layar
        TextWatcher draftWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                syncDraftToViewModel();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etNama.addTextChangedListener(draftWatcher);
        binding.etEmail.addTextChangedListener(draftWatcher);
        binding.etNim.addTextChangedListener(draftWatcher);
        binding.etJurusan.addTextChangedListener(draftWatcher);
        binding.etIpk.addTextChangedListener(draftWatcher);
    }

    private void syncDraftToViewModel() {
        if (suppressDraftSync) return;
        MahasiswaDraft draft = viewModel.getMahasiswaDraft();
        draft.setId(editingId);
        draft.setNama(getText(binding.etNama));
        draft.setEmail(getText(binding.etEmail));
        draft.setNim(getText(binding.etNim));
        draft.setJurusan(getText(binding.etJurusan));
        draft.setIpk(getText(binding.etIpk));
        draft.setEditing(isEditing);
    }

    private void restoreDraftState() {
        MahasiswaDraft draft = viewModel.getMahasiswaDraft();
        suppressDraftSync = true;
        try {
            if (draft.isEditing()) {
                isEditing = true;
                editingId = draft.getId();
                binding.etNama.setText(draft.getNama());
                binding.etEmail.setText(draft.getEmail());
                binding.etNim.setText(draft.getNim());
                binding.etJurusan.setText(draft.getJurusan());
                binding.etIpk.setText(draft.getIpk());
                setEditModeUi(true, draft.getNama());
            } else if (!draft.getNama().isEmpty()
                    || !draft.getEmail().isEmpty()
                    || !draft.getNim().isEmpty()
                    || !draft.getJurusan().isEmpty()
                    || !draft.getIpk().isEmpty()) {
                binding.etNama.setText(draft.getNama());
                binding.etEmail.setText(draft.getEmail());
                binding.etNim.setText(draft.getNim());
                binding.etJurusan.setText(draft.getJurusan());
                binding.etIpk.setText(draft.getIpk());
            }
        } finally {
            suppressDraftSync = false;
        }
    }

    private void setupSearchListener() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                viewModel.setMahasiswaSearchQuery(s != null ? s.toString() : "");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        // Observasi daftar Mahasiswa (otomatis diperbarui melalui Room LiveData)
        viewModel.getMahasiswaList().observe(getViewLifecycleOwner(), mahasiswas -> {
            adapter.submitList(mahasiswas != null ? new ArrayList<>(mahasiswas) : new ArrayList<>());
        });

        // Observasi status error form dari Model
        viewModel.getMahasiswaFormErrors().observe(getViewLifecycleOwner(), this::renderFormErrors);

        // Observasi hasil operasi (Toast/Snackbar)
        viewModel.getMahasiswaOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                return;
            }

            int snackbarColor = result.isSuccess()
                    ? requireContext().getColor(R.color.primary)
                    : requireContext().getColor(R.color.danger);

            Snackbar snackbar = Snackbar.make(binding.getRoot(), result.getMessage(), Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(snackbarColor);
            snackbar.show();

            if (result.isSuccess() && !"DELETE".equals(result.getAction())) {
                resetForm();
            }
        });
    }

    private void renderFormErrors(FormErrorState errorState) {
        if (errorState == null) {
            clearErrors();
            return;
        }

        binding.etNama.setError(errorState.getNamaError());
        binding.etNim.setError(errorState.getIdentifierError());
        binding.etJurusan.setError(errorState.getJurusanError());
        binding.etIpk.setError(errorState.getIpkError());
    }

    private void clearErrors() {
        binding.etNama.setError(null);
        binding.etNim.setError(null);
        binding.etJurusan.setError(null);
        binding.etIpk.setError(null);
    }

    private void resetForm() {
        suppressDraftSync = true;
        try {
            isEditing = false;
            editingId = 0;

            binding.etNama.setText("");
            binding.etEmail.setText("");
            binding.etNim.setText("");
            binding.etJurusan.setText("");
            binding.etIpk.setText("");

            clearErrors();
            viewModel.resetMahasiswaFormErrors();
            viewModel.getMahasiswaDraft().clear();
            setEditModeUi(false, "");
        } finally {
            suppressDraftSync = false;
        }
    }

    private void setEditModeUi(boolean editing, String nama) {
        if (editing) {
            binding.btnSimpan.setText("Perbarui");
            binding.btnBatal.setVisibility(View.VISIBLE);
        } else {
            binding.btnSimpan.setText("Simpan");
            binding.btnBatal.setVisibility(View.GONE);
        }
    }

    // ==========================================
    // IMPLEMENTASI ON ITEM ACTION LISTENER
    // ==========================================

    @Override
    public void onEdit(Person item) {
        if (!(item instanceof Mahasiswa)) return;
        Mahasiswa m = (Mahasiswa) item;

        suppressDraftSync = true;
        try {
            isEditing = true;
            editingId = m.getId();

            binding.etNama.setText(m.getNama());
            binding.etEmail.setText(m.getEmail());
            binding.etNim.setText(m.getNim());
            binding.etJurusan.setText(m.getJurusan());
            binding.etIpk.setText(String.format(Locale.US, "%.2f", m.getIpk()));

            clearErrors();
            setEditModeUi(true, m.getNama());
        } finally {
            suppressDraftSync = false;
        }
        syncDraftToViewModel();

        // Gulir halus ke formulir di atas
        binding.getRoot().smoothScrollTo(0, 0);
    }

    @Override
    public void onDelete(Person item) {
        if (!(item instanceof Mahasiswa)) return;
        Mahasiswa m = (Mahasiswa) item;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Hapus Data")
                .setMessage("Apakah Anda yakin ingin menghapus data mahasiswa:\n\n"
                        + m.getNama() + " (" + m.getNim() + ")?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Hapus", (dialog, which) -> viewModel.deleteMahasiswa(m))
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String getText(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
