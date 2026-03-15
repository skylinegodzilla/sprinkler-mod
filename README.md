# Sprinkler Mod

A tiered sprinkler system for Minecraft 1.21.1 that accelerates plant growth using gravity-fed water gutters.

---

## What it does

Sprinklers hang below overhead gutters and spray water down onto crops, applying growth ticks to any plant within their coverage area. Supply the gutters with water and your farms grow faster. Supply them with fertilised water and they grow *very* fast.

The system is gravity fed — a tank catches flowing water at the top, gutters carry it horizontally overhead, and sprinklers hang below them. No pumps, no redstone, just water flowing downhill the way it should.

---

## Sprinkler Tiers

| Tier | Coverage Area | Notes |
|------|--------------|-------|
| Copper | 3x3 | Entry tier |
| Iron | 5x5 | |
| Gold | 7x7 | |
| Diamond | 9x9 | Also known as the Smallish Beans Sprinkler |
| Netherite | 11x11 | |

---

## Fluids

| Fluid | Growth Speed | How to Obtain |
|-------|-------------|---------------|
| Water | Noticeably faster than vanilla | Vanilla water source |
| Fertilised Water | Very fast | Drop bonemeal into a water source *(coming soon)* |

Fertilised water cannot form an infinite source — you need to maintain supply.

---

## How to set up a farm

1. Place a **tank** next to a water source — it fills automatically *(coming soon)*
2. Run **gutters** horizontally from the tank overhead across your farm
3. Gutters can **waterfall** down to lower gutters by stacking them vertically
4. Hang a **sprinkler** below the gutter line
5. Plant your crops underneath
6. Watch them grow

> Each tank feeds one line of gutters. For multiple rows you need multiple tanks.

---

## How sprinklers work

- The sprinkler searches **10 blocks downward** through air for plants
- Any `BonemealableBlock` plant is affected — wheat, carrots, potatoes, saplings, sugarcane, and more
- This includes plants added by other mods that implement the bonemeal interface
- The sprinkler does **nothing** if it has no fluid supply — no cheating by placing sprinklers without gutters

---

## Particles

Each active sprinkler shows two layers of particles:
- A **spinning cloud ring** at the sprinkler head showing it is active
- **Coloured dots** on the ground surface showing the exact coverage area
  - Water — cyan at centre fading to deep blue at edges
  - Fertilised water — yellow at centre fading to deep green at edges *(coming soon)*

---

## Crafting *(coming soon)*

Recipes are not yet implemented. Use `/give` to obtain items for now:

```
/give @p sprinklermod:copper_sprinkler
/give @p sprinklermod:iron_sprinkler
/give @p sprinklermod:gold_sprinkler
/give @p sprinklermod:diamond_sprinkler
/give @p sprinklermod:netherite_sprinkler
/give @p sprinklermod:gutter
```

---

## Installation

### Requirements
- Minecraft 1.21.1
- NeoForge 21.1.x

### Steps
1. Download the mod `.jar` file
2. Place it in your Minecraft `mods/` folder
3. Launch Minecraft with NeoForge 21.1.x

---

## Coming Soon

- Fertilised water fluid
- Tank block
- Crafting recipes for all blocks
- Custom models and textures
- Creative tab

---

## Development

### Requirements
- Java 21 (Temurin recommended)
- IntelliJ IDEA
- NeoForge MDK (ModDevGradle)

### Setup
```bash
git clone https://github.com/skylinegodzilla/sprinkler-mod.git
cd sprinkler-mod
./gradlew neoForgeIdeSync
```

Then open in IntelliJ and run the `runClient` configuration.

### Project Structure

```
src/main/java/com/benca/sprinklermod/
├── SprinklerMod.java           # Main entry point — assembles modules
├── SprinklerModClient.java     # Client side setup
├── growth/                     # Pure logic, no Minecraft API
│   ├── SprinklerTier.java      # Tier enum — radius and display names
│   ├── GrowthHandler.java      # Fluid to growth multiplier map
│   ├── GrowthArea.java         # Coverage area calculator
│   └── PlantGrowthTicker.java  # Applies growth ticks to plants
├── block/                      # Physical blocks
│   ├── BlockRegistry.java      # Registers all blocks
│   ├── SprinklerBlock.java     # Sprinkler block logic
│   └── GutterBlock.java        # Gutter block
├── blockentity/                # Stateful block data
│   ├── BlockEntityRegistry.java
│   └── GutterBlockEntity.java  # Fluid propagation chain
├── item/
│   └── ItemRegistry.java       # Registers all block items
└── client/                     # Visual effects only
    ├── SprinklerParticleHandler.java
    └── SprinklerParticleColour.java
```

### Running Tests

The `growth/` package is pure Java and can be tested without launching Minecraft:

```
src/test/java/com/benca/sprinklermod/growth/GrowthModuleTest.java
```

Run it directly from IntelliJ — all growth logic is verified without launching the game.

### Adding a New Sprinkler Tier

1. Add an entry to `SprinklerTier.java`
2. Add a block in `BlockRegistry.java`
3. Add a block item in `ItemRegistry.java`
4. Add blockstate, model, and language file entries
5. Add a crafting recipe

Nothing else needs to change — the growth and particle systems pick up new tiers automatically.

### Adding a New Fluid

1. Register the fluid in `FluidRegistry.java` *(coming soon)*
2. Add a multiplier constant in `GrowthHandler.java`
3. Add a case in `GrowthHandler.getMultiplier()`
4. Add colour definitions in `SprinklerParticleColour.java`

---

## Known Issues

- Sprinklers currently always use water even without a gutter connected — this will be fixed when `SprinklerBlockEntity` is built
- Recipes not yet implemented
- Placeholder textures — custom models coming when assets are finalised in Blender

---

## License

All Rights Reserved — personal project, not currently open for redistribution.

---

*Minecraft 1.21.1 — NeoForge 21.1.819 — Built with ModDevGradle*
