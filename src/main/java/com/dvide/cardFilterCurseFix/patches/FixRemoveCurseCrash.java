package com.dvide.cardFilterCurseFix.patches;

import cardFilter.CardFilterMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.lang.reflect.Field;
import java.util.HashMap;

@SuppressWarnings("unused")
public class FixRemoveCurseCrash {
    @SpirePatch2(clz = CardFilterMod.class, method = "removeCardFromPool")
    public static class RemoveCardFromPoolPatches {
        @SpireInsertPatch(locator = CurseCardPoolAccessLocator.class)
        public static void removeCurseByRarityFix(String cardName) throws NoSuchFieldException, IllegalAccessException {
            removeCardFromLibraryCurses(cardName);
        }

        private static class CurseCardPoolAccessLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctBehavior) throws CannotCompileException, PatchingException {
                Matcher fieldMatcher = new Matcher.FieldAccessMatcher(AbstractDungeon.class, "curseCardPool");
                return LineFinder.findInOrder(ctBehavior, fieldMatcher);
            }
        }

        @SpireInsertPatch(locator = CardColorAccessLocator.class, localvars={"card"})
        public static void removeCurseByColorFix(String cardName, @ByRef AbstractCard[] card) throws NoSuchFieldException, IllegalAccessException {
            if (card[0].color == AbstractCard.CardColor.CURSE || card[0].type == AbstractCard.CardType.CURSE) {
                AbstractDungeon.curseCardPool.removeCard(card[0]);
                AbstractDungeon.srcCurseCardPool.removeCard(card[0]);
                removeCardFromLibraryCurses(cardName);
            }
        }

        private static class CardColorAccessLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctBehavior) throws CannotCompileException, PatchingException {
                Matcher fieldMatcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "color");
                return LineFinder.findInOrder(ctBehavior, fieldMatcher);
            }
        }

        private static void removeCardFromLibraryCurses(final String cardName) throws NoSuchFieldException, IllegalAccessException {
            Field field = CardLibrary.class.getDeclaredField("curses");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, AbstractCard> cardLibraryCurses = (HashMap<String, AbstractCard>) field.get(null);
            cardLibraryCurses.remove(cardName);
        }
    }
}
