package org.ta4j.core.constructor;

import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.TradeResultWrapper;

/**
 * @author VKozlov
 * Реализация индикатора
 */
public interface IIndicatorHandler {

    TradeResultWrapper execute(final IndicatorRequestWrapper requestWrapper);
}
