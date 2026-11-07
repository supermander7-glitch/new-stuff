package com.example.addon;

import com.example.addon.modules.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddonTemplate extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("MaceClient");
    public static final Category CATEGORY = new Category("Mace");

    @Override
    public void onInitialize() {
        LOG.info("Loading Mace Client addon");

        Modules.get().add(new AutoMace());
        Modules.get().add(new WindLaunch());
        Modules.get().add(new MaceEquip());
        Modules.get().add(new MaceESP());
        Modules.get().add(new MaceAlert());
        Modules.get().add(new SmashInfo());
        Modules.get().add(new MaceDodge());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}
