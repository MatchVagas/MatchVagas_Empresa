package com.edu.matchvagasempresas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.edu.matchvagasempresas.data.local.entity.VagaEntity;

import java.util.List;

@Dao
public interface VagaDao {

    @Query("SELECT * FROM vagas ORDER BY dataPublicacao DESC")
    List<VagaEntity> getAll();

    @Query("SELECT * FROM vagas WHERE id = :id")
    VagaEntity getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VagaEntity> vagas);

    @Query("DELETE FROM vagas")
    void deleteAll();

    @Query("SELECT cachedAt FROM vagas LIMIT 1")
    Long getCachedAt();
}
