/*
 * Copyright (C) 2026 Uninery
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gtmjeifix;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GTM JEI Fix.
 *
 * <p>Since JEI 15.55.0.201 the ingredient tooltip of every recipe slot is built by calling the
 * <em>new</em> rich tooltip method {@code IIngredientRenderer#getTooltip(ITooltipBuilder, T, Player, TooltipFlag)}
 * that was added in 15.54.0. Renderers that were compiled against an older JEI only override the
 * <em>legacy</em> rich overload {@code getTooltip(ITooltipBuilder, T, TooltipFlag)}. The new default
 * implementation delegates to the plain (list based) methods instead, so such a renderer contributes
 * absolutely nothing to the tooltip - JEI then only appends the mod name.</p>
 *
 * <p>LDLib (used by GregTech Modern for every recipe category) registers exactly such a renderer for
 * each recipe slot, which is why GT recipe ingredients only showed their mod name.</p>
 *
 * <p>This mod wraps those legacy renderers inside
 * {@code mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder#setCustomRenderer} and routes the
 * new 4-argument call back to the legacy 3-argument overload. See {@link LegacyTooltipRendererAdapter}
 * and {@code com.gtmjeifix.mixin.RecipeSlotBuilderMixin}.</p>
 */
@Mod(GtmJeiFix.MOD_ID)
public final class GtmJeiFix {

    public static final String MOD_ID = "gtmjeifix";
    public static final Logger LOGGER = LoggerFactory.getLogger("GTM JEI Fix");

    public GtmJeiFix() {
        LOGGER.info("GTM JEI Fix loaded - restoring recipe slot tooltips for legacy ingredient renderers (GTCEu / LDLib).");
        logModVersion("jei");
        logModVersion("ldlib");
        logModVersion("gtceu");
    }

    private static void logModVersion(String modId) {
        try {
            ModList.get().getModContainerById(modId).ifPresent(container -> LOGGER.info(
                "  {} {}", modId, container.getModInfo().getVersion()
            ));
        } catch (Throwable ignored) {
            // never let a diagnostic break mod loading
        }
    }
}
