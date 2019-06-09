package org.ta4j.core.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Название алгоритма используемоего для торговли
 */
@Getter
@AllArgsConstructor
public enum Indicator {

    CCI,
    RSI,
    MACD,
    STOCH,
    EMA,
    DEMA,
    TEMA,
    STOP_LOSS,
    TAKE_PROFIT,
    TRAILING_SELL,
    TRAILING_BUY;

    public static Indicator getByName(String name) {
        return Stream.of(Indicator.values()).filter(n -> n.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean isLossOrTraining() {
        return Stream.of(STOP_LOSS, TAKE_PROFIT, TRAILING_BUY, TRAILING_SELL).anyMatch(expressionSymbol ->
                name().equalsIgnoreCase(expressionSymbol.name()));
    }
}
