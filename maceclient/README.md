# Mace Client (Meteor addon)

Modules (category **Mace**):

| Module | What it does |
|---|---|
| **auto-mace** | Full mace bot: finds nearest player, chases, jump + wind-charge launches, pre-equips mace in the air, smashes on the way down. |
| **wind-launch** | One-shot jump + wind charge at your feet. Bind it to a key. |
| **mace-equip** | Equips mace while falling, swaps back on landing. |
| **mace-esp** | Boxes + tracers on players holding maces (red) / wind charges (cyan). |
| **mace-alert** | Chat alerts for nearby mace users and incoming dives. |
| **smash-info** | Prints estimated smash bonus damage on each hit. |
| **mace-dodge** | Wind-charges you away from an incoming mace dive. |

## Install
1. Clone https://github.com/MeteorDevelopment/meteor-addon-template
2. Copy `src/main/java/com/example/addon/` from this folder over the template's
   (overwrite `AddonTemplate.java`, add the `modules/` files; delete `ModuleExample`/`HudExample` if they error).
3. `./gradlew build` -> jar is in `build/libs/`. Drop it in `mods/` next to Meteor + Fabric API.

## Setup tips
- Put the **mace in your hotbar** and a **wind charge stack in the offhand** (offhand throws don't reset the mace cooldown).
- Turn off Meteor's NoFall while using this, it cancels the fall distance the smash needs.
- Pair with Meteor's AutoTotem / AutoEat.
- Only use it where cheats are allowed (singleplayer, your own server, anarchy servers).
