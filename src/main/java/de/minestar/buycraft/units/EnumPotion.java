package de.minestar.buycraft.units;

public enum EnumPotion {

    //@formatter:off
    
    WATER_BOTTLE(0, "Water Bottle"),
    AWKWARD_POTION(16,"Akward Potion"),
    THICK_POTION(32, "Thick Potion"),
    MUNDANE_POTION(64, "Mundane Potion"),
    REGENERATION_POTION(8193, "Regeneration Potion"),
    SWIFTNESS_POTION(8194, "Swiftness Potion (3:00)"),
    FIRE_RESISTANTE_POTION(8195, "Fire Resistance Potion (3:00)"),    
    POISION_POTION(8196, "Poison Potion (0:45)"),
    HEALING_POTION(8197, "Healing Potion"),
    WEAKNESS_POTION(8200, "Weakness Potion (1:30)"),
    STRENGTH_POTION(8201, "Strength Potion (3:00)"),
    SLOWNESS_POTION(8202, "Slowness Potion (1:30)"),
    HARMING_POTION(8204, "Harming Potion"),
    
    REGENERATION_POTION_II(8225, "Regeneration Potion II (0:22)"),
    SWIFTNESS_POTION_II(8226, "Swiftness Potion II (1:30)"),
    POISON_POTION_II(8228, "Poison Potion II (0:22)"),
    HEALING_POTION_I(8229, "Healing Potion II"),
    STRENGTH_POTION_II(8233, "Strength Potion II (1:30)"),
    HARMING_POTION_II(8236, "Harming Potion II"),
    
    REGENERATION_POTION_I(8257, "Regeneration Potion (2:00)"),
    SWIFTNESS_POTION_I(8258, "Swiftness Potion (8:00)"),
    FIRE_RESISTANCE_POTION_I(8259, "Fire Resistance Potion (8:00)"),
    POISON_POTION_I(8260, "Poison Potion (2:00)"),
    WEAKNESS_POTION_I(8264, "Weakness Potion (4:00)"),
    STRENGTH_POTION_I(8265, "Strength Potion (8:00)"),
    SLOWNESS_POTION_I(8266, "Slowness Potion (4:00)"),
    
    FIRE_RESISTANCE_SPLASH(16378, "Fire Resistance Splash (2:15)"),
    REGENERATION_SPLASH(16385, "Regeneration Splash (0:33)"),
    SWIFTNESS_SPLASH(16386, "Swiftness Splash (2:15)"),
    POISON_SPLASH(16388, "Poison Splash (0:33)"),
    HEALING_SPLASH(16389, "Healing Splash"),
    WEAKNESS_SPLASH(16392, "Weakness Splash (1:07)"),
    STRENGTH_SPLASH(16393, "Strength Splash (2:15)"),
    SLOWNESS_SPLASH(16394, "Slowness Splash (1:07)"),
    HARMING_SPLASH(16396, "Harming Splash"),
    
    SWIFTNESS_SPLASH_II(16418, "Swiftness Splash II (1:07)"),
    POISON_SPLASH_II(16420, "Poison Splash II (0:16)"),
    HEALING_SPLASH_II(16421, "Healing Splash II"),
    STRENGTH_SPLASH_II(16425, "Strength Splash II (1:07)"),
    HARMING_SPLASH_II(16428, "Harming Splash II"),
    REGENERATION_SPLASH_II(16471, "Regeneration Splash II (0:16)"),
    
    REGENERATION_SPLASH_I(16449, "Regeneration Splash (1:30)"),
    SWIFTNESS_SPLASH_I(16450, "Swiftness Splash (6:00)"),
    FIRE_RESISTANCE_SPLASH_I(16451, "Fire Resistance Splash (6:00)"),
    POISON_SPLASH_I(16452, "Poison Splash (1:30)"),
    WEAKNESS_SPLASH_I(16456, "Weakness Splash (3:00)"),
    STRENGTH_SPLASH_I(16457, "Strength Splash (6:00)"),
    SLOWNESS_SPLASH_I(16458, "Slowness Splash (3:00)");

    //@formatter:on

    private final String name;
    private final int id;

    private EnumPotion(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static String getName(short id) {
        for (EnumPotion p : values())
            if (p.getId() == id)
                return p.getName();

        return null;
    }

    @Override
    public String toString() {
        return id + " " + name;
    }

}
