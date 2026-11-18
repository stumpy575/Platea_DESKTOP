package com.project.ServerModel;

import java.math.BigDecimal;

// entrada d'un esdeveniment, el seient es null si la zona es general
public record Entrada(
        String dataCompra,
        String localitzador,
        String tipus,
        String zona,
        String seient,
        String titular,
        String comprador,
        BigDecimal preu,
        String estat) {
}
