package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Chat alerts when someone with a mace shows up or dives at you. */
public class MaceAlert extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range").defaultValue(48).min(8).sliderMax(128).build());
    private final Setting<Boolean> diveAlert = sgGeneral.add(new BoolSetting.Builder()
        .name("dive-alert").description("Warn when a mace holder is diving at you.").defaultValue(true).build());
    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown-seconds").description("Per-player alert cooldown.")
        .defaultValue(10).min(1).sliderMax(60).build());

    private final Map<UUID, Long> lastAlert = new HashMap<>();
    private final Map<UUID, Long> lastDive = new HashMap<>();

    public MaceAlert() {
        super(AddonTemplate.CATEGORY, "mace-alert", "Alerts you about nearby mace users and incoming smashes.");
    }

    @Override
    public void onActivate() {
        lastAlert.clear();
        lastDive.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        long cd = cooldown.get() * 1000L;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            if (mc.player.distanceTo(p) > range.get()) continue;
            if (!p.getMainHandStack().isOf(net.minecraft.item.Items.MACE)) continue;

            String name = p.getName().getString();

            if (now - lastAlert.getOrDefault(p.getUuid(), 0L) > cd) {
                lastAlert.put(p.getUuid(), now);
                warning("%s is holding a mace (%.0fm away).", name, (double) mc.player.distanceTo(p));
            }

            if (diveAlert.get()) {
                boolean above = p.getY() - mc.player.getY() >= 3;
                boolean close = MaceUtils.horizontalDistance(p) <= 8;
                boolean diving = p.getVelocity().y < -0.15;
                if (above && close && diving && now - lastDive.getOrDefault(p.getUuid(), 0L) > 1500) {
                    lastDive.put(p.getUuid(), now);
                    error("%s is DIVING at you!", name);
                }
            }
        }
    }
}
