package org.ta4j.core.trading.rules;

import org.ta4j.core.Indicator;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;

/**
 * @author VKozlov
 * Кастомное правило отслеживающее в зависимости от переданного символа больше или меньше значение индикатора
 */
public class CustomUnderOverIndicatorRule extends UnderIndicatorRule {

    /**
     * The first indicator
     */
    private Indicator<Num> first;
    /**
     * The second indicator
     */
    private Indicator<Num> second;
    private ExpressionSymbol expressionSymbol;
    private String indicatorName;

    public CustomUnderOverIndicatorRule(Indicator<Num> first, Number second, ExpressionSymbol expressionSymbol, String indicatorName) {
        super(first, second);
        this.first = first;
        this.indicatorName = indicatorName;
        this.second = new ConstantIndicator<>(first.getTimeSeries(), first.numOf(second));
        this.expressionSymbol = expressionSymbol;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        final boolean satisfied = expressionSymbol.getExpressionOperation().test(first.getValue(index), second.getValue(index));

        if (satisfied) {
            setEventType(tradingRecord, "satisfied by under over indicator " + indicatorName);
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
