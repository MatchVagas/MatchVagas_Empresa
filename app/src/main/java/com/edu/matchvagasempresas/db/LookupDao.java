package com.edu.matchvagasempresas.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LookupDao {

    @Query("SELECT * FROM lookups WHERE tipo = :tipo")
    List<LookupEntity> getByTipo(String tipo);

    @Query("SELECT COUNT(*) FROM lookups")
    int count();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LookupEntity> items);

    @Query("DELETE FROM lookups WHERE tipo = :tipo")
    void deleteByTipo(String tipo);

    @Query("DELETE FROM lookups")
    void deleteAll();
}
