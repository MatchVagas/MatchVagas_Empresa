package com.edu.matchvagasempresas.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.edu.matchvagasempresas.data.local.dao.CandidaturaDao;
import com.edu.matchvagasempresas.data.local.dao.EmpresaDao;
import com.edu.matchvagasempresas.data.local.dao.LookupDao;
import com.edu.matchvagasempresas.data.local.dao.VagaDao;
import com.edu.matchvagasempresas.data.local.entity.CandidaturaEntity;
import com.edu.matchvagasempresas.data.local.entity.Converters;
import com.edu.matchvagasempresas.data.local.entity.EmpresaEntity;
import com.edu.matchvagasempresas.data.local.entity.LookupEntity;
import com.edu.matchvagasempresas.data.local.entity.VagaEntity;

@Database(
    entities = {VagaEntity.class, CandidaturaEntity.class, LookupEntity.class, EmpresaEntity.class},
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract VagaDao vagaDao();
    public abstract CandidaturaDao candidaturaDao();
    public abstract LookupDao lookupDao();
    public abstract EmpresaDao empresaDao();

    public static AppDatabase get(Context ctx) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            ctx.getApplicationContext(),
                            AppDatabase.class,
                            "matchvagas.db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return instance;
    }
}
