package com.michaelshan.mykampuslite.viewmodel;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LiveData yang hanya memicu observer satu kali untuk setiap pembaruan data.
 * Mencegah pengiriman ulang event (seperti pesan Snackbar atau Toast) saat layar diputar.
 *
 * @param <T> Tipe data event
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private static final String TAG = "SingleLiveEvent";
    private final AtomicBoolean pending = new AtomicBoolean(false);

    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<? super T> observer) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Beberapa observer terdaftar tetapi hanya satu yang akan menerima notifikasi.");
        }

        super.observe(owner, t -> {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    @MainThread
    @Override
    public void setValue(T t) {
        pending.set(true);
        super.setValue(t);
    }

    @Override
    public void postValue(T value) {
        pending.set(true);
        super.postValue(value);
    }

    /**
     * Helper untuk memicu pemanggilan tanpa payload.
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}
