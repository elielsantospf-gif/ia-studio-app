package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_entries")
data class FinancialEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val type: String, // "RECEITA" ou "DESPESA"
    val category: String, // "Venda de Pedido", "Compra de Insumos", "Manutenção de Impressora", "Energia/Aluguel", "Outros"
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val orderId: Long? = null
) {
    val isRevenue: Boolean
        get() = type.equals("RECEITA", ignoreCase = true)
}
