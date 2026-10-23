
package anchortotemswap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class AnchorTotemSwap implements ClientModInitializer {
    private static boolean enabled = true;

    private static KeyMapping.Category category;
    private static KeyMapping toggleKey;
    private static KeyMapping swapKey;

    @Override
    public void onInitializeClient() {
        category = KeyMapping.Category.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                "anchor-totem-swap", "main"
            )
        );

        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.anchor-totem-swap.toggle",
                GLFW.GLFW_KEY_UNKNOWN,
                category
            )
        );

        swapKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.anchor-totem-swap.manual_swap",
                GLFW.GLFW_KEY_UNKNOWN,
                category
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;
                message(client, "Anchor Totem Swap: " +
                    (enabled ? "ON" : "OFF"));
            }

            while (swapKey.consumeClick()) {
                if (!enabled || client.player == null ||
                    client.gameMode == null) {
                    continue;
                }

                manualSwap(client);
            }
        });
    }

    private static void manualSwap(Minecraft client) {
        var player = client.player;
        var inventory = player.getInventory();

        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            message(client, "A totem is already in your offhand.");
            return;
        }

        int slot = -1;

        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (inventory.getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                slot = i;
                break;
            }
        }

        if (slot == -1) {
            for (int i = Inventory.getSelectionSize();
                 i < inventory.getContainerSize(); i++) {
                if (inventory.getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                    slot = i;
                    break;
                }
            }
        }

        if (slot == -1) {
            message(client, "No totems found in your inventory.");
            return;
        }

        // Use Minecraft's normal inventory interaction.
        client.gameMode.handleInventoryMouseClick(
            player.inventoryMenu.containerId,
            slot < Inventory.getSelectionSize()
                ? 36 + slot
                : slot,
            40,
            net.minecraft.world.inventory.ClickType.SWAP,
            player
        );

        message(client, "Offhand totem swap requested.");
    }

    private static void message(Minecraft client, String text) {
        if (client.player != null) {
            client.player.displayClientMessage(
                Component.literal("[Anchor Totem Swap] " + text),
                true
            );
        }
    }
}
