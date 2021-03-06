package me.libraryaddict.disguise.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.BaseDisguiseCommand;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class DisguiseHelpCommand extends BaseDisguiseCommand {
    private class EnumHelp {
        private String enumDescription;
        private String enumName;
        private String[] enums;
        private String readableEnum;

        public EnumHelp(String enumName, String enumReadable, String enumDescription, Enum[] enums) {
            String[] strings = new String[enums.length];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = toReadable(enums[i].name());
            }
            this.enumName = enumName;
            this.enumDescription = enumDescription;
            this.enums = strings;
            this.readableEnum = enumReadable;
        }

        public EnumHelp(String enumName, String enumReadable, String enumDescription, String[] enums) {
            this.enumName = enumName;
            this.enumDescription = enumDescription;
            this.enums = enums;
            this.readableEnum = enumReadable;
        }

        public String getEnumDescription() {
            return enumDescription;
        }

        public String getEnumName() {
            return enumName;
        }

        public String[] getEnums() {
            return enums;
        }

        public String getReadableEnum() {
            return readableEnum;
        }
    }

    private ArrayList<EnumHelp> enumHelp = new ArrayList<EnumHelp>();

    public DisguiseHelpCommand() {
        try {
            enumHelp.add(new EnumHelp("AnimalColor", "Animal colors", ChatColor.RED + "/disguisehelp AnimalColors "
                    + ChatColor.GREEN + "- View all the colors you can use for a animal color", AnimalColor.values()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            enumHelp.add(new EnumHelp("Art", "Arts", ChatColor.RED + "/disguisehelp Art " + ChatColor.GREEN
                    + "- View all the painting arts you can use on a painting disguise", (Enum[]) Class.forName("org.bukkit.Art")
                    .getEnumConstants()));
        } catch (Exception ex) {
        }
        try {
            enumHelp.add(new EnumHelp("HorseColor", "Horse colors", ChatColor.RED + "/disguisehelp HorseColors "
                    + ChatColor.GREEN + "- View all the colors you can use for a horses color", (Enum[]) Class.forName(
                    "org.bukkit.entity.Horse$Color").getEnumConstants()));
        } catch (Exception ex) {
        }
        try {
            enumHelp.add(new EnumHelp("HorseStyle", "Horse styles", ChatColor.RED + "/disguisehelp HorseStyles "
                    + ChatColor.GREEN + "- View all the styles you can use for a horses style", (Enum[]) Class.forName(
                    "org.bukkit.entity.Horse$Style").getEnumConstants()));
        } catch (Exception ex) {
        }
        try {
            enumHelp.add(new EnumHelp("OcelotType", "Ocelot types", ChatColor.RED + "/disguisehelp OcelotTypes "
                    + ChatColor.GREEN + "- View all the ocelot types you can use for ocelots", (Enum[]) Class.forName(
                    "org.bukkit.entity.Ocelot$Type").getEnumConstants()));
        } catch (Exception ex) {
        }
        try {
            ArrayList<String> enumReturns = new ArrayList<String>();
            for (PotionEffectType potionType : PotionEffectType.values()) {
                if (potionType != null)
                    enumReturns.add(toReadable(potionType.getName()) + ChatColor.RED + "(" + ChatColor.GREEN + potionType.getId()
                            + ChatColor.RED + ")");
            }
            enumHelp.add(new EnumHelp("PotionEffect", "PotionEffect", ChatColor.RED + "/disguisehelp PotionEffect "
                    + ChatColor.GREEN + "- View all the potion effects you can set", enumReturns.toArray(new String[enumReturns
                    .size()])));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            enumHelp.add(new EnumHelp("Profession", "Villager professions", ChatColor.RED + "/disguisehelp Professions "
                    + ChatColor.GREEN + "- View all the professions you can set on a villager", (Enum[]) Class.forName(
                    "org.bukkit.entity.Villager$Profession").getEnumConstants()));
        } catch (Exception ex) {
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (String node : new String[] { "disguise", "disguiseradius", "disguiseentity", "disguiseplayer" }) {
            ArrayList<String> allowedDisguises = getAllowedDisguises(sender, "libsdisguises." + node + ".");
            if (!allowedDisguises.isEmpty()) {
                if (args.length == 0) {
                    sendCommandUsage(sender);
                    return true;
                } else {
                    EnumHelp help = null;
                    for (EnumHelp s : enumHelp) {
                        if (args[0].equalsIgnoreCase(s.getEnumName()) || args[0].equalsIgnoreCase(s.getEnumName() + "s")) {
                            help = s;
                            break;
                        }
                    }
                    if (help != null) {
                        sender.sendMessage(ChatColor.RED + help.getReadableEnum() + ": " + ChatColor.GREEN
                                + StringUtils.join(help.getEnums(), ChatColor.RED + ", " + ChatColor.GREEN));
                        return true;
                    }
                    DisguiseType type = null;
                    for (DisguiseType disguiseType : DisguiseType.values()) {
                        if (disguiseType.getEntityType() == null) {
                            continue;
                        }
                        if (args[0].equalsIgnoreCase(disguiseType.name())
                                || disguiseType.name().replace("_", "").equalsIgnoreCase(args[0])) {
                            type = disguiseType;
                            break;
                        }
                    }
                    if (type == null) {
                        sender.sendMessage(ChatColor.RED + "Cannot find the disguise " + args[0]);
                        return true;
                    }
                    ArrayList<String> methods = new ArrayList<String>();
                    Class watcher = type.getWatcherClass();
                    try {
                        for (Method method : watcher.getMethods()) {
                            if (!method.getName().startsWith("get") && method.getParameterTypes().length == 1
                                    && method.getAnnotation(Deprecated.class) == null) {
                                Class c = method.getParameterTypes()[0];
                                String valueType = null;
                                if (c == String.class) {
                                    valueType = "String";
                                } else if (boolean.class == c) {
                                    valueType = "True/False";
                                } else if (int.class == c) {
                                    valueType = "Number";
                                } else if (float.class == c || double.class == c) {
                                    valueType = "Number.0";
                                } else if (AnimalColor.class == c) {
                                    valueType = "Color";
                                } else if (ItemStack.class == c) {
                                    valueType = "Item ID with optional :Durability";
                                } else if (ItemStack[].class == c) {
                                    valueType = "Item ID,ID,ID,ID with optional :Durability";
                                } else if (c.getSimpleName().equals("Style")) {
                                    valueType = "Horse Style";
                                } else if (c.getSimpleName().equals("Color")) {
                                    valueType = "Horse Color";
                                } else if (c.getSimpleName().equals("Type")) {
                                    valueType = "Ocelot type";
                                } else if (c.getSimpleName().equals("Profession")) {
                                    valueType = "Villager Profession";
                                } else if (PotionEffectType.class == c) {
                                    valueType = "Potioneffect";
                                }
                                if (valueType != null) {
                                    methods.add(ChatColor.RED + method.getName() + ChatColor.DARK_RED + "(" + ChatColor.GREEN
                                            + valueType + ChatColor.DARK_RED + ")");
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Collections.sort(methods, String.CASE_INSENSITIVE_ORDER);
                    sender.sendMessage(ChatColor.DARK_RED + type.toReadable() + " options: "
                            + StringUtils.join(methods, ChatColor.DARK_RED + ", "));
                    return true;
                }
            }
        }
        sender.sendMessage(ChatColor.RED + "You are forbidden from using this command!");
        return true;
    }

    /**
     * Send the player the information
     */
    protected void sendCommandUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/disguisehelp <DisguiseType> " + ChatColor.GREEN
                + "- View the options you can set on a disguise");
        for (EnumHelp s : enumHelp) {
            sender.sendMessage(s.getEnumDescription());
        }
    }

    public String toReadable(String string) {
        String[] split = string.split("_");
        for (int i = 0; i < split.length; i++)
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        return StringUtils.join(split, "_");
    }
}
