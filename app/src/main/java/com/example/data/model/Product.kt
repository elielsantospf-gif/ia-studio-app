package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: String = "Impressos",
    val salePrice: Double,                 // Preço sugerido de venda (R$)
    val productionTimeMinutes: Int = 15,    // Tempo padrão de produção em minutos por unidade/lote
    val machineHourlyRate: Double = 35.0,  // Custo por hora da máquina/operador (R$/hora)
    val isAvailableInCatalog: Boolean = true, // Exibir no catálogo digital de divulgação
    val inStockFinished: Boolean = false,  // Tem estoque a pronta entrega ou sob demanda
    val finishedStockQty: Int = 0,
    val minOrderQty: Int = 1,
    val deliveryDaysEstimate: Int = 2
)
