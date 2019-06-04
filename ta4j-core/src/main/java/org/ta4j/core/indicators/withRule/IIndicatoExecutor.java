package org.ta4j.core.indicators.withRule;

import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.TradeResultWrapper;

public interface IIndicatoExecutor {

    /**
     * Метод для выполнения индикатора
     *
     * @param indicator тип индикатора
     * @return результат работы индикатора
     */
    TradeResultWrapper execIndicator(final Indicator indicator);
}
