package com.designer.alarmclock.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldClockDao {
    @Query("SELECT * FROM world_clock_cities ORDER BY cityName ASC")
    fun getAllCities(): Flow<List<WorldClockCity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCity(city: WorldClockCity): Long

    @Delete
    suspend fun deleteCity(city: WorldClockCity)
}
