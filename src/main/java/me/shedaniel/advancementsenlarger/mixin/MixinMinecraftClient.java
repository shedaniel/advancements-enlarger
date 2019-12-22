/*
 * Advancements Enlarger by shedaniel.
 * Licensed under the CC-BY-NC-4.0.
 */

package me.shedaniel.advancementsenlarger.mixin;

import me.shedaniel.advancementsenlarger.gui.BiggerAdvancementsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Code taken from FabricControlling because I am too lazy to write it.
 * Source: https://github.com/jaredlll08/Fabric-Controlling/blob/master/LICENSE
 * Licensed under MIT by jaredlll08.
 */
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow @Nullable public ClientPlayerEntity player;
    
    @Inject(method = "openScreen", at = @At("HEAD"))
    private void dummyGenerateRefmap(Screen screen, CallbackInfo ci) {
        // NO-OP this injection is only here to generate the refmap
    }
    
    @ModifyVariable(method = "openScreen", at = @At("HEAD"), argsOnly = true)
    private Screen openScreen(Screen screen) {
        if (screen != null && AdvancementsScreen.class == screen.getClass())
            return new BiggerAdvancementsScreen(player.networkHandler.getAdvancementHandler(), (AdvancementsScreen) screen);
        return screen;
    }
}
