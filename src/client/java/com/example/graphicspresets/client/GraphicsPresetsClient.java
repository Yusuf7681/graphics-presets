package com.example.graphicspresets.client;

import net.fabricmc.api.ClientModInitializer;

public class GraphicsPresetsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Otomatik ekran açma kaldırıldı.
        // Kullanıcı menüden veya tuşla dilediği zaman PresetScreen'i açabilir.
    }
}