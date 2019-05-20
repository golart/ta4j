package org.ta4j.core.constructor;

import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.CustomTakeProfitRule;
import org.ta4j.core.trading.rules.CustomTrailingStopLossRule;
import org.ta4j.core.trading.rules.OrRule;

/**
 * @author VKozlov
 */
public class StrategyConstructorExitRuleBuilder extends StrategyConstructorRuleBuilder {

    public StrategyConstructorExitRuleBuilder(IIndicatorResolver indicatorResolver,
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
     * @return получить правило подажи индикатора
     */
    protected Rule getIndicatorRule(
            Indicator indicator,
            String indicatorLogicalValue,
            ExpressionSymbol expressionSymbol) {

        IndicatorRequestWrapper requestWrapper = IndicatorRequestWrapper.builder()
                .timeSeries(series)
                .sellValue(indicatorLogicalValue)
                .expressionSymbolToSellRule(expressionSymbol)
                .build();

        return super.indicatorResolver.resolveIndicator(indicator)
                .execute(requestWrapper)
                .getExitRule();
    }

    protected Rule createByTradePropertiesRule(Rule baseRule, StrategyPropertyWrapper strategyProperties) {
        Rule sellRule = null;
        if (strategyProperties.getTrailingSell() == null) {
//            if (strategyProperties.getStopLoss() != null) {
//                sellRule = new CustomStopLossRule(new ClosePriceIndicator(series), PrecisionNum.valueOf(strategyProperties.getStopLoss()), series);
//            }

            if (strategyProperties.getTakeProfit() != null) {
                if (sellRule != null) {
                    sellRule = new OrRule(sellRule, new CustomTakeProfitRule(new ClosePriceIndicator(series), PrecisionNum.valueOf(strategyProperties.getTakeProfit()), series));
                } else {
                    sellRule = new CustomTakeProfitRule(new ClosePriceIndicator(series), PrecisionNum.valueOf(strategyProperties.getTakeProfit()), series);
                }
            }
        } else if (strategyProperties.getTrailingSell() != null) {
            sellRule = new CustomTrailingStopLossRule(new ClosePriceIndicator(series),
                    PrecisionNum.valueOf(strategyProperties.getTrailingSell()),
                    strategyProperties.getTakeProfit());
        }
        return sellRule != null ? baseRule != null ? sellRule.or(baseRule) : sellRule : baseRule;
    }
}
