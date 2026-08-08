package net.koala.jasm.structure;

import net.koala.jasm.component.SpacecraftComponent;
import net.minecraft.core.BlockPos;

public record RelativeComponent(BlockPos relPos, SpacecraftComponent component) {



}
