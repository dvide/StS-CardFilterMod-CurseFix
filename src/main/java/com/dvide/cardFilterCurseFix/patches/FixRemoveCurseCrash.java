package com.dvide.cardFilterCurseFix.patches;

import basemod.ReflectionHacks;
import cardFilter.CardFilterMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.HashMap;

@SuppressWarnings("unused")
public class FixRemoveCurseCrash {
    @SpirePatch2(clz = CardFilterMod.class, method = "removeCardFromPool")
    public static class CardFilterMod_removeCardFromPool_Patches {
        @SpireInsertPatch(locator = AbstractDungeon_curseCardPool_Locator.class)
        public static void removeFromCardLibraryCursesByRarity(final String cardName) {
            removeFromCardLibraryCurses(cardName);
        }

        private static class AbstractDungeon_curseCardPool_Locator extends SpireInsertLocator {
            public int[] Locate(final CtBehavior ctBehavior) throws CannotCompileException, PatchingException {
                final Matcher fieldAccessMatcher = new Matcher.FieldAccessMatcher(AbstractDungeon.class, "curseCardPool");
                return LineFinder.findInOrder(ctBehavior, fieldAccessMatcher);
            }
        }

        @SpireInsertPatch(locator = AbstractCard_color_Locator.class, localvars={"card"})
        public static void removeFromCardLibraryByColorAndType(final String cardName, @ByRef final AbstractCard[] card) {
            if (card[0].color == AbstractCard.CardColor.CURSE || card[0].type == AbstractCard.CardType.CURSE) {
                AbstractDungeon.curseCardPool.removeCard(card[0]);
                AbstractDungeon.srcCurseCardPool.removeCard(card[0]);
                removeFromCardLibraryCurses(cardName);
            }
        }

        private static class AbstractCard_color_Locator extends SpireInsertLocator {
            public int[] Locate(final CtBehavior ctBehavior) throws CannotCompileException, PatchingException {
                final Matcher fieldAccessMatcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "color");
                return LineFinder.findInOrder(ctBehavior, fieldAccessMatcher);
            }
        }

        private static void removeFromCardLibraryCurses(final String cardID) {
            final HashMap<String, AbstractCard> cardLibraryCurses = ReflectionHacks.getPrivateStatic(CardLibrary.class, "curses");
            cardLibraryCurses.remove(cardID);
        }
    }
}
