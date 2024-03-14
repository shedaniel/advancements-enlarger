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
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BiggerAdvancementTab {
    private final MinecraftClient client;
    private final BiggerAdvancementsScreen screen;
    private final AdvancementTabTypeHooks type;
    private final int index;
    private final PlacedAdvancement root;
    private final AdvancementDisplay display;
    private final ItemStack icon;
    private final Text title;
    private final BiggerAdvancementWidget rootWidget;
    private final Map<AdvancementEntry, BiggerAdvancementWidget> widgets = Maps.newLinkedHashMap();
    private double originX;
    private double originY;
    private int minPanX = 2147483647;
    private int minPanY = 2147483647;
    private int maxPanX = -2147483648;
    private int maxPanY = -2147483648;
    private float alpha;
    private boolean initialized;

    public BiggerAdvancementTab(MinecraftClient client, BiggerAdvancementsScreen screen, AdvancementTabTypeHooks type, int index, PlacedAdvancement root, AdvancementDisplay display) {
        this.client = client;
        this.screen = screen;
        this.type = type;
        this.index = index;
        this.root = root;
        this.display = display;
        this.icon = display.getIcon();
        this.title = display.getTitle();
        this.rootWidget = new BiggerAdvancementWidget(this, client, root, display);
        this.addWidget(this.rootWidget, root.getAdvancementEntry());
    }

    public static BiggerAdvancementTab create(MinecraftClient minecraft, BiggerAdvancementsScreen screen, int index, PlacedAdvancement root)
            throws ClassNotFoundException {
        Optional<AdvancementDisplay> display = root.getAdvancement().display();
        if (display.isPresent()) {
            Object[] var4 = Class.forName(FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_453")).getEnumConstants();
            int var5 = var4.length;

            for (Object o : var4) {
                AdvancementTabTypeHooks advancementTabType = (AdvancementTabTypeHooks) o;
                return new BiggerAdvancementTab(minecraft, screen, advancementTabType, index, root, display.get());
            }
        }
        return null;
    }

    public PlacedAdvancement getRoot() {
        return this.root;
    }

    public Text getTitle() {
        return this.title;
    }

    public void drawBackground(DrawContext context, int x, int y, boolean selected) {
        this.type.ae_drawBackground(context, x, y, selected, this.index);
    }

    public void drawIcon(DrawContext context, int x, int y) {
        this.type.ae_drawIcon(context, x, y, this.index, this.icon);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public void render(DrawContext context) {
        int width = screen.width - 34;
        int height = screen.height - 68;
        if (!this.initialized) {
            this.originX = width / 2 - (this.maxPanX + this.minPanX) / 2;
            this.originY = height / 2 - (this.maxPanY + this.minPanY) / 2;
            this.initialized = true;
        }
        
        Vector4f i1 = new Vector4f(0, 0, 0, 1), i2 = new Vector4f(width, height, 0, 1);
        i1.mul(context.getMatrices().peek().getPositionMatrix());
        i2.mul(context.getMatrices().peek().getPositionMatrix());
        context.enableScissor(Math.round(i1.x), Math.round(i1.y), Math.round(i2.x), Math.round(i2.y));
        context.getMatrices().push();
        context.fill(width, height, 0, 0, 0xff000000);
        Identifier identifier = this.display.getBackground().orElse(null);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        if (identifier == null) {
            identifier = TextureManager.MISSING_IDENTIFIER;
        }

        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        int k = i % 16;
        int l = j % 16;

        for (int m = -1; m <= MathHelper.ceil(width / 16f) + 1; ++m) {
            for (int n = -1; n <= MathHelper.ceil(height / 16f) + 1; ++n) {
                context.drawTexture(identifier, k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        this.rootWidget.renderLines(context, i, j, true);
        this.rootWidget.renderLines(context, i, j, false);
        this.rootWidget.renderWidgets(context, i, j);
        RenderSystem.depthFunc(518);
        context.getMatrices().translate(0.0F, 0.0F, -950.0F);
        RenderSystem.colorMask(false, false, false, false);
        context.fill(4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        context.getMatrices().translate(0.0F, 0.0F, 950.0F);
        RenderSystem.depthFunc(515);
        context.getMatrices().pop();
        context.disableScissor();
    }

    public void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
        int width = screen.width - 34;
        int height = screen.height - 68;
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 200.0F);
        context.fill(0, 0, width, height, MathHelper.floor(this.alpha * 255.0F) << 24);
        boolean bl = false;
        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        if (mouseX > 0 && mouseX < width && mouseY > 0 && mouseY < height) {
            for (BiggerAdvancementWidget advancementWidget : this.widgets.values()) {
                if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
                    bl = true;
                    advancementWidget.drawTooltip(context, i, j, this.alpha, x, y);
                    break;
                }
            }
        }

        context.getMatrices().pop();
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
            move(amount * 10, 0);
            return;
        }
        if (this.maxPanY - this.minPanY > height) {
            move(0, amount * 10);
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

    public void addAdvancement(PlacedAdvancement advancement) {
        Optional<AdvancementDisplay> display = advancement.getAdvancement().display();
        if (display.isPresent()) {
            BiggerAdvancementWidget advancementWidget = new BiggerAdvancementWidget(this, this.client, advancement, display.get());
            this.addWidget(advancementWidget, advancement.getAdvancementEntry());
        }
    }

    private void addWidget(BiggerAdvancementWidget widget, AdvancementEntry advancement) {
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

    public BiggerAdvancementWidget getWidget(AdvancementEntry advancement) {
        return this.widgets.get(advancement);
    }

    public BiggerAdvancementsScreen getScreen() {
        return this.screen;
    }
}
