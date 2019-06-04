package org.ta4j.core.constructor;

import lombok.Getter;
import lombok.Setter;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.data.strategy.StrategyExpressionParserWrapper;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.withRule.IndicatorResolverImpl;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.CustomStopLossRule;

import java.util.HashMap;
import java.util.List;

/**
 * @author VKozlov
 */
public class StrategyConstructorImpl implements IStrategyConstructor {

    @Setter
    @Getter
    private final StrategyConstructorExpressionParser expressionParser;
    @Setter
    @Getter
    private final IIndicatorResolver indicatorResolver;
    @Setter
    @Getter
    private StrategyPropertyWrapper strategyProperty;

    public StrategyConstructorImpl() {
        this.indicatorResolver = new IndicatorResolverImpl();
        this.expressionParser = new StrategyConstructorExpressionParser();
    }

    @Override
    public Strategy createStrategyByExpression(String ruleToBuyExpression,
                                               String ruleToSellExpression,
                                               final TimeSeries series,
                                               final StrategyPropertyWrapper strategyProperty) {
        this.strategyProperty = strategyProperty;
        return new BaseStrategy(
                createRuleToBuy(ruleToBuyExpression, series),
                createRuleToSell(ruleToSellExpression, series),
                series);
    }

    @Override
    public Rule createRuleToBuy(String ruleToBuyExpression, TimeSeries series) {
        return createRule(ruleToBuyExpression, new StrategyConstructorEntryRuleBuilder(indicatorResolver, series, strategyProperty));
    }

    @Override
    public Rule createRuleToSell(String ruleToSellExpression, TimeSeries series) {
        return createStopLossRule(createRule(ruleToSellExpression, new StrategyConstructorExitRuleBuilder(indicatorResolver, series, strategyProperty)), series);
    }

    private Rule createRule(String ruleExpression,
                            StrategyConstructorRuleBuilder ruleBuilder) {
        final List<HashMap<Integer, List<StrategyExpressionParserWrapper>>> buyExpressionParts = expressionParser.parseExpression(ruleExpression);
        return ruleBuilder.createRule(buyExpressionParts);
    }

    private Rule createStopLossRule(Rule baseRule, TimeSeries series) {
        Rule sellRule = null;
        if (strategyProperty.getStopLoss() != null) {
            sellRule = new CustomStopLossRule(new ClosePriceIndicator(series), PrecisionNum.valueOf(strategyProperty.getStopLoss()), series);
        }
        return sellRule != null ? baseRule != null ? sellRule.or(baseRule) : sellRule : baseRule;
    }
}
