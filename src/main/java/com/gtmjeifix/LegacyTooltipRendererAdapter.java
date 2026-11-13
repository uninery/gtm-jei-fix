/*
 * Copyright (C) 2026 Uninery
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gtmjeifix;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

/**
 * Adapter for ingredient renderers that were written against JEI &lt; 15.54.0 and therefore only
 * implement the legacy rich tooltip method {@code getTooltip(ITooltipBuilder, T, TooltipFlag)}.
 *
 * <p>JEI 15.55.0.201 changed the tooltip pipeline to call the new 4-argument overload
 * {@code getTooltip(ITooltipBuilder, T, Player, TooltipFlag)}. Its default implementation does
 * <em>not</em> delegate to the legacy rich overload (it goes through the plain
 * {@code getTooltip(T, Player, TooltipFlag)} / {@code getTooltip(T, TooltipFlag)} methods instead),
 * so legacy renderers silently produce an empty tooltip.</p>
 *
 * <p>This adapter keeps the delegate's rendering behaviour untouched and only redirects the new rich
 * call to the legacy rich call, i.e. it restores exactly the behaviour of JEI 15.49.0.188 and older.</p>
 */
@SuppressWarnings("removal")
public final class LegacyTooltipRendererAdapter<T> implements IIngredientRenderer<T> {

    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    /**
     * Caches "this renderer class only implements the legacy rich tooltip overload" per class.
     * {@link ClassValue} is automatically cleared when a class is unloaded (i.e. on resource reload).
     */
    private static final ClassValue<Boolean> LEGACY_RICH_ONLY = new ClassValue<Boolean>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            return Boolean.valueOf(
                // the legacy rich overload is overridden by the renderer ...
                declares(type, "getTooltip", ITooltipBuilder.class, Object.class, TooltipFlag.class)
                    // ... and the new overloads are not
                    && !declares(type, "getTooltip", ITooltipBuilder.class, Object.class, Player.class, TooltipFlag.class)
                    && !declares(type, "getTooltip", Object.class, Player.class, TooltipFlag.class)
            );
        }
    };

    private final IIngredientRenderer<T> delegate;

    private LegacyTooltipRendererAdapter(IIngredientRenderer<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns a renderer that behaves like the given renderer on JEI &gt;= 15.55.0.201. Renderers that
     * already implement the new API are returned unchanged, so behaviour never changes for them.
     */
    public static <R> IIngredientRenderer<R> wrap(IIngredientRenderer<R> renderer) {
        if (renderer == null || renderer instanceof LegacyTooltipRendererAdapter<?>) {
            return renderer;
        }
        if (!isLegacyRichOnly(renderer.getClass())) {
            return renderer;
        }
        if (LOGGED.compareAndSet(false, true)) {
            GtmJeiFix.LOGGER.info(
                "[GTM JEI Fix] Restoring rich tooltips for legacy ingredient renderer {}",
                renderer.getClass().getName()
            );
        }
        return new LegacyTooltipRendererAdapter<>(renderer);
    }

    public static boolean isLegacyRichOnly(Class<?> rendererClass) {
        return LEGACY_RICH_ONLY.get(rendererClass).booleanValue();
    }

    private static boolean declares(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            Method method = type.getMethod(name, parameterTypes);
            return method.getDeclaringClass() != IIngredientRenderer.class;
        } catch (NoSuchMethodException absent) {
            return false;
        } catch (Throwable unexpected) {
            // a class that cannot be inspected is treated as "not legacy", i.e. left alone
            GtmJeiFix.LOGGER.warn("[GTM JEI Fix] Could not inspect ingredient renderer {}", type.getName(), unexpected);
            return false;
        }
    }

    // ------------------------------------------------------------------
    // rendering - pure delegation
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphics guiGraphics, T ingredient) {
        delegate.render(guiGraphics, ingredient);
    }

    @Override
    public void render(GuiGraphics guiGraphics, T ingredient, int posX, int posY) {
        delegate.render(guiGraphics, ingredient, posX, posY);
    }

    @Override
    public void renderBatch(GuiGraphics guiGraphics, List<BatchRenderElement<T>> elements) {
        delegate.renderBatch(guiGraphics, elements);
    }

    @Override
    public Font getFontRenderer(Minecraft minecraft, T ingredient) {
        return delegate.getFontRenderer(minecraft, ingredient);
    }

    @Override
    public int getWidth() {
        return delegate.getWidth();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    // ------------------------------------------------------------------
    // plain tooltips - pure delegation (used by JEI for searching)
    // ------------------------------------------------------------------

    @Override
    public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
        return delegate.getTooltip(ingredient, tooltipFlag);
    }

    @Override
    public List<Component> getTooltip(T ingredient, Player player, TooltipFlag tooltipFlag) {
        return delegate.getTooltip(ingredient, player, tooltipFlag);
    }

    // ------------------------------------------------------------------
    // rich tooltips
    // ------------------------------------------------------------------

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T ingredient, TooltipFlag tooltipFlag) {
        delegate.getTooltip(tooltip, ingredient, tooltipFlag);
    }

    /**
     * The method JEI 15.55.0.201+ actually calls. Route it back to the legacy rich overload, which is
     * what JEI used to call before 15.55.0.201.
     */
    @Override
    public void getTooltip(ITooltipBuilder tooltip, T ingredient, Player player, TooltipFlag tooltipFlag) {
        delegate.getTooltip(tooltip, ingredient, tooltipFlag);
    }
}
