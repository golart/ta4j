package org.ta4j.core.indicators.withRule.handler.rsi;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.constructor.IIndicatorHandler;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.TradeResultWrapper;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CustomUnderOverIndicatorRule;

/**
 * @author VKozlov
 * Правило для работы с RSI индикатором
 */
@Slf4j
public class RSIIndicatorHandlerImpl implements IIndicatorHandler {

    private org.slf4j.Logger tradeLogger = org.slf4j.LoggerFactory.getLogger("trade-logger");

    /**
     * Дефолтные значения
     */
    private int defaultValueToBuy = 30;
    private int defaultValueToSell = 70;
    private int defaultPeriod = 14;

    @Override
    public TradeResultWrapper execute(final IndicatorRequestWrapper requestWrapper) {
        TimeSeries series = requestWrapper.getTimeSeries();

        tradeLogger.info("start execute RSI method");
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(close, NumberUtils.toInt(getParam(0, requestWrapper.getParams()), defaultPeriod));

        int buyValue = NumberUtils.toInt(requestWrapper.getBuyValue(), defaultValueToBuy);
        int sellValue = NumberUtils.toInt(requestWrapper.getSellValue(), defaultValueToSell);

        ExpressionSymbol symbolToBuy = requestWrapper.getExpressionSymbolToBuyRule() != null ?
                requestWrapper.getExpressionSymbolToBuyRule() : ExpressionSymbol.LESS_OR_EQUAL;
        ExpressionSymbol symbolToSell = requestWrapper.getExpressionSymbolToSellRule() != null ?
                requestWrapper.getExpressionSymbolToSellRule() : ExpressionSymbol.MORE_OR_EQUAL;

        Rule entryRule = new CustomUnderOverIndicatorRule(rsi, buyValue, symbolToBuy, Indicator.RSI.name());
        Rule exitRule = new CustomUnderOverIndicatorRule(rsi, sellValue, symbolToSell, Indicator.RSI.name());

        tradeLogger.info("end execute RSI method \n");
        return TradeResultWrapper.builder()
                .entryRule(entryRule)
                .exitRule(exitRule)
                .build();
    }
}
