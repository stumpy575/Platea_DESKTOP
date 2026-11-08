package com.project.ServerModel;

import java.time.LocalDateTime;

// hará falta cambiarlo para la migracion de datos
public record Esdeveniment(
        String id,
        String nom,
        String recinte,
        LocalDateTime dataHora,
        String categoria,
        String descripcio,
        String imatge) {
}