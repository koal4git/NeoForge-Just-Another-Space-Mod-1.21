package net.koala.jasm.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {

    public static final KeyMapping ASCEND = new KeyMapping(
            "key.justanotherspace.ascend",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            "key.categories.justanotherspace"
    );

    private ModKeyMappings() {}

}
