package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.RawMaterial
import com.example.ui.components.formatCurrency
import com.example.ui.viewmodel.GraficaViewModel

enum class MaterialFilter {
    ALL,
    IN_STOCK,
    NON_STOCK,
    LOW_STOCK
}

@Composable
fun MaterialsScreen(
    viewModel: GraficaViewModel,
    rawMaterials: List<RawMaterial>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(MaterialFilter.ALL) }
    var isNewMaterialDialogOpen by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<RawMaterial?>(null) }
    var stockAdjustmentMaterial by remember { mutableStateOf<RawMaterial?>(null) }

    val filteredMaterials = rawMaterials.filter { mat ->
        val matchesFilter = when (selectedFilter) {
            MaterialFilter.ALL -> true
            MaterialFilter.IN_STOCK -> mat.inStock
            MaterialFilter.NON_STOCK -> !mat.inStock
            MaterialFilter.LOW_STOCK -> mat.isLowStock
        }
        val matchesSearch = searchQuery.isBlank() ||
                mat.name.contains(searchQuery, ignoreCase = true) ||
                mat.category.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar matéria-prima ou categoria...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("materials_search_input")
            )

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == MaterialFilter.ALL,
                        onClick = { selectedFilter = MaterialFilter.ALL },
                        label = { Text("Todos (${rawMaterials.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == MaterialFilter.IN_STOCK,
                        onClick = { selectedFilter = MaterialFilter.IN_STOCK },
                        label = { Text("Em Estoque (${rawMaterials.count { it.inStock }})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == MaterialFilter.NON_STOCK,
                        onClick = { selectedFilter = MaterialFilter.NON_STOCK },
                        label = { Text("Sob Demanda / Fora Estoque (${rawMaterials.count { !it.inStock }})") }
                    )
                }
                item {
                    val lowCount = rawMaterials.count { it.isLowStock }
                    FilterChip(
                        selected = selectedFilter == MaterialFilter.LOW_STOCK,
                        onClick = { selectedFilter = MaterialFilter.LOW_STOCK },
                        label = { Text("Estoque Baixo ($lowCount)") }
                    )
                }
            }

            // Material List
            if (filteredMaterials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Nenhum insumo encontrado",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Toque no botão + para cadastrar novos custos de matéria-prima.",
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
                    items(filteredMaterials, key = { it.id }) { material ->
                        MaterialItemCard(
                            material = material,
                            onEdit = { editingMaterial = material },
                            onDelete = { viewModel.deleteMaterial(material) },
                            onAdjustStock = { stockAdjustmentMaterial = material },
                            onQuickAddStock = { viewModel.updateStock(material.id, material.stockQuantity + 10.0) },
                            onQuickSubStock = { viewModel.updateStock(material.id, (material.stockQuantity - 10.0).coerceAtLeast(0.0)) }
                        )
                    }
                }
            }
        }

        // FAB to add material
        FloatingActionButton(
            onClick = { isNewMaterialDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_material"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo Insumo")
        }
    }

    // New/Edit Dialog
    if (isNewMaterialDialogOpen || editingMaterial != null) {
        MaterialEditDialog(
            existingMaterial = editingMaterial,
            onDismiss = {
                isNewMaterialDialogOpen = false
                editingMaterial = null
            },
            onSave = { id, name, category, unit, unitCost, inStock, stockQty, minAlert, notes ->
                viewModel.saveMaterial(
                    id = id,
                    name = name,
                    category = category,
                    unit = unit,
                    unitCost = unitCost,
                    inStock = inStock,
                    stockQuantity = stockQty,
                    minStockAlert = minAlert,
                    notes = notes
                )
                isNewMaterialDialogOpen = false
                editingMaterial = null
            }
        )
    }

    // Direct Stock Adjustment Dialog
    stockAdjustmentMaterial?.let { material ->
        StockAdjustDialog(
            material = material,
            onDismiss = { stockAdjustmentMaterial = null },
            onConfirm = { newQuantity ->
                viewModel.updateStock(material.id, newQuantity)
                stockAdjustmentMaterial = null
            }
        )
    }
}

@Composable
fun MaterialItemCard(
    material: RawMaterial,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: () -> Unit,
    onQuickAddStock: () -> Unit,
    onQuickSubStock: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category & InStock / NonStock Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            material.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                if (material.inStock) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (material.inStock) "EM ESTOQUE" else "SOB DEMANDA (FORA DE ESTOQUE)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (material.inStock) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                material.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (material.notes.isNotBlank()) {
                Text(
                    material.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Custo Unitário", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${formatCurrency(material.unitCost)} / ${material.unit}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (material.inStock) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Saldo em Estoque", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${String.format("%.1f", material.stockQuantity)} ${material.unit}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (material.isLowStock) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Abastecimento", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "Comprado por Pedido",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            if (material.isLowStock) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEE2E2), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Estoque baixo! Mínimo configurado: ${material.minStockAlert} ${material.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB91C1C),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (material.inStock) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onAdjustStock) {
                        Text("Ajustar Quantidade", style = MaterialTheme.typography.labelSmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onQuickSubStock, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "-10", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            "±10",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = onQuickAddStock, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "+10", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialEditDialog(
    existingMaterial: RawMaterial?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        category: String,
        unit: String,
        unitCost: Double,
        inStock: Boolean,
        stockQty: Double,
        minAlert: Double,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(existingMaterial?.name ?: "") }
    var category by remember { mutableStateOf(existingMaterial?.category ?: "Papéis") }
    var unit by remember { mutableStateOf(existingMaterial?.unit ?: "Folha") }
    var unitCostText by remember { mutableStateOf(existingMaterial?.unitCost?.toString() ?: "0.50") }
    var inStock by remember { mutableStateOf(existingMaterial?.inStock ?: true) }
    var stockQtyText by remember { mutableStateOf(existingMaterial?.stockQuantity?.toString() ?: "100.0") }
    var minAlertText by remember { mutableStateOf(existingMaterial?.minStockAlert?.toString() ?: "20.0") }
    var notes by remember { mutableStateOf(existingMaterial?.notes ?: "") }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (existingMaterial == null) "Novo Insumo / Custo" else "Editar Insumo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Matéria-Prima *") },
                    placeholder = { Text("Ex: Papel Couchê 150g A3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria") },
                    placeholder = { Text("Ex: Papéis, Tintas, Acabamento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidade (ex: Folha, m²)") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = unitCostText,
                        onValueChange = { unitCostText = it },
                        label = { Text("Custo Un. (R$) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Controlar em Estoque",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (inStock) "Item físico armazenado na gráfica" else "Sob demanda / Fora do estoque (sem contagem)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = inStock,
                        onCheckedChange = { inStock = it }
                    )
                }

                if (inStock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = stockQtyText,
                            onValueChange = { stockQtyText = it },
                            label = { Text("Saldo Atual") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = minAlertText,
                            onValueChange = { minAlertText = it },
                            label = { Text("Alerta Mínimo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Fornecedor") },
                    placeholder = { Text("Prazo de entrega do fornecedor, especificações") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            val cost = unitCostText.toDoubleOrNull() ?: 0.0
                            val qty = stockQtyText.toDoubleOrNull() ?: 0.0
                            val min = minAlertText.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) {
                                onSave(
                                    existingMaterial?.id ?: 0L,
                                    name,
                                    category,
                                    unit,
                                    cost,
                                    inStock,
                                    qty,
                                    min,
                                    notes
                                )
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Salvar Insumo")
                    }
                }
            }
        }
    }
}

@Composable
fun StockAdjustDialog(
    material: RawMaterial,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantityText by remember { mutableStateOf(material.stockQuantity.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Ajustar Estoque: ${material.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Digite a quantidade atual aferida fisicamente no almoxarifado:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Nova Quantidade (${material.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            val newQty = quantityText.toDoubleOrNull() ?: material.stockQuantity
                            onConfirm(newQty)
                        }
                    ) {
                        Text("Atualizar")
                    }
                }
            }
        }
    }
}
