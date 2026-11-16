package com.michaelshan.mykampuslite.ui.dosen;

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
import com.michaelshan.mykampuslite.databinding.FragmentDosenBinding;
import com.michaelshan.mykampuslite.model.Dosen;
import com.michaelshan.mykampuslite.model.Person;
import com.michaelshan.mykampuslite.ui.adapter.AkademikMultiTypeAdapter;
import com.michaelshan.mykampuslite.viewmodel.AkademikViewModel;
import com.michaelshan.mykampuslite.viewmodel.AkademikViewModelFactory;
import com.michaelshan.mykampuslite.viewmodel.DosenDraft;
import com.michaelshan.mykampuslite.viewmodel.FormErrorState;

import java.util.ArrayList;

/**
 * Fragment untuk pengelolaan data Dosen.
 * Mengamati LiveData dari AkademikViewModel tanpa menyimpan logika bisnis atau akses database.
 * Mendukung pencarian, validasi Model visual, proteksi draft rotasi layar, dan status polimorfik.
 */
public class DosenFragment extends Fragment implements AkademikMultiTypeAdapter.OnItemActionListener {

    private FragmentDosenBinding binding;
    private AkademikViewModel viewModel;
    private AkademikMultiTypeAdapter adapter;

    private boolean isEditing = false;
    private long editingId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDosenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        binding.rvDosen.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDosen.setAdapter(adapter);
    }

    private void setupFormListeners() {
        binding.btnSimpan.setOnClickListener(v -> {
            String nama = getText(binding.etNama);
            String email = getText(binding.etEmail);
            String nidn = getText(binding.etNidn);
            String mataKuliah = getText(binding.etMataKuliah);

            viewModel.saveDosen(editingId, nama, email, nidn, mataKuliah, isEditing);
        });

        binding.btnBatal.setOnClickListener(v -> resetForm());

        // Sinkronisasi draft ke ViewModel untuk ketahanan rotasi layar
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
        binding.etNidn.addTextChangedListener(draftWatcher);
        binding.etMataKuliah.addTextChangedListener(draftWatcher);
    }

    private void syncDraftToViewModel() {
        DosenDraft draft = viewModel.getDosenDraft();
        draft.setId(editingId);
        draft.setNama(getText(binding.etNama));
        draft.setEmail(getText(binding.etEmail));
        draft.setNidn(getText(binding.etNidn));
        draft.setMataKuliahDiampu(getText(binding.etMataKuliah));
        draft.setEditing(isEditing);
    }

    private void restoreDraftState() {
        DosenDraft draft = viewModel.getDosenDraft();
        if (draft.isEditing()) {
            isEditing = true;
            editingId = draft.getId();
            binding.etNama.setText(draft.getNama());
            binding.etEmail.setText(draft.getEmail());
            binding.etNidn.setText(draft.getNidn());
            binding.etMataKuliah.setText(draft.getMataKuliahDiampu());
            setEditModeUi(true, draft.getNama());
        } else if (!draft.getNama().isEmpty() || !draft.getNidn().isEmpty()) {
            binding.etNama.setText(draft.getNama());
            binding.etEmail.setText(draft.getEmail());
            binding.etNidn.setText(draft.getNidn());
            binding.etMataKuliah.setText(draft.getMataKuliahDiampu());
        }
    }

    private void setupSearchListener() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                viewModel.setDosenSearchQuery(s != null ? s.toString() : "");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        viewModel.getDosenList().observe(getViewLifecycleOwner(), dosens -> {
            adapter.submitList(dosens != null ? new ArrayList<>(dosens) : new ArrayList<>());
        });

        viewModel.getDosenFormErrors().observe(getViewLifecycleOwner(), this::renderFormErrors);

        viewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null || !"Dosen".equals(result.getEntityName())) {
                return;
            }

            int snackbarColor = result.isSuccess()
                    ? requireContext().getColor(R.color.secondary)
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
        binding.etNidn.setError(errorState.getIdentifierError());
        binding.etMataKuliah.setError(errorState.getMataKuliahError());
    }

    private void clearErrors() {
        binding.etNama.setError(null);
        binding.etNidn.setError(null);
        binding.etMataKuliah.setError(null);
    }

    private void resetForm() {
        isEditing = false;
        editingId = 0;

        binding.etNama.setText("");
        binding.etEmail.setText("");
        binding.etNidn.setText("");
        binding.etMataKuliah.setText("");

        clearErrors();
        viewModel.resetDosenFormErrors();
        viewModel.getDosenDraft().clear();
        setEditModeUi(false, "");
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
        if (!(item instanceof Dosen)) return;
        Dosen d = (Dosen) item;

        isEditing = true;
        editingId = d.getId();

        binding.etNama.setText(d.getNama());
        binding.etEmail.setText(d.getEmail());
        binding.etNidn.setText(d.getNidn());
        binding.etMataKuliah.setText(d.getMataKuliahDiampu());

        clearErrors();
        setEditModeUi(true, d.getNama());
        syncDraftToViewModel();

        binding.getRoot().smoothScrollTo(0, 0);
    }

    @Override
    public void onDelete(Person item) {
        if (!(item instanceof Dosen)) return;
        Dosen d = (Dosen) item;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Hapus Data")
                .setMessage("Apakah Anda yakin ingin menghapus data dosen:\n\n"
                        + d.getNama() + " (" + d.getNidn() + ")?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Hapus", (dialog, which) -> viewModel.deleteDosen(d))
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
