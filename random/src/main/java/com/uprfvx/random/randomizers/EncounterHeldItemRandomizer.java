package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link Randomizer} for the held items of wild Pokemon.
 * In some games, these items may be shared between Pokemon
 * in normal and static encounters, thus the separation from
 * {@link WildEncounterRandomizer} which only does normal encounters.
 */
public class EncounterHeldItemRandomizer extends Randomizer {

    private static final int GUARANTEED_ITEMS_MAX_ATTEMPTS = 1000;

    /**
     * Items that should always be available as held items in at least one encounter.
     */
    // As a guideline, this should include:
    // - items that are reasonnable to want more than one of
    // - items that are not available at all in certain game versions
    // - items that can make gameplay more interesting
    private static final Set<Integer> GUARANTEED_ITEMS = Set.of(
            // Consumable items
            ItemIDs.cherishBall,
            ItemIDs.dreamBall,
            ItemIDs.masterBall,
            ItemIDs.ppMax,
            ItemIDs.ppUp,
            ItemIDs.rareCandy,

            // Type-specific items
            ItemIDs.blackBelt,
            ItemIDs.blackGlasses,
            ItemIDs.charcoal,
            ItemIDs.dragonFang,
            ItemIDs.hardStone,
            ItemIDs.magnet,
            ItemIDs.metalCoat,
            ItemIDs.miracleSeed,
            ItemIDs.mysticWater,
            ItemIDs.neverMeltIce,
            ItemIDs.poisonBarb,
            ItemIDs.silkScarf,
            ItemIDs.silverPowder,
            ItemIDs.softSand,
            ItemIDs.spellTag,
            ItemIDs.twistedSpoon,

            // Pokémon-specific items
            ItemIDs.adamantOrb,
            ItemIDs.burnDrive,
            ItemIDs.chillDrive,
            ItemIDs.douseDrive,
            ItemIDs.gracidea,
            ItemIDs.leek,
            ItemIDs.lightBall,
            ItemIDs.luckyPunch,
            ItemIDs.lustrousOrb,
            ItemIDs.metalPowder,
            ItemIDs.quickPowder,
            ItemIDs.shockDrive,
            ItemIDs.soulDew,
            ItemIDs.thickClub,

            // Evolution items
            ItemIDs.berrySweet,
            ItemIDs.cloverSweet,
            ItemIDs.dawnStone,
            ItemIDs.deepSeaScale,
            ItemIDs.deepSeaTooth,
            ItemIDs.dragonScale,
            ItemIDs.dubiousDisc,
            ItemIDs.duskStone,
            ItemIDs.electirizer,
            ItemIDs.fireStone,
            ItemIDs.flowerSweet,
            ItemIDs.iceStone,
            ItemIDs.kingsRock,
            ItemIDs.leafStone,
            ItemIDs.loveSweet,
            ItemIDs.magmarizer,
            ItemIDs.moonStone,
            ItemIDs.ovalStone,
            ItemIDs.prismScale,
            ItemIDs.protector,
            ItemIDs.razorClaw,
            ItemIDs.razorFang,
            ItemIDs.reaperCloth,
            ItemIDs.ribbonSweet,
            ItemIDs.sachet,
            ItemIDs.shinyStone,
            ItemIDs.starSweet,
            ItemIDs.strawberrySweet,
            ItemIDs.sunStone,
            ItemIDs.thunderStone,
            ItemIDs.upgrade,
            ItemIDs.waterStone,
            ItemIDs.whippedDream,

            // Contest items
            ItemIDs.blueScarf,
            ItemIDs.greenScarf,
            ItemIDs.pinkScarf,
            ItemIDs.redScarf,
            ItemIDs.yellowScarf,

            // Other equippable items
            ItemIDs.airBalloon,
            ItemIDs.amuletCoin,
            ItemIDs.assaultVest,
            ItemIDs.bigRoot,
            ItemIDs.bindingBand,
            ItemIDs.blackSludge,
            ItemIDs.blunderPolicy,
            ItemIDs.brightPowder,
            ItemIDs.choiceBand,
            ItemIDs.choiceScarf,
            ItemIDs.choiceSpecs,
            ItemIDs.cleanseTag,
            ItemIDs.dampRock,
            ItemIDs.destinyKnot,
            ItemIDs.everstone,
            ItemIDs.eviolite,
            ItemIDs.expShare,
            ItemIDs.expertBelt,
            ItemIDs.floatStone,
            ItemIDs.fluffyTail,
            ItemIDs.focusBand,
            ItemIDs.focusSash,
            ItemIDs.fullIncense,
            ItemIDs.gripClaw,
            ItemIDs.heatRock,
            ItemIDs.heavyDutyBoots,
            ItemIDs.icyRock,
            ItemIDs.laggingTail,
            ItemIDs.laxIncense,
            ItemIDs.leftovers,
            ItemIDs.lifeOrb,
            ItemIDs.lightClay,
            ItemIDs.luckIncense,
            ItemIDs.luckyEgg,
            ItemIDs.machoBrace,
            ItemIDs.mentalHerb,
            ItemIDs.metronome,
            ItemIDs.muscleBand,
            ItemIDs.oddIncense,
            ItemIDs.powerHerb,
            ItemIDs.protectivePads,
            ItemIDs.pureIncense,
            ItemIDs.quickClaw,
            ItemIDs.ringTarget,
            ItemIDs.rockIncense,
            ItemIDs.rockyHelmet,
            ItemIDs.roomService,
            ItemIDs.roseIncense,
            ItemIDs.safetyGoggles,
            ItemIDs.scopeLens,
            ItemIDs.seaIncense,
            ItemIDs.sharpBeak,
            ItemIDs.shedShell,
            ItemIDs.shellBell,
            ItemIDs.smokeBall,
            ItemIDs.smoothRock,
            ItemIDs.sootheBell,
            ItemIDs.terrainExtender,
            ItemIDs.utilityUmbrella,
            ItemIDs.waveIncense,
            ItemIDs.whiteHerb,
            ItemIDs.wideLens,
            ItemIDs.wiseGlasses,
            ItemIDs.zoomLens,

            // Other items
            ItemIDs.blackFlute,
            ItemIDs.blueFlute,
            ItemIDs.redFlute,
            ItemIDs.whiteFlute,
            ItemIDs.yellowFlute
    );

    public EncounterHeldItemRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeWildHeldItems() {
        boolean banBadItems = settings.isBanBadRandomWildPokemonHeldItems();

        List<Item> possible = new ArrayList<>(banBadItems ? romHandler.getNonBadItems() : romHandler.getAllowedItems());
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.getGuaranteedHeldItem() == null && pk.getCommonHeldItem() == null && pk.getRareHeldItem() == null
                    && pk.getDarkGrassHeldItem() == null) {
                // No held items at all, skip
                // TODO: does this make sense? do we want pokes to never get held items if they didn't already?
                continue;
            }
            
            double decision = random.nextDouble();
            if (pk.getGuaranteedHeldItem() != null) {
                // Currently have a guaranteed item
                if (decision < 0.9) {
                    setRandomGuaranteedItem(pk, possible);
                } else {
                    setRandomCommonAndRareItems(pk, possible);
                }
            } else {
                // No guaranteed item atm
                if (decision < 0.5) {
                    setNoItem(pk);
                } else if (decision < 0.65) {
                    setRandomOnlyRareItem(pk, possible);
                } else if (decision < 0.8) {
                    setRandomOnlyCommonItem(pk, possible);
                } else if (decision < 0.95 || !romHandler.hasGuaranteedWildHeldItems()) {
                    setRandomCommonAndRareItems(pk, possible);
                } else {
                    setRandomGuaranteedItem(pk, possible);
                }
            }
            
            if (romHandler.hasDarkGrassHeldItems() && pk.getGuaranteedHeldItem() == null) {
                double dgDecision = random.nextDouble();
                if (dgDecision < 0.5) {
                    // Yes, dark grass item
                    pk.setDarkGrassHeldItem(possible.get(random.nextInt(possible.size())));
                } else {
                    pk.setDarkGrassHeldItem(null);
                }
            } 
        }

        addGuaranteedItems();

        changesMade = true;
    }

    private void setNoItem(Species spec) {
        spec.setGuaranteedHeldItem(null);
        spec.setCommonHeldItem(null);
        spec.setRareHeldItem(null);
        spec.setDarkGrassHeldItem(null);
    }

    private void setRandomGuaranteedItem(Species pk, List<Item> possible) {
        pk.setGuaranteedHeldItem(possible.get(random.nextInt(possible.size())));
        pk.setCommonHeldItem(null);
        pk.setRareHeldItem(null);
        pk.setDarkGrassHeldItem(null);
    }

    private void setRandomOnlyCommonItem(Species pk, List<Item> possible) {
        pk.setGuaranteedHeldItem(null);
        pk.setCommonHeldItem(possible.get(random.nextInt(possible.size())));
        pk.setRareHeldItem(null);
    }

    private void setRandomOnlyRareItem(Species pk, List<Item> possible) {
        pk.setGuaranteedHeldItem(null);
        pk.setCommonHeldItem(null);
        pk.setRareHeldItem(possible.get(random.nextInt(possible.size())));
    }

    private void setRandomCommonAndRareItems(Species pk, List<Item> possible) {
        pk.setGuaranteedHeldItem(null);
        pk.setCommonHeldItem(possible.get(random.nextInt(possible.size())));
        pk.setRareHeldItem(possible.get(random.nextInt(possible.size())));
        while (pk.getRareHeldItem().equals(pk.getCommonHeldItem())) {
            pk.setRareHeldItem(possible.get(random.nextInt(possible.size())));
        }
    }

    private void addGuaranteedItems() {
        Set<Item> items = romHandler.getAllowedItems()
                .stream()
                .filter(item -> GUARANTEED_ITEMS.contains(item.getId()))
                .collect(Collectors.toSet());

        List<Species> allSpecies = romHandler.getSpecies();

        for (Species species : allSpecies) {
            if (species != null) {
                removeIfNonNull(species.getGuaranteedHeldItem(), items);
                removeIfNonNull(species.getCommonHeldItem(), items);
            }
        }

        for (Item item : items) {
            addGuaranteedItemToAnySpecies(item, allSpecies);
        }
    }

    private static void removeIfNonNull(Item item, Set<Item> items) {
        if (item != null) {
            items.remove(item);
        }
    }

    private void addGuaranteedItemToAnySpecies(Item item, List<Species> allSpecies) {
        for (int i = 0; i < GUARANTEED_ITEMS_MAX_ATTEMPTS; i++) {
            Species species = allSpecies.get(random.nextInt(allSpecies.size()));

            if (species != null && tryAddGuaranteedItemToSpecies(item, species)) {
                return;
            }
        }

        throw new RuntimeException("Could not find a species to which to add " + item + " within " + GUARANTEED_ITEMS_MAX_ATTEMPTS + " attempts");
    }

    private boolean tryAddGuaranteedItemToSpecies(Item item, Species species) {
        if (random.nextBoolean()) {
            if (species.getCommonHeldItem() == null) {
                species.setCommonHeldItem(item);
                return true;
            } else {
                return false;
            }
        } else {
            if (species.getRareHeldItem() == null) {
                species.setRareHeldItem(item);
                return true;
            } else {
                return false;
            }
        }
    }
}
