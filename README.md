# GTM JEI Fix

Minecraft 1.20.1 · Forge 47.x · JEI ≥ 15.55.0.201

修复 GTCEu 配方在 JEI 中的物品提示只剩模组名的问题。

## 问题

JEI 15.55.0.201 起改用新的富文本重载生成槽位提示：

```java
void getTooltip(ITooltipBuilder tooltip, T ingredient, @Nullable Player player, TooltipFlag flag)
```

它的默认实现**不会**回退到旧的 3 参富文本，而是走返回 `List<Component>` 的普通重载。LDLib 的槽位渲染器（GTCEu 每个配方类目都用它）只实现了旧重载，普通重载返回空列表，于是提示为空，JEI 只剩下一行模组名。

## 修复

Mixin 注入 `RecipeSlotBuilder#setCustomRenderer`——每个配方类目安装自定义槽位渲染器都会调用的公开 API 方法。对只声明了旧 3 参富文本的渲染器包一层 `LegacyTooltipRendererAdapter`，把新的 4 参调用转回旧 3 参；渲染、尺寸、字体、普通提示均原样转发。已实现新 API 的渲染器直接放行，不会重复添加提示。

钩的是公开 API 而不是 JEI 内部实现，所以 JEI 频繁重构时补丁依然有效；万一将来类名变了，Mixin 会自行跳过（`"required": false`），不影响启动。

## 依赖

- Minecraft 1.20.1、Forge 47.x
- JEI 15.55.0.201 及以上
- GTCEu，或任何用 LDLib 构建 JEI 配方槽位的模组

已在 JEI 15.55.0.201、15.74.0.229、15.74.0.234 上核对过注入点。

## 安装

把 jar 放进 `mods/` 即可，无需配置。生效时日志中会出现以 `[GTM JEI Fix]` 开头的行。

## 构建

```
./gradlew build
```

产物在 `build/libs/`。JEI API 从 maven.blamejared.com 解析，首次构建需联网。

## 许可

GPL-3.0-or-later（GNU GPLv3）© 2026 Uninery，见 [LICENSE](LICENSE)。
