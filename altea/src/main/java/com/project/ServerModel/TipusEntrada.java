package com.project.ServerModel;

import java.math.BigDecimal;

// cambiarlo con la migracion
public record TipusEntrada(
        String id,
        String nom,
        String zona,
        BigDecimal preu,
        int disponibles) {
}