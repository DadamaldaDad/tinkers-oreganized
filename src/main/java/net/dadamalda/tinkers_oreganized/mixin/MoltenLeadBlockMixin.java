package net.dadamalda.tinkers_oreganized.mixin;

import com.mojang.logging.LogUtils;
import galena.oreganized.content.block.MoltenLeadBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MoltenLeadBlock.class)
public class MoltenLeadBlockMixin {

    private static final ResourceLocation TINKERS_PLATE_BOOTS_ID =
            new ResourceLocation("tconstruct", "plate_boots");
    private static final ResourceLocation LIGHTER_THAN_LEAD_ENTITIES_ID =
            new ResourceLocation("oreganized", "lighter_than_lead");
    private static final ResourceLocation LIGHTER_THAN_LEAD_ITEMS_ID =
            new ResourceLocation("oreganized", "lighter_than_lead");

    private static final TagKey<EntityType<?>> LIGHTER_THAN_LEAD_ENTITIES = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), LIGHTER_THAN_LEAD_ENTITIES_ID);
    private static final TagKey<Item> LIGHTER_THAN_LEAD_ITEMS = ItemTags.create(LIGHTER_THAN_LEAD_ITEMS_ID);

    private static Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "isEntityLighterThanLead", at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectTinkersIronPlateBoots(Entity entity, CallbackInfoReturnable<Boolean> cir) {

        // ✅ Preserve original behavior if it returns true
        if (entity.getType().is(LIGHTER_THAN_LEAD_ENTITIES)) {
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        if (entity instanceof LivingEntity living) {
            ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);

            // 🥾 Only proceed if boots are Tinkers' Plate Boots
            Item plateBoots = ForgeRegistries.ITEMS.getValue(TINKERS_PLATE_BOOTS_ID);

            if (plateBoots != null && boots.is(plateBoots)) {
                CompoundTag nbt = boots.getTag();
                if (nbt != null && !nbt.getBoolean("tic_broken")) {
                    // 🔍 Check materials array for iron
                    if (nbt.contains("tic_materials", Tag.TAG_LIST)) {
                        ListTag materials = nbt.getList("tic_materials", Tag.TAG_STRING);
                        if (!materials.isEmpty()) {
                            String materialId = materials.getString(0);
                            if ("tconstruct:iron".equals(materialId)) {
                                cir.setReturnValue(true); // ✅ Custom condition met
                                cir.cancel();
                                return;
                            }
                        }
                    }
                }
            }

            // 🔁 Original check for feet slot items tagged LIGHTER_THAN_LEAD
            if (boots.is(LIGHTER_THAN_LEAD_ITEMS)) {
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }

        cir.setReturnValue(false);
        cir.cancel();
    }
}
