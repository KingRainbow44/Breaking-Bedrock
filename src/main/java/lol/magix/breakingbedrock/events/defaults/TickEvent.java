package lol.magix.breakingbedrock.events.defaults;

import lol.magix.breakingbedrock.events.Event;
import lombok.Data;

@Data
public class TickEvent implements Event {
    private final long tick;
}
