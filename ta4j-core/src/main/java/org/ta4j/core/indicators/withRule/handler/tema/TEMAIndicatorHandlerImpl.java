package org.ta4j.core.indicators.withRule.handler.tema;

import org.apache.commons.lang3.math.NumberUtils;
import org.ta4j.core.Rule;
import org.ta4j.core.constructor.IIndicatorHandler;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.TradeResultWrapper;
import org.ta4j.core.indicators.TripleEMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CustomUnderOverIndicatorRule;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

public class TEMAIndicatorHandlerImpl implements IIndicatorHandler {

    private org.slf4j.Logger tradeLogger = org.slf4j.LoggerFactory.getLogger("trade-logger");

    @Override
    public TradeResultWrapper execute(final IndicatorRequestWrapper requestWrapper) {

        tradeLogger.info("Start execute TEMA method \n");

        TripleEMAIndicator ema = new TripleEMAIndicator(
                new ClosePriceIndicator(requestWrapper.getTimeSeries()),
                toInt(getParam(0, requestWrapper.getParams())));

        int buyValue = NumberUtils.toInt(requestWrapper.getBuyValue());
        int sellValue = NumberUtils.toInt(requestWrapper.getSellValue());

        ExpressionSymbol symbolToBuy = requestWrapper.getExpressionSymbolToBuyRule() != null ?
                requestWrapper.getExpressionSymbolToBuyRule() : ExpressionSymbol.LESS_OR_EQUAL;

        ExpressionSymbol symbolToSell = requestWrapper.getExpressionSymbolToSellRule() != null ?
                requestWrapper.getExpressionSymbolToSellRule() : ExpressionSymbol.MORE_OR_EQUAL;

        Rule entryRule = new CustomUnderOverIndicatorRule(ema, buyValue, symbolToBuy, Indicator.TEMA.name());
        Rule exitRule = new CustomUnderOverIndicatorRule(ema, sellValue, symbolToSell, Indicator.TEMA.name());

        tradeLogger.info("End execute TEMA method \n");

        return TradeResultWrapper.builder()
                .entryRule(entryRule)
                .exitRule(exitRule)
                .build();
    }

}
