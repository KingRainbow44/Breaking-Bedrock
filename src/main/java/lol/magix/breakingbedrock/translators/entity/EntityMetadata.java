package lol.magix.breakingbedrock.translators.entity;

import net.minecraft.entity.Entity;

public record EntityMetadata<T>(Entity entity, T value) {
}
