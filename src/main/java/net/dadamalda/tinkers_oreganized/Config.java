package net.dadamalda.tinkers_oreganized;

import net.dadamalda.tinkers_oreganized.config.ModularGolemsMode;
import net.dadamalda.tinkers_oreganized.config.TinkersMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Tinkers_oreganized.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.EnumValue<TinkersMode> TINKERS_MODE = BUILDER
            .comment("Mode for Tinkers' Construct boots")
            .comment(
                    "DISABLED = Tinkers' boots don't float on lead",
                    "PLATING = Boots with floating plating will float",
                    "MAILLE = Boots with floating maille will float",
                    "EITHER = Boots with floating plating OR maille will float",
                    "BOTH = Boots with floating plating AND maille will float"
            )
            .defineEnum("tinkers_mode", TinkersMode.PLATING);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FLOATING_TINKERS_MATERIALS = BUILDER
            .comment("Use /data to find the material of an item")
            .comment("List of Tinkers' Construct materials that float on lead")
            .defineListAllowEmpty("floating_tinkers_materials", List.of(
                    "tconstruct:iron", "tconstruct:pig_iron"
            ), (final Object obj) -> true);

    private static final ForgeConfigSpec.EnumValue<ModularGolemsMode> MODULAR_GOLEMS_MODE = BUILDER
            .comment("Mode for Modular Golems")
            .comment(
                    "DISABLED = Modular Golems don't float on lead",
                    "LEGS = Modular Golems with floating legs will float",
                    "ANY = Modular Golems with any floating part will float",
                    "ALL = Modular Golems with all floating parts will float"
            )
            .defineEnum("modular_golems_mode", ModularGolemsMode.LEGS);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FLOATING_GOLEM_MATERIALS = BUILDER
            .comment("Use /data to find the material of a golem")
            .comment("List of Modular Golems materials that float on lead")
            .defineListAllowEmpty("floating_golem_materials", List.of(
                    "modulargolems:iron", "tconstruct:pig_iron"
            ), (final Object obj) -> true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static TinkersMode tinkersMode;
    public static List<String> floatingTinkersMaterials;

    public static ModularGolemsMode modularGolemsMode;
    public static List<String> floatingGolemMaterials;

    public static int configHash;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        tinkersMode = TINKERS_MODE.get();
        floatingTinkersMaterials = FLOATING_TINKERS_MATERIALS.get().stream().map(value -> (String) value).collect(Collectors.toList());

        modularGolemsMode = MODULAR_GOLEMS_MODE.get();
        floatingGolemMaterials = FLOATING_GOLEM_MATERIALS.get().stream().map(value -> (String) value).collect(Collectors.toList());

        configHash = Objects.hash(tinkersMode, floatingTinkersMaterials, modularGolemsMode, floatingGolemMaterials);
    }
}
