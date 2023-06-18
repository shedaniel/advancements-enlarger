/*
 * Advancements Enlarger by shedaniel.
 * Licensed under the CC-BY-NC-4.0.
 */

package me.shedaniel.advancementsenlarger.mixin;

import me.shedaniel.advancementsenlarger.hooks.AdvancementTabTypeHooks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screen.advancement.AdvancementTabType")
public abstract class MixinAdvancementTabType implements AdvancementTabTypeHooks {

    @Shadow
    @Final
    private int u;

    @Shadow
    @Final
    private int width;

    @Shadow
    @Final
    private int tabCount;

    @Shadow
    @Final
    private int v;

    @Shadow
    @Final
    private int height;

    @Override
    public void ae_drawBackground(DrawContext context, Identifier texture, int x, int y, boolean selected, int index) {
        int i = this.u;
        if (index > 0) {
            i += this.width;
        }

        if (index == this.tabCount - 1) {
            i += this.width;
        }

        int j = selected ? this.v + this.height : this.v;
        context.drawTexture(texture, x + (this.width + 2) * index, y + -this.height + 4, i, j, this.width, this.height);
    }

    @Override
    public void ae_drawIcon(DrawContext context, int x, int y, int index, ItemStack icon) {
        int i = x + (this.width + 2) * index + 6;
        int j = y + -this.height + 4 + 9;
        Vector4f vector4f = new Vector4f(i, j, 0, 1.0F);
        context.drawItemWithoutEntity(icon, i, j);
    }

    @Override
    public boolean ae_isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
        int i = screenX + (this.width + 2) * index;
        int j = screenY + -this.height + 4;
        return mouseX > (double) i && mouseX < (double) (i + this.width) && mouseY > (double) j && mouseY < (double) (j + this.height);
    }
}
