package com.example

import com.example.data.model.MaterialRequirement
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductWithMaterials
import com.example.data.model.RawMaterial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testProductionCostAndProfitCalculations() {
        val matPaper = RawMaterial(
            id = 1,
            name = "Papel Triplex",
            unitCost = 1.25,
            inStock = true,
            stockQuantity = 100.0
        )
        val matInk = RawMaterial(
            id = 2,
            name = "Toner Laser",
            unitCost = 100.0,
            inStock = true,
            stockQuantity = 2.0
        )

        val product = Product(
            id = 10,
            name = "Cartões de Visita 1000un",
            salePrice = 80.0,
            productionTimeMinutes = 30, // 0.5 hours
            machineHourlyRate = 30.0   // 0.5 * 30.0 = 15.0 labor cost
        )

        val requirements = listOf(
            MaterialRequirement(material = matPaper, quantityUsed = 10.0), // 12.50
            MaterialRequirement(material = matInk, quantityUsed = 0.05)     // 5.00
        )

        val pwm = ProductWithMaterials(product = product, requirements = requirements)

        // Raw materials: 12.50 + 5.00 = 17.50
        assertEquals(17.50, pwm.rawMaterialCostPerUnit, 0.01)
        // Labor: 15.00
        assertEquals(15.00, pwm.productionLaborCostPerUnit, 0.01)
        // Total cost: 17.50 + 15.00 = 32.50
        assertEquals(32.50, pwm.totalCostPerUnit, 0.01)
        // Gross Profit: 80.0 - 32.50 = 47.50
        assertEquals(47.50, pwm.grossProfitPerUnit, 0.01)
        // Margin: (47.50 / 80.0) * 100 = 59.375%
        assertEquals(59.375, pwm.profitMarginPercent, 0.01)

        // Order of quantity 3
        val simulation = pwm.calculateForQuantity(3)
        assertEquals(3, simulation.quantity)
        assertEquals(52.50, simulation.materialCostTotal, 0.01)
        assertEquals(45.00, simulation.productionCostTotal, 0.01)
        assertEquals(97.50, simulation.totalCostTotal, 0.01)
        assertEquals(240.0, simulation.totalSalePrice, 0.01)
        assertEquals(142.50, simulation.grossProfitTotal, 0.01)
        assertEquals(90, simulation.totalProductionMinutes)
    }

    @Test
    fun testOrderStatusEnumValues() {
        assertEquals("Em Negociação", OrderStatus.NEGOCIACAO.title)
        assertEquals("Na Fila", OrderStatus.EM_FILA.title)
        assertEquals("Produzindo", OrderStatus.PRODUZINDO.title)
        assertEquals("Concluído", OrderStatus.CONCLUIDO.title)
        assertEquals("Entregue", OrderStatus.ENTREGUE.title)
    }
}
