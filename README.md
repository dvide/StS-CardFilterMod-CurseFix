# Slay the Spire's Card Filter: Curse Fix

Fixes crash in [Card Filter](https://steamcommunity.com/sharedfiles/filedetails/?id=1645339812) (by [mrdelish](https://steamcommunity.com/id/mrdelish/myworkshopfiles/?appid=646570)) when curses are filtered and random curses are generated.

This mod should be loaded alongside Card Filter. It can be loaded in any order.

Steam workshop page: https://steamcommunity.com/sharedfiles/filedetails/?id=3417656017

## Explanation of crash

The base `CardLibrary.getCurse()` functions return a random curse, except for the four special curses: Ascender's Bane, Curse of the Bell, Necronomicurse, and Pride. These functions are called whenever a random curse is to be generated during a run. For example, when choosing the third Neow bonus, when generating the curse(s) for the Match and Keep event, or when transforming a curse into another random curse.

The `CardLibrary.getCurse()` functions can return a `null` value if the `CardLibrary.cards` map does not contain the same curses that are in the `CardLibrary.curses` map. Normally this is never the case, so there is usually no problem. However, when Card Filter is used to filter a card, it will always remove it from the `CardLibrary.cards` map, but it will not remove it from the `CardLibrary.curses` map.

This means that a filtered curse can still be randomly chosen by the `CardLibrary.getCurse()` internals, because it is still in the `CardLibrary.curses` map. However, the functions will return `null` in the end because the chosen card is not in the `CardLibrary.cards` map. This causes a `NullPointerException` to be raised when the `null` card is subsequently accessed later on.

Note that when using Card Filter with [StSLib](https://github.com/kiooeht/StSLib), this will cause a `NullPointerException` to be raised right away in the `CardLibrary.getCurse()` functions whenever you have chosen to filter any curses at all (excluding the four base-game special curses). This doesn't require any of the filtered curses to have been randomly chosen by the functions' internals. It will always crash just because you have chosen to filter a curse. This is due to StSLib patching the `CardLibrary.getCurse()` functions to remove any cards with `CardType.SPECIAL` from consideration. This is so that mod makers can create curses that won't randomly drop, just like Necronomicurse or Curse of the Bell, etc., just by giving them `CardType.SPECIAL`.

The way StSLib implements this is by checking the type of all cards in the the `CardLibrary.cards` map which are assumed to be present if they're in the `CardLibrary.curses` map (except for the four base-game special curse cards). This assumption is not true when at least one filtered curse card is not present in the `CardLibrary.cards` map, so this causes a `NullPointerException` to be raised right away, and this happens regardless of whether a filtered curse card was the one that was randomly chosen by the function. This means that simply using StSLib (a very common dependancy for many mods) turns this problematic crash from being fairly rare to being fairly common.

We fix these crashes by patching Card Filter to also remove filtered curses from `CardLibrary.curses` as well as `CardLibrary.cards`.

Additionally, we also patch Card Filter to attempt to remove filtered cards from the curses pool and from the `CardLibrary.curses` map if their color is `CardColor.CURSE`, or if their type is `CardType.CURSE`, and not just if their rarity is `CardRarity.CURSE`. Normally this would not be needed, but this is just to ensure that non-standard kinds of curses from mods are definitely going to be removed properly.

## Alternative fix

Alternatively, we could choose to not remove any filtered cards from `CardLibrary`, which is probably the better option. However, we would then have to patch the `CardLibrary.getCurse()` functions to always remove the curse cards that we have filtered, much like how StSLib patches them to always remove cards with `CardType.SPECIAL`. Perhaps this is easy and the correct thing to do, but there are likely many other places in the code where we would probably have to do something similar.

For example, the Watcher card Foreign Influence generates its three options by using `CardLibrary.cards` as well. If we did not remove filtered cards from `CardLibrary`, then Foreign Influence could still generate filtered cards. That is unless we also patch the `CardLibrary.getAnyColorCard()` functions to remove any filtered cards from consideration. I'm not sure if there are any other functions like this, in order to cover all the bases, as I have only done a cursory review. So for now, the simplest crash fix for me was to not change Card Filter's default behaviour too radically, and just continue to remove the filtered cards from `CardLibrary`.

One big downside of not fixing things properly is that filtered cards will not show up in the compendium after a run (although this is not something my fix causes). When first loading StS, filtered cards will still show up in the compendium because they only get removed from `CardLibrary` once you start a new run or load a save. This isn't too big of a deal because you can simply restart the game to see the cards in the compendium again.

One other big downside is that if you get a specific curse during a run that you have chosen to filter, the game will still give it to you, but it will not function correctly if you save and reload the game. For example, the Ominous Forge event will always give you a copy of the Pain curse if you choose the option to obtain the Warped Tongs. Since this is not a random curse that is generated, but a specific one, this will still work even if you have filtered the Pain curse. I think this is the correct behaviour, and you can finish your run as normal with a Pain in your deck, or remove it at a shop, etc. However, if you save and reload your game, you will find that the Pain will be replaced with a Madness card. This is because upon reloading, the Pain card doesn't exist in the `CardLibrary`, and the loading function will give you a Madness card by default as a fallback when this happens. Perhaps this was added in the event that Megacrit wanted to remove a card in a balance patch, so that it wouldn't crash users' games when they loaded an old save. They'll just get a Madness in their deck instead. However, if we didn't remove filtered cards from `CardLibrary`, but instead did the proper patches, I believe it would also fix this problem.
