package com.edu.matchvagasempresas.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CandidaturaDao {

    @Query("SELECT * FROM candidaturas WHERE vagaId = :vagaId ORDER BY dataCandidatura DESC")
    List<CandidaturaEntity> getByVaga(long vagaId);

    @Query("SELECT * FROM candidaturas WHERE id = :id")
    CandidaturaEntity getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CandidaturaEntity> candidaturas);

    @Query("DELETE FROM candidaturas WHERE vagaId = :vagaId")
    void deleteByVaga(long vagaId);

    @Query("SELECT cachedAt FROM candidaturas WHERE vagaId = :vagaId LIMIT 1")
    Long getCachedAt(long vagaId);

    @Query("DELETE FROM candidaturas")
    void deleteAll();
}
