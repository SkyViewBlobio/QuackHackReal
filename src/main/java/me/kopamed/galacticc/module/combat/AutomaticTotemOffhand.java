package me.kopamed.galacticc.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutomaticTotemOffhand extends Module {

    private final Minecraft mc;
    private boolean prioritiseEnchantedApple;
    private boolean holdingRightClick = false;
    private ItemStack originalFoodItem = null;

    public AutomaticTotemOffhand() {
        super("AutoTotem", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Automatisiert das Wechseln eines Totems der Unsterblichkeit| in die *Offhand* (linke Hand) und ermoeglicht| Ausnahmen fuer das Essen." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Prioritise EnchantedApple: " + ChatFormatting.WHITE +
                        "Aktiviert die Priorisierung des| Verzehrs von verzauberten| goldenen Aepfeln, falls vorhanden; andernfalls werden| normale goldene Aepfel verwendet." + ChatFormatting.RED +
                        "- Beste Essen: " + ChatFormatting.WHITE +
                        "Falls **Prioritise EnchantedApple** deaktiviert ist,| wird automatisch das| beste Essen in die Offhand genommen." + ChatFormatting.RED +
                        "- Kampfmodus: " + ChatFormatting.WHITE +
                        "Erlaubt dir, waehrend des Kampfes| mit einem Schwert in der| Offhand zu essen, ohne den Angriff| zu unterbrechen." + ChatFormatting.RED +
                        "- Automatische Umschaltung: " + ChatFormatting.WHITE +
                        "Halte den **RECHTEN MAUSKNOPF**| gedrueckt, um zwischen Totem und Essen zu wechseln. Diese Funktion| funktioniert nur mit einem Schwert| in der Haupthand, nicht mit anderen Werkzeugen." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:|" + ChatFormatting.WHITE +
                        "Halte ein Schwert in der Haupthand und druecke| den **RECHTEN MAUSKNOPF**, um automatisch zwischen| Totem und Essen zu wechseln.",
                true, false, Category.ANGRIFF);
        this.mc = Minecraft.getMinecraft();

        Galacticc.instance.settingsManager.rSetting(new Setting("Prioritise EnchantedApple", this, false));
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (isInteractingWithContainer()) {
            return;
        }

        prioritiseEnchantedApple = Galacticc.instance.settingsManager
                .getSettingByName(this, "Prioritise EnchantedApple")
                .getValBoolean();

        boolean isRightClickHeld = mc.gameSettings.keyBindUseItem.isKeyDown();
        boolean isHoldingSword = isSword(mc.player.getHeldItemMainhand());

        if (isRightClickHeld && isHoldingSword) {
            if (!holdingRightClick) {
                holdingRightClick = true;
                originalFoodItem = mc.player.getHeldItemOffhand();
                ItemStack foodItem = findFoodItem();
                if (foodItem != null) {
                    equipItemInOffhand(foodItem);
                }
            }
        } else {
            if (holdingRightClick) {
                holdingRightClick = false;
                restoreOriginalFoodItem();
            }
        }

        if (!isRightClickHeld && (mc.player.getHeldItemOffhand().isEmpty()
                || mc.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING)) {
            int totemSlot = findTotemInInventory();
            if (totemSlot != -1) {
                equipItemInOffhand(new ItemStack(Items.TOTEM_OF_UNDYING));
            }
        }
    }

    private boolean isInteractingWithContainer() {
        if (mc.currentScreen instanceof GuiContainer) {
            return true;
        }

        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            Block block = mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();
            if (isContainerBlock(block)) {
                return true;
            }
        }

        return mc.objectMouseOver != null &&
                mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY &&
                isContainerEntity(mc.objectMouseOver.entityHit);
    }

    private boolean isContainerBlock(Block block) {
        return block instanceof BlockChest ||
                block instanceof BlockShulkerBox ||
                block instanceof BlockFurnace ||
                block instanceof BlockDispenser ||
                block instanceof BlockHopper ||
                block instanceof BlockEnderChest ||
                block instanceof BlockEnchantmentTable ||
                block instanceof BlockAnvil ||
                block instanceof BlockBeacon ||
                block instanceof BlockBrewingStand ||
                block instanceof BlockCommandBlock;
    }

    private boolean isContainerEntity(Entity entity) {
        return entity instanceof EntityMinecartChest ||
                entity instanceof EntityMinecartHopper ||
                entity instanceof EntityMinecartFurnace;
    }

    private boolean isSword(ItemStack stack) {
        return stack.getItem() instanceof ItemSword;
    }

    private ItemStack findFoodItem() {
        ItemStack enchantedApple = null;
        ItemStack normalGoldenApple = null;
        ItemStack bestFood = null;
        int maxHungerRestored = 0;

        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() == Items.GOLDEN_APPLE) {
                if (stack.getMetadata() == 1) {
                    enchantedApple = stack;
                } else if (normalGoldenApple == null) {
                    normalGoldenApple = stack;
                }
            } else if (stack.getItem() instanceof ItemFood) {
                ItemFood food = (ItemFood) stack.getItem();
                int hungerRestored = food.getHealAmount(stack);

                if (hungerRestored > maxHungerRestored) {
                    bestFood = stack;
                    maxHungerRestored = hungerRestored;
                }
            }
        }

        // Logic when "Prioritise EnchantedApple" is enabled
        if (prioritiseEnchantedApple) {
            if (enchantedApple != null) {
                equipItemInOffhand(enchantedApple);
                return enchantedApple;
            }
            if (bestFood != null) {
                equipItemInOffhand(bestFood);
                return bestFood;
            }
            if (normalGoldenApple != null) {
                equipItemInOffhand(normalGoldenApple);
                return normalGoldenApple;
            }
        }

        // Logic when "Prioritise EnchantedApple" is disabled
        if (bestFood != null) {
            equipItemInOffhand(bestFood);
            return bestFood;
        }
        if (normalGoldenApple != null) {
            equipItemInOffhand(normalGoldenApple);
            return normalGoldenApple;
        }
        if (enchantedApple != null) {
            equipItemInOffhand(enchantedApple);
            return enchantedApple;
        }

        return null;
    }

    private void equipItemInOffhand(ItemStack item) {
        int slot = findItemSlot(item);
        if (slot != -1) {
            if (slot < 9) {
                mc.playerController.windowClick
                        (0, 36 + slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick
                        (0, 45, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick
                        (0, 36 + slot, 0, ClickType.PICKUP, mc.player);
            } else {
                mc.playerController.windowClick
                        (0, slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick
                        (0, 45, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick
                        (0, slot, 0, ClickType.PICKUP, mc.player);
            }
        }
    }

    private void equipTotemInOffhand() {
        if (mc.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING) {
            int totemSlot = findTotemInInventory();
            if (totemSlot != -1) {
                equipItemInOffhand(new ItemStack(Items.TOTEM_OF_UNDYING));
            }
        }
    }

    private int findTotemInInventory() {
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private int findItemSlot(ItemStack targetItem) {
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == targetItem.getItem()) {
                return i;
            }
        }
        return -1;
    }

    private void restoreOriginalFoodItem() {
        if (originalFoodItem != null) {
            equipItemInOffhand(originalFoodItem);
            originalFoodItem = null;
        } else {
            equipTotemInOffhand();
        }
    }

    private int countTotemsInInventory() {
        int count = 0;
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public String getHUDInfo() {
        int totemCount = countTotemsInInventory();
        return ChatFormatting.GRAY +
                "[" + ChatFormatting.GRAY + "Totems: " + ChatFormatting.GRAY + totemCount + ChatFormatting.GRAY + "]";
    }
}