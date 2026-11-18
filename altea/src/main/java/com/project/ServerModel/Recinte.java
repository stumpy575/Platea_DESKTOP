package com.project.ServerModel;

import java.util.List;

// recinte amb les seves zones, ve de getRecintos
public record Recinte(String id, String nom, List<Zona> zones) {

}
