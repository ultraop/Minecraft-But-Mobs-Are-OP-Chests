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
        new Definition("kaal_dagger", "Kaal Dagger", Material.BOW, EntityType.WITHER, 90, new String[]{"power:400","flame:3","punch:5","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("garudra", "Garudra", Material.BOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","flame:3","punch:5","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("trinetra", "Trinetra", Material.BOW, EntityType.WITHER, 90, new String[]{"power:400","flame:3","punch:5","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("suryakant", "Suryakant", Material.BOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","flame:3","punch:5","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("rudraveer", "Rudraveer", Material.BOW, EntityType.WITHER, 90, new String[]{"power:400","flame:3","punch:5","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("tejraksh", "Tejraksh", Material.BOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","flame:3","punch:5","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("agnivajra", "Agnivajra", Material.BOW, EntityType.WITHER, 90, new String[]{"power:400","flame:3","punch:5","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("raktkaal", "Raktkaal", Material.BOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","flame:3","punch:5","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("vajramukh", "Vajramukh", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("surya_fang", "Surya Fang", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("agnipath", "Agnipath", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("brahmtej", "Brahmtej", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("divyastra", "Divyastra", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("tejora", "Tejora", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("agniketu", "Agniketu", Material.NETHERITE_AXE, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("chandra_edge", "Chandra Edge", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("vajraveer", "Vajraveer", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("raktastra", "Raktastra", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("agnivansh", "Agnivansh", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("rudrakal", "Rudrakal", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("agni_astra", "Agni Astra", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("nagshakti", "Nagshakti", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("rudrashakti", "Rudrashakti", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("kshatriya", "Kshatriya", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("rajvansh", "Rajvansh", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("dahan", "Dahan", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("garudvega", "Garudvega", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("vajragni", "Vajragni", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("mahakaal", "Mahakaal", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("kaalchakra", "Kaalchakra", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("samrat", "Samrat", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("chandrabaan", "Chandrabaan", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("shakti_blade", "Shakti Blade", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("shakti_fang", "Shakti Fang", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("veer_astra", "Veer Astra", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("asurantak", "Asurantak", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("dev_astra", "Dev Astra", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("dharma_edge", "Dharma Edge", Material.NETHERITE_SWORD, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("indra_vajra", "Indra Vajra", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("vishkala", "Vishkala", Material.DIAMOND_SPEAR, EntityType.WARDEN, 100, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Warden Relic"),
        new Definition("senapati", "Senapati", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("aaryan_edge", "Aaryan Edge", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("chakravart", "Chakravart", Material.DIAMOND_SPEAR, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("rudra_blade", "Rudra Blade", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("vajra_edge", "Vajra Edge", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("kaalagni", "Kaalagni", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("rudrani", "Rudrani", Material.DIAMOND_SPEAR, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("jwalamukh", "Jwalamukh", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("brahmastra", "Brahmastra", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("aryaveer", "Aryaveer", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("shivansh", "Shivansh", Material.DIAMOND_SPEAR, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("mahaveer", "Mahaveer", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("bhairav_astra", "Bhairav Astra", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("shoolveer", "Shoolveer", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("rudraveg", "Rudraveg", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("suryaveer", "Suryaveer", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("agnigarj", "Agnigarj", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("raudra", "Raudra", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("kaaldoot", "Kaaldoot", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("bhairav_sword", "Bhairav Sword", Material.DIAMOND_SPEAR, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("astra_king", "Astra King", Material.DIAMOND_SPEAR, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("nirbhay", "Nirbhay", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("abhedya", "Abhedya", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("vijayastra", "Vijayastra", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("agniraaj", "Agniraaj", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("kaalrudra", "Kaalrudra", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("shatrughna", "Shatrughna", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("chandraketu", "Chandraketu", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("dhanushraj", "Dhanushraj", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("baanveer", "Baanveer", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("astrajyot", "Astrajyot", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("vajrakaal", "Vajrakaal", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("nagastra", "Nagastra", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("shivtej", "Shivtej", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("mahendra", "Mahendra", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("devaraj", "Devaraj", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("astraveer", "AstraVeer", Material.DIAMOND_SPEAR, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("vajranath", "Vajranath", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("yamraj_astra", "Yamraj Astra", Material.NETHERITE_SWORD, EntityType.ENDER_DRAGON, 110, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Dragon Sovereign Relic"),
        new Definition("ranbhairav", "Ranbhairav", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("indraneel", "Indraneel", Material.NETHERITE_SWORD, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("garudastra", "Garudastra", Material.NETHERITE_SWORD, EntityType.WITHER, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("yugveer", "Yugveer", Material.NETHERITE_AXE, EntityType.ELDER_GUARDIAN, 90, new String[]{"sharpness:10","looting:5","fire_aspect:3","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("raktveer", "Raktveer", Material.CROSSBOW, EntityType.WITHER, 90, new String[]{"power:400","quick_charge:5","piercing:10","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("garuda_talon", "Garuda Talon", Material.CROSSBOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","quick_charge:5","piercing:10","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("vajraksha", "Vajraksha", Material.CROSSBOW, EntityType.WITHER, 90, new String[]{"power:400","quick_charge:5","piercing:10","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("kaalnakh", "Kaalnakh", Material.CROSSBOW, EntityType.ELDER_GUARDIAN, 90, new String[]{"power:400","quick_charge:5","piercing:10","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("naga_strike", "Naga Strike", Material.CROSSBOW, EntityType.WITHER, 90, new String[]{"power:400","quick_charge:5","piercing:10","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("yama_fang", "Yama Fang", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("agnishikha", "Agnishikha", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("ranveer", "Ranveer", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("adhiraj", "Adhiraj", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("rakshak", "Rakshak", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("mahashakti", "Mahashakti", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("trishul_edge", "Trishul Edge", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("tejasvi", "Tejasvi", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("vikrant", "Vikrant", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("pralay", "Pralay", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Wither Relic"),
        new Definition("yugantak", "Yugantak", Material.SHIELD, EntityType.ELDER_GUARDIAN, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Abyss Relic"),
        new Definition("agnikaal", "Agnikaal", Material.SHIELD, EntityType.WITHER, 90, new String[]{"thorns:50","unbreaking:10","mending:1"}, "Wither Relic")
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
        meta.displayName(Component.text(d.name()));
        meta.lore(List.of(
            Component.text("ULTRAOP LEGENDARY RELIC",NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC,false),
            Component.text("Relic: "+d.lore(),NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false),
            Component.text("Power Tier: "+d.power(),NamedTextColor.RED).decoration(TextDecoration.ITALIC,false),
            Component.text("Source: "+d.source().name().replace('_',' '),NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC,false)
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
