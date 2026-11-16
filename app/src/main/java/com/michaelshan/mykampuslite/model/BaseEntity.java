package com.michaelshan.mykampuslite.model;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

/**
 * BaseEntity merupakan kelas abstrak induk tertinggi untuk seluruh entitas database.
 * Menyediakan ID unik serta timestamp pembuatan dan pembaruan data.
 * Seluruh field bersifat private untuk menjamin prinsip enkapsulasi.
 */
public abstract class BaseEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public BaseEntity() {
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public BaseEntity(long id, long createdAt, long updatedAt) {
        this.id = id;
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Memperbarui timestamp perubahan ke waktu saat ini.
     */
    public void markUpdated() {
        this.updatedAt = System.currentTimeMillis();
    }
}
