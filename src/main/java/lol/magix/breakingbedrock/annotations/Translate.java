package lol.magix.breakingbedrock.annotations;

import lol.magix.breakingbedrock.objects.absolute.PacketType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* A game translator. */
@Retention(RetentionPolicy.RUNTIME)
public @interface Translate {
    PacketType value() default PacketType.BEDROCK;
}