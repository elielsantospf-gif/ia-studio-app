package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.FinancialEntry
import com.example.data.model.MaterialRequirement
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductMaterialCrossRef
import com.example.data.model.ProductWithMaterials
import com.example.data.model.RawMaterial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GraficaRepository(private val database: AppDatabase) {
    private val materialDao = database.materialDao()
    private val productDao = database.productDao()
    private val orderDao = database.orderDao()
    private val financialDao = database.financialDao()

    val allMaterials: Flow<List<RawMaterial>> = materialDao.getAllMaterials()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val catalogProducts: Flow<List<Product>> = productDao.getCatalogProducts()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allFinancialEntries: Flow<List<FinancialEntry>> = financialDao.getAllEntries()

    /**
     * Combines products, materials, and cross-references into complete BOM ProductWithMaterials
     */
    val productsWithMaterials: Flow<List<ProductWithMaterials>> = combine(
        productDao.getAllProducts(),
        materialDao.getAllMaterials(),
        productDao.getAllProductMaterials()
    ) { products, materials, crossRefs ->
        val materialMap = materials.associateBy { it.id }
        products.map { product ->
            val productRefs = crossRefs.filter { it.productId == product.id }
            val requirements = productRefs.mapNotNull { ref ->
                materialMap[ref.materialId]?.let { mat ->
                    MaterialRequirement(material = mat, quantityUsed = ref.quantityUsed)
                }
            }
            ProductWithMaterials(product = product, requirements = requirements)
        }
    }

    // Material operations
    suspend fun insertMaterial(material: RawMaterial): Long = materialDao.insertMaterial(material)
    suspend fun updateMaterial(material: RawMaterial) = materialDao.updateMaterial(material)
    suspend fun deleteMaterial(material: RawMaterial) = materialDao.deleteMaterial(material)
    suspend fun updateMaterialStock(materialId: Long, newStock: Double) = materialDao.updateStock(materialId, newStock)

    // Product & BOM operations
    suspend fun saveProductWithMaterials(
        product: Product,
        materialsWithQuantities: List<Pair<Long, Double>>
    ): Long {
        val productId = if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
            product.id
        }

        // Replace BOM cross refs
        productDao.deleteProductMaterials(productId)
        val refs = materialsWithQuantities.map { (matId, qty) ->
            ProductMaterialCrossRef(productId = productId, materialId = matId, quantityUsed = qty)
        }
        productDao.insertProductMaterials(refs)
        return productId
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProductMaterials(product.id)
        productDao.deleteProduct(product)
    }

    // Order operations
    suspend fun createOrder(order: Order, deductStock: Boolean = true): Long {
        val orderId = orderDao.insertOrder(order)
        if (deductStock) {
            // Deduct raw materials from stock if tracked
            val productRefs = productDao.getMaterialsForProductSync(order.productId)
            for (ref in productRefs) {
                val material = materialDao.getMaterialByIdSync(ref.materialId)
                if (material != null && material.inStock) {
                    val needed = ref.quantityUsed * order.quantity
                    val updatedStock = (material.stockQuantity - needed).coerceAtLeast(0.0)
                    materialDao.updateStock(material.id, updatedStock)
                }
            }
        }
        return orderId
    }

    suspend fun updateOrder(order: Order) = orderDao.updateOrder(order)
    suspend fun deleteOrder(order: Order) = orderDao.deleteOrder(order)

    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        val completedAt = if (newStatus == OrderStatus.CONCLUIDO || newStatus == OrderStatus.ENTREGUE) {
            System.currentTimeMillis()
        } else null
        orderDao.updateOrderStatus(orderId, newStatus.name, completedAt)
    }

    suspend fun markOrderPaid(order: Order, isPaid: Boolean) {
        orderDao.updateOrderPayment(order.id, isPaid)
        if (isPaid) {
            financialDao.insertEntry(
                FinancialEntry(
                    description = "Recebimento Pedido ${order.orderNumber} - ${order.clientName}",
                    type = "RECEITA",
                    category = "Venda de Pedido",
                    amount = order.totalSalePrice,
                    date = System.currentTimeMillis(),
                    orderId = order.id
                )
            )
        } else {
            financialDao.deleteEntryByOrderId(order.id)
        }
    }

    // Financial operations
    suspend fun addFinancialEntry(entry: FinancialEntry): Long = financialDao.insertEntry(entry)
    suspend fun deleteFinancialEntry(entry: FinancialEntry) = financialDao.deleteEntry(entry)
}
