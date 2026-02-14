package com.example.geoeverytp.data.repository

import android.content.Context
import com.example.geoeverytp.data.DatabaseProvider
import com.example.geoeverytp.data.GeoLogEntity
import kotlinx.coroutines.flow.Flow

/** Repository for geo log CRUD; delegates to [com.example.geoeverytp.data.GeoLogDao]. */
class GeoLogRepository(context: Context) {
    private val dao = DatabaseProvider.get(context).geoLogDao()

    fun getAll(): Flow<List<GeoLogEntity>> = dao.getAll()

    suspend fun getById(id: Long): GeoLogEntity? = dao.getById(id)

    suspend fun insert(entity: GeoLogEntity): Long = dao.insert(entity)

    suspend fun delete(entity: GeoLogEntity) = dao.delete(entity)
}
