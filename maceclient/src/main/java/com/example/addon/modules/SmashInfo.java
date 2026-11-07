package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Prints the estimated smash bonus damage when you hit something with a mace. */
public class SmashInfo extends Module {
    public SmashInfo() {
        super(AddonTemplate.CATEGORY, "smash-info", "Shows estimated bonus smash damage in chat.");
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (mc.player == null || !MaceUtils.holdingMace()) return;
        if (!(event.entity instanceof LivingEntity)) return;

        double fall = mc.player.fallDistance;
        double bonus = MaceUtils.smashBonus(fall);
        if (bonus <= 0) return;

        info("Fell %.1f blocks -> ~%.1f bonus dmg (%.1f hearts)", fall, bonus, bonus / 2);
    }
}
