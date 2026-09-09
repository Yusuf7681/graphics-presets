package com.example.graphicspresets.client.mixin;

import com.example.graphicspresets.client.PresetScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void initPresetMenu(CallbackInfo ci) {
        // Otomatik açılışı sağlayan firstLaunch kontrolü ve setScreen çağrısı kaldırıldı.

        // Ana Menüdeki "⚡ Quality" Butonu
        this.addRenderableWidget(Button.builder(
                Component.literal("⚡ Quality"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new PresetScreen(this));
                    }
                }
        ).bounds(150, this.height / 4 + 48, 70, 20).build());
    }
}