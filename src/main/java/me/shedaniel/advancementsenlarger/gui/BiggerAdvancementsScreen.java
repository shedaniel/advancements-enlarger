/*
 * Advancements Enlarger by shedaniel.
 * Licensed under the CC-BY-NC-4.0.
 */

package me.shedaniel.advancementsenlarger.gui;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class BiggerAdvancementsScreen extends Screen implements ClientAdvancementManager.Listener {
    private static final Identifier WINDOW_TEXTURE = new Identifier("advancements-enlarger:textures/gui/advancements/recipecontainer.png");
    private static final Identifier WINDOW_DARK_TEXTURE = new Identifier("advancements-enlarger:textures/gui/advancements/recipecontainer_dark.png");
    private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
    private static final Identifier TABS_DARK_TEXTURE = new Identifier("advancements-enlarger:textures/gui/advancements/tabs_dark.png");
    private final ClientAdvancementManager advancementHandler;
    private final Map<AdvancementEntry, BiggerAdvancementTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private BiggerAdvancementTab selectedTab;
    private boolean movingTab;
    private Supplier<Boolean> reiExists = Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded("roughlyenoughitems"));
    private BooleanSupplier darkMode = () -> {
        if (!reiExists.get())
            return false;
        try {
            Object reiHelper = Class.forName("me.shedaniel.rei.api.REIHelper").getDeclaredMethod("getInstance").invoke(null);
            return (boolean) Class.forName("me.shedaniel.rei.api.REIHelper").getDeclaredMethod("isDarkThemeEnabled").invoke(reiHelper);
        } catch (Throwable ignored) {
        }
        return false;
    };

    public BiggerAdvancementsScreen(ClientAdvancementManager clientAdvancementManager) {
        super(NarratorManager.EMPTY);
        this.advancementHandler = clientAdvancementManager;
    }

    private boolean isDarkMode() {
        try {
            return darkMode.getAsBoolean();
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    protected void init() {
        this.tabs.clear();
        this.selectedTab = null;
        this.advancementHandler.setListener(this);
        if (this.selectedTab == null && !this.tabs.isEmpty()) {
            BiggerAdvancementTab advancementTab = this.tabs.values().iterator().next();
            this.advancementHandler.selectTab(advancementTab.getRoot().getAdvancementEntry(), true);
        } else {
            this.advancementHandler.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot().getAdvancementEntry(), true);
        }
    }

    @Override
    public void removed() {
        this.advancementHandler.setListener(null);
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
        if (clientPlayNetworkHandler != null) {
            clientPlayNetworkHandler.sendPacket(AdvancementTabC2SPacket.close());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selectedTab == null)
            return false;
        selectedTab.move(horizontalAmount * 16, verticalAmount * 16);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int i = 8;
            int j = 33;

            for (BiggerAdvancementTab advancementTab : this.tabs.values()) {
                if (advancementTab.isClickOnTab(i, j, mouseX, mouseY)) {
                    this.advancementHandler.selectTab(advancementTab.getRoot().getAdvancementEntry(), true);
                    break;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client.options.advancementsKey.matchesKey(keyCode, scanCode)) {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int i = 8;
        int j = 33;
        this.renderBackground(context, mouseX, mouseY, delta);
        this.drawAdvancementTree(context, mouseX, mouseY, i, j);
        this.drawWidgets(context, i, j);
        this.drawWidgetTooltip(context, mouseX, mouseY, i, j);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0) {
            this.movingTab = false;
            return false;
        } else {
            if (!this.movingTab) {
                this.movingTab = true;
            } else if (this.selectedTab != null) {
                this.selectedTab.move(deltaX, deltaY);
            }

            return true;
        }
    }

    private void drawAdvancementTree(DrawContext context, int mouseX, int mouseY, int x, int i) {
        BiggerAdvancementTab advancementTab = this.selectedTab;
        if (advancementTab == null) {
            context.fill(x + 9, i + 18, width - 9, height - 17, -16777216);
            String string = I18n.translate("advancements.empty");
            int j = this.textRenderer.getWidth(string);
            context.drawText(textRenderer, string, (width - j) / 2, (height - 33) / 2 + 33 - 9 / 2, -1, false);
            context.drawText(textRenderer, ":(", (width - this.textRenderer.getWidth(":(")) / 2, (height - 33) / 2 + 33 + 9 + 9 / 2, -1, false);
        } else {
            context.getMatrices().push();
            context.getMatrices().translate((float) (x + 9), (float) (i + 18), 0.0F);
            advancementTab.render(context);
            context.getMatrices().pop();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
        }
    }

    public void drawWidgets(DrawContext context, int x, int i) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        drawWindow(context, x, i);
        if (this.tabs.size() > 1) {
            Identifier texture = isDarkMode() ? TABS_DARK_TEXTURE : TABS_TEXTURE;
            RenderSystem.setShaderTexture(0, texture);
            Iterator<BiggerAdvancementTab> var3 = this.tabs.values().iterator();

            BiggerAdvancementTab advancementTab2;
            while (var3.hasNext()) {
                advancementTab2 = var3.next();
                advancementTab2.drawBackground(context, texture, x, i, advancementTab2 == this.selectedTab);
            }

            RenderSystem.defaultBlendFunc();
            var3 = this.tabs.values().iterator();

            while (var3.hasNext()) {
                advancementTab2 = var3.next();
                advancementTab2.drawIcon(context, x, i);
            }

            RenderSystem.disableBlend();
        }

        context.drawText(this.textRenderer, Text.translatable("gui.advancements"), x + 8, i + 6, isDarkMode() ? -1 : 4210752, false);
    }

    private void drawWindow(DrawContext context, int x, int y) {
        boolean darkMode = isDarkMode();
        Identifier texture = !darkMode ? WINDOW_TEXTURE : WINDOW_DARK_TEXTURE;
        int width = this.width - 16;
        int height = this.height - 41;
        //Four Corners
        context.drawTexture(texture, x, y, 106, 124 + 66, 4, 4);
        context.drawTexture(texture, x + width - 4, y, 252, 124 + 66, 4, 4);
        context.drawTexture(texture, x, y + height - 4, 106, 186 + 66, 4, 4);
        context.drawTexture(texture, x + width - 4, y + height - 4, 252, 186 + 66, 4, 4);

        //Sides
        for (int xx = 4; xx < width - 4; xx += 128) {
            int thisWidth = Math.min(128, width - 4 - xx);
            context.drawTexture(texture, x + xx, y, 110, 124 + 66, thisWidth, 4);
            context.drawTexture(texture, x + xx, y + height - 4, 110, 186 + 66, thisWidth, 4);
        }
        for (int yy = 4; yy < height - 4; yy += 50) {
            int thisHeight = Math.min(50, height - 4 - yy);
            context.drawTexture(texture, x, y + yy, 106, 128 + 66, 4, thisHeight);
            context.drawTexture(texture, x + width - 4, y + yy, 252, 128 + 66, 4, thisHeight);
        }
        int color = darkMode ? -13750738 : -3750202;
        context.fillGradient(x + 4, y + 4, x + width - 4, y + 18, color, color);
        context.fillGradient(x + 4, y + 4, x + 9, y + height - 4, color, color);
        context.fillGradient(x + width - 9, y + 4, x + width - 4, y + height - 4, color, color);
        context.fillGradient(x + 4, y + height - 9, x + width - 4, y + height - 4, color, color);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        int zOffset = 0;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(x + width - 9, y + 18, zOffset).color(0, 0, 0, 150).next();
        bufferBuilder.vertex(x + 9, y + 18, zOffset).color(0, 0, 0, 150).next();
        bufferBuilder.vertex(x + 9, y + 24, zOffset).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(x + width - 9, y + 24, zOffset).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(x + width - 9, y + height - 9 - 9, zOffset).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(x + 9, y + height - 9 - 9, zOffset).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(x + 9, y + height - 9, zOffset).color(0, 0, 0, 150).next();
        bufferBuilder.vertex(x + width - 9, y + height - 9, zOffset).color(0, 0, 0, 150).next();
        bufferBuilder.vertex(x + 15, y + 18, zOffset).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(x + 9, y + 18, zOffset).color(0, 0, 0, 150).next();
        bufferBuilder.vertex(x + 9, y + height - 9, zOffset).color(0, 0, 0, 150).next();
        bufferBuilder.vertex(x + 15, y + height - 9, zOffset).color(0, 0, 0, 0).next();

        bufferBuilder.vertex(x + width - 9, y + 18, zOffset).color(0, 0, 0, 150).next();
        bufferBuilder.vertex(x + width - 9 - 9, y + 18, zOffset).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(x + width - 9 - 9, y + height - 9, zOffset).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(x + width - 9, y + height - 9, zOffset).color(0, 0, 0, 150).next();
        tessellator.draw();
        RenderSystem.disableBlend();
    }

    private void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab != null) {
            context.getMatrices().push();
            RenderSystem.enableDepthTest();
            context.getMatrices().translate((float) (x + 9), (float) (y + 18), 400.0F);
            this.selectedTab.drawWidgetTooltip(context, mouseX - x - 9, mouseY - y - 18, x, y);
            RenderSystem.disableDepthTest();
            context.getMatrices().pop();
        }

        if (this.tabs.size() > 1) {
            for (BiggerAdvancementTab advancementTab : this.tabs.values()) {
                if (advancementTab.isClickOnTab(x, y, mouseX, mouseY)) {
                    context.drawTooltip(this.textRenderer, advancementTab.getTitle(), mouseX, mouseY);
                }
            }
        }

    }

    @Override
    public void onRootAdded(PlacedAdvancement root) {
        try {
            BiggerAdvancementTab advancementTab = BiggerAdvancementTab.create(this.client, this, this.tabs.size(), root);
            if (advancementTab != null) {
                this.tabs.put(root.getAdvancementEntry(), advancementTab);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRootRemoved(PlacedAdvancement root) {
    }

    @Override
    public void onDependentAdded(PlacedAdvancement dependent) {
        BiggerAdvancementTab advancementTab = this.getTab(dependent);
        if (advancementTab != null) {
            advancementTab.addAdvancement(dependent);
        }

    }

    @Override
    public void onDependentRemoved(PlacedAdvancement dependent) {
    }

    @Override
    public void setProgress(PlacedAdvancement advancement, AdvancementProgress progress) {
        BiggerAdvancementWidget advancementWidget = this.getAdvancementWidget(advancement);
        if (advancementWidget != null) {
            advancementWidget.setProgress(progress);
        }

    }

    @Override
    public void selectTab(AdvancementEntry advancement) {
        this.selectedTab = this.tabs.get(advancement);
    }

    public void onClear() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    public BiggerAdvancementWidget getAdvancementWidget(PlacedAdvancement advancement) {
        BiggerAdvancementTab advancementTab = this.getTab(advancement);
        return advancementTab == null ? null : advancementTab.getWidget(advancement.getAdvancementEntry());
    }

    private BiggerAdvancementTab getTab(PlacedAdvancement advancement) {
        PlacedAdvancement placedAdvancement = advancement.getRoot();
        return this.tabs.get(placedAdvancement.getAdvancementEntry());
    }
}
