package com.geoalarm.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoAlarmDao {
    @Query("SELECT * FROM geo_alarms ORDER BY id DESC")
    fun observeAll(): Flow<List<GeoAlarmEntity>>

    @Query("SELECT * FROM geo_alarms WHERE isEnabled = 1")
    fun observeEnabled(): Flow<List<GeoAlarmEntity>>

    @Query("SELECT * FROM geo_alarms WHERE isEnabled = 1")
    suspend fun getAllEnabledOnce(): List<GeoAlarmEntity>

    @Query("SELECT * FROM geo_alarms WHERE id = :id")
    suspend fun getById(id: Long): GeoAlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GeoAlarmEntity): Long

    @Update
    suspend fun update(entity: GeoAlarmEntity)

    @Query("DELETE FROM geo_alarms WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE geo_alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
