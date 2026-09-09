package com.example.graphicspresets.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PresetScreen extends Screen {

    private static final String MOD_ID = "graphics-presets";

    private static final String[] SHADER_IDS = {
            "bliss", "complementary", "iteration", "photon"
    };
    private static final String[] SHADER_LABELS = {
            "Bliss", "Complementary", "Iteration", "Photon"
    };

    private static final String[] SHADER_TOOLTIPS = {
            "High Quality Shader",
            "Medium Quality Shader",
            "Ultra Quality Shader\nRECOMMENDED: Best used on Medium Preset or lower!\n(DH max 32 Radius recommended with Iteration)",
            "High Quality Shader"
    };

    private static final PresetApplier.GraphicsPreset[] PRESETS = PresetApplier.GraphicsPreset.values();

    private final Screen parent;

    private String currentShader = SHADER_IDS[0];
    private int selectedShaderIndex = 0;

    private int previewQualityIndex = 1;
    private int selectedPresetIndex = 0;

    private boolean showAppliedDialog = false;
    private Button applyPresetBtn;

    private final List<Button> shaderButtons = new ArrayList<>();
    private final List<Button> presetButtons = new ArrayList<>();
    private final List<Button> dialogButtons = new ArrayList<>();

    private static final int LEFT_MARGIN = 20;
    private static final int LIST_BUTTON_WIDTH = 120;
    private static final int LIST_BUTTON_HEIGHT = 20;
    private static final int LIST_BUTTON_SPACING = 24;
    private static final int LIST_TOP = 40;

    private static final int PREVIEW_WIDTH = 240;
    private static final int PREVIEW_HEIGHT = 135;

    public PresetScreen(Screen parent) {
        super(Component.literal("Graphics Presets"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.shaderButtons.clear();
        this.presetButtons.clear();
        this.dialogButtons.clear();

        if (this.showAppliedDialog) {
            initDialogStage();
            return;
        }

        // 1. Sol Taraf: Shader Seçimleri
        for (int i = 0; i < SHADER_IDS.length; i++) {
            final int index = i;
            int y = LIST_TOP + i * LIST_BUTTON_SPACING;

            Button button = Button.builder(
                            Component.literal(SHADER_LABELS[i]),
                            btn -> onShaderClicked(index))
                    .bounds(LEFT_MARGIN, y, LIST_BUTTON_WIDTH, LIST_BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.literal(SHADER_TOOLTIPS[i])))
                    .build();

            this.shaderButtons.add(button);
            this.addRenderableWidget(button);
        }

        // 2. Sol Alt: "Select Shaders" (Iris Screen)
        int selectShaderY = LIST_TOP + SHADER_IDS.length * LIST_BUTTON_SPACING + 15;
        Button selectShadersBtn = Button.builder(
                        Component.literal("Select Shaders"),
                        btn -> openIrisShaderScreen())
                .bounds(LEFT_MARGIN, selectShaderY, LIST_BUTTON_WIDTH, LIST_BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open Iris Shader selection menu")))
                .build();
        this.addRenderableWidget(selectShadersBtn);

        // 3. Orta Alt: Fotoğraf Okları (< ve >)
        int previewX = getPreviewX();
        int arrowY = LIST_TOP + PREVIEW_HEIGHT + 10;

        Button leftArrow = Button.builder(Component.literal("<"), btn -> changePhoto(-1))
                .bounds(previewX, arrowY, 25, 20)
                .build();
        this.addRenderableWidget(leftArrow);

        Button rightArrow = Button.builder(Component.literal(">"), btn -> changePhoto(1))
                .bounds(previewX + PREVIEW_WIDTH - 25, arrowY, 25, 20)
                .build();
        this.addRenderableWidget(rightArrow);

        // 4. Sağ Taraf: Yeşil Renkli "Apply Preset" Butonu ve Preset Listesi
        int rightX = previewX + PREVIEW_WIDTH + 25;
        int rightWidth = 120;

        // "Apply Preset" butonunu belirginleştirmek için Yeşil (§a) formatı verildi
        this.applyPresetBtn = Button.builder(Component.literal("§a✔ Apply Preset"), btn -> onApplyPressed())
                .bounds(rightX, LIST_TOP, rightWidth, 20)
                .tooltip(Tooltip.create(Component.literal("Click to save and apply selected preset configuration")))
                .build();
        this.addRenderableWidget(this.applyPresetBtn);

        int presetsTopY = LIST_TOP + 23;
        for (int i = 0; i < PRESETS.length; i++) {
            final int presetIndex = i;
            String rawName = PRESETS[i].getName();
            String cleanPresetName = rawName.replaceAll("§.", "");

            // Seçili olan preset'in yanına gösterge eklenebilir
            if (presetIndex == this.selectedPresetIndex) {
                cleanPresetName = "> " + cleanPresetName;
            }

            String tooltipText = PRESETS[i].getDesc();
            int y = presetsTopY + i * 19;

            Button pBtn = Button.builder(
                            Component.literal(cleanPresetName),
                            btn -> onPresetClicked(presetIndex))
                    .bounds(rightX, y, rightWidth, 17)
                    .tooltip(Tooltip.create(Component.literal(tooltipText)))
                    .build();

            this.presetButtons.add(pBtn);
            this.addRenderableWidget(pBtn);
        }

        // 5. Sol En Alt: Back
        Button backButton = Button.builder(Component.literal("< Back"), btn -> this.onClose())
                .bounds(LEFT_MARGIN, this.height - 30, 80, 20)
                .build();
        this.addRenderableWidget(backButton);
    }

    private void initDialogStage() {
        int dialogWidth = 260;
        int dialogHeight = 100;
        int dialogX = (this.width - dialogWidth) / 2;
        int dialogY = (this.height - dialogHeight) / 2;

        int btnY = dialogY + dialogHeight - 28;

        // Quit Butonu (Oyunu Kapatır)
        Button quitBtn = Button.builder(Component.literal("Quit"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.stop();
            }
        }).bounds(dialogX + 20, btnY, 100, 20).build();

        // Later Butonu (Kapatır, Oyuna Devam Eder)
        Button laterBtn = Button.builder(Component.literal("Later"), btn -> {
            this.showAppliedDialog = false;
            this.onClose();
        }).bounds(dialogX + 140, btnY, 100, 20).build();

        this.dialogButtons.add(quitBtn);
        this.dialogButtons.add(laterBtn);

        this.addRenderableWidget(quitBtn);
        this.addRenderableWidget(laterBtn);
    }

    private void onShaderClicked(int index) {
        this.selectedShaderIndex = index;
        this.currentShader = SHADER_IDS[index];
    }

    private void onPresetClicked(int index) {
        this.selectedPresetIndex = index;
        // Bir presete tıklandığında buton görünümünü yeniler (Seçili olanı belli etmek için)
        this.clearWidgets();
        this.init();
    }

    private void changePhoto(int direction) {
        int maxPhotos = 7;
        int next = this.previewQualityIndex - 1 + direction;
        next = ((next % maxPhotos) + maxPhotos) % maxPhotos;
        this.previewQualityIndex = next + 1;
    }

    private void openIrisShaderScreen() {
        if (this.minecraft == null) return;

        // Iris'in farklı sürümlerde kullandığı olası ekran sınıfları
        String[] irisScreenClasses = {
                "net.irisshaders.iris.gui.screen.ShaderPackScreen",
                "net.irisshaders.iris.gui.option.IrisShaderScreen",
                "net.irisshaders.iris.gui.option.ShaderPackScreen"
        };

        for (String className : irisScreenClasses) {
            try {
                Class<?> clazz = Class.forName(className);
                Screen irisScreen = null;

                // 1. Önce Screen parametresi alan constructor'ı dener: new ShaderPackScreen(parent)
                try {
                    irisScreen = (Screen) clazz.getConstructor(Screen.class).newInstance(this);
                } catch (NoSuchMethodException e) {
                    // 2. Parametresiz constructor varsa onu dener
                    irisScreen = (Screen) clazz.getConstructor().newInstance();
                }

                if (irisScreen != null) {
                    this.minecraft.setScreen(irisScreen);
                    return; // Ekran başarıyla açıldı, döngüden çık
                }
            } catch (Exception ignored) {
                // Sınıf bulunamadıysa bir sonrakini dener
            }
        }

        System.err.println("[GraphicsPresets] Iris Shader ekranı sınıfları bulunamadı.");
    }

    private void onApplyPressed() {
        if (this.selectedPresetIndex >= 0 && this.selectedPresetIndex < PRESETS.length) {
            PresetApplier.GraphicsPreset selectedPreset = PRESETS[this.selectedPresetIndex];
            PresetApplier.apply(selectedPreset);
        }

        this.showAppliedDialog = true;
        this.clearWidgets();
        this.init();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x80000000);

        if (this.showAppliedDialog) {
            int dialogWidth = 260;
            int dialogHeight = 100;
            int dialogX = (this.width - dialogWidth) / 2;
            int dialogY = (this.height - dialogHeight) / 2;

            // Diyalog Kutusu Çerçevesi
            guiGraphics.fill(dialogX - 1, dialogY - 1, dialogX + dialogWidth + 1, dialogY + dialogHeight + 1, 0xFFFFFFFF);
            guiGraphics.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, 0xFF101010);

            // Başlık ve Açıklama Metni
            drawCentered(guiGraphics, Component.literal("Settings Applied!"),
                    this.width / 2, dialogY + 14, 0xFF55FF55);

            drawCentered(guiGraphics, Component.literal("Please restart the game for all settings"),
                    this.width / 2, dialogY + 34, 0xFFDDDDDD);
            drawCentered(guiGraphics, Component.literal("to take full effect."),
                    this.width / 2, dialogY + 46, 0xFFAAAAAA);

            super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
            return;
        }

        drawCentered(guiGraphics, this.title, this.width / 2, 12, 0xFFFFFFFF);

        int previewX = getPreviewX();
        int previewY = LIST_TOP;

        int bx = previewX - 2, by = previewY - 2;
        int bw = PREVIEW_WIDTH + 4, bh = PREVIEW_HEIGHT + 4;
        guiGraphics.fill(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
        guiGraphics.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFFFFFFFF);
        guiGraphics.fill(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
        guiGraphics.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFFFFFFFF);

        if (this.currentShader != null) {
            Identifier texture = Identifier.fromNamespaceAndPath(MOD_ID,
                    "textures/gui/" + this.currentShader + this.previewQualityIndex + ".png");

            try {
                guiGraphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        texture,
                        previewX, previewY,
                        0f, 0f,
                        PREVIEW_WIDTH, PREVIEW_HEIGHT,
                        PREVIEW_WIDTH, PREVIEW_HEIGHT
                );
            } catch (Exception ignored) {
            }

            String labelText = SHADER_LABELS[selectedShaderIndex] + this.previewQualityIndex;
            drawCentered(guiGraphics, Component.literal(labelText),
                    previewX + PREVIEW_WIDTH / 2, previewY + PREVIEW_HEIGHT + 15, 0xFFFFFFFF);
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }

    private void drawCentered(GuiGraphicsExtractor guiGraphics, Component text, int centerX, int y, int color) {
        int textWidth = this.font.width(text);
        guiGraphics.text(this.font, text, centerX - textWidth / 2, y, color);
    }

    private int getPreviewX() {
        return LEFT_MARGIN + LIST_BUTTON_WIDTH + 25;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}