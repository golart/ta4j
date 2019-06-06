package org.ta4j.core.indicators.withRule;

import org.ta4j.core.constructor.IIndicatorHandler;
import org.ta4j.core.constructor.IIndicatorResolver;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.indicators.withRule.handler.cci.CCIIndicatorHandlerImpl;
import org.ta4j.core.indicators.withRule.handler.macd.MACDIndicatorHandlerImpl;
import org.ta4j.core.indicators.withRule.handler.rsi.RSIIndicatorHandlerImpl;
import org.ta4j.core.indicators.withRule.handler.stoch.StochasticIndicatorHandlerImpl;

public class IndicatorResolverImpl implements IIndicatorResolver {

    @Override
    public IIndicatorHandler resolveIndicator(final Indicator indicator) {
        switch (indicator) {
            case RSI:
                return new RSIIndicatorHandlerImpl();
            case MACD:
                return new MACDIndicatorHandlerImpl();
            case STOCH:
                return new StochasticIndicatorHandlerImpl();
            case CCI:
                return new CCIIndicatorHandlerImpl();
            default:
                throw new IllegalArgumentException("Unknown indicator " + indicator.name());
        }
    }
}
