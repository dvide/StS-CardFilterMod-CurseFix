package com.dvide.cardFilterCurseFix.patches;

import KOAmod.CardPoolKOA;
import basemod.ReflectionHacks;
import cardFilter.CardFilterMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class KoakumaCompatibility {
    private static Map<String, Boolean> KOABooksToPurge = null;

    @SpirePatch2(clz = CardFilterMod.class, method = "purgeCardPool", requiredModId = "Koakuma", optional = true)
    public static class CardFilterMod_purgeCardPool_Patches {
        @SpirePrefixPatch
        public static void setupKOABookPurging() {
            CardPoolKOA.resetBooksList();

            final ArrayList<String> BooksCommons = ReflectionHacks.getPrivateStatic(CardPoolKOA.class, "BooksCommons");
            final ArrayList<String> BooksUncommons = ReflectionHacks.getPrivateStatic(CardPoolKOA.class, "BooksUncommons");
            final ArrayList<String> BooksRares = ReflectionHacks.getPrivateStatic(CardPoolKOA.class, "BooksRares");

            KOABooksToPurge = Stream.of(BooksCommons, BooksUncommons, BooksRares)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(s -> s, s -> Boolean.FALSE));
        }

        @SpirePostfixPatch
        public static void resetKOACardPool() {
            CardPoolKOA.resetAllclassList();
            CardPoolKOA.resetOffclassList();
            purgeKOABooks();
        }

        private static void purgeKOABooks() {
            final ArrayList<String> BooksCommons = ReflectionHacks.getPrivateStatic(CardPoolKOA.class, "BooksCommons");
            final ArrayList<String> BooksUncommons = ReflectionHacks.getPrivateStatic(CardPoolKOA.class, "BooksUncommons");
            final ArrayList<String> BooksRares = ReflectionHacks.getPrivateStatic(CardPoolKOA.class, "BooksRares");

            BooksCommons.removeIf(CardFilterMod_purgeCardPool_Patches::shouldPurgeKOABook);
            BooksUncommons.removeIf(CardFilterMod_purgeCardPool_Patches::shouldPurgeKOABook);
            BooksRares.removeIf(CardFilterMod_purgeCardPool_Patches::shouldPurgeKOABook);

            KOABooksToPurge = null;
        }

        private static Boolean shouldPurgeKOABook(final String cardID) {
            assert KOABooksToPurge != null;
            return KOABooksToPurge.getOrDefault(cardID, Boolean.FALSE);
        }
    }

    @SpirePatch2(clz = CardFilterMod.class, method = "removeCardFromPool", requiredModId = "Koakuma", optional = true)
    public static class CardFilterMod_removeCardFromPool_Patches {
        @SpirePrefixPatch
        public static void flagKOABookForPurging(final String cardName) {
            assert KOABooksToPurge != null;

            if (KOABooksToPurge.containsKey(cardName)) {
                KOABooksToPurge.put(cardName, Boolean.TRUE);
            }
        }
    }
}
