package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Auto-equips the mace while you fall (so the attack cooldown is already charged) and swaps back on landing. */
public class MaceEquip extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> minFall = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-fall-distance").description("Start equipping after falling this far.")
        .defaultValue(2.0).min(0).sliderMax(20).build());

    private final Setting<Boolean> needPlayer = sgGeneral.add(new BoolSetting.Builder()
        .name("only-with-player").description("Only equip if another player is nearby.")
        .defaultValue(true).build());

    private final Setting<Double> playerRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("player-range").description("Range for the nearby-player check.")
        .defaultValue(12).min(2).sliderMax(40).visible(needPlayer::get).build());

    public MaceEquip() {
        super(AddonTemplate.CATEGORY, "mace-equip", "Equips your mace while falling and swaps back after landing.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isOnGround()) {
            InvUtils.swapBack();
            return;
        }

        if (mc.player.fallDistance < minFall.get() || MaceUtils.holdingMace()) return;

        if (needPlayer.get()) {
            boolean near = mc.world.getPlayers().stream()
                .anyMatch(p -> p != mc.player && mc.player.distanceTo(p) <= playerRange.get());
            if (!near) return;
        }

        FindItemResult mace = InvUtils.findInHotbar(Items.MACE);
        if (mace.found()) InvUtils.swap(mace.slot(), true);
    }

    @Override
    public void onDeactivate() {
        InvUtils.swapBack();
    }
}
