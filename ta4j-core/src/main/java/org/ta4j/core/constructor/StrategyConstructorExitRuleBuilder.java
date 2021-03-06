package org.ta4j.core.constructor;

import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.event.DisabledRuleEvent;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.CustomTakeProfitRule;
import org.ta4j.core.trading.rules.CustomTrailingStopLossRule;
import org.ta4j.core.trading.rules.OrRule;

import java.util.List;

/**
 * @author VKozlov
 */
public class StrategyConstructorExitRuleBuilder extends StrategyConstructorRuleBuilder {
    /**
     * Событие оключения индикатора
     */
    private DisabledRuleEvent disabledRuleEvent;

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
            List<String> params,
            ExpressionSymbol expressionSymbol) {

        IndicatorRequestWrapper requestWrapper = IndicatorRequestWrapper.builder()
                .timeSeries(series)
                .params(params)
                .sellValue(indicatorLogicalValue)
                .expressionSymbolToSellRule(expressionSymbol)
                .build();

        return super.indicatorResolver.resolveIndicator(indicator)
                .execute(requestWrapper)
                .getExitRule();
    }

    public StrategyConstructorExitRuleBuilder withDisabledEvent(DisabledRuleEvent disabledEvent) {
        this.disabledRuleEvent = disabledEvent;
        return this;
    }

    protected Rule createByTradePropertiesRule(Rule baseRule, StrategyPropertyWrapper strategyProperties) {
        Rule sellRule = null;
        if (strategyProperties.getTrailingSell() == null) {
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
                    strategyProperties.getTakeProfit())
                .withDisabledEvent(disabledRuleEvent);
        }
        return sellRule != null ? baseRule != null ? sellRule.or(baseRule) : sellRule : baseRule;
    }
}
