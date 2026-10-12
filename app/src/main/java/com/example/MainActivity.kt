package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.FinancialScreen
import com.example.ui.screens.KanbanScreen
import com.example.ui.screens.MaterialsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GraficaViewModel

enum class MainNavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    KANBAN("Kanban", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_kanban"),
    ORDERS("Pedidos", Icons.Filled.ListAlt, Icons.Outlined.ListAlt, "tab_orders"),
    PRODUCTS("Produtos", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "tab_products"),
    MATERIALS("Insumos", Icons.Filled.Inventory, Icons.Outlined.Inventory, "tab_materials"),
    CATALOG("Catálogo", Icons.Filled.Storefront, Icons.Outlined.Storefront, "tab_catalog"),
    FINANCIAL("Financeiro", Icons.Filled.AttachMoney, Icons.Outlined.AttachMoney, "tab_financial")
}

class MainActivity : ComponentActivity() {
    private val viewModel: GraficaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GraficaMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraficaMainApp(viewModel: GraficaViewModel) {
    var selectedTab by remember { mutableStateOf(MainNavigationTab.KANBAN) }
    var isNewOrderDialogOpen by remember { mutableStateOf(false) }

    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val productsWithMaterials by viewModel.productsWithMaterials.collectAsStateWithLifecycle()
    val rawMaterials by viewModel.rawMaterials.collectAsStateWithLifecycle()
    val financialEntries by viewModel.financialEntries.collectAsStateWithLifecycle()
    val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Back button behavior: Return to Kanban tab before closing
    BackHandler(enabled = selectedTab != MainNavigationTab.KANBAN) {
        selectedTab = MainNavigationTab.KANBAN
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo_1791761545283),
                                contentDescription = "Logo GráficaPro",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "GráficaPro",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        MainNavigationTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainNavigationTab.values().forEach { tab ->
                            NavigationRailItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (selectedTab) {
                        MainNavigationTab.KANBAN -> {
                            KanbanScreen(
                                viewModel = viewModel,
                                orders = orders,
                                productsWithMaterials = productsWithMaterials,
                                onOpenNewOrder = { isNewOrderDialogOpen = true }
                            )
                        }
                        MainNavigationTab.ORDERS -> {
                            OrdersScreen(
                                viewModel = viewModel,
                                orders = orders,
                                productsWithMaterials = productsWithMaterials,
                                isNewOrderDialogOpen = isNewOrderDialogOpen,
                                onCloseNewOrderDialog = { isNewOrderDialogOpen = false },
                                onOpenNewOrderDialog = { isNewOrderDialogOpen = true }
                            )
                        }
                        MainNavigationTab.PRODUCTS -> {
                            ProductsScreen(
                                viewModel = viewModel,
                                productsWithMaterials = productsWithMaterials,
                                rawMaterials = rawMaterials
                            )
                        }
                        MainNavigationTab.MATERIALS -> {
                            MaterialsScreen(
                                viewModel = viewModel,
                                rawMaterials = rawMaterials
                            )
                        }
                        MainNavigationTab.CATALOG -> {
                            CatalogScreen(
                                viewModel = viewModel,
                                productsWithMaterials = productsWithMaterials
                            )
                        }
                        MainNavigationTab.FINANCIAL -> {
                            FinancialScreen(
                                viewModel = viewModel,
                                financialSummary = financialSummary,
                                entries = financialEntries
                            )
                        }
                    }
                }
            }
        }
    }
}
