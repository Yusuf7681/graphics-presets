package com.example.graphicspresets.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface render {
    void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta);
}
