package com.bukkit.gemo.BuyCraft.statics;

import java.util.HashMap;

public class Potions {
    private static HashMap<Short, String> potionList = new HashMap<Short, String>();

    public Potions() {
        potionList.put(Short.valueOf("0"), "Water Bottle");
        potionList.put(Short.valueOf("16"), "Awkward Potion");
        potionList.put(Short.valueOf("32"), "Thick Potion");
        potionList.put(Short.valueOf("64"), "Mundane Potion");
        potionList.put(Short.valueOf("8193"), "Regeneration Potion");
        potionList.put(Short.valueOf("8194"), "Swiftness Potion (3:00)");
        potionList.put(Short.valueOf("8195"), "Fire Resistance Potion (3:00)");
        potionList.put(Short.valueOf("8196"), "Poison Potion (0:45)");
        potionList.put(Short.valueOf("8197"), "Healing Potion");
        potionList.put(Short.valueOf("8200"), "Weakness Potion (1:30)");
        potionList.put(Short.valueOf("8201"), "Strength Potion (3:00)");
        potionList.put(Short.valueOf("8202"), "Slowness Potion (1:30)");
        potionList.put(Short.valueOf("8204"), "Harming Potion");
        potionList.put(Short.valueOf("8225"), "Regeneration Potion II (0:22)");
        potionList.put(Short.valueOf("8226"), "Swiftness Potion II (1:30)");
        potionList.put(Short.valueOf("8228"), "Poison Potion II (0:22)");
        potionList.put(Short.valueOf("8229"), "Healing Potion II");
        potionList.put(Short.valueOf("8233"), "Strength Potion II (1:30)");
        potionList.put(Short.valueOf("8236"), "Harming Potion II");
        potionList.put(Short.valueOf("8257"), "Regeneration Potion (2:00)");
        potionList.put(Short.valueOf("8258"), "Swiftness Potion (8:00)");
        potionList.put(Short.valueOf("8259"), "Fire Resistance Potion (8:00)");
        potionList.put(Short.valueOf("8260"), "Poison Potion (2:00)");
        potionList.put(Short.valueOf("8264"), "Weakness Potion (4:00)");
        potionList.put(Short.valueOf("8265"), "Strength Potion (8:00)");
        potionList.put(Short.valueOf("8266"), "Slowness Potion (4:00)");
        potionList.put(Short.valueOf("16378"), "Fire Resistance Splash (2:15)");
        potionList.put(Short.valueOf("16385"), "Regeneration Splash (0:33)");
        potionList.put(Short.valueOf("16386"), "Swiftness Splash (2:15)");
        potionList.put(Short.valueOf("16388"), "Poison Splash (0:33)");
        potionList.put(Short.valueOf("16389"), "Healing Splash");
        potionList.put(Short.valueOf("16392"), "Weakness Splash (1:07)");
        potionList.put(Short.valueOf("16393"), "Strength Splash (2:15)");
        potionList.put(Short.valueOf("16394"), "Slowness Splash (1:07)");
        potionList.put(Short.valueOf("16396"), "Harming Splash");
        potionList.put(Short.valueOf("16418"), "Swiftness Splash II (1:07)");
        potionList.put(Short.valueOf("16420"), "Poison Splash II (0:16)");
        potionList.put(Short.valueOf("16421"), "Healing Splash II");
        potionList.put(Short.valueOf("16425"), "Strength Splash II (1:07)");
        potionList.put(Short.valueOf("16428"), "Harming Splash II");
        potionList.put(Short.valueOf("16449"), "Regeneration Splash (1:30)");
        potionList.put(Short.valueOf("16450"), "Swiftness Splash (6:00)");
        potionList.put(Short.valueOf("16451"), "Fire Resistance Splash (6:00)");
        potionList.put(Short.valueOf("16452"), "Poison Splash (1:30)");
        potionList.put(Short.valueOf("16456"), "Weakness Splash (3:00)");
        potionList.put(Short.valueOf("16457"), "Strength Splash (6:00)");
        potionList.put(Short.valueOf("16458"), "Slowness Splash (3:00)");
        potionList.put(Short.valueOf("16471"), "Regeneration Splash II (0:16)");
    }

    public static String getName(short ID) {
        String name = potionList.get(ID);
        if (name == null)
            name = potionList.get(Short.valueOf("0"));
        return name;
    }
}
