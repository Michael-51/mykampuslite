package com.michaelshan.mykampuslite.data.repository;

/**
 * Interface callback untuk operasi repository asinkron.
 *
 * @param <T> Tipe data hasil operasi
 */
public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(Exception exception);
}
