package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** One-shot launch. Bind it to a key: press = jump + wind charge at your feet. */
public class WindLaunch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> jumpFirst = sgGeneral.add(new BoolSetting.Builder()
        .name("jump-first").description("Jump and throw at the apex for extra height.")
        .defaultValue(true).build());

    private int stage;
    private int ticks;

    public WindLaunch() {
        super(AddonTemplate.CATEGORY, "wind-launch", "Launches you into the air with a wind charge. Bind to a key.");
    }

    @Override
    public void onActivate() {
        stage = 0;
        ticks = 0;
        if (mc.player != null && !MaceUtils.hasWindCharge()) {
            error("No wind charge in hotbar or offhand.");
            toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        ticks++;

        if (stage == 0) {
            if (jumpFirst.get() && mc.player.isOnGround()) {
                mc.player.jump();
                stage = 1;
                return;
            }
            stage = 2;
        }

        if (stage == 1) {
            if (mc.player.getVelocity().y <= 0.05 || ticks > 12) stage = 2;
            else return;
        }

        if (stage == 2) {
            MaceUtils.throwWindChargeDown();
            toggle();
        }
    }
}
