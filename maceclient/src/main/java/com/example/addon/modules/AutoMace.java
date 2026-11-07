package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * The main bot. Finds a target, (optionally) chases it, launches itself with a wind charge,
 * pre-equips the mace so the attack cooldown is charged, then smashes on the way down.
 */
public class AutoMace extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBot = settings.createGroup("Bot");

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range").description("How far away to look for targets.")
        .defaultValue(24).min(3).sliderMax(64).build());

    private final Setting<Double> attackRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("attack-range").description("Max distance to swing at the target.")
        .defaultValue(3.0).min(1).sliderMax(6).build());

    private final Setting<Double> minFall = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-fall-distance").description("Minimum fall distance before smashing (vanilla needs > 1.5).")
        .defaultValue(3.0).min(1.5).sliderMax(30).build());

    private final Setting<Double> equipRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("equip-range").description("Pre-equip the mace while airborne when the target is this close (horizontal).")
        .defaultValue(14).min(2).sliderMax(40).build());

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate").description("Silently rotate towards the target when attacking.")
        .defaultValue(true).build());

    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
        .name("swap-back").description("Return to your previous slot after landing.")
        .defaultValue(true).build());

    private final Setting<Boolean> chase = sgBot.add(new BoolSetting.Builder()
        .name("chase").description("Walk / steer towards the target (mace bot mode).")
        .defaultValue(true).build());

    private final Setting<Boolean> autoLaunch = sgBot.add(new BoolSetting.Builder()
        .name("auto-launch").description("Throw a wind charge at your feet to launch up when close to the target.")
        .defaultValue(true).build());

    private final Setting<Double> launchRange = sgBot.add(new DoubleSetting.Builder()
        .name("launch-range").description("Horizontal distance to the target at which to launch.")
        .defaultValue(6).min(1).sliderMax(15).visible(autoLaunch::get).build());

    private final Setting<Boolean> jumpLaunch = sgBot.add(new BoolSetting.Builder()
        .name("jump-launch").description("Jump first and throw at the apex for extra height.")
        .defaultValue(true).visible(autoLaunch::get).build());

    private final Setting<Integer> launchCooldown = sgBot.add(new IntSetting.Builder()
        .name("launch-cooldown").description("Ticks between launches.")
        .defaultValue(30).min(5).sliderMax(100).visible(autoLaunch::get).build());

    private PlayerEntity target;
    private int launchTimer;
    private int launchStage;   // 0 = idle, 1 = jumped, waiting for apex
    private int jumpTicks;
    private boolean keysHeld;

    public AutoMace() {
        super(AddonTemplate.CATEGORY, "auto-mace", "Mace bot: chases, launches with wind charges and smashes players.");
    }

    @Override
    public void onActivate() {
        launchTimer = 0;
        launchStage = 0;
        target = null;
    }

    @Override
    public void onDeactivate() {
        releaseKeys();
        if (swapBack.get()) InvUtils.swapBack();
        target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (launchTimer > 0) launchTimer--;

        target = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.LowestDistance);
        boolean onGround = mc.player.isOnGround();

        if (target == null) {
            launchStage = 0;
            releaseKeys();
            if (onGround && swapBack.get()) InvUtils.swapBack();
            return;
        }

        double horiz = MaceUtils.horizontalDistance(target);
        double dist = mc.player.distanceTo(target);
        double vy = mc.player.getVelocity().y;

        // --- jump-launch: wait for the apex of the jump, then throw ---
        if (launchStage == 1) {
            jumpTicks++;
            if (!onGround && vy <= 0.05) {
                MaceUtils.throwWindChargeDown();
                launchStage = 0;
                launchTimer = launchCooldown.get();
                return;
            }
            if (onGround && jumpTicks > 3) launchStage = 0; // jump failed, reset
        }

        // --- start a launch ---
        if (autoLaunch.get() && onGround && launchStage == 0 && launchTimer == 0
            && horiz <= launchRange.get() && MaceUtils.hasWindCharge()) {
            if (jumpLaunch.get()) {
                mc.player.jump();
                launchStage = 1;
                jumpTicks = 0;
            } else {
                MaceUtils.throwWindChargeDown();
                launchTimer = launchCooldown.get();
            }
            return;
        }

        // --- steering ---
        if (chase.get() && launchStage == 0) steer(horiz, onGround);

        // --- pre-equip the mace in the air so the cooldown charges ---
        if (!onGround && launchStage == 0 && horiz <= equipRange.get() && !MaceUtils.holdingMace()) {
            FindItemResult mace = InvUtils.findInHotbar(Items.MACE);
            if (mace.found()) InvUtils.swap(mace.slot(), swapBack.get());
        }

        // --- smash ---
        boolean falling = !onGround && vy < -0.05 && mc.player.fallDistance >= minFall.get();
        if (falling && dist <= attackRange.get() && MaceUtils.holdingMace()
            && mc.player.getAttackCooldownProgress(0.5f) >= 0.95f) {
            smash(target);
        }

        // --- landed ---
        if (onGround && launchStage == 0 && swapBack.get() && horiz > launchRange.get()) {
            InvUtils.swapBack();
        }
    }

    private void steer(double horiz, boolean onGround) {
        mc.player.setYaw((float) Rotations.getYaw(target));
        boolean go = onGround ? horiz > attackRange.get() * 0.7 : horiz > 1.2;
        mc.options.forwardKey.setPressed(go);
        if (go) mc.player.setSprinting(true);
        keysHeld = true;
    }

    private void releaseKeys() {
        if (keysHeld) {
            mc.options.forwardKey.setPressed(false);
            keysHeld = false;
        }
    }

    private void smash(PlayerEntity t) {
        if (rotate.get()) {
            Rotations.rotate(Rotations.getYaw(t), Rotations.getPitch(t), 50, () -> hit(t));
        } else {
            hit(t);
        }
    }

    private void hit(PlayerEntity t) {
        if (mc.player == null || t == null) return;
        mc.interactionManager.attackEntity(mc.player, t);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public String getInfoString() {
        return target != null ? target.getName().getString() : null;
    }
}
