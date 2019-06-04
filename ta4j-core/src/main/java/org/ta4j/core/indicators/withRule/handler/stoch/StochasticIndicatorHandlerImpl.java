package org.ta4j.core.indicators.withRule.handler.stoch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.constructor.IIndicatorHandler;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.TradeResultWrapper;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.trading.rules.CustomUnderOverIndicatorRule;
import org.ta4j.core.utils.StringUtils;

/**
 * @author VKozlov
 * Правило для работы с RSI индикатором
 */
@Slf4j
public class StochasticIndicatorHandlerImpl implements IIndicatorHandler {

    private org.slf4j.Logger tradeLogger = org.slf4j.LoggerFactory.getLogger("trade-logger");

    /**
     * Дефолтные значения
     */
    private int defaultValueToBuy = 20;
    private int defaultValueToSell = 80;
    private int defaultPeriod = 14;
    private int defaultSmooth = 3;

    @Override
    public TradeResultWrapper execute(final IndicatorRequestWrapper requestWrapper) {
        tradeLogger.info("Start STOCH method");
        TimeSeries series = requestWrapper.getTimeSeries();

        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, NumberUtils.toInt(getParam(0, requestWrapper.getParams()), defaultPeriod));
        SMAIndicator smoothStochK = new SMAIndicator(stochK, NumberUtils.toInt(getParam(1, requestWrapper.getParams()), defaultSmooth));

        Object buyValue = NumberUtils.toInt(requestWrapper.getBuyValue(), defaultValueToBuy);
        Object sellValue = NumberUtils.toInt(requestWrapper.getSellValue(), defaultValueToSell);

        Rule entryRule = new CustomUnderOverIndicatorRule(smoothStochK, 
                        Integer.valueOf(buyValue.toString()),
                        requestWrapper.getExpressionSymbolToBuyRule() == null ? ExpressionSymbol.LESS_OR_EQUAL
                                : requestWrapper.getExpressionSymbolToBuyRule(),
                        Indicator.STOCH.name());
        
        
        Rule exitRule = new CustomUnderOverIndicatorRule(smoothStochK, 
                        Integer.valueOf(sellValue.toString()),
                        requestWrapper.getExpressionSymbolToSellRule() == null ? ExpressionSymbol.MORE_OR_EQUAL
                                : requestWrapper.getExpressionSymbolToSellRule(),
                        Indicator.STOCH.name());

        tradeLogger.info("End STOCH method \n");
        return TradeResultWrapper.builder()
                .entryRule(entryRule)
                .exitRule(exitRule)
                .build();
    }
    
    private String getValue(String value, String defaultValue) {
       if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
           return defaultValue;
       }
       return value;
    }
}
