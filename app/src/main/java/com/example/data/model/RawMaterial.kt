package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_materials")
data class RawMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "Papéis",
    val unit: String = "Folha", // Folha, m², Metro, Litro, Kg, Unidade
    val unitCost: Double,       // Custo por unidade em R$
    val inStock: Boolean = true, // Se é mantido em estoque ou comprado sob demanda
    val stockQuantity: Double = 0.0,
    val minStockAlert: Double = 10.0,
    val notes: String = ""
) {
    val isLowStock: Boolean
        get() = inStock && stockQuantity <= minStockAlert
}
