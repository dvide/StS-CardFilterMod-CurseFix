package com.dvide.cardFilterCurseFix;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.helpers.ImageMaster;

@SpireInitializer
public class CardFilterCurseFix implements PostInitializeSubscriber {
    @SuppressWarnings("unused")
    public static void initialize() {
        new CardFilterCurseFix();
    }

    public CardFilterCurseFix() {
        BaseMod.subscribe(this);
    }

    @Override
    public void receivePostInitialize() {
        final ModInfo ourModInfo = getOurModInfo();
        final Texture badgeTexture = ImageMaster.loadImage("resources.dvide.cardFilterCurseFix/images/ModBadge.png");
        BaseMod.registerModBadge(badgeTexture, ourModInfo.Name, ourModInfo.Authors[0], ourModInfo.Description, null);
    }

    private static ModInfo getOurModInfo() {
        final String ourModID = "CardFilterMod-CurseFix";

        for (ModInfo modInfo : Loader.MODINFOS) {
            if (modInfo.getIDName().equals(ourModID)) {
                return modInfo;
            }
        }

        throw new RuntimeException("Failed to load ModInfo for ''" + ourModID + "'.");
    }
}
