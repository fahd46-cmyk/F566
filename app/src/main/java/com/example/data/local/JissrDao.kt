package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JissrDao {
    @Query("SELECT * FROM bridge_nodes ORDER BY isFavorite DESC, CASE WHEN lastLatencyMs > 0 THEN lastLatencyMs ELSE 9999 END ASC, city ASC")
    fun observeAllNodes(): Flow<List<BridgeNodeEntity>>

    @Query("SELECT * FROM bridge_nodes")
    suspend fun getAllNodesOnce(): List<BridgeNodeEntity>

    @Query("SELECT * FROM bridge_nodes WHERE id = :id LIMIT 1")
    suspend fun getNodeById(id: String): BridgeNodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<BridgeNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: BridgeNodeEntity)

    @Update
    suspend fun updateNode(node: BridgeNodeEntity)

    @Query("UPDATE bridge_nodes SET lastLatencyMs = :latencyMs WHERE id = :id")
    suspend fun updateNodeLatency(id: String, latencyMs: Int)

    @Query("UPDATE bridge_nodes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateNodeFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM bridge_nodes WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomNode(id: String)

    @Query("SELECT * FROM tunnel_audit_logs ORDER BY timestamp DESC LIMIT 80")
    fun observeAuditLogs(): Flow<List<TunnelAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: TunnelAuditLogEntity)

    @Query("DELETE FROM tunnel_audit_logs")
    suspend fun clearAuditLogs()
}
