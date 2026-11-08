package com.uprfvx.random.randomizers;

import com.uprfvx.random.util.SetUnion;
import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.AbilityIDs;
import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.constants.GlobalConstants;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.gamedata.cueh.BasicSpeciesAction;
import com.uprfvx.romio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.romio.gamedata.cueh.EvolvedSpeciesAction;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SpeciesAbilityRandomizer extends Randomizer {

    public SpeciesAbilityRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeAbilities() {
        boolean evolutionSanity = settings.isAbilitiesFollowEvolutions();
        boolean allowWonderGuard = settings.isAllowWonderGuard();
        boolean banTrappingAbilities = settings.isBanTrappingAbilities();
        boolean banNegativeAbilities = settings.isBanNegativeAbilities();
        boolean banBadAbilities = settings.isBanBadAbilities();
        boolean megaEvolutionSanity = settings.isAbilitiesFollowMegaEvolutions();
        boolean weighDuplicatesTogether = settings.isWeighDuplicateAbilitiesTogether();
        boolean ensureTwoAbilities = settings.isEnsureTwoAbilities();
        boolean isMultiBattleOnly = settings.getBattleStyle().isOnlyMultiBattles();

        // Abilities don't exist in some games...
        if (romHandler.abilitiesPerSpecies() == 0) {
            return;
        }

        final boolean hasHiddenAbilities = (romHandler.abilitiesPerSpecies() == 3);

        final Set<Integer> bannedAbilities = new HashSet<>(romHandler.getUselessAbilities());

        if (!allowWonderGuard) {
            bannedAbilities.add(AbilityIDs.wonderGuard);
        }

        if (banTrappingAbilities) {
            bannedAbilities.addAll(GlobalConstants.battleTrappingAbilities);
        }

        if (banNegativeAbilities) {
            bannedAbilities.addAll(GlobalConstants.negativeAbilities);
        }

        if (banBadAbilities) {
            bannedAbilities.addAll(GlobalConstants.badAbilities);
            if (!isMultiBattleOnly) {
                bannedAbilities.addAll(GlobalConstants.doubleBattleAbilities);
            }
        }

        if (weighDuplicatesTogether) {
            bannedAbilities.addAll(GlobalConstants.duplicateAbilities);
            if (romHandler.generationOfPokemon() == 3) {
                bannedAbilities.add(Gen3Constants.airLockIndex); // Special case for Air Lock in Gen 3
            }
        }

        final int maxAbility = romHandler.highestAbilityIndex();

        // copy abilities straight up evolution lines
        // still keep WG as an exception, though
        BasicSpeciesAction basicAction = pk -> {
            if (pk.getAbility1() != AbilityIDs.wonderGuard && pk.getAbility2() != AbilityIDs.wonderGuard
                    && pk.getAbility3() != AbilityIDs.wonderGuard) {
                Set<Integer> speciesSpecificBannedAbilities = banNegativeAbilities ? getSpeciesSpecificNegativeAbilities(pk) : new HashSet<>();
                Set<Integer> allAbilitiesBannedForSpecies = new SetUnion<>(bannedAbilities, speciesSpecificBannedAbilities);

                // Pick first ability
                pk.setAbility1(pickRandomAbility(maxAbility, allAbilitiesBannedForSpecies, weighDuplicatesTogether));
                speciesSpecificBannedAbilities.add(pk.getAbility1());

                // Second ability?
                if (ensureTwoAbilities || random.nextDouble() < 0.5) {
                    // Yes, second ability
                    pk.setAbility2(pickRandomAbility(maxAbility, allAbilitiesBannedForSpecies, weighDuplicatesTogether));
                    speciesSpecificBannedAbilities.add(pk.getAbility2());
                } else {
                    // Nope
                    pk.setAbility2(0);
                }

                // Third ability?
                if (hasHiddenAbilities) {
                    pk.setAbility3(pickRandomAbility(maxAbility, allAbilitiesBannedForSpecies, weighDuplicatesTogether));
                }
            }
        };
        EvolvedSpeciesAction evolvedAction = (evFrom, evTo, _) -> {
            if (evTo.getAbility1() != AbilityIDs.wonderGuard && evTo.getAbility2() != AbilityIDs.wonderGuard
                    && evTo.getAbility3() != AbilityIDs.wonderGuard) {
                evTo.copyBaseFormeAbilities(evFrom);
            }
        };

        CopyUpEvolutionsHelper.Options cuehOptions = new CopyUpEvolutionsHelper.Options
                .Builder(basicAction, evolvedAction)
                .cosmeticAction((baseForme, altForme) -> altForme.copyBaseFormeAbilities(baseForme))
                .altFormeAction(basicAction)
                .evolutionSanity(evolutionSanity)
                .treatMegasAsEvos(megaEvolutionSanity)
                .build();
        copyUpEvolutionsHelper.apply(cuehOptions);

        changesMade = true;
    }

    private int pickRandomAbilityVariation(int selectedAbility, Set<Integer> bannedAbilities) {
        int newAbility = selectedAbility;

        do {
            Map<Integer, List<Integer>> abilityVariations = romHandler.getAbilityVariations();
            for (int baseAbility: abilityVariations.keySet()) {
                if (selectedAbility == baseAbility) {
                    List<Integer> variationsForThisAbility = abilityVariations.get(selectedAbility);
                    newAbility = variationsForThisAbility.get(random.nextInt(variationsForThisAbility.size()));
                    break;
                }
            }
        } while (bannedAbilities.contains(newAbility));

        return newAbility;
    }

    private int pickRandomAbility(int maxAbility, Set<Integer> bannedAbilities, boolean useVariations) {
        int newAbility;

        while (true) {
            newAbility = random.nextInt(maxAbility) + 1;

            if (bannedAbilities.contains(newAbility)) {
                continue;
            }

            if (useVariations) {
                newAbility = pickRandomAbilityVariation(newAbility, bannedAbilities);
            }
            break;
        }

        return newAbility;
    }

    private Set<Integer> getSpeciesSpecificNegativeAbilities(Species species) {
        HashSet<Integer> abilities = new HashSet<>();

        if (!isHarmedByHarshSunlight(species)) {
            abilities.add(AbilityIDs.desolateLand);
            abilities.add(AbilityIDs.drought);
        }

        if (!isHarmedByRain(species)) {
            abilities.add(AbilityIDs.drizzle);
            abilities.add(AbilityIDs.primordialSea);
        }

        if (!isImmuneToSandstorm(species)) {
            abilities.add(AbilityIDs.sandSpit);
            abilities.add(AbilityIDs.sandStream);
        }

        if (!isImmuneToHail(species)) {
            abilities.add(AbilityIDs.snowWarning);
        }

        return abilities;
    }

    private boolean isHarmedByHarshSunlight(Species species) {
        return species.hasType(Type.WATER, false) // Decreased damage
                || species.hasType(Type.ICE, false) // Prevents freezing
                || species.hasType(Type.ELECTRIC, false) // Lowered Thunder accuracy
                || species.hasType(Type.FLYING, false); // Lowered Hurricane accuracy
    }

    private boolean isHarmedByRain(Species species) {
        return species.hasType(Type.FIRE, false); // Decreased damage
    }

    private boolean isImmuneToSandstorm(Species species) {
        return species.hasType(Type.ROCK, false)
                || species.hasType(Type.STEEL, false)
                || species.hasType(Type.GROUND, false);
    }

    private boolean isImmuneToHail(Species species) {
        return species.hasType(Type.ICE, false);
    }
}
