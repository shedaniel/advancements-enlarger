/*
 * Advancements Enlarger by shedaniel.
 * Licensed under the CC-BY-NC-4.0.
 */

package me.shedaniel.advancementsenlarger.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class BiggerAdvancementWidget extends DrawableHelper {
    private static final Identifier WIDGETS_TEX = new Identifier("textures/gui/advancements/widgets.png");
    private static final Pattern BACKSLASH_S_PATTERN = Pattern.compile("(.+) \\S+");
    private final BiggerAdvancementTab tab;
    private final Advancement advancement;
    private final AdvancementDisplay display;
    private final String title;
    private final int width;
    private final List<String> description;
    private final MinecraftClient client;
    private final List<BiggerAdvancementWidget> children = Lists.newArrayList();
    private final int xPos;
    private final int yPos;
    private BiggerAdvancementWidget parent;
    private AdvancementProgress progress;
    
    public BiggerAdvancementWidget(BiggerAdvancementTab tab, MinecraftClient client, Advancement advancement, AdvancementDisplay display) {
        this.tab = tab;
        this.advancement = advancement;
        this.display = display;
        this.client = client;
        this.title = client.textRenderer.trimToWidth(display.getTitle().asFormattedString(), 163);
        this.xPos = MathHelper.floor(display.getX() * 28.0F);
        this.yPos = MathHelper.floor(display.getY() * 27.0F);
        int i = advancement.getRequirementCount();
        int j = String.valueOf(i).length();
        int k = i > 1 ? client.textRenderer.getStringWidth("  ") + client.textRenderer.getStringWidth("0") * j * 2 + client.textRenderer.getStringWidth("/") : 0;
        int l = 29 + client.textRenderer.getStringWidth(this.title) + k;
        String string = display.getDescription().asFormattedString();
        this.description = this.wrapDescription(string, l);
        
        String string2;
        for (Iterator<String> var10 = this.description.iterator(); var10.hasNext(); l = Math.max(l, client.textRenderer.getStringWidth(string2))) {
            string2 = var10.next();
        }
        
        this.width = l + 3 + 5;
    }
    
    private List<String> wrapDescription(String description, int width) {
        if (description.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<String> list = this.client.textRenderer.wrapStringToWidthAsList(description, width);
            if (list.size() < 2) {
                return list;
            } else {
                String string = list.get(0);
                String string2 = list.get(1);
                int i = this.client.textRenderer.getStringWidth(string + ' ' + string2.split(" ")[0]);
                if (i - width <= 10) {
                    return this.client.textRenderer.wrapStringToWidthAsList(description, i);
                } else {
                    Matcher matcher = BACKSLASH_S_PATTERN.matcher(string);
                    if (matcher.matches()) {
                        int j = this.client.textRenderer.getStringWidth(matcher.group(1));
                        if (width - j <= 10) {
                            return this.client.textRenderer.wrapStringToWidthAsList(description, j);
                        }
                    }
                    
                    return list;
                }
            }
        }
    }
    
    private BiggerAdvancementWidget getParent(Advancement advancement) {
        do {
            advancement = advancement.getParent();
        } while (advancement != null && advancement.getDisplay() == null);
        
        if (advancement != null && advancement.getDisplay() != null) {
            return this.tab.getWidget(advancement);
        } else {
            return null;
        }
    }
    
    public void renderLines(int x, int y, boolean firstPass) {
        if (this.parent != null) {
            int i = x + this.parent.xPos + 13;
            int j = x + this.parent.xPos + 26 + 4;
            int k = y + this.parent.yPos + 13;
            int l = x + this.xPos + 13;
            int m = y + this.yPos + 13;
            int n = firstPass ? -16777216 : -1;
            if (firstPass) {
                this.drawHorizontalLine(j, i, k - 1, n);
                this.drawHorizontalLine(j + 1, i, k, n);
                this.drawHorizontalLine(j, i, k + 1, n);
                this.drawHorizontalLine(l, j - 1, m - 1, n);
                this.drawHorizontalLine(l, j - 1, m, n);
                this.drawHorizontalLine(l, j - 1, m + 1, n);
                this.drawVerticalLine(j - 1, m, k, n);
                this.drawVerticalLine(j + 1, m, k, n);
            } else {
                this.drawHorizontalLine(j, i, k, n);
                this.drawHorizontalLine(l, j, m, n);
                this.drawVerticalLine(j, m, k, n);
            }
        }
        
        for (BiggerAdvancementWidget advancementWidget : this.children) {
            advancementWidget.renderLines(x, y, firstPass);
        }
    }
    
    public void renderWidgets(int x, int y) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            float f = this.progress == null ? 0.0F : this.progress.getProgressBarPercentage();
            AdvancementObtainedStatus advancementObtainedStatus2;
            if (f >= 1.0F) {
                advancementObtainedStatus2 = AdvancementObtainedStatus.OBTAINED;
            } else {
                advancementObtainedStatus2 = AdvancementObtainedStatus.UNOBTAINED;
            }
            
            this.client.getTextureManager().bindTexture(WIDGETS_TEX);
            this.drawTexture(x + this.xPos + 3, y + this.yPos, this.display.getFrame().texV(), 128 + advancementObtainedStatus2.getSpriteIndex() * 26, 26, 26);
            this.client.getItemRenderer().renderGuiItem(null, this.display.getIcon(), x + this.xPos + 8, y + this.yPos + 5);
        }
        
        for (BiggerAdvancementWidget advancementWidget : this.children) {
            advancementWidget.renderWidgets(x, y);
        }
    }
    
    public void setProgress(AdvancementProgress progress) {
        this.progress = progress;
    }
    
    public void addChild(BiggerAdvancementWidget widget) {
        this.children.add(widget);
    }
    
    public void drawTooltip(int originX, int originY, float alpha, int x, int y) {
        boolean bl = x + originX + this.xPos + this.width + 26 >= this.tab.getScreen().width;
        String string = this.progress == null ? null : this.progress.getProgressBarFraction();
        int i = string == null ? 0 : this.client.textRenderer.getStringWidth(string);
        int var10000 = 113 - originY - this.yPos - 26;
        int var10002 = this.description.size();
        boolean bl2 = var10000 <= 6 + var10002 * 9;
        float f = this.progress == null ? 0.0F : this.progress.getProgressBarPercentage();
        int j = MathHelper.floor(f * (float) this.width);
        AdvancementObtainedStatus advancementObtainedStatus10;
        AdvancementObtainedStatus advancementObtainedStatus11;
        AdvancementObtainedStatus advancementObtainedStatus12;
        if (f >= 1.0F) {
            j = this.width / 2;
            advancementObtainedStatus10 = AdvancementObtainedStatus.OBTAINED;
            advancementObtainedStatus11 = AdvancementObtainedStatus.OBTAINED;
            advancementObtainedStatus12 = AdvancementObtainedStatus.OBTAINED;
        } else if (j < 2) {
            j = this.width / 2;
            advancementObtainedStatus10 = AdvancementObtainedStatus.UNOBTAINED;
            advancementObtainedStatus11 = AdvancementObtainedStatus.UNOBTAINED;
            advancementObtainedStatus12 = AdvancementObtainedStatus.UNOBTAINED;
        } else if (j > this.width - 2) {
            j = this.width / 2;
            advancementObtainedStatus10 = AdvancementObtainedStatus.OBTAINED;
            advancementObtainedStatus11 = AdvancementObtainedStatus.OBTAINED;
            advancementObtainedStatus12 = AdvancementObtainedStatus.UNOBTAINED;
        } else {
            advancementObtainedStatus10 = AdvancementObtainedStatus.OBTAINED;
            advancementObtainedStatus11 = AdvancementObtainedStatus.UNOBTAINED;
            advancementObtainedStatus12 = AdvancementObtainedStatus.UNOBTAINED;
        }
        
        int k = this.width - j;
        this.client.getTextureManager().bindTexture(WIDGETS_TEX);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        int l = originY + this.yPos;
        int n;
        if (bl) {
            n = originX + this.xPos - this.width + 26 + 6;
        } else {
            n = originX + this.xPos;
        }
        
        int var10001 = this.description.size();
        int o = 32 + var10001 * 9;
        if (!this.description.isEmpty()) {
            if (bl2) {
                this.method_2324(n, l + 26 - o, this.width, o, 10, 200, 26, 0, 52);
            } else {
                this.method_2324(n, l, this.width, o, 10, 200, 26, 0, 52);
            }
        }
        
        this.drawTexture(n, l, 0, advancementObtainedStatus10.getSpriteIndex() * 26, j, 26);
        this.drawTexture(n + j, l, 200 - k, advancementObtainedStatus11.getSpriteIndex() * 26, k, 26);
        this.drawTexture(originX + this.xPos + 3, originY + this.yPos, this.display.getFrame().texV(), 128 + advancementObtainedStatus12.getSpriteIndex() * 26, 26, 26);
        if (bl) {
            this.client.textRenderer.drawWithShadow(this.title, (float) (n + 5), (float) (originY + this.yPos + 9), -1);
            if (string != null) {
                this.client.textRenderer.drawWithShadow(string, (float) (originX + this.xPos - i), (float) (originY + this.yPos + 9), -1);
            }
        } else {
            this.client.textRenderer.drawWithShadow(this.title, (float) (originX + this.xPos + 32), (float) (originY + this.yPos + 9), -1);
            if (string != null) {
                this.client.textRenderer.drawWithShadow(string, (float) (originX + this.xPos + this.width - i - 5), (float) (originY + this.yPos + 9), -1);
            }
        }
        
        int p;
        int var10003;
        TextRenderer var20;
        String var21;
        float var22;
        if (bl2) {
            for (p = 0; p < this.description.size(); ++p) {
                var20 = this.client.textRenderer;
                var21 = this.description.get(p);
                var22 = (float) (n + 5);
                var10003 = l + 26 - o + 7;
                var20.draw(var21, var22, (float) (var10003 + p * 9), -5592406);
            }
        } else {
            for (p = 0; p < this.description.size(); ++p) {
                var20 = this.client.textRenderer;
                var21 = this.description.get(p);
                var22 = (float) (n + 5);
                var10003 = originY + this.yPos + 9 + 17;
                var20.draw(var21, var22, (float) (var10003 + p * 9), -5592406);
            }
        }
        
        this.client.getItemRenderer().renderGuiItem(null, this.display.getIcon(), originX + this.xPos + 8, originY + this.yPos + 5);
    }
    
    protected void method_2324(int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        this.drawTexture(i, j, p, q, m, m);
        this.method_2321(i + m, j, k - m - m, m, p + m, q, n - m - m, o);
        this.drawTexture(i + k - m, j, p + n - m, q, m, m);
        this.drawTexture(i, j + l - m, p, q + o - m, m, m);
        this.method_2321(i + m, j + l - m, k - m - m, m, p + m, q + o - m, n - m - m, o);
        this.drawTexture(i + k - m, j + l - m, p + n - m, q + o - m, m, m);
        this.method_2321(i, j + m, m, l - m - m, p, q + m, n, o - m - m);
        this.method_2321(i + m, j + m, k - m - m, l - m - m, p + m, q + m, n - m - m, o - m - m);
        this.method_2321(i + k - m, j + m, m, l - m - m, p + n - m, q + m, n, o - m - m);
    }
    
    protected void method_2321(int i, int j, int k, int l, int m, int n, int o, int p) {
        for (int q = 0; q < k; q += o) {
            int r = i + q;
            int s = Math.min(o, k - q);
            
            for (int t = 0; t < l; t += p) {
                int u = j + t;
                int v = Math.min(p, l - t);
                this.drawTexture(r, u, m, n, s, v);
            }
        }
        
    }
    
    public boolean shouldRender(int originX, int originY, int mouseX, int mouseY) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            int i = originX + this.xPos;
            int j = i + 26;
            int k = originY + this.yPos;
            int l = k + 26;
            return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
        } else {
            return false;
        }
    }
    
    public void addToTree() {
        if (this.parent == null && this.advancement.getParent() != null) {
            this.parent = this.getParent(this.advancement);
            if (this.parent != null) {
                this.parent.addChild(this);
            }
        }
        
    }
    
    public int getY() {
        return this.yPos;
    }
    
    public int getX() {
        return this.xPos;
    }
}