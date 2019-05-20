package org.ta4j.core.trading.rules;

import org.ta4j.core.Indicator;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.CrossIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.utils.StringUtils;

/**
 * @author VKozlov
 */
public class CustomCrossedUpIndicatorRule extends AbstractRule {

    private CrossIndicator cross;
    private String indicatorName;
    private ExpressionSymbol expressionSymbol;

    public CustomCrossedUpIndicatorRule(Indicator<Num> first, Indicator<Num> second, ExpressionSymbol expressionSymbol, String indicatorName) {
        this.indicatorName = indicatorName;
        this.expressionSymbol = expressionSymbol;
        this.cross = new CrossIndicator(second, first);
    }

    public CustomCrossedUpIndicatorRule(Indicator<Num> indicator, Number threshold, ExpressionSymbol expressionSymbol, String indicatorName) {
        this(indicator, new ConstantIndicator<>(indicator.getTimeSeries(), indicator.numOf(threshold)), expressionSymbol, indicatorName);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        final boolean satisfied = cross.getValue(index);
        if (satisfied) {
            setEventType(tradingRecord, "satisfied by crossed up indicator " + indicatorName);
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
