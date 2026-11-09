# MasterCrafter – Playable Resources

This directory contains the filtered, self-contained Minecraft 26.3 resource set used by MasterCrafter.

## Directory structure

```text
playable/resources/
├── items.json          # All playable item IDs
├── item-tags.json      # Filtered item tags
├── recipes/            # Playable crafting recipes
├── item-icons/         # Original Minecraft 16×16 PNGs
├── render-icons/       # Block Render 64×64 PNGs
└── lang/
    ├── en_us.json      # English item names
    └── sv_se.json      # Swedish item names
```

## Items and icons

`items.json` contains all item IDs referenced by playable recipes, including items reachable through ingredient tags.

Every listed item has an icon in exactly one of these locations:

- `item-icons/<namespace>/<name>.png` – original Minecraft texture (16×16).
- `render-icons/<namespace>/<name>.png` – rendered inventory icon (64×64).

For example, `minecraft:acacia_boat` corresponds to `<namespace> = minecraft` and `<name> = acacia_boat`.

Render both sizes using nearest-neighbor scaling to preserve Minecraft's pixel-art appearance.

## Recipes

`recipes/` contains the original Minecraft JSON recipe definitions, filtered to include only recipes that can be represented using available items.

Supported recipe types:

- `minecraft:crafting_shaped` – ingredients occupy a pattern within the crafting grid. Patterns may be translated or mirrored.
- `minecraft:crafting_shapeless` – ingredient positions do not matter.

Other recipe types, including special crafting recipes, are excluded.

Recipes retain Minecraft's original format, including `pattern`, `key`, `ingredients`, `result`, and `group` fields.

Multiple recipes may produce the same result item. Do not assume a one-to-one relationship between recipes and items.

## Item tags

`item-tags.json` maps tag IDs to arrays of concrete item IDs:

```json
{
  "#minecraft:planks": [
    "minecraft:acacia_planks",
    "minecraft:birch_planks"
  ]
}
```

Tags have already been expanded recursively and filtered to include only items with available icons. Empty tags are removed.

A recipe ingredient such as `#minecraft:planks` accepts **any item in that tag**.

Do not expand a tag into multiple mandatory ingredients; its members are alternatives.

## Localized names

`lang/en_us.json` and `lang/sv_se.json` map item IDs to localized display names:

```json
{
  "minecraft:acacia_boat": "Acacia Boat"
}
```

The JSON keys are canonical Minecraft item IDs, not native Minecraft translation keys.

Use the selected language file for display text. Always use item IDs internally for recipe matching and game logic.

## Consistency guarantees

The build script ensures that:

1. Every item in `items.json` has an available PNG icon.
2. Every recipe result and explicit ingredient has an icon.
3. Every referenced ingredient tag exists and contains at least one available item.
4. Every member of every included tag has an icon.
5. Only supported crafting recipes are included.
6. Only items used by the included recipes, directly or through tags, are retained.

Language files contain only relevant item entries. Missing translations, if any, are reported by the build script.

## Important implementation notes

- Use these generated resources as the authoritative dataset for the game.
- Do not load recipes or icons from the unfiltered source directories.
- Use item IDs for all comparisons; names are presentation-only.
- Do not assume all Minecraft 26.3 recipes or items are available.
- A recipe excluded because of missing icons must not appear as a playable challenge.
