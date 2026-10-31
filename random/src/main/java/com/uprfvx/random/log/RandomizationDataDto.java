package com.uprfvx.random.log;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.AbilityIDs;
import com.uprfvx.romio.gamedata.Encounter;
import com.uprfvx.romio.gamedata.EncounterArea;
import com.uprfvx.romio.gamedata.EncounterType;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public record RandomizationDataDto(
        List<SpeciesDto> species,
        List<SpeciesIdDto> starters,
        List<EncounterAreaDto> encounterAreas) {

    public static RandomizationDataDto from(RomHandler romHandler, Settings settings) {
        return new RandomizationDataDto(
                romHandler.getSpecies()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(species -> SpeciesDto.from(species, romHandler))
                        .toList(),
                romHandler.getStarters().stream().map(SpeciesIdDto::from).toList(),
                EncounterAreaDto.from(romHandler, settings));
    }

    public record SpeciesDto(
            String name,
            int number,
            List<Type> types,
            List<String> abilities,
            List<HeldItemDto> heldItems) {

        public static SpeciesDto from(Species species, RomHandler romHandler) {
            return new SpeciesDto(
                    species.getName(),
                    species.getNumber(),
                    getTypes(species),
                    getAbilities(species, romHandler),
                    getHeldItems(species));
        }

        private static List<Type> getTypes(Species species) {
            List<Type> types = new ArrayList<>(2);
            types.add(species.getPrimaryType(false));

            if (species.hasSecondaryType(false)) {
                types.add(species.getSecondaryType(false));
            }
            return types;
        }

        private static List<String> getAbilities(Species species, RomHandler romHandler) {
            return IntStream.of(species.getAbility1(), species.getAbility2(), species.getAbility3())
                    .filter(ability -> ability != AbilityIDs.undefined)
                    .distinct()
                    .mapToObj(romHandler::abilityName)
                    .toList();
        }

        private static List<HeldItemDto> getHeldItems(Species species) {
            List<HeldItemDto> items = new ArrayList<>(4);
            addIfNotNull(species.getGuaranteedHeldItem(), HeldItemDto.Rarity.GUARANTEED, items);
            addIfNotNull(species.getCommonHeldItem(), HeldItemDto.Rarity.COMMON, items);
            addIfNotNull(species.getRareHeldItem(), HeldItemDto.Rarity.RARE, items);
            addIfNotNull(species.getDarkGrassHeldItem(), HeldItemDto.Rarity.DARK_GRASS, items);
            return items;
        }

        private static void addIfNotNull(Item item, HeldItemDto.Rarity rarity, List<HeldItemDto> items) {
            if (item != null) {
                items.add(new HeldItemDto(item.getName(), rarity));
            }
        }

        public record HeldItemDto(String name, Rarity rarity) {
            public enum Rarity {
                GUARANTEED,
                COMMON,
                RARE,
                DARK_GRASS,
            }
        }
    }

    public record SpeciesIdDto(String name, int number) {
        public static SpeciesIdDto from(Species species) {
            return new SpeciesIdDto(species.getName(), species.getNumber());
        }
    }

    public record EncounterAreaDto(String name, int number, int rate, List<EncounterDto> encounters) {

        public static List<EncounterAreaDto> from(RomHandler romHandler, Settings settings) {
            boolean useTimeBasedEncounters = settings.isUseTimeBasedEncounters() ||
                    (!settings.isRandomizeWildPokemon() && settings.isWildLevelsModified());
            List<EncounterArea> areas = romHandler.getSortedEncounters(useTimeBasedEncounters);
            List<EncounterAreaDto> areaDtos = new ArrayList<>(areas.size());
            int number = 0;

            for (EncounterArea area : areas) {
                if (area.getEncounterType() == EncounterType.UNUSED) {
                    continue;
                }

                number++;
                areaDtos.add(from(area, number));
            }

            return areaDtos;
        }

        public static EncounterAreaDto from(EncounterArea area, int number) {
            return new EncounterAreaDto(
                    area.getDisplayName(),
                    number,
                    area.getRate(),
                    area.stream().map(EncounterDto::from).toList());
        }

        public record EncounterDto(SpeciesIdDto species, int minLevel, int maxLevel) {

            public static EncounterDto from(Encounter encounter) {
                int minLevel = encounter.getLevel();
                int maxLevel = encounter.getMaxLevel();
                return new EncounterDto(
                        SpeciesIdDto.from(encounter.getSpecies()),
                        minLevel,
                        maxLevel == 0 ? minLevel : maxLevel);
            }
        }
    }
}
