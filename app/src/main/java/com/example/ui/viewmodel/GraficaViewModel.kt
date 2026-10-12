package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.FinancialEntry
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductWithMaterials
import com.example.data.model.RawMaterial
import com.example.data.repository.GraficaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FinancialSummary(
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val pendingRevenue: Double = 0.0,
    val totalOrderProductionCost: Double = 0.0,
    val profitMarginPercent: Double = 0.0
)

class GraficaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GraficaRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = GraficaRepository(database)
    }

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productsWithMaterials: StateFlow<List<ProductWithMaterials>> = repository.productsWithMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawMaterials: StateFlow<List<RawMaterial>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialEntries: StateFlow<List<FinancialEntry>> = repository.allFinancialEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial KPI aggregation
    val financialSummary: StateFlow<FinancialSummary> = combine(
        orders,
        financialEntries
    ) { orderList, entries ->
        val totalRevenueFromEntries = entries.filter { it.isRevenue }.sumOf { it.amount }
        val totalExpenses = entries.filter { !it.isRevenue }.sumOf { it.amount }
        val netBalance = totalRevenueFromEntries - totalExpenses

        val deliveredOrders = orderList.filter { it.currentStatus == OrderStatus.ENTREGUE || it.currentStatus == OrderStatus.CONCLUIDO }
        val pendingOrders = orderList.filter { it.currentStatus != OrderStatus.ENTREGUE && it.currentStatus != OrderStatus.CONCLUIDO }

        val pendingRevenue = pendingOrders.sumOf { it.totalSalePrice }
        val deliveredCost = deliveredOrders.sumOf { it.totalCostTotal }
        val deliveredRevenue = deliveredOrders.sumOf { it.totalSalePrice }
        val margin = if (deliveredRevenue > 0) ((deliveredRevenue - deliveredCost) / deliveredRevenue) * 100.0 else 0.0

        FinancialSummary(
            totalRevenue = totalRevenueFromEntries,
            totalExpenses = totalExpenses,
            netBalance = netBalance,
            pendingRevenue = pendingRevenue,
            totalOrderProductionCost = deliveredCost,
            profitMarginPercent = margin
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Message feedback
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // --- Order Operations ---
    fun createOrder(
        clientName: String,
        clientPhone: String,
        productWithMaterials: ProductWithMaterials,
        quantity: Int,
        customUnitSalePrice: Double? = null,
        notes: String = "",
        deductStock: Boolean = true
    ) {
        viewModelScope.launch {
            val unitPrice = customUnitSalePrice ?: productWithMaterials.product.salePrice
            val simulation = productWithMaterials.calculateForQuantity(quantity)
            val orderNum = "PED-${Random.nextInt(100, 999)}"

            val order = Order(
                orderNumber = orderNum,
                clientName = clientName.trim(),
                clientPhone = clientPhone.trim(),
                productId = productWithMaterials.product.id,
                productName = productWithMaterials.product.name,
                quantity = quantity,
                unitSalePrice = unitPrice,
                totalSalePrice = unitPrice * quantity,
                materialCostTotal = simulation.materialCostTotal,
                productionCostTotal = simulation.productionCostTotal,
                totalCostTotal = simulation.totalCostTotal,
                productionTimeMinutesTotal = simulation.totalProductionMinutes,
                status = OrderStatus.NEGOCIACAO.name,
                notes = notes.trim(),
                createdAt = System.currentTimeMillis()
            )

            repository.createOrder(order, deductStock)
            _userMessage.value = "Pedido $orderNum criado com sucesso!"
        }
    }

    fun moveOrderStatus(order: Order, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(order.id, newStatus)
            _userMessage.value = "Pedido ${order.orderNumber} movido para ${newStatus.title}"
        }
    }

    fun advanceOrderStatus(order: Order) {
        val nextStatus = when (order.currentStatus) {
            OrderStatus.NEGOCIACAO -> OrderStatus.EM_FILA
            OrderStatus.EM_FILA -> OrderStatus.PRODUZINDO
            OrderStatus.PRODUZINDO -> OrderStatus.CONCLUIDO
            OrderStatus.CONCLUIDO -> OrderStatus.ENTREGUE
            OrderStatus.ENTREGUE -> OrderStatus.ENTREGUE
        }
        if (nextStatus != order.currentStatus) {
            moveOrderStatus(order, nextStatus)
        }
    }

    fun previousOrderStatus(order: Order) {
        val prevStatus = when (order.currentStatus) {
            OrderStatus.NEGOCIACAO -> OrderStatus.NEGOCIACAO
            OrderStatus.EM_FILA -> OrderStatus.NEGOCIACAO
            OrderStatus.PRODUZINDO -> OrderStatus.EM_FILA
            OrderStatus.CONCLUIDO -> OrderStatus.PRODUZINDO
            OrderStatus.ENTREGUE -> OrderStatus.CONCLUIDO
        }
        if (prevStatus != order.currentStatus) {
            moveOrderStatus(order, prevStatus)
        }
    }

    fun toggleOrderPaid(order: Order) {
        viewModelScope.launch {
            val newPaidState = !order.isPaid
            repository.markOrderPaid(order, newPaidState)
            _userMessage.value = if (newPaidState) "Pagamento recebido e lançado no financeiro!" else "Pagamento desmarcado"
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            _userMessage.value = "Pedido ${order.orderNumber} excluído"
        }
    }

    // --- Raw Material Operations ---
    fun saveMaterial(
        id: Long = 0,
        name: String,
        category: String,
        unit: String,
        unitCost: Double,
        inStock: Boolean,
        stockQuantity: Double,
        minStockAlert: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val material = RawMaterial(
                id = id,
                name = name.trim(),
                category = category.trim(),
                unit = unit.trim(),
                unitCost = unitCost,
                inStock = inStock,
                stockQuantity = if (inStock) stockQuantity else 0.0,
                minStockAlert = minStockAlert,
                notes = notes.trim()
            )
            if (id == 0L) {
                repository.insertMaterial(material)
                _userMessage.value = "Insumo cadastrado com sucesso!"
            } else {
                repository.updateMaterial(material)
                _userMessage.value = "Insumo atualizado com sucesso!"
            }
        }
    }

    fun updateStock(materialId: Long, newStock: Double) {
        viewModelScope.launch {
            repository.updateMaterialStock(materialId, newStock.coerceAtLeast(0.0))
            _userMessage.value = "Estoque atualizado!"
        }
    }

    fun deleteMaterial(material: RawMaterial) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
            _userMessage.value = "Insumo excluído"
        }
    }

    // --- Product & BOM Operations ---
    fun saveProduct(
        id: Long = 0,
        name: String,
        description: String,
        category: String,
        salePrice: Double,
        productionTimeMinutes: Int,
        machineHourlyRate: Double,
        isAvailableInCatalog: Boolean,
        inStockFinished: Boolean,
        finishedStockQty: Int,
        deliveryDaysEstimate: Int,
        materialsWithQuantities: List<Pair<Long, Double>>
    ) {
        viewModelScope.launch {
            val product = Product(
                id = id,
                name = name.trim(),
                description = description.trim(),
                category = category.trim(),
                salePrice = salePrice,
                productionTimeMinutes = productionTimeMinutes,
                machineHourlyRate = machineHourlyRate,
                isAvailableInCatalog = isAvailableInCatalog,
                inStockFinished = inStockFinished,
                finishedStockQty = if (inStockFinished) finishedStockQty else 0,
                deliveryDaysEstimate = deliveryDaysEstimate
            )
            repository.saveProductWithMaterials(product, materialsWithQuantities)
            _userMessage.value = if (id == 0L) "Produto cadastrado com Ficha Técnica!" else "Produto e ficha técnica atualizados!"
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _userMessage.value = "Produto excluído"
        }
    }

    // --- Financial Operations ---
    fun addFinancialEntry(
        description: String,
        type: String,
        category: String,
        amount: Double
    ) {
        viewModelScope.launch {
            val entry = FinancialEntry(
                description = description.trim(),
                type = type,
                category = category.trim(),
                amount = amount,
                date = System.currentTimeMillis()
            )
            repository.addFinancialEntry(entry)
            _userMessage.value = "Lançamento financeiro registrado!"
        }
    }

    fun deleteFinancialEntry(entry: FinancialEntry) {
        viewModelScope.launch {
            repository.deleteFinancialEntry(entry)
            _userMessage.value = "Lançamento excluído"
        }
    }
}
