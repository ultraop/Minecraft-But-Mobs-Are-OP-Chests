package in.ultraop.mobchests.loot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class CustomMobItems {
    public record Definition(String id,String name,Material material,EntityType source,int power,String[] enchants,String lore) {}
    private static final List<Definition> ITEMS = List.of(
        new Definition("kaal_dagger", "THUNDER WRATH", Material.BOW, EntityType.WITHER, 90, new String[]{"power:400","flame:10","punch:15","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("garudra", "STORM REIGN", Material.BOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","flame:10","punch:15","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("trinetra", "NIGHTFALL BOW", Material.BOW, EntityType.WITHER, 90, new String[]{"power:400","flame:10","punch:15","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("suryakant", "BLACKWING", Material.BOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","flame:10","punch:15","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("rudraveer", "SKY BREAKER", Material.BOW, EntityType.WITHER, 90, new String[]{"power:400","flame:10","punch:15","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("tejraksh", "VOID HUNTER", Material.BOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","flame:10","punch:15","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("agnivajra", "SHADOW VOLT", Material.BOW, EntityType.WITHER, 90, new String[]{"power:400","flame:10","punch:15","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("raktkaal", "FROSTBITE", Material.BOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","flame:10","punch:15","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("vajramukh", "BLACK KNIGHT", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("surya_fang", "NIGHT SLAYER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("agnipath", "VOID REAVER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("brahmtej", "DARK FANG", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("divyastra", "THUNDER BLADE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("tejora", "SHADOW STRIKE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("agniketu", "THUNDER CLEAVER", Material.NETHERITE_AXE, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("chandra_edge", "SOUL RENDER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("vajraveer", "DREADFIRE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("raktastra", "STORMBREAKER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("agnivansh", "BLOOD MOON", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("rudrakal", "DARK MATTER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("agni_astra", "PHANTOM EDGE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("nagshakti", "DRAGON FANG", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("rudrashakti", "HELLFIRE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("kshatriya", "RAVENCLAW", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("rajvansh", "SKYFALL", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("dahan", "DOOMBRINGER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("garudvega", "IRON REIGN", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("vajragni", "WRAITHBLADE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("mahakaal", "NIGHT REAPER", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("kaalchakra", "FROSTFANG", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("samrat", "VENOM EDGE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("chandrabaan", "WARLORD", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("shakti_blade", "DEATHBRINGER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("shakti_fang", "RIFT CUTTER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("veer_astra", "OBLIVION", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("asurantak", "DUSK RENDER", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("dev_astra", "EMBER BLADE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("dharma_edge", "CHAOS EDGE", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("indra_vajra", "SHADOWFANG", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("vishkala", "STORM PIKE", Material.DIAMOND_SPEAR, EntityType.WARDEN, 100, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Warden Relic"),
        new Definition("senapati", "TITAN SLAYER", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("aaryan_edge", "VOID KNIGHT", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("chakravart", "VOID LANCE", Material.DIAMOND_SPEAR, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("rudra_blade", "STORM FANG", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("vajra_edge", "DARK SENTINEL", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("kaalagni", "SOULBANE", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("rudrani", "DRAGON SPEAR", Material.DIAMOND_SPEAR, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("jwalamukh", "ASHEN WRATH", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("brahmastra", "CRIMSON EDGE", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("aryaveer", "THUNDERBANE", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("shivansh", "THUNDER PIKE", Material.DIAMOND_SPEAR, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("mahaveer", "GRIM REAPER", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("bhairav_astra", "ETERNAL FANG", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("shoolveer", "NIGHTFIRE", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("rudraveg", "DREAD KNIGHT", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("suryaveer", "DRAGON SLAYER", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("agnigarj", "RAGNAROK", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("raudra", "INFERNO EDGE", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("kaaldoot", "BLACK TALON", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("bhairav_sword", "NIGHT LANCE", Material.DIAMOND_SPEAR, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("astra_king", "SHADOW PIKE", Material.DIAMOND_SPEAR, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("nirbhay", "WARFANG", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("abhedya", "SKY REAVER", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("vijayastra", "DOOM FANG", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("agniraaj", "PHANTOM SLAYER", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("kaalrudra", "VOID STRIKER", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("shatrughna", "BLOOD REAPER", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("chandraketu", "FIRESTORM", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("dhanushraj", "DARK VENGEANCE", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("baanveer", "STORM KATANA", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("astrajyot", "HELLBOUND", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("vajrakaal", "FROST REAPER", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("nagastra", "SHADOW KATANA", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("shivtej", "CHAOS SLAYER", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("mahendra", "DEATHSTORM", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("devaraj", "DARK WARRIOR", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("astraveer", "BLACK JAVELIN", Material.DIAMOND_SPEAR, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("vajranath", "BLADE OF RUIN", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("yamraj_astra", "KINGSLAYER", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("ranbhairav", "THUNDER REAPER", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("indraneel", "NIGHT TERROR", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("garudastra", "ABYSS WALKER", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("yugveer", "TITAN BREAKER", Material.NETHERITE_AXE, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:30","looting:15","fire_aspect:10","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("raktveer", "NIGHT HUNTER", Material.CROSSBOW, EntityType.WITHER, 90, new String[]{"quick_charge:10","piercing:30","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("garuda_talon", "THUNDER BOLT", Material.CROSSBOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"quick_charge:10","piercing:30","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("vajraksha", "VOID RANGER", Material.CROSSBOW, EntityType.WITHER, 90, new String[]{"quick_charge:10","piercing:30","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("kaalnakh", "DARKSHOT", Material.CROSSBOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"quick_charge:10","piercing:30","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("naga_strike", "STORM PIERCER", Material.CROSSBOW, EntityType.WITHER, 90, new String[]{"quick_charge:10","piercing:30","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("yama_fang", "BLACK AEGIS", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("agnishikha", "VOID GUARD", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("ranveer", "THUNDER WALL", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("adhiraj", "NIGHT WARD", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("rakshak", "DARK BASTION", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("mahashakti", "STORM GUARD", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("trishul_edge", "IRON SENTINEL", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("tejasvi", "SOUL AEGIS", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("vikrant", "DRAGON WARD", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("pralay", "FROST GUARD", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Wither Relic"),
        new Definition("yugantak", "SHADOW BARRIER", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Abyss Relic"),
        new Definition("agnikaal", "OBSIDIAN AEGIS", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:30","mending:1"}, "Wither Relic")
    );
    private CustomMobItems() {}
    public static List<Definition> all() { return ITEMS; }
    public static List<Definition> forMobAll(EntityType type) { return ITEMS.stream().filter(d -> d.source() == type).collect(Collectors.toList()); }
    public static Definition forMob(EntityType type) { return forMobAll(type).stream().findFirst().orElse(null); }
    public static Definition byName(String value) {
        String query=value.trim().toLowerCase(Locale.ROOT).replace('_',' ');
        return ITEMS.stream().filter(d -> d.name().equalsIgnoreCase(query) || d.id().replace('_',' ').equalsIgnoreCase(query)).findFirst().orElse(null);
    }
    public static ItemStack create(Definition d) {
        ItemStack item=new ItemStack(d.material());
        ItemMeta meta=item.getItemMeta();
        // Plain text is intentional: it must match the merged pack's custom_name selector exactly.
        meta.displayName(Component.text(d.name(), d.material() == Material.BOW || d.material() == Material.CROSSBOW ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).decoration(TextDecoration.BOLD, TextDecoration.State.TRUE));
        meta.lore(List.of(
            Component.text("LEGENDARY RELIC",NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE),
            Component.text("Relic: "+d.lore(),NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE),
            Component.text("Power Tier: "+d.power(),NamedTextColor.RED).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE),
            Component.text("Source: "+d.source().name().replace('_',' '),NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE)
        ));
        for(String entry:d.enchants()){
            String[] parts=entry.split(":");
            Enchantment enchantment=Registry.ENCHANTMENT.get(NamespacedKey.minecraft(parts[0]));
            if(enchantment!=null) meta.addEnchant(enchantment,Integer.parseInt(parts[1]),true);
        }
        if(d.material()==Material.NETHERITE_SWORD || d.material()==Material.NETHERITE_AXE
                || d.material()==Material.DIAMOND_SPEAR || d.material()==Material.SHIELD){
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,new AttributeModifier(
                new NamespacedKey("mobchests","relic_attack_damage"),95.0,
                AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.MAINHAND));
        }
        item.setItemMeta(meta);
        return item;
    }
}
