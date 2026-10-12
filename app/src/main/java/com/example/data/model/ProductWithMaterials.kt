package com.example.data.model

data class MaterialRequirement(
    val material: RawMaterial,
    val quantityUsed: Double
) {
    val totalCost: Double
        get() = material.unitCost * quantityUsed
}

data class ProductWithMaterials(
    val product: Product,
    val requirements: List<MaterialRequirement> = emptyList()
) {
    /**
     * Custo total de insumos/matéria-prima para 1 unidade deste produto
     */
    val rawMaterialCostPerUnit: Double
        get() = requirements.sumOf { it.totalCost }

    /**
     * Custo de mão de obra e máquina para 1 unidade: (tempo em minutos / 60) * taxa horária
     */
    val productionLaborCostPerUnit: Double
        get() = (product.productionTimeMinutes / 60.0) * product.machineHourlyRate

    /**
     * Custo total de produção unitário (Insumos + Mão de Obra)
     */
    val totalCostPerUnit: Double
        get() = rawMaterialCostPerUnit + productionLaborCostPerUnit

    /**
     * Lucro bruto unitário: Preço de venda - Custo Total
     */
    val grossProfitPerUnit: Double
        get() = product.salePrice - totalCostPerUnit

    /**
     * Margem de lucro em porcentagem: (Lucro / Preço de venda) * 100
     */
    val profitMarginPercent: Double
        get() = if (product.salePrice > 0) (grossProfitPerUnit / product.salePrice) * 100.0 else 0.0

    /**
     * Multiplica custos para uma determinada quantidade de pedido
     */
    fun calculateForQuantity(quantity: Int): OrderCostSimulation {
        val matCost = rawMaterialCostPerUnit * quantity
        val prodCost = productionLaborCostPerUnit * quantity
        val totalCost = matCost + prodCost
        val saleTotal = product.salePrice * quantity
        val profit = saleTotal - totalCost
        val margin = if (saleTotal > 0) (profit / saleTotal) * 100.0 else 0.0
        val totalMinutes = product.productionTimeMinutes * quantity

        return OrderCostSimulation(
            quantity = quantity,
            materialCostTotal = matCost,
            productionCostTotal = prodCost,
            totalCostTotal = totalCost,
            totalSalePrice = saleTotal,
            grossProfitTotal = profit,
            marginPercent = margin,
            totalProductionMinutes = totalMinutes,
            requirementsDetailed = requirements.map {
                MaterialCostItem(
                    name = it.material.name,
                    unit = it.material.unit,
                    unitCost = it.material.unitCost,
                    totalQuantity = it.quantityUsed * quantity,
                    totalCost = it.material.unitCost * it.quantityUsed * quantity,
                    inStock = it.material.inStock,
                    availableStock = it.material.stockQuantity
                )
            }
        )
    }
}

data class OrderCostSimulation(
    val quantity: Int,
    val materialCostTotal: Double,
    val productionCostTotal: Double,
    val totalCostTotal: Double,
    val totalSalePrice: Double,
    val grossProfitTotal: Double,
    val marginPercent: Double,
    val totalProductionMinutes: Int,
    val requirementsDetailed: List<MaterialCostItem>
)

data class MaterialCostItem(
    val name: String,
    val unit: String,
    val unitCost: Double,
    val totalQuantity: Double,
    val totalCost: Double,
    val inStock: Boolean,
    val availableStock: Double
)
