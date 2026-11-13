/*
 * Copyright (C) 2026 Uninery
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gtmjeifix.mixin;

import com.gtmjeifix.LegacyTooltipRendererAdapter;
import mezz.jei.api.ingredients.IIngredientRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Wraps every custom ingredient renderer that a mod registers for a recipe slot.
 *
 * <p>{@code RecipeSlotBuilder#setCustomRenderer} is the implementation of the public JEI API method
 * {@code IRecipeSlotBuilder#setCustomRenderer}, so hooking here catches LDLib (GregTech Modern) and any
 * other mod that relies on the legacy rich tooltip overload, while only depending on one JEI internal
 * class name.</p>
 *
 * <p>The target method looks like this in JEI 15.55.0.201 ... 15.74.0.234:</p>
 * <pre>
 * public &lt;T&gt; IRecipeSlotBuilder setCustomRenderer(IIngredientType&lt;T&gt; ingredientType, IIngredientRenderer&lt;T&gt; ingredientRenderer)
 * </pre>
 * <p>index 0 = this, 1 = ingredientType, 2 = ingredientRenderer.</p>
 */
@Mixin(targets = "mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder", remap = false)
public abstract class RecipeSlotBuilderMixin {

    @ModifyVariable(
        method = "setCustomRenderer",
        at = @At("HEAD"),
        argsOnly = true,
        index = 2,
        remap = false
    )
    private IIngredientRenderer<?> gtmjeifix$wrapLegacyIngredientRenderer(IIngredientRenderer<?> ingredientRenderer) {
        return LegacyTooltipRendererAdapter.wrap(ingredientRenderer);
    }
}
