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

import java.util.Map;

@Mixin(MoltenLeadBlock.class)
public class MoltenLeadBlockMixin {

    private static final ResourceLocation TINKERS_PLATE_BOOTS_ID =
            ResourceLocation.parse("tconstruct:plate_boots");
    private static final ResourceLocation LIGHTER_THAN_LEAD_TAG_ID =
            ResourceLocation.parse("oreganized:lighter_than_lead");

    private static final TagKey<EntityType<?>> LIGHTER_THAN_LEAD_ENTITIES = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), LIGHTER_THAN_LEAD_TAG_ID);
    private static final TagKey<Item> LIGHTER_THAN_LEAD_ITEMS = ItemTags.create(LIGHTER_THAN_LEAD_TAG_ID);

    private static final Map<ResourceLocation, Integer> MODULAR_GOLEMS = Map.of(
            ResourceLocation.parse("modulargolems:metal_golem"), 3,
            ResourceLocation.parse("modulargolems:humanoid_golem"), 2,
            ResourceLocation.parse("modulargolems:dog_golem"), 1
    );

    private static final String LIGHTER_THAN_LEAD_GOLEM_MATERIAL = "modulargolems:iron";

    private static Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "isEntityLighterThanLead", at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectIsEntityLighterThanLead(Entity entity, CallbackInfoReturnable<Boolean> cir) {

        // Note: Parts of the code below were written with the help of AI
        // Preserve original behavior if it returns true
        if (entity.getType().is(LIGHTER_THAN_LEAD_ENTITIES)) {
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        if (entity instanceof LivingEntity living) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType());

            Integer modularGolemType = MODULAR_GOLEMS.getOrDefault(entityId, 0);

            if(modularGolemType != 0) {
                CompoundTag entityNBT = new CompoundTag();
                living.saveWithoutId(entityNBT);
                if(entityNBT.contains("auto-serial", Tag.TAG_COMPOUND)) {
                    CompoundTag autoSerial = entityNBT.getCompound("auto-serial");
                    if(autoSerial.contains("materials", Tag.TAG_LIST)) {
                        ListTag materials = autoSerial.getList("materials", Tag.TAG_COMPOUND);
                        if(materials.size() == modularGolemType + 1) {
                            CompoundTag material = materials.getCompound(modularGolemType);
                            if(material.contains("id", Tag.TAG_STRING)) {
                                String materialId = material.getString("id");
                                if(materialId.equals(LIGHTER_THAN_LEAD_GOLEM_MATERIAL)) {
                                    cir.setReturnValue(true);
                                    cir.cancel();
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);

            // Only proceed if boots are Tinkers' Plate Boots
            Item plateBoots = ForgeRegistries.ITEMS.getValue(TINKERS_PLATE_BOOTS_ID);

            if (plateBoots != null && boots.is(plateBoots)) {
                CompoundTag nbt = boots.getTag();
                if (nbt != null && !nbt.getBoolean("tic_broken")) {
                    // Check materials array for iron
                    if (nbt.contains("tic_materials", Tag.TAG_LIST)) {
                        ListTag materials = nbt.getList("tic_materials", Tag.TAG_STRING);
                        if (!materials.isEmpty()) {
                            String materialId = materials.getString(0);
                            if ("tconstruct:iron".equals(materialId)) {
                                cir.setReturnValue(true); // Custom condition met
                                cir.cancel();
                                return;
                            }
                        }
                    }
                }
            }

            // Original check for feet slot items tagged LIGHTER_THAN_LEAD
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
