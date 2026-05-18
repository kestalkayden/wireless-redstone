# Wireless Redstone

Wireless redstone transmitters and receivers for Minecraft 26.1.x. Pair them by channel + frequency, one transmitter feeds as many receivers as you want — up to 128 blocks away by default, configurable.

## Requirements

- Minecraft **26.1.x**
- Java **25**
- Fabric Loader **0.18.4+** with **Fabric API**, *or* NeoForge **26.1+**

## Downloads

- Modrinth: <https://modrinth.com/mod/wireless-redstone-lite>
- CurseForge: <https://www.curseforge.com/minecraft/mc-mods/wireless-redstone-lite>

## What's in it

- **Transmitter** — takes a redstone input on any side, broadcasts on its channel.
- **Receiver** — outputs the strongest signal it receives on its channel, like a redstone block.
- **Channel** (1–128) and **Frequency** (0–65535) — pick any pair to keep your networks separate.
- **Lock** toggle — freezes the channel/frequency so you can't bump them while sneak-clicking.
- **Private** toggle — binds the pair to your player UUID, so two players can use the same channel without crossing wires.
- **Item tooltips** show channel/frequency/lock/private state without placing the block.
- **Jade compat** — install [Jade](https://modrinth.com/mod/jade) for in-world tooltips showing channel, frequency, lock, private, and manual mode when you point at a placed block. Optional; mod works fine without it.

## How it works

1. Craft a transmitter and a receiver (recipes below).
2. Place the transmitter — feed it redstone from any side.
3. Place the receiver anywhere in the same dimension, within range.
4. Right-click either block to open the config screen and match channel + frequency.
5. The receiver mirrors whatever signal strength the transmitter sees.

Multiple transmitters on the same channel? The receiver outputs the strongest. Multiple receivers? They all output the same strength. No cross-dimension signalling.

**Manual override**: sneak-right-click a transmitter to cycle between **Toggle** (default; responds to redstone input), **Always On** (broadcasts full signal regardless of input), and **Always Off** (broadcasts nothing). Use Always On as a wireless lever, Always Off as a quick kill switch.

## Recipes

**Transmitter** (4 iron + 3 redstone + 1 ender pearl + 1 redstone block)

```
R R R
I E I
I B I

R = redstone   I = iron ingot
E = ender pearl   B = redstone block
```

**Receiver** (2 iron + 3 redstone + 1 ender pearl + 1 redstone torch)

```
. T .
I E I
R R R

T = redstone torch   I = iron ingot
E = ender pearl   R = redstone
```

## Configuration

Config file at `config/wirelessredstone.json` (created on first launch):

```json
{
  "rangeLocked": true,
  "maxRange": 128
}
```

| Key | Default | Notes |
|---|---|---|
| `rangeLocked` | `true` | When true, receivers ignore transmitters farther than `maxRange` blocks. Set false for unlimited range within a dimension. |
| `maxRange` | `128` | Block distance. Clamped to [4, 1024]. |

Edit the file and restart Minecraft (or the dedicated server) for changes to take effect.

## Building

```bash
./gradlew buildAll
```

Produces:
- `fabric/build/libs/wirelessredstone-fabric-<version>.jar`
- `neoforge/build/libs/wirelessredstone-neoforge-<version>.jar`

Individual loaders: `./gradlew :fabric:build` or `./gradlew :neoforge:build`.

Dev clients: `./gradlew :fabric:runClient` or `./gradlew :neoforge:runClient`.

## Repo layout

```
shared-resources/   assets, recipes, loot tables, lang — shared between both loaders
fabric/             Fabric loader subproject
neoforge/           NeoForge loader subproject
```

Java source classes that don't touch loader-specific APIs (block entities, pairing registry, menus, screens) live in both subprojects' `src/main/java/` and are byte-identical, since Minecraft 26.1 ships deobfuscated and uses Mojang's official names on both sides.

## License

CC0-1.0.
