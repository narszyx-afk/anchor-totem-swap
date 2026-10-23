package anchortotemswap;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class AnchorTotemSwap implements ClientModInitializer {
    private static final long SWAP_DELAY_MS = 250L;
    private static long lastSwapTime = 0L;
    private static boolean enabled = true;

    private static KeyMapping.Category category;
    private static KeyMapping toggleKey;
    private static KeyMapping swapKey;

    @Override
    public void onInitializeClient() {
        category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("anchor-totem-swap", "main")
        );

        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.anchor-totem-swap.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category
            )
        );

        swapKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.anchor-totem-swap.manual_swap",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                category
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;
                // No on-screen message when toggling.
            }

            while (swapKey.consumeClick()) {
                if (!enabled || client.player == null || client.gameMode == null) {
                    continue;
                }
                manualSwap(client);
            }
        });
    }

    private static void manualSwap(Minecraft client) {
        long now = System.currentTimeMillis();
        if (now - lastSwapTime < SWAP_DELAY_MS) {
            return;
        }

        var player = client.player;
        var inventory = player.getInventory();

        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        int inventoryIndex = -1;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                inventoryIndex = i;
                break;
            }
        }

        if (inventoryIndex < 0) {
            return;
        }

        // Player inventory menu slots: hotbar 0-8 are menu slots 36-44;
        // main inventory slots 9-35 keep their menu slot indices.
        int menuSlot = inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;

        client.gameMode.handleInventoryMouseClick(
            player.inventoryMenu.containerId,
            menuSlot,
            40,
            ClickType.SWAP,
            player
        );

        lastSwapTime = now;
    }
}