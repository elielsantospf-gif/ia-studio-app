package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_materials",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RawMaterial::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("productId"),
        Index("materialId")
    ]
)
data class ProductMaterialCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val materialId: Long,
    val quantityUsed: Double // Quantidade gasta deste insumo para 1 unidade do produto
)
