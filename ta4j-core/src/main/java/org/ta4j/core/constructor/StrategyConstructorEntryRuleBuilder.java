package org.ta4j.core.constructor;

import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.CustomTrailingStopLossRule;

/**
 * @author VKozlov
 */
public class StrategyConstructorEntryRuleBuilder extends StrategyConstructorRuleBuilder {

    public StrategyConstructorEntryRuleBuilder(IIndicatorResolver indicatorResolver,
                                               TimeSeries series,
                                               StrategyPropertyWrapper strategyProperties) {
        super(indicatorResolver, series, strategyProperties);
    }

    /**
     * Получить индикатор с преднастройками
     *
     * @param indicator             Тип индикатора
     * @param indicatorLogicalValue Значение индикатора указанное в логическом выражении (например в выражении RSI > 40 в данной переменной будет значение 40 )
     * @param expressionSymbol      Символ логического выражения
     * @return получить правило покупки индикатора
     */
    @Override
    protected Rule getIndicatorRule(Indicator indicator, String indicatorLogicalValue, ExpressionSymbol expressionSymbol) {
        IndicatorRequestWrapper requestWrapper = IndicatorRequestWrapper.builder()
                .timeSeries(series)
                .buyValue(indicatorLogicalValue)
                .expressionSymbolToBuyRule(expressionSymbol)
                .build();

        return super.indicatorResolver.resolveIndicator(indicator)
                .execute(requestWrapper)
                .getEntryRule();
    }

    protected Rule createByTradePropertiesRule(Rule baseRule, StrategyPropertyWrapper strategyProperties) {
        Rule sellRule = null;
        if (strategyProperties.getTrailingBuy() != null) {
            sellRule = new CustomTrailingStopLossRule(new ClosePriceIndicator(series), PrecisionNum.valueOf(strategyProperties.getTrailingBuy()));
        }
        return sellRule != null ? baseRule != null ? sellRule.or(baseRule) : sellRule : baseRule;
    }
}
