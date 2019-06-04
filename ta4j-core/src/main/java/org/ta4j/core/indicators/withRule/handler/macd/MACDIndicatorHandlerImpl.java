package org.ta4j.core.indicators.withRule.handler.macd;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.constructor.IIndicatorHandler;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.TradeResultWrapper;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CustomCrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CustomCrossedUpIndicatorRule;

/**
 * @author VKozlov
 * Правило для работы с MACD индикатором
 */
@Slf4j
public class MACDIndicatorHandlerImpl implements IIndicatorHandler {

    private org.slf4j.Logger tradeLogger = org.slf4j.LoggerFactory.getLogger("trade-logger");

    /**
     * Дефолтные значения
     */
    private Integer shortTermEmaPeriod = 12;
    private Integer longTermEmaPeriod = 26;
    private Integer signalLineEmaPeriod = 9;

    @Override
    public TradeResultWrapper execute(final IndicatorRequestWrapper requestWrapper) {
        tradeLogger.info("start execute MACD method");
        TimeSeries series = requestWrapper.getTimeSeries();

        ClosePriceIndicator close = new ClosePriceIndicator(series);

        MACDIndicator macd = new MACDIndicator(close,
                NumberUtils.toInt(getParam(0, requestWrapper.getParams()), shortTermEmaPeriod),
                NumberUtils.toInt(getParam(1, requestWrapper.getParams()), longTermEmaPeriod));

        EMAIndicator emaMacd = new EMAIndicator(macd,
                NumberUtils.toInt(getParam(2, requestWrapper.getParams()), signalLineEmaPeriod));

        Rule entryRule = new CustomCrossedUpIndicatorRule(macd, emaMacd, ExpressionSymbol.MORE_OR_EQUAL, Indicator.MACD.name());
        Rule exitRule = new CustomCrossedDownIndicatorRule(macd, emaMacd, ExpressionSymbol.LESS_OR_EQUAL, Indicator.MACD.name());

        tradeLogger.info("end execute MACD method \n");
        return TradeResultWrapper.builder()
                .entryRule(entryRule)
                .exitRule(exitRule)
                .build();
    }
}
