package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Defensive: when someone with a mace is diving at you, throws a wind charge at your feet
 * so you get launched out of the smash. Needs a wind charge in hotbar/offhand.
 */
public class MaceDodge extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontalRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-range").defaultValue(6).min(2).sliderMax(15).build());
    private final Setting<Double> minHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-height").description("Attacker must be at least this far above you.")
        .defaultValue(3).min(1).sliderMax(20).build());
    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown").description("Ticks between dodges.").defaultValue(20).min(5).sliderMax(60).build());

    private int timer;

    public MaceDodge() {
        super(AddonTemplate.CATEGORY, "mace-dodge", "Wind-charges away from incoming mace dives.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (timer > 0) { timer--; return; }
        if (!mc.player.isOnGround() || !MaceUtils.hasWindCharge()) return;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            if (!p.getMainHandStack().isOf(Items.MACE)) continue;
            if (p.getY() - mc.player.getY() < minHeight.get()) continue;
            if (MaceUtils.horizontalDistance(p) > horizontalRange.get()) continue;
            if (p.getVelocity().y > -0.2) continue;

            if (MaceUtils.throwWindChargeDown()) {
                timer = cooldown.get();
                info("Dodging %s!", p.getName().getString());
            }
            return;
        }
    }
}
