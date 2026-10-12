package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FinancialDao
import com.example.data.dao.MaterialDao
import com.example.data.dao.OrderDao
import com.example.data.dao.ProductDao
import com.example.data.model.FinancialEntry
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductMaterialCrossRef
import com.example.data.model.RawMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RawMaterial::class,
        Product::class,
        ProductMaterialCrossRef::class,
        Order::class,
        FinancialEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun financialDao(): FinancialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grafica_pro_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    val materialDao = database.materialDao()
    val productDao = database.productDao()
    val orderDao = database.orderDao()
    val financialDao = database.financialDao()

    // 1. Raw Materials
    val matCouche = RawMaterial(
        name = "Papel Couchê 150g A3",
        category = "Papéis",
        unit = "Folha",
        unitCost = 0.65,
        inStock = true,
        stockQuantity = 850.0,
        minStockAlert = 200.0,
        notes = "Ideal para panfletos, folders e cartazes"
    )
    val matTriplex = RawMaterial(
        name = "Papel Triplex 300g A3+",
        category = "Papéis",
        unit = "Folha",
        unitCost = 1.25,
        inStock = true,
        stockQuantity = 420.0,
        minStockAlert = 100.0,
        notes = "Papel grosso para cartões e capas"
    )
    val matSulfite = RawMaterial(
        name = "Papel Sulfite 75g A4",
        category = "Papéis",
        unit = "Folha",
        unitCost = 0.08,
        inStock = true,
        stockQuantity = 2500.0,
        minStockAlert = 500.0,
        notes = "Blocos, receituários e miolo de apostilas"
    )
    val matLona = RawMaterial(
        name = "Lona Fosca 440g",
        category = "Grandes Formatos",
        unit = "m²",
        unitCost = 12.50,
        inStock = true,
        stockQuantity = 65.0,
        minStockAlert = 20.0,
        notes = "Banners e faixas de alta durabilidade"
    )
    val matVinil = RawMaterial(
        name = "Vinil Adesivo Brilho",
        category = "Grandes Formatos",
        unit = "m²",
        unitCost = 8.90,
        inStock = true,
        stockQuantity = 45.0,
        minStockAlert = 15.0,
        notes = "Adesivos promocionais e rótulos"
    )
    val matTintaEco = RawMaterial(
        name = "Tinta ECO Solvente CMYK",
        category = "Tintas",
        unit = "Litro",
        unitCost = 135.00,
        inStock = true,
        stockQuantity = 4.2,
        minStockAlert = 1.0,
        notes = "Plotter de impressão digital"
    )
    val matTonerLaser = RawMaterial(
        name = "Toner Digital Color (Kit)",
        category = "Tintas",
        unit = "Unidade",
        unitCost = 350.00,
        inStock = true,
        stockQuantity = 2.0,
        minStockAlert = 1.0,
        notes = "Impressora de produção Konica/Xerox"
    )
    val matIlhos = RawMaterial(
        name = "Ilhós Metálico Niquelado #0",
        category = "Acabamento",
        unit = "Unidade",
        unitCost = 0.15,
        inStock = true,
        stockQuantity = 1200.0,
        minStockAlert = 250.0,
        notes = "Fixação para acabamento em lonas"
    )
    val matBopp = RawMaterial(
        name = "Película BOPP Fosca",
        category = "Acabamento",
        unit = "Metro",
        unitCost = 1.70,
        inStock = true,
        stockQuantity = 190.0,
        minStockAlert = 40.0,
        notes = "Laminação a quente profissional"
    )
    val matPlacaPvc = RawMaterial(
        name = "Placa PVC Rígido 2mm",
        category = "Rígidos",
        unit = "m²",
        unitCost = 36.00,
        inStock = false, // Item FORA DE ESTOQUE (sob demanda)
        stockQuantity = 0.0,
        minStockAlert = 0.0,
        notes = "Item sob demanda - Fornecedor parceiro entrega em 24h"
    )
    val matEspiral = RawMaterial(
        name = "Espiral Plástico 17mm c/ Capas",
        category = "Acabamento",
        unit = "Unidade",
        unitCost = 1.10,
        inStock = true,
        stockQuantity = 18.0, // Low stock trigger
        minStockAlert = 25.0,
        notes = "Encadernação térmica/espiral"
    )

    val matCoucheId = materialDao.insertMaterial(matCouche)
    val matTriplexId = materialDao.insertMaterial(matTriplex)
    val matSulfiteId = materialDao.insertMaterial(matSulfite)
    val matLonaId = materialDao.insertMaterial(matLona)
    val matVinilId = materialDao.insertMaterial(matVinil)
    val matTintaEcoId = materialDao.insertMaterial(matTintaEco)
    val matTonerLaserId = materialDao.insertMaterial(matTonerLaser)
    val matIlhosId = materialDao.insertMaterial(matIlhos)
    val matBoppId = materialDao.insertMaterial(matBopp)
    val matPlacaPvcId = materialDao.insertMaterial(matPlacaPvc)
    val matEspiralId = materialDao.insertMaterial(matEspiral)

    // 2. Products
    val prodCartao = Product(
        name = "Cartão de Visita 1000un (Triplex 300g)",
        description = "Milheiro de cartões 9x5cm, papel triplex 300g encorpado, laminação fosca BOPP e cantos retos.",
        category = "Identidade Visual",
        salePrice = 85.00,
        productionTimeMinutes = 30,
        machineHourlyRate = 30.00, // R$ 15,00 mão de obra
        isAvailableInCatalog = true,
        inStockFinished = false,
        deliveryDaysEstimate = 2
    )
    val prodBanner = Product(
        name = "Banner Promocional 1x0.8m",
        description = "Banner em lona 440g fosca anti-reflexo com acabamento de bastão de madeira, ponteiras e ilhós reforçado.",
        category = "Grandes Formatos",
        salePrice = 68.00,
        productionTimeMinutes = 20,
        machineHourlyRate = 42.00, // R$ 14,00 mão de obra
        isAvailableInCatalog = true,
        inStockFinished = false,
        deliveryDaysEstimate = 1
    )
    val prodPanfletos = Product(
        name = "Panfletos 10x15cm (1000un)",
        description = "Milheiro de panfletos coloridos frente e verso em couchê 150g brilho com corte reto de alta precisão.",
        category = "Impressos Promocionais",
        salePrice = 135.00,
        productionTimeMinutes = 45,
        machineHourlyRate = 36.00, // R$ 27,00 mão de obra
        isAvailableInCatalog = true,
        inStockFinished = false,
        deliveryDaysEstimate = 2
    )
    val prodAdesivos = Product(
        name = "Cartela Adesivos Vinil A4",
        description = "Adesivos impressos em vinil automotivo de alta resistência com recorte eletrônico personalizado.",
        category = "Rótulos & Adesivos",
        salePrice = 14.00,
        productionTimeMinutes = 8,
        machineHourlyRate = 35.00,
        isAvailableInCatalog = true,
        inStockFinished = true,
        finishedStockQty = 25,
        deliveryDaysEstimate = 1
    )
    val prodApostila = Product(
        name = "Apostila Encadernada 50 fls",
        description = "Apostila sulfite 75g preto e branco com capa em papel grosso colorido e encadernação em espiral.",
        category = "Documentos & Papelaria",
        salePrice = 24.50,
        productionTimeMinutes = 12,
        machineHourlyRate = 25.00,
        isAvailableInCatalog = true,
        inStockFinished = false,
        deliveryDaysEstimate = 1
    )

    val prodCartaoId = productDao.insertProduct(prodCartao)
    val prodBannerId = productDao.insertProduct(prodBanner)
    val prodPanfletosId = productDao.insertProduct(prodPanfletos)
    val prodAdesivosId = productDao.insertProduct(prodAdesivos)
    val prodApostilaId = productDao.insertProduct(prodApostila)

    // 3. Link BOM Recipes (ProductMaterialCrossRef)
    productDao.insertProductMaterials(
        listOf(
            // Cartão de Visita recipe
            ProductMaterialCrossRef(productId = prodCartaoId, materialId = matTriplexId, quantityUsed = 12.0), // 12 folhas A3 = R$ 15,00
            ProductMaterialCrossRef(productId = prodCartaoId, materialId = matBoppId, quantityUsed = 5.0),      // 5 metros = R$ 8,50
            ProductMaterialCrossRef(productId = prodCartaoId, materialId = matTonerLaserId, quantityUsed = 0.02), // R$ 7,00

            // Banner 1x0.8m recipe
            ProductMaterialCrossRef(productId = prodBannerId, materialId = matLonaId, quantityUsed = 0.8),       // 0.8 m² = R$ 10,00
            ProductMaterialCrossRef(productId = prodBannerId, materialId = matTintaEcoId, quantityUsed = 0.04),  // 0.04 L = R$ 5,40
            ProductMaterialCrossRef(productId = prodBannerId, materialId = matIlhosId, quantityUsed = 4.0),      // 4 un = R$ 0,60

            // Panfletos recipe
            ProductMaterialCrossRef(productId = prodPanfletosId, materialId = matCoucheId, quantityUsed = 125.0), // 125 fls A3 = R$ 81,25
            ProductMaterialCrossRef(productId = prodPanfletosId, materialId = matTonerLaserId, quantityUsed = 0.04), // R$ 14,00

            // Adesivo Vinil A4 recipe
            ProductMaterialCrossRef(productId = prodAdesivosId, materialId = matVinilId, quantityUsed = 0.07),    // 0.07 m² = R$ 0,62
            ProductMaterialCrossRef(productId = prodAdesivosId, materialId = matTintaEcoId, quantityUsed = 0.005), // R$ 0,67

            // Apostila recipe
            ProductMaterialCrossRef(productId = prodApostilaId, materialId = matSulfiteId, quantityUsed = 50.0),  // 50 fls = R$ 4,00
            ProductMaterialCrossRef(productId = prodApostilaId, materialId = matEspiralId, quantityUsed = 1.0),   // 1 kit = R$ 1,10
            ProductMaterialCrossRef(productId = prodApostilaId, materialId = matTriplexId, quantityUsed = 1.0)     // capa = R$ 1,25
        )
    )

    // 4. Initial Orders distributed across Kanban stages
    val now = System.currentTimeMillis()
    val dayMs = 24 * 60 * 60 * 1000L

    orderDao.insertAll(
        listOf(
            Order(
                orderNumber = "PED-101",
                clientName = "Restaurante Sabor & Arte",
                clientPhone = "(11) 98765-4321",
                productId = prodPanfletosId,
                productName = prodPanfletos.name,
                quantity = 2, // 2000 panfletos
                unitSalePrice = 135.00,
                totalSalePrice = 270.00,
                materialCostTotal = 190.50,
                productionCostTotal = 54.00,
                totalCostTotal = 244.50,
                productionTimeMinutesTotal = 90,
                status = OrderStatus.NEGOCIACAO.name,
                notes = "Cliente pediu amostra digital do cardápio especial",
                createdAt = now - (dayMs / 2)
            ),
            Order(
                orderNumber = "PED-102",
                clientName = "Dra. Camila Dermatologia",
                clientPhone = "(11) 99123-8877",
                productId = prodCartaoId,
                productName = prodCartao.name,
                quantity = 1,
                unitSalePrice = 85.00,
                totalSalePrice = 85.00,
                materialCostTotal = 30.50,
                productionCostTotal = 15.00,
                totalCostTotal = 45.50,
                productionTimeMinutesTotal = 30,
                status = OrderStatus.EM_FILA.name,
                notes = "Arquivo aprovado com sangria e marcas de corte",
                createdAt = now - (dayMs * 1)
            ),
            Order(
                orderNumber = "PED-103",
                clientName = "Imobiliária Prime Brokers",
                clientPhone = "(11) 97654-1122",
                productId = prodBannerId,
                productName = prodBanner.name,
                quantity = 3,
                unitSalePrice = 68.00,
                totalSalePrice = 204.00,
                materialCostTotal = 48.00,
                productionCostTotal = 42.00,
                totalCostTotal = 90.00,
                productionTimeMinutesTotal = 60,
                status = OrderStatus.PRODUZINDO.name,
                notes = "Imprimindo no plotter Roland 1.60m, ilhós nos cantos",
                createdAt = now - (dayMs * 2)
            ),
            Order(
                orderNumber = "PED-104",
                clientName = "Academia IronFit Centro",
                clientPhone = "(11) 98222-3344",
                productId = prodAdesivosId,
                productName = prodAdesivos.name,
                quantity = 20,
                unitSalePrice = 14.00,
                totalSalePrice = 280.00,
                materialCostTotal = 25.80,
                productionCostTotal = 93.30,
                totalCostTotal = 119.10,
                productionTimeMinutesTotal = 160,
                status = OrderStatus.CONCLUIDO.name,
                notes = "Aguardando retirada no balcão pelo motoboy",
                createdAt = now - (dayMs * 3),
                completedAt = now - (dayMs / 4)
            ),
            Order(
                orderNumber = "PED-105",
                clientName = "Colégio Futuro & Cidadania",
                clientPhone = "(11) 99876-0099",
                productId = prodApostilaId,
                productName = prodApostila.name,
                quantity = 35,
                unitSalePrice = 24.50,
                totalSalePrice = 857.50,
                materialCostTotal = 222.25,
                productionCostTotal = 175.00,
                totalCostTotal = 397.25,
                productionTimeMinutesTotal = 420,
                status = OrderStatus.ENTREGUE.name,
                notes = "Entregue com recibo e nota de serviço",
                createdAt = now - (dayMs * 5),
                completedAt = now - (dayMs * 1),
                isPaid = true
            )
        )
    )

    // 5. Initial Financial entries
    financialDao.insertAll(
        listOf(
            FinancialEntry(
                description = "Recebimento Pedido #PED-105 - Colégio Futuro",
                type = "RECEITA",
                category = "Venda de Pedido",
                amount = 857.50,
                date = now - (dayMs * 1),
                orderId = 5
            ),
            FinancialEntry(
                description = "Compra Lote Lona Fosca + Adesivos",
                type = "DESPESA",
                category = "Compra de Insumos",
                amount = 450.00,
                date = now - (dayMs * 4)
            ),
            FinancialEntry(
                description = "Manutenção Preventiva Cabeça de Impressão Plotter",
                type = "DESPESA",
                category = "Manutenção de Máquinas",
                amount = 180.00,
                date = now - (dayMs * 6)
            ),
            FinancialEntry(
                description = "Conta de Energia Oficina Gráfica",
                type = "DESPESA",
                category = "Energia/Aluguel",
                amount = 320.00,
                date = now - (dayMs * 8)
            )
        )
    )
}
