package org.ta4j.core.indicators.withRule.handler.cci;

import org.apache.commons.lang3.math.NumberUtils;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.constructor.IIndicatorHandler;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.TradeResultWrapper;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.trading.rules.CustomUnderOverIndicatorRule;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

public class CCIIndicatorHandlerImpl implements IIndicatorHandler {

    private org.slf4j.Logger tradeLogger = org.slf4j.LoggerFactory.getLogger("trade-logger");

    @Override
    public TradeResultWrapper execute(final IndicatorRequestWrapper requestWrapper) {

        tradeLogger.info("Start execute CCI method \n");

        TimeSeries series = requestWrapper.getTimeSeries();

        CCIIndicator cci = new CCIIndicator(series, toInt(getParam(0, requestWrapper.getParams())));

        int buyValue = NumberUtils.toInt(requestWrapper.getBuyValue());
        int sellValue = NumberUtils.toInt(requestWrapper.getSellValue());

        ExpressionSymbol symbolToBuy = requestWrapper.getExpressionSymbolToBuyRule() != null ?
                requestWrapper.getExpressionSymbolToBuyRule() : ExpressionSymbol.LESS_OR_EQUAL;
        ExpressionSymbol symbolToSell = requestWrapper.getExpressionSymbolToSellRule() != null ?
                requestWrapper.getExpressionSymbolToSellRule() : ExpressionSymbol.MORE_OR_EQUAL;

        Rule entryRule = new CustomUnderOverIndicatorRule(cci, buyValue, symbolToBuy, Indicator.CCI.name());
        Rule exitRule = new CustomUnderOverIndicatorRule(cci, sellValue, symbolToSell, Indicator.CCI.name());

        tradeLogger.info("End execute CCI method \n");

        return TradeResultWrapper.builder()
                .entryRule(entryRule)
                .exitRule(exitRule)
                .build();
    }
}

