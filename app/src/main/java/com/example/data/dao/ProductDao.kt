package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import com.example.data.model.ProductMaterialCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isAvailableInCatalog = 1 ORDER BY name ASC")
    fun getCatalogProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdSync(id: Long): Product?

    @Query("SELECT * FROM products")
    suspend fun getAllProductsSync(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>): List<Long>

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductMaterials(items: List<ProductMaterialCrossRef>)

    @Query("DELETE FROM product_materials WHERE productId = :productId")
    suspend fun deleteProductMaterials(productId: Long)

    @Query("SELECT * FROM product_materials WHERE productId = :productId")
    fun getMaterialsForProduct(productId: Long): Flow<List<ProductMaterialCrossRef>>

    @Query("SELECT * FROM product_materials WHERE productId = :productId")
    suspend fun getMaterialsForProductSync(productId: Long): List<ProductMaterialCrossRef>

    @Query("SELECT * FROM product_materials")
    fun getAllProductMaterials(): Flow<List<ProductMaterialCrossRef>>

    @Query("SELECT * FROM product_materials")
    suspend fun getAllProductMaterialsSync(): List<ProductMaterialCrossRef>
}
