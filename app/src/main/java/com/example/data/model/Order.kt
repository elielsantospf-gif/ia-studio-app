package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val clientName: String,
    val clientPhone: String = "",
    val productId: Long,
    val productName: String,
    val quantity: Int = 1,
    val unitSalePrice: Double,
    val totalSalePrice: Double,
    val materialCostTotal: Double,
    val productionCostTotal: Double,
    val totalCostTotal: Double,
    val productionTimeMinutesTotal: Int,
    val status: String = OrderStatus.NEGOCIACAO.name,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isPaid: Boolean = false
) {
    val currentStatus: OrderStatus
        get() = OrderStatus.fromString(status)

    val grossProfit: Double
        get() = totalSalePrice - totalCostTotal

    val marginPercent: Double
        get() = if (totalSalePrice > 0) (grossProfit / totalSalePrice) * 100.0 else 0.0
}
