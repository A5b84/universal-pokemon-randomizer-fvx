# Universal Pokemon Randomizer FVX

This is a fork of [Universal Pokemon Randomizer FVX](https://github.com/upr-fvx/universal-pokemon-randomizer-fvx), a tool to randomize Pokémon roms, with the following changes:

- When saving logs after randomization, a JSON file containing randomization data (similar to what's in logs) is also saved.
- When randomizing abilities and with 'Ban bad abilities' selected, weather abilities are not assigned to Pokémons that would be harmed by them (e.g. no Sand Stream on Pokémons vulnerable to sandstorms).
- When randomizing held items, certain items can be configured to always be available through wild Pokémon encounters.
  These items are listed in [EncounterHeldItemRandomizer](random/src/main/java/com/uprfvx/random/randomizers/EncounterHeldItemRandomizer.java).

See the [Universal Pokemon Randomizer FVX website](https://upr-fvx.github.io/universal-pokemon-randomizer-fvx/) and the upstream repo for more information.

## Usage

Build using `gradlew build` or run `RandomizerGUI` from your favorite IDE.

As with the upstream repo, CLI usage is possible as described [here](docs/src/_wikipages/cli_randomizer.md).
TLDR:

```
java -jar UPR-FVX.jar cli -i <path to source ROM> -o <path for output ROM> -s <path to settings file> -l
```
