package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RawMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM raw_materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<RawMaterial>>

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    fun getMaterialById(id: Long): Flow<RawMaterial?>

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    suspend fun getMaterialByIdSync(id: Long): RawMaterial?

    @Query("SELECT * FROM raw_materials")
    suspend fun getAllMaterialsSync(): List<RawMaterial>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: RawMaterial): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<RawMaterial>): List<Long>

    @Update
    suspend fun updateMaterial(material: RawMaterial)

    @Delete
    suspend fun deleteMaterial(material: RawMaterial)

    @Query("UPDATE raw_materials SET stockQuantity = :newQuantity WHERE id = :materialId")
    suspend fun updateStock(materialId: Long, newQuantity: Double)
}
