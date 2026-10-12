package com.example.data.model

enum class OrderStatus(
    val title: String,
    val description: String
) {
    NEGOCIACAO("Em Negociação", "Orçamento e negociação com o cliente"),
    EM_FILA("Na Fila", "Aguardando início de impressão e acabamento"),
    PRODUZINDO("Produzindo", "Na máquina / corte / laminação"),
    CONCLUIDO("Concluído", "Produto finalizado, pronto para retirada"),
    ENTREGUE("Entregue", "Entregue ao cliente e finalizado");

    companion object {
        fun fromString(value: String): OrderStatus {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NEGOCIACAO
        }
    }
}
