package com.edu.matchvagasempresas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.edu.matchvagasempresas.data.local.entity.EmpresaEntity;

@Dao
public interface EmpresaDao {

    @Query("SELECT * FROM empresa LIMIT 1")
    EmpresaEntity get();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EmpresaEntity empresa);

    @Query("DELETE FROM empresa")
    void delete();
}
