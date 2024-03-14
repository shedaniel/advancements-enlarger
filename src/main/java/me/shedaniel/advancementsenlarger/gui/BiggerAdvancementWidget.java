/*
 * Advancements Enlarger by shedaniel.
 * Licensed under the CC-BY-NC-4.0.
 */

package me.shedaniel.advancementsenlarger.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class BiggerAdvancementWidget {
    private static final int[] field_24262 = new int[]{0, 10, -10, 25, -25};

    private static final Identifier TITLE_BOX_TEXTURE = new Identifier("advancements/title_box");
    //private static final Identifier WIDGETS_TEX = new Identifier("textures/gui/advancements/window.png");
    private static final Pattern BACKSLASH_S_PATTERN = Pattern.compile("(.+) \\S+");
    private final BiggerAdvancementTab tab;
    private final PlacedAdvancement advancement;
    private final AdvancementDisplay display;
    private final OrderedText title;
    private final int width;
    private final List<OrderedText> description;
    private final MinecraftClient client;
    private final List<BiggerAdvancementWidget> children = Lists.newArrayList();
    private final int xPos;
    private final int yPos;
    private BiggerAdvancementWidget parent;
    private AdvancementProgress progress;

    public BiggerAdvancementWidget(BiggerAdvancementTab tab, MinecraftClient client, PlacedAdvancement advancement, AdvancementDisplay display) {
        this.tab = tab;
        this.advancement = advancement;
        this.display = display;
        this.client = client;
        this.title = Language.getInstance().reorder(client.textRenderer.trimToWidth(display.getTitle(), 163));
        this.xPos = MathHelper.floor(display.getX() * 28.0F);
        this.yPos = MathHelper.floor(display.getY() * 27.0F);
        int i = advancement.getAdvancement().requirements().getLength();
        int j = String.valueOf(i).length();
        int k = i > 1 ? client.textRenderer.getWidth("  ") + client.textRenderer.getWidth("0") * j * 2 + client.textRenderer.getWidth("/") : 0;
        int l = 29 + client.textRenderer.getWidth(this.title) + k;
        Text description = display.getDescription();
        this.description = Language.getInstance().reorder(this.wrapDescription(description, l));

        OrderedText string2;
        for (Iterator<OrderedText> var10 = this.description.iterator(); var10.hasNext(); l = Math.max(l, client.textRenderer.getWidth(string2))) {
            string2 = var10.next();
        }

        this.width = l + 3 + 5;
    }

    private List<StringVisitable> wrapDescription(Text text, int width) {
        TextHandler textHandler = this.client.textRenderer.getTextHandler();
        List<StringVisitable> list = null;
        float f = Float.MAX_VALUE;
        int[] var6 = field_24262;
        int var7 = var6.length;

        for (int i : var6) {
            List<StringVisitable> list2 = textHandler.wrapLines(text, width - i, Style.EMPTY);
            float g = Math.abs(method_27572(textHandler, list2) - (float) width);
            if (g <= 10.0F) {
                return list2;
            }

            if (g < f) {
                f = g;
                list = list2;
            }
        }

        return list;
    }

    private static float method_27572(TextHandler textHandler, List<StringVisitable> list) {
        Stream<StringVisitable> var10000 = list.stream();
        return (float) var10000.mapToDouble(textHandler::getWidth).max().orElse(0.0D);
    }

    private BiggerAdvancementWidget getParent(PlacedAdvancement advancement) {
        do {
            advancement = advancement.getParent();
        } while (advancement != null && advancement.getAdvancement().display().isEmpty());

        if (advancement != null) {
            return this.tab.getWidget(advancement.getAdvancementEntry());
        } else {
            return null;
        }
    }

    public void renderLines(DrawContext context, int x, int y, boolean firstPass) {
        if (this.parent != null) {
            int i = x + this.parent.xPos + 13;
            int j = x + this.parent.xPos + 26 + 4;
            int k = y + this.parent.yPos + 13;
            int l = x + this.xPos + 13;
            int m = y + this.yPos + 13;
            int n = firstPass ? -16777216 : -1;
            if (firstPass) {
                context.drawHorizontalLine(j, i, k - 1, n);
                context.drawHorizontalLine(j + 1, i, k, n);
                context.drawHorizontalLine(j, i, k + 1, n);
                context.drawHorizontalLine(l, j - 1, m - 1, n);
                context.drawHorizontalLine(l, j - 1, m, n);
                context.drawHorizontalLine(l, j - 1, m + 1, n);
                context.drawVerticalLine(j - 1, m, k, n);
                context.drawVerticalLine(j + 1, m, k, n);
            } else {
                context.drawHorizontalLine(j, i, k, n);
                context.drawHorizontalLine(l, j, m, n);
                context.drawVerticalLine(j, m, k, n);
            }
        }

        for (BiggerAdvancementWidget advancementWidget : this.children) {
            advancementWidget.renderLines(context, x, y, firstPass);
        }
    }

    public void renderWidgets(DrawContext context, int x, int y) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            float f = this.progress == null ? 0.0F : this.progress.getProgressBarPercentage();
            AdvancementObtainedStatus advancementObtainedStatus2;
            if (f >= 1.0F) {
                advancementObtainedStatus2 = AdvancementObtainedStatus.OBTAINED;
            } else {
                advancementObtainedStatus2 = AdvancementObtainedStatus.UNOBTAINED;
            }
            context.drawGuiTexture(advancementObtainedStatus2.getFrameTexture(this.display.getFrame()), x + this.xPos + 3, y + this.yPos, 26, 26);
            context.drawItemWithoutEntity(this.display.getIcon(), x + this.xPos + 8, y + this.yPos + 5);
            /*context.drawTexture(WIDGETS_TEX, x + this.xPos + 3, y + this.yPos, advancementObtainedStatus2.getFrameTexture(this.display.getFrame()), 128 + advancementObtainedStatus2.getSpriteIndex() * 26, 26, 26);
            context.drawItemWithoutEntity(this.display.getIcon(), x + this.xPos + 8, y + this.yPos + 5);*/
        }

        for (BiggerAdvancementWidget advancementWidget : this.children) {
            advancementWidget.renderWidgets(context, x, y);
        }
    }

    public void setProgress(AdvancementProgress progress) {
        this.progress = progress;
    }

    public void addChild(BiggerAdvancementWidget widget) {
        this.children.add(widget);
    }

    public void drawTooltip(DrawContext context, int originX, int originY, float alpha, int x, int y) {
        boolean bl = x + originX + this.xPos + this.width + 26 >= this.tab.getScreen().width;
        Text text = this.progress == null ? null : this.progress.getProgressBarFraction();
        int i = text == null ? 0 : this.client.textRenderer.getWidth(text);
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
        //RenderSystem.setShaderTexture(0, WIDGETS_TEX);
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        int l = originY + this.yPos;
        int m;
        if (bl) {
            m = originX + this.xPos - this.width + 26 + 6;
        } else {
            m = originX + this.xPos;
        }

        int var10001 = this.description.size();
        int n = 32 + var10001 * 9;
        if (!this.description.isEmpty()) {
            if (bl2) {
                context.drawGuiTexture(TITLE_BOX_TEXTURE, m, l + 26 - n, this.width, n);
                //this.method_2324(context, WIDGETS_TEX, n, l + 26 - o, this.width, o, 10, 200, 26, 0, 52);
            } else {
                context.drawGuiTexture(TITLE_BOX_TEXTURE, m, l, this.width, n);
                //this.method_2324(context, WIDGETS_TEX, n, l, this.width, o, 10, 200, 26, 0, 52);
            }
        }
        context.drawGuiTexture(advancementObtainedStatus10.getBoxTexture(), 200, 26, 0, 0, m, l, j, 26);
        context.drawGuiTexture(advancementObtainedStatus11.getBoxTexture(), 200, 26, 200 - k, 0, m + j, l, k, 26);
        context.drawGuiTexture(advancementObtainedStatus12.getFrameTexture(this.display.getFrame()), originX + this.xPos + 3, originY + this.yPos, 26, 26);
        if (bl) {
            context.drawTextWithShadow(this.client.textRenderer, this.title, m + 5, originY + this.yPos + 9, -1);
            if (text != null) {
                context.drawTextWithShadow(this.client.textRenderer, text, originX + this.xPos - i, originY + this.yPos + 9, -1);
            }
        } else {
            context.drawTextWithShadow(this.client.textRenderer, this.title, originX + this.xPos + 32, originY + this.yPos + 9, -1);
            if (text != null) {
                context.drawTextWithShadow(this.client.textRenderer, text, originX + this.xPos + this.width - i - 5, originY + this.yPos + 9, -1);
            }
        }

        int p;
        int var10003;
        TextRenderer var20;
        OrderedText var21;
        int var22;
        if (bl2) {
            for (p = 0; p < this.description.size(); ++p) {
                var20 = this.client.textRenderer;
                var21 = this.description.get(p);
                var22 = m + 5;
                var10003 = l + 26 - n + 7;
                context.drawText(var20, var21, var22, var10003 + p * 9, -5592406, false);
            }
        } else {
            for (p = 0; p < this.description.size(); ++p) {
                var20 = this.client.textRenderer;
                var21 = this.description.get(p);
                var22 = m + 5;
                var10003 = originY + this.yPos + 9 + 17;
                context.drawText(var20, var21, var22, var10003 + p * 9, -5592406, false);
            }
        }

        context.drawItemWithoutEntity(this.display.getIcon(), originX + this.xPos + 8, originY + this.yPos + 5);
    }

    protected void method_2324(DrawContext context, Identifier texture, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        context.drawTexture(texture, i, j, p, q, m, m);
        this.method_2321(context, texture, i + m, j, k - m - m, m, p + m, q, n - m - m, o);
        context.drawTexture(texture, i + k - m, j, p + n - m, q, m, m);
        context.drawTexture(texture, i, j + l - m, p, q + o - m, m, m);
        this.method_2321(context, texture, i + m, j + l - m, k - m - m, m, p + m, q + o - m, n - m - m, o);
        context.drawTexture(texture, i + k - m, j + l - m, p + n - m, q + o - m, m, m);
        this.method_2321(context, texture, i, j + m, m, l - m - m, p, q + m, n, o - m - m);
        this.method_2321(context, texture, i + m, j + m, k - m - m, l - m - m, p + m, q + m, n - m - m, o - m - m);
        this.method_2321(context, texture, i + k - m, j + m, m, l - m - m, p + n - m, q + m, n, o - m - m);
    }

    protected void method_2321(DrawContext context, Identifier texture, int i, int j, int k, int l, int m, int n, int o, int p) {
        for (int q = 0; q < k; q += o) {
            int r = i + q;
            int s = Math.min(o, k - q);

            for (int t = 0; t < l; t += p) {
                int u = j + t;
                int v = Math.min(p, l - t);
                context.drawTexture(texture, r, u, m, n, s, v);
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
