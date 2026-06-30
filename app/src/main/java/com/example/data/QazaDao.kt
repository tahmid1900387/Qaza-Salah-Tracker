package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QazaDao {
    @Query("SELECT * FROM qaza_prayers")
    fun getPrayersFlow(): Flow<List<QazaPrayerEntity>>

    @Query("SELECT * FROM qaza_prayers")
    suspend fun getPrayers(): List<QazaPrayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayers(prayers: List<QazaPrayerEntity>)

    @Query("SELECT * FROM qaza_history ORDER BY timestamp DESC")
    fun getHistoryFlow(): Flow<List<QazaHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: QazaHistoryEntity)

    @Delete
    suspend fun deleteHistory(history: QazaHistoryEntity)

    @Query("DELETE FROM qaza_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("SELECT * FROM qaza_history WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: Int): QazaHistoryEntity?

    @Query("SELECT * FROM qaza_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<QazaSettingsEntity?>

    @Query("SELECT * FROM qaza_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): QazaSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: QazaSettingsEntity)

    @Query("DELETE FROM qaza_history")
    suspend fun clearHistory()

    @Query("DELETE FROM qaza_prayers")
    suspend fun clearPrayers()

    @Query("SELECT * FROM tasbih ORDER BY lastUpdated DESC")
    fun getTasbihListFlow(): Flow<List<TasbihEntity>>

    @Query("SELECT * FROM tasbih WHERE id = :id LIMIT 1")
    suspend fun getTasbihById(id: Int): TasbihEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbih(tasbih: TasbihEntity)

    @Delete
    suspend fun deleteTasbih(tasbih: TasbihEntity)

    @Query("DELETE FROM tasbih")
    suspend fun clearTasbihs()
}
