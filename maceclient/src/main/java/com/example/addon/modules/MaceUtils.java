package com.example.addon.modules;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Shared helpers for the Mace modules. */
public final class MaceUtils {
    private MaceUtils() {}

    public static double horizontalDistance(Entity e) {
        return Math.hypot(e.getX() - mc.player.getX(), e.getZ() - mc.player.getZ());
    }

    public static boolean hasWindCharge() {
        return mc.player.getOffHandStack().isOf(Items.WIND_CHARGE) || InvUtils.findInHotbar(Items.WIND_CHARGE).found();
    }

    public static boolean holdingMace() {
        return mc.player.getMainHandStack().isOf(Items.MACE);
    }

    /**
     * Looks straight down and throws a wind charge at your feet.
     * Uses the offhand if a charge is in it (no slot swap = mace attack cooldown is not reset).
     */
    public static boolean throwWindChargeDown() {
        final Hand hand;
        final int slot;

        if (mc.player.getOffHandStack().isOf(Items.WIND_CHARGE)) {
            hand = Hand.OFF_HAND;
            slot = -1;
        } else {
            FindItemResult r = InvUtils.findInHotbar(Items.WIND_CHARGE);
            if (!r.found()) return false;
            hand = Hand.MAIN_HAND;
            slot = r.slot();
        }

        Rotations.rotate(mc.player.getYaw(), 90, 100, () -> {
            if (hand == Hand.MAIN_HAND) InvUtils.swap(slot, true);
            mc.interactionManager.interactItem(mc.player, hand);
            mc.player.swingHand(hand);
            if (hand == Hand.MAIN_HAND) InvUtils.swapBack();
        });
        return true;
    }

    /** Vanilla mace smash bonus damage: 4/block for 3 blocks, 2/block for next 5, 1/block after. */
    public static double smashBonus(double fall) {
        if (fall <= 1.5) return 0;
        double first = Math.min(fall, 3) * 4;
        double second = Math.min(Math.max(fall - 3, 0), 5) * 2;
        double third = Math.max(fall - 8, 0);
        return first + second + third;
    }
}
