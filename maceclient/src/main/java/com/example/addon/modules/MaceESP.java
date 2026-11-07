package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Highlights players holding maces and wind charges, with optional tracers. */
public class MaceESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range").defaultValue(64).min(8).sliderMax(128).build());
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode").defaultValue(ShapeMode.Both).build());
    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers").description("Draw lines to highlighted players.").defaultValue(true).build());
    private final Setting<Boolean> windCharges = sgGeneral.add(new BoolSetting.Builder()
        .name("wind-charges").description("Also highlight players holding wind charges.").defaultValue(true).build());

    private final Setting<SettingColor> maceColor = sgColors.add(new ColorSetting.Builder()
        .name("mace-color").defaultValue(new SettingColor(255, 40, 40, 255)).build());
    private final Setting<SettingColor> windColor = sgColors.add(new ColorSetting.Builder()
        .name("wind-charge-color").defaultValue(new SettingColor(60, 220, 255, 255)).build());

    public MaceESP() {
        super(AddonTemplate.CATEGORY, "mace-esp", "Highlights players holding maces / wind charges.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || mc.player.distanceTo(p) > range.get()) continue;

            boolean mace = p.getMainHandStack().isOf(Items.MACE) || p.getOffHandStack().isOf(Items.MACE);
            boolean wind = windCharges.get()
                && (p.getMainHandStack().isOf(Items.WIND_CHARGE) || p.getOffHandStack().isOf(Items.WIND_CHARGE));
            if (!mace && !wind) continue;

            SettingColor c = mace ? maceColor.get() : windColor.get();
            Color side = new Color(c.r, c.g, c.b, 40);

            double dx = MathHelper.lerp(event.tickDelta, p.lastRenderX, p.getX()) - p.getX();
            double dy = MathHelper.lerp(event.tickDelta, p.lastRenderY, p.getY()) - p.getY();
            double dz = MathHelper.lerp(event.tickDelta, p.lastRenderZ, p.getZ()) - p.getZ();

            Box b = p.getBoundingBox();
            event.renderer.box(b.minX + dx, b.minY + dy, b.minZ + dz, b.maxX + dx, b.maxY + dy, b.maxZ + dz,
                side, c, shapeMode.get(), 0);

            if (tracers.get()) {
                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                    p.getX() + dx, p.getY() + p.getHeight() / 2 + dy, p.getZ() + dz, c);
            }
        }
    }
}
