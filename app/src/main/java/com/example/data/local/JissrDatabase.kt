package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BridgeNodeEntity::class, TunnelAuditLogEntity::class],
    version = 2,
    exportSchema = false
)
abstract class JissrDatabase : RoomDatabase() {
    abstract fun jissrDao(): JissrDao

    companion object {
        @Volatile
        private var INSTANCE: JissrDatabase? = null

        fun getInstance(context: Context): JissrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JissrDatabase::class.java,
                    "jissr_vpn_bridge.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
