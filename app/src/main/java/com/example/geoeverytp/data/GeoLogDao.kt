package com.example.geoeverytp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data access for [GeoLogEntity]; list ordered by [GeoLogEntity.recordedAtMillis] descending. */
@Dao
interface GeoLogDao {

    @Query("SELECT * FROM geo_log ORDER BY recordedAtMillis DESC")
    fun getAll(): Flow<List<GeoLogEntity>>

    @Query("SELECT * FROM geo_log WHERE id = :id")
    suspend fun getById(id: Long): GeoLogEntity?

    @Insert
    suspend fun insert(entity: GeoLogEntity): Long

    @Delete
    suspend fun delete(entity: GeoLogEntity): Int
}
