package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.ProductWithMaterials
import com.example.ui.components.OrderCostDetailDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDateShort
import com.example.ui.components.formatMinutes
import com.example.ui.viewmodel.GraficaViewModel

@Composable
fun OrdersScreen(
    viewModel: GraficaViewModel,
    orders: List<Order>,
    productsWithMaterials: List<ProductWithMaterials>,
    isNewOrderDialogOpen: Boolean,
    onCloseNewOrderDialog: () -> Unit,
    onOpenNewOrderDialog: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

    val filteredOrders = orders.filter { order ->
        val matchesFilter = selectedStatusFilter == null || order.currentStatus == selectedStatusFilter
        val matchesSearch = searchQuery.isBlank() ||
                order.clientName.contains(searchQuery, ignoreCase = true) ||
                order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                order.productName.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar pedidos por cliente ou número...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("orders_search_input")
            )

            // Status Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Todos (${orders.size})") }
                    )
                }
                items(OrderStatus.values()) { status ->
                    val count = orders.count { it.currentStatus == status }
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text("${status.title} ($count)") }
                    )
                }
            }

            // Orders List
            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Nenhum pedido encontrado",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Toque no botão + para lançar um novo pedido.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        OrderListItem(
                            order = order,
                            onOpenDetail = { selectedOrderForDetail = order },
                            onTogglePaid = { viewModel.toggleOrderPaid(order) },
                            onDelete = { viewModel.deleteOrder(order) },
                            onStatusChange = { newStatus -> viewModel.moveOrderStatus(order, newStatus) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to launch order
        FloatingActionButton(
            onClick = onOpenNewOrderDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_launch_new_order"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Lançar Pedido")
        }
    }

    // New Order Dialog / Sheet
    if (isNewOrderDialogOpen) {
        NewOrderDialog(
            productsWithMaterials = productsWithMaterials,
            onDismiss = onCloseNewOrderDialog,
            onCreateOrder = { clientName, clientPhone, pwm, qty, unitPrice, notes, deductStock ->
                viewModel.createOrder(
                    clientName = clientName,
                    clientPhone = clientPhone,
                    productWithMaterials = pwm,
                    quantity = qty,
                    customUnitSalePrice = unitPrice,
                    notes = notes,
                    deductStock = deductStock
                )
                onCloseNewOrderDialog()
            }
        )
    }

    // Detail modal
    selectedOrderForDetail?.let { order ->
        val associatedPwm = productsWithMaterials.firstOrNull { it.product.id == order.productId }
        OrderCostDetailDialog(
            order = order,
            productWithMaterials = associatedPwm,
            quantity = order.quantity,
            onDismiss = { selectedOrderForDetail = null }
        )
    }
}

@Composable
fun OrderListItem(
    order: Order,
    onOpenDetail: () -> Unit,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit
) {
    var isStatusMenuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        order.orderNumber,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        formatDateShort(order.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status chip dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (order.currentStatus) {
                            OrderStatus.NEGOCIACAO -> Color(0xFFFEF3C7)
                            OrderStatus.EM_FILA -> Color(0xFFE0E7FF)
                            OrderStatus.PRODUZINDO -> Color(0xFFCFFAFE)
                            OrderStatus.CONCLUIDO -> Color(0xFFD1FAE5)
                            OrderStatus.ENTREGUE -> Color(0xFFE2E8F0)
                        },
                        modifier = Modifier
                            .clickable { isStatusMenuOpen = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = order.currentStatus.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (order.currentStatus) {
                                OrderStatus.NEGOCIACAO -> Color(0xFF92400E)
                                OrderStatus.EM_FILA -> Color(0xFF3730A3)
                                OrderStatus.PRODUZINDO -> Color(0xFF155E75)
                                OrderStatus.CONCLUIDO -> Color(0xFF065F46)
                                OrderStatus.ENTREGUE -> Color(0xFF334155)
                            }
                        )
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = isStatusMenuOpen,
                        onDismissRequest = { isStatusMenuOpen = false }
                    ) {
                        OrderStatus.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.title) },
                                onClick = {
                                    isStatusMenuOpen = false
                                    onStatusChange(st)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                order.clientName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "${order.quantity}x ${order.productName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cost summary bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Venda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatCurrency(order.totalSalePrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Custo Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatCurrency(order.totalCostTotal), style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Lucro Líquido", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${formatCurrency(order.grossProfit)} (${String.format("%.0f", order.marginPercent)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (order.grossProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpenDetail) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Insumos e Custos", style = MaterialTheme.typography.labelSmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onTogglePaid) {
                        Text(
                            if (order.isPaid) "Pago ✓" else "Marcar Pago",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (order.isPaid) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderDialog(
    productsWithMaterials: List<ProductWithMaterials>,
    onDismiss: () -> Unit,
    onCreateOrder: (
        clientName: String,
        clientPhone: String,
        productWithMaterials: ProductWithMaterials,
        quantity: Int,
        unitPrice: Double?,
        notes: String,
        deductStock: Boolean
    ) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var customPriceText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var deductStock by remember { mutableStateOf(true) }

    var selectedProductIndex by remember { mutableIntStateOf(0) }
    var isProductDropdownExpanded by remember { mutableStateOf(false) }

    val selectedPwm = productsWithMaterials.getOrNull(selectedProductIndex)
    val quantity = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1

    // Dynamic cost simulation
    val simulation = selectedPwm?.calculateForQuantity(quantity)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Lançamento de Pedido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Client Info
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nome do Cliente *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_client_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Telefone / WhatsApp") },
                    placeholder = { Text("(11) 99999-9999") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Product Selection
                Text("Produto da Gráfica *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                if (productsWithMaterials.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = isProductDropdownExpanded,
                        onExpandedChange = { isProductDropdownExpanded = !isProductDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedPwm?.product?.name ?: "Selecione um produto",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProductDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isProductDropdownExpanded,
                            onDismissRequest = { isProductDropdownExpanded = false }
                        ) {
                            productsWithMaterials.forEachIndexed { index, pwm ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(pwm.product.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "Preço: ${formatCurrency(pwm.product.salePrice)} • Custo: ${formatCurrency(pwm.totalCostPerUnit)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProductIndex = index
                                        isProductDropdownExpanded = false
                                        customPriceText = ""
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text("Nenhum produto cadastrado na gráfica.", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity and Unit Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Qtd *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_quantity_input")
                    )

                    OutlinedTextField(
                        value = customPriceText,
                        onValueChange = { customPriceText = it },
                        label = { Text("Preço Un. (R$)") },
                        placeholder = { Text(selectedPwm?.let { formatCurrency(it.product.salePrice) } ?: "") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ASSOCIATED COSTS DISPLAY
                if (simulation != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Custos Associados à Produção",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF10B981), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "Margem ${String.format("%.0f", simulation.marginPercent)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Custo Insumos", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        formatCurrency(simulation.materialCostTotal),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text("Mão de Obra / Máquina", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        formatCurrency(simulation.productionCostTotal),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Custo Total", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        formatCurrency(simulation.totalCostTotal),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Tempo de Produção: ${formatMinutes(simulation.totalProductionMinutes)}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                Text(
                                    "Lucro: ${formatCurrency(simulation.grossProfitTotal)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                            }

                            if (simulation.requirementsDetailed.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Insumos Necessários:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

                                simulation.requirementsDetailed.forEach { req ->
                                    val isStockShort = req.inStock && req.availableStock < req.totalQuantity
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "• ${req.name}: ${String.format("%.2f", req.totalQuantity)} ${req.unit}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (isStockShort) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    "Estoque: ${req.availableStock}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFB45309)
                                                )
                                            }
                                        } else {
                                            Text(
                                                formatCurrency(req.totalCost),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stock deduction checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { deductStock = !deductStock }
                ) {
                    Checkbox(
                        checked = deductStock,
                        onCheckedChange = { deductStock = it }
                    )
                    Text(
                        "Baixar matérias-primas do estoque automaticamente",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações de Acabamento / Arquivo") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && selectedPwm != null) {
                                val unitPrice = customPriceText.toDoubleOrNull()
                                onCreateOrder(
                                    clientName,
                                    clientPhone,
                                    selectedPwm,
                                    quantity,
                                    unitPrice,
                                    notes,
                                    deductStock
                                )
                            }
                        },
                        enabled = clientName.isNotBlank() && selectedPwm != null,
                        modifier = Modifier.testTag("submit_create_order_button")
                    ) {
                        Text("Lançar Pedido")
                    }
                }
            }
        }
    }
}
