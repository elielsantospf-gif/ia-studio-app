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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Product
import com.example.data.model.ProductWithMaterials
import com.example.data.model.RawMaterial
import com.example.ui.components.OrderCostDetailDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatMinutes
import com.example.ui.viewmodel.GraficaViewModel

@Composable
fun ProductsScreen(
    viewModel: GraficaViewModel,
    productsWithMaterials: List<ProductWithMaterials>,
    rawMaterials: List<RawMaterial>
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingProductWithMaterials by remember { mutableStateOf<ProductWithMaterials?>(null) }
    var isNewProductDialogOpen by remember { mutableStateOf(false) }
    var selectedForDetail by remember { mutableStateOf<ProductWithMaterials?>(null) }

    val filteredProducts = productsWithMaterials.filter { pwm ->
        searchQuery.isBlank() ||
                pwm.product.name.contains(searchQuery, ignoreCase = true) ||
                pwm.product.category.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar produtos gráficos ou categorias...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("products_search_input")
            )

            // Header summary
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Fichas Técnicas & Associação de Insumos",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${filteredProducts.size} produtos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Products List
            if (filteredProducts.isEmpty()) {
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
                            "Nenhum produto cadastrado",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Toque no botão + para cadastrar um produto e associar matérias-primas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredProducts, key = { it.product.id }) { pwm ->
                        ProductItemCard(
                            pwm = pwm,
                            onEdit = { editingProductWithMaterials = pwm },
                            onOpenDetail = { selectedForDetail = pwm },
                            onDelete = { viewModel.deleteProduct(pwm.product) }
                        )
                    }
                }
            }
        }

        // FAB to add product
        FloatingActionButton(
            onClick = { isNewProductDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_product"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo Produto")
        }
    }

    // New/Edit Product Dialog
    if (isNewProductDialogOpen || editingProductWithMaterials != null) {
        ProductEditDialog(
            existingPwm = editingProductWithMaterials,
            rawMaterials = rawMaterials,
            onDismiss = {
                isNewProductDialogOpen = false
                editingProductWithMaterials = null
            },
            onSave = { product, materialsWithQty ->
                viewModel.saveProduct(
                    id = product.id,
                    name = product.name,
                    description = product.description,
                    category = product.category,
                    salePrice = product.salePrice,
                    productionTimeMinutes = product.productionTimeMinutes,
                    machineHourlyRate = product.machineHourlyRate,
                    isAvailableInCatalog = product.isAvailableInCatalog,
                    inStockFinished = product.inStockFinished,
                    finishedStockQty = product.finishedStockQty,
                    deliveryDaysEstimate = product.deliveryDaysEstimate,
                    materialsWithQuantities = materialsWithQty
                )
                isNewProductDialogOpen = false
                editingProductWithMaterials = null
            }
        )
    }

    // Detail dialog
    selectedForDetail?.let { pwm ->
        OrderCostDetailDialog(
            productWithMaterials = pwm,
            quantity = 1,
            onDismiss = { selectedForDetail = null }
        )
    }
}

@Composable
fun ProductItemCard(
    pwm: ProductWithMaterials,
    onEdit: () -> Unit,
    onOpenDetail: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category, Badges, Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        pwm.product.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pwm.product.isAvailableInCatalog) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE0F2FE), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "No Catálogo",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
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
                pwm.product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (pwm.product.description.isNotBlank()) {
                Text(
                    pwm.product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOM Insumos Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${pwm.requirements.size} insumo(s) na Ficha Técnica (Custo: ${formatCurrency(pwm.rawMaterialCostPerUnit)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Tempo: ${formatMinutes(pwm.product.productionTimeMinutes)} • Taxa: ${formatCurrency(pwm.product.machineHourlyRate)}/h (Custo Mão de Obra: ${formatCurrency(pwm.productionLaborCostPerUnit)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cost vs Price card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Preço de Venda", style = MaterialTheme.typography.labelSmall)
                    Text(
                        formatCurrency(pwm.product.salePrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("Custo Total Unit.", style = MaterialTheme.typography.labelSmall)
                    Text(
                        formatCurrency(pwm.totalCostPerUnit),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Lucro Unitário", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${formatCurrency(pwm.grossProfitPerUnit)} (${String.format("%.0f", pwm.profitMarginPercent)}%)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pwm.grossProfitPerUnit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenDetail) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Detalhamento de Custos", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditDialog(
    existingPwm: ProductWithMaterials?,
    rawMaterials: List<RawMaterial>,
    onDismiss: () -> Unit,
    onSave: (Product, List<Pair<Long, Double>>) -> Unit
) {
    var name by remember { mutableStateOf(existingPwm?.product?.name ?: "") }
    var description by remember { mutableStateOf(existingPwm?.product?.description ?: "") }
    var category by remember { mutableStateOf(existingPwm?.product?.category ?: "Impressos Promocionais") }
    var salePriceText by remember { mutableStateOf(existingPwm?.product?.salePrice?.toString() ?: "50.00") }
    var productionTimeText by remember { mutableStateOf(existingPwm?.product?.productionTimeMinutes?.toString() ?: "20") }
    var machineHourlyRateText by remember { mutableStateOf(existingPwm?.product?.machineHourlyRate?.toString() ?: "35.00") }
    var isAvailableInCatalog by remember { mutableStateOf(existingPwm?.product?.isAvailableInCatalog ?: true) }
    var inStockFinished by remember { mutableStateOf(existingPwm?.product?.inStockFinished ?: false) }
    var finishedStockQtyText by remember { mutableStateOf(existingPwm?.product?.finishedStockQty?.toString() ?: "0") }

    val selectedRecipeItems = remember {
        mutableStateListOf<Pair<RawMaterial, Double>>().apply {
            existingPwm?.requirements?.forEach { req ->
                add(Pair(req.material, req.quantityUsed))
            }
        }
    }

    var selectedMaterialIndex by remember { mutableIntStateOf(0) }
    var isMaterialDropdownExpanded by remember { mutableStateOf(false) }
    var addMaterialQtyText by remember { mutableStateOf("1.0") }

    val salePrice = salePriceText.toDoubleOrNull() ?: 0.0
    val prodTime = productionTimeText.toIntOrNull() ?: 0
    val machineRate = machineHourlyRateText.toDoubleOrNull() ?: 0.0
    val materialCostTotal = selectedRecipeItems.sumOf { it.first.unitCost * it.second }
    val laborCost = (prodTime / 60.0) * machineRate
    val totalCost = materialCostTotal + laborCost
    val profit = salePrice - totalCost
    val margin = if (salePrice > 0) (profit / salePrice) * 100.0 else 0.0

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
                        if (existingPwm == null) "Novo Produto & Ficha Técnica" else "Editar Ficha Técnica",
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
                    label = { Text("Nome do Produto Gráfico *") },
                    placeholder = { Text("Ex: Cartão de Visita 1000un") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria") },
                    placeholder = { Text("Ex: Identidade Visual, Banners, Adesivos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Especificações") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = salePriceText,
                        onValueChange = { salePriceText = it },
                        label = { Text("Preço Venda (R$) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = productionTimeText,
                        onValueChange = { productionTimeText = it },
                        label = { Text("Tempo (minutos)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = machineHourlyRateText,
                    onValueChange = { machineHourlyRateText = it },
                    label = { Text("Custo Hora Máquina/Operador (R$/hora)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Exibir no Catálogo Digital", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Clientes poderão pedir este produto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isAvailableInCatalog,
                        onCheckedChange = { isAvailableInCatalog = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Estoque Pronto / Acabado", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Produto pronto para pronta entrega", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = inStockFinished,
                        onCheckedChange = { inStockFinished = it }
                    )
                }

                if (inStockFinished) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = finishedStockQtyText,
                        onValueChange = { finishedStockQtyText = it },
                        label = { Text("Quantidade Pronta em Estoque") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Ficha Técnica: Insumos & Matérias-Primas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Associe quais insumos e quantidades compõem a produção de 1 unidade deste produto:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (rawMaterials.isNotEmpty()) {
                    val currentPickerMat = rawMaterials.getOrNull(selectedMaterialIndex) ?: rawMaterials[0]

                    ExposedDropdownMenuBox(
                        expanded = isMaterialDropdownExpanded,
                        onExpandedChange = { isMaterialDropdownExpanded = !isMaterialDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${currentPickerMat.name} (${formatCurrency(currentPickerMat.unitCost)} / ${currentPickerMat.unit})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Escolher Matéria-Prima") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMaterialDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isMaterialDropdownExpanded,
                            onDismissRequest = { isMaterialDropdownExpanded = false }
                        ) {
                            rawMaterials.forEachIndexed { index, mat ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${mat.name} (${formatCurrency(mat.unitCost)} / ${mat.unit})")
                                    },
                                    onClick = {
                                        selectedMaterialIndex = index
                                        isMaterialDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = addMaterialQtyText,
                            onValueChange = { addMaterialQtyText = it },
                            label = { Text("Qtd (${currentPickerMat.unit})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val qty = addMaterialQtyText.toDoubleOrNull() ?: 1.0
                                val existingIndex = selectedRecipeItems.indexOfFirst { it.first.id == currentPickerMat.id }
                                if (existingIndex >= 0) {
                                    selectedRecipeItems[existingIndex] = Pair(currentPickerMat, qty)
                                } else {
                                    selectedRecipeItems.add(Pair(currentPickerMat, qty))
                                }
                                addMaterialQtyText = "1.0"
                            },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Incluir")
                        }
                    }
                } else {
                    Text(
                        "Nenhuma matéria-prima cadastrada. Cadastre na aba 'Insumos'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (selectedRecipeItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Insumos Associados:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    selectedRecipeItems.forEachIndexed { index, pair ->
                        val mat = pair.first
                        val qty = pair.second
                        val itemCost = mat.unitCost * qty

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mat.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${String.format("%.2f", qty)} ${mat.unit} x ${formatCurrency(mat.unitCost)} = ${formatCurrency(itemCost)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { selectedRecipeItems.removeAt(index) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remover",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Resumo de Custos Unitários Calculados",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1. Insumos:")
                            Text(formatCurrency(materialCostTotal), fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("2. Mão de Obra (${prodTime} min):")
                            Text(formatCurrency(laborCost), fontWeight = FontWeight.SemiBold)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Custo Total de Produção:", fontWeight = FontWeight.Bold)
                            Text(formatCurrency(totalCost), fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Preço de Venda:", fontWeight = FontWeight.Bold)
                            Text(formatCurrency(salePrice), fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lucro Unitário Estimado:", fontWeight = FontWeight.Bold)
                            Text(
                                "${formatCurrency(profit)} (${String.format("%.0f", margin)}%)",
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }
                }

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
                            if (name.isNotBlank()) {
                                val product = Product(
                                    id = existingPwm?.product?.id ?: 0L,
                                    name = name,
                                    description = description,
                                    category = category,
                                    salePrice = salePrice,
                                    productionTimeMinutes = prodTime,
                                    machineHourlyRate = machineRate,
                                    isAvailableInCatalog = isAvailableInCatalog,
                                    inStockFinished = inStockFinished,
                                    finishedStockQty = finishedStockQtyText.toIntOrNull() ?: 0
                                )
                                val refs = selectedRecipeItems.map { Pair(it.first.id, it.second) }
                                onSave(product, refs)
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Salvar Produto")
                    }
                }
            }
        }
    }
}
