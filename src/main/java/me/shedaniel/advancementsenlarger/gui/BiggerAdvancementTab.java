/*
 * Advancements Enlarger by shedaniel.
 * Licensed under the CC-BY-NC-4.0.
 */

package me.shedaniel.advancementsenlarger.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.advancementsenlarger.hooks.AdvancementTabTypeHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class BiggerAdvancementTab extends DrawableHelper {
    private final MinecraftClient client;
    private final BiggerAdvancementsScreen screen;
    private final AdvancementTabTypeHooks type;
    private final int index;
    private final Advancement root;
    private final AdvancementDisplay display;
    private final ItemStack icon;
    private final String title;
    private final BiggerAdvancementWidget rootWidget;
    private final Map<Advancement, BiggerAdvancementWidget> widgets = Maps.newLinkedHashMap();
    private double originX;
    private double originY;
    private int minPanX = 2147483647;
    private int minPanY = 2147483647;
    private int maxPanX = -2147483648;
    private int maxPanY = -2147483648;
    private float alpha;
    private boolean initialized;
    
    public BiggerAdvancementTab(MinecraftClient client, BiggerAdvancementsScreen screen, AdvancementTabTypeHooks type, int index, Advancement root, AdvancementDisplay display) {
        this.client = client;
        this.screen = screen;
        this.type = type;
        this.index = index;
        this.root = root;
        this.display = display;
        this.icon = display.getIcon();
        this.title = display.getTitle().asFormattedString();
        this.rootWidget = new BiggerAdvancementWidget(this, client, root, display);
        this.addWidget(this.rootWidget, root);
    }
    
    public static BiggerAdvancementTab create(MinecraftClient minecraft, BiggerAdvancementsScreen screen, int index, Advancement root)
            throws ClassNotFoundException {
        if (root.getDisplay() != null) {
            Object[] var4 = Class.forName(FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_453")).getEnumConstants();
            int var5 = var4.length;
            
            for (Object o : var4) {
                AdvancementTabTypeHooks advancementTabType = (AdvancementTabTypeHooks) o;
                return new BiggerAdvancementTab(minecraft, screen, advancementTabType, index, root, root.getDisplay());
            }
        }
        return null;
    }
    
    public Advancement getRoot() {
        return this.root;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void drawBackground(int x, int y, boolean selected) {
        this.type.ae_drawBackground(this, x, y, selected, this.index);
    }
    
    public void drawIcon(int x, int y, ItemRenderer itemRenderer) {
        this.type.ae_drawIcon(x, y, this.index, itemRenderer, this.icon);
    }
    
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public void render() {
        int width = screen.width - 34;
        int height = screen.height - 68;
        if (!this.initialized) {
            this.originX = width / 2 - (this.maxPanX + this.minPanX) / 2;
            this.originY = height / 2 - (this.maxPanY + this.minPanY) / 2;
            this.initialized = true;
        }
        
        RenderSystem.pushMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.translatef(0.0F, 0.0F, 950.0F);
        RenderSystem.colorMask(false, false, false, false);
        fill(4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.translatef(0.0F, 0.0F, -950.0F);
        RenderSystem.depthFunc(518);
        fill(width, height, 0, 0, -16777216);
        RenderSystem.depthFunc(515);
        Identifier identifier = this.display.getBackground();
        if (identifier != null) {
            this.client.getTextureManager().bindTexture(identifier);
        } else {
            this.client.getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);
        }
        
        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        int k = i % 16;
        int l = j % 16;
        
        for (int m = -1; m <= MathHelper.ceil(width / 16f) + 1; ++m) {
            for (int n = -1; n <= MathHelper.ceil(height / 16f) + 1; ++n) {
                drawTexture(k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }
        
        this.rootWidget.renderLines(i, j, true);
        this.rootWidget.renderLines(i, j, false);
        this.rootWidget.renderWidgets(i, j);
        RenderSystem.depthFunc(518);
        RenderSystem.translatef(0.0F, 0.0F, -950.0F);
        RenderSystem.colorMask(false, false, false, false);
        fill(4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.translatef(0.0F, 0.0F, 950.0F);
        RenderSystem.depthFunc(515);
        RenderSystem.popMatrix();
    }
    
    public void drawWidgetTooltip(int mouseX, int mouseY, int x, int y) {
        int width = screen.width - 34;
        int height = screen.height - 68;
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0F, 0.0F, 200.0F);
        fill(0, 0, width, height, MathHelper.floor(this.alpha * 255.0F) << 24);
        boolean bl = false;
        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        if (mouseX > 0 && mouseX < width && mouseY > 0 && mouseY < height) {
            for (BiggerAdvancementWidget advancementWidget : this.widgets.values()) {
                if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
                    bl = true;
                    advancementWidget.drawTooltip(i, j, this.alpha, x, y);
                    break;
                }
            }
        }
        
        RenderSystem.popMatrix();
        if (bl) {
            this.alpha = MathHelper.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
        } else {
            this.alpha = MathHelper.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
        }
        
    }
    
    public boolean isClickOnTab(int screenX, int screenY, double mouseX, double mouseY) {
        return this.type.ae_isClickOnTab(screenX, screenY, this.index, mouseX, mouseY);
    }
    
    public void scroll(double amount) {
        int width = screen.width - 34;
        int height = screen.height - 68;
        if (this.maxPanX - this.minPanX > width) {
            move(amount, 0);
            return;
        }
        if (this.maxPanY - this.minPanY > height) {
            move(0, amount);
        }
    }
    
    public void move(double offsetX, double offsetY) {
        int width = screen.width - 34;
        int height = screen.height - 68;
        if (this.maxPanX - this.minPanX > width) {
            this.originX = MathHelper.clamp(this.originX + offsetX, -(this.maxPanX - width), 0.0D);
        }
        
        if (this.maxPanY - this.minPanY > height) {
            this.originY = MathHelper.clamp(this.originY + offsetY, -(this.maxPanY - height), 0.0D);
        }
        
    }
    
    public void addAdvancement(Advancement advancement) {
        if (advancement.getDisplay() != null) {
            BiggerAdvancementWidget advancementWidget = new BiggerAdvancementWidget(this, this.client, advancement, advancement.getDisplay());
            this.addWidget(advancementWidget, advancement);
        }
    }
    
    private void addWidget(BiggerAdvancementWidget widget, Advancement advancement) {
        this.widgets.put(advancement, widget);
        int i = widget.getX();
        int j = i + 28;
        int k = widget.getY();
        int l = k + 27;
        this.minPanX = Math.min(this.minPanX, i);
        this.maxPanX = Math.max(this.maxPanX, j);
        this.minPanY = Math.min(this.minPanY, k);
        this.maxPanY = Math.max(this.maxPanY, l);
        
        for (BiggerAdvancementWidget advancementWidget : this.widgets.values()) {
            advancementWidget.addToTree();
        }
    }
    
    public BiggerAdvancementWidget getWidget(Advancement advancement) {
        return this.widgets.get(advancement);
    }
    
    public BiggerAdvancementsScreen getScreen() {
        return this.screen;
    }
}
