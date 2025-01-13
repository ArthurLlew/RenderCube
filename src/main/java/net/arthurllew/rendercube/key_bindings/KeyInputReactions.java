package net.arthurllew.rendercube.key_bindings;

import com.mojang.logging.LogUtils;
import net.arthurllew.rendercube.RenderCube;
import net.arthurllew.rendercube.gui.RenderScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.nio.file.Paths;

public class KeyInputReactions {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void openRenderScreen(){
        MinecraftClient.getInstance().setScreen(new RenderScreen());
    }

    public static void dumpTextures(){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null){
            MinecraftClient.getInstance().getTextureManager().dumpDynamicTextures(Paths.get(RenderCube.TEXTURE_ATLASES_DIR));
            player.sendMessage(Text.literal("Texture atlases saved successfully"));
        }
        else{
            LOGGER.error("Player is suddenly null");
        }
    }
}
