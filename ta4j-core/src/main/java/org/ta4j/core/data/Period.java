package org.ta4j.core.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.stream.Stream;

/**
 * @author VKozlov
 * # Длина свечи доступные значения "1DAY" "4HRS" "1HRS" "30MIN" "5MIN" "1MIN"
 * Типы периодов свечи
 */
@Getter
@AllArgsConstructor
public enum Period {

    PERIOD_1DAY("1DAY", Duration.ofDays(1)),
    PERIOD_4HRS("4HRS", Duration.ofHours(4)),
    PERIOD_1HRS("1HRS", Duration.ofHours(1)),
    PERIOD_30MIN("30MIN", Duration.ofMinutes(30)),
    PERIOD_5MIN("5MIN", Duration.ofMinutes(5)),
    PERIOD_1MIN("1MIN", Duration.ofMinutes(1));

    private String name;
    private Duration duration;

    public static Period getByName(String name) {
        return Stream.of(Period.values()).filter(n -> n.getName().equals(name)).findFirst().orElse(null);
    }
}


  
