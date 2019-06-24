package org.ta4j.core.constructor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import org.ta4j.core.trading.rules.RuleReset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author VKozlov
 */
@Slf4j
public class StrategyConstructor implements IStrategyConstructor {

    @Setter
    @Getter
    private final StrategyConstructorExpressionParser expressionParser;

    @Setter
    @Getter
    private final IIndicatorResolver indicatorResolver;

    @Setter
    @Getter
    private StrategyPropertyWrapper strategyProperty;

    private List<RuleReset> listToReset;

    public StrategyConstructor() {
        this.indicatorResolver = new IndicatorResolverImpl();
        this.expressionParser = new StrategyConstructorExpressionParser();
        this.listToReset = new ArrayList<>();
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
                series,
                listToReset);
    }

    @Override
    public Rule createRuleToBuy(String ruleToBuyExpression, TimeSeries series) {
        return createRule(ruleToBuyExpression, new StrategyConstructorEntryRuleBuilder(indicatorResolver, series, strategyProperty));
    }

    @Override
    public Rule createRuleToSell(String ruleToSellExpression, TimeSeries series) {
        StrategyConstructorExitRuleBuilder constructorExitRuleBuilder =
                new StrategyConstructorExitRuleBuilder(indicatorResolver, series, strategyProperty);

        CustomStopLossRule stopLossRule = null;
        if (strategyProperty.getStopLoss() != null) {
            stopLossRule = new CustomStopLossRule(new ClosePriceIndicator(series), PrecisionNum.valueOf(strategyProperty.getStopLoss()), series);
            CustomStopLossRule finalStopLossRule = stopLossRule;
            constructorExitRuleBuilder.withDisabledEvent(() -> {
                finalStopLossRule.setDisabled(true);
            });
            listToReset.add(stopLossRule);
        }
        Rule sellRule = createRule(ruleToSellExpression, constructorExitRuleBuilder);

        return createStopLossRule(sellRule, stopLossRule);
    }

    private Rule createRule(String ruleExpression,
                            StrategyConstructorRuleBuilder ruleBuilder) {
        final List<HashMap<Integer, List<StrategyExpressionParserWrapper>>> buyExpressionParts = expressionParser.parseExpression(ruleExpression);
        return ruleBuilder.createRule(buyExpressionParts);
    }

    private Rule createStopLossRule(Rule baseRule, Rule stopLossRule) {
        return stopLossRule != null ? baseRule != null ? stopLossRule.or(baseRule) : stopLossRule : baseRule;
    }
}
