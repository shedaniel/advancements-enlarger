/*
 * Advancements Enlarger by shedaniel.
 * Licensed under the CC-BY-NC-4.0.
 */

package me.shedaniel.advancementsenlarger.hooks;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface AdvancementTabTypeHooks {
    void ae_drawBackground(DrawContext context, int x, int y, boolean selected, int index);
    
    void ae_drawIcon(DrawContext context, int x, int y, int index, ItemStack icon);
    
    boolean ae_isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY);
}
