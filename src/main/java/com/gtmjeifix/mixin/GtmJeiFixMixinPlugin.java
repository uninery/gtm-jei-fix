/*
 * Copyright (C) 2026 Uninery
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gtmjeifix.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Minimal mixin config plugin: it only reports whether the patch was applied, so a future JEI refactor
 * that moves {@code RecipeSlotBuilder#setCustomRenderer} is visible in the log instead of failing silently.
 *
 * <p>The config is marked {@code "required": false}, therefore a failed application is a warning and never
 * a crash. The mod is a no-op in that case.</p>
 */
public final class GtmJeiFixMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("GTM JEI Fix");

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("[GTM JEI Fix] Loading mixin config for package {}", mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // no target restrictions
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // nothing to do
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        LOGGER.info("[GTM JEI Fix] Patch applied: {} -> {}", mixinClassName, targetClassName);
    }
}
