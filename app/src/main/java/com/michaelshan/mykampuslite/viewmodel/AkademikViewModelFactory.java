package com.michaelshan.mykampuslite.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.michaelshan.mykampuslite.data.local.MyKampusDatabase;
import com.michaelshan.mykampuslite.data.repository.AkademikRepository;
import com.michaelshan.mykampuslite.data.repository.MahasiswaRepository;

/**
 * ViewModelProvider.Factory untuk menyuntikkan ketergantungan AkademikRepository ke AkademikViewModel.
 */
public class AkademikViewModelFactory implements ViewModelProvider.Factory {

    private final AkademikRepository repository;

    public AkademikViewModelFactory(@NonNull AkademikRepository repository) {
        this.repository = repository;
    }

    /**
     * Helper factory untuk menginisialisasi repository default dari context aplikasi.
     */
    public static AkademikViewModelFactory createDefault(Context context) {
        MyKampusDatabase database = MyKampusDatabase.getInstance(context);
        AkademikRepository repository = new MahasiswaRepository(database);
        return new AkademikViewModelFactory(repository);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AkademikViewModel.class)) {
            return (T) new AkademikViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
