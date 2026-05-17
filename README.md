# Wireless Redstone

Wireless redstone transmitters and receivers for Minecraft 26.1.x. Pair them by channel + frequency, one transmitter feeds as many receivers as you want.

Scaffold-only at this stage. See [TODO/design.md](TODO/design.md) for the planned feature set.

## Building

```bash
./gradlew buildAll
```

Produces:
- `fabric/build/libs/wirelessredstone-fabric-<version>.jar`
- `neoforge/build/libs/wirelessredstone-neoforge-<version>.jar`

Individual loaders: `./gradlew :fabric:build` or `./gradlew :neoforge:build`.

Dev clients: `./gradlew :fabric:runClient` or `./gradlew :neoforge:runClient`.

## Requirements

- Minecraft **26.1.x**
- Java **25**
- Fabric Loader **0.18.4+** with **Fabric API**, *or* NeoForge **26.1+**

## License

CC0-1.0.
