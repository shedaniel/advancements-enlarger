/*
 * Advancements Enlarger by shedaniel.
 * Licensed under the CC-BY-NC-4.0.
 */

package me.shedaniel.advancementsenlarger.hooks;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;

public interface AdvancementTabTypeHooks {
    void ae_drawBackground(DrawableHelper drawable, int x, int y, boolean selected, int index);
    
    void ae_drawIcon(int x, int y, int index, ItemRenderer itemRenderer, ItemStack icon);
    
    boolean ae_isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY);
}
