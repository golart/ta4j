package org.ta4j.core.constructor;

import lombok.Builder;
import lombok.Getter;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.Indicator;
import org.ta4j.core.data.strategy.StrategyExpressionParserWrapper;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;
import org.ta4j.core.trading.rules.ChainRule;
import org.ta4j.core.utils.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @author VKozlov
 * Составление правила по заданной строке
 */
public abstract class StrategyConstructorRuleBuilder {

    protected final IIndicatorResolver indicatorResolver;
    protected final TimeSeries series;
    protected final StrategyPropertyWrapper strategyProperties;
    protected final List<StopLossOrTrailingIndicatorWrapper> lossOrTrailingIndicatorWrappers = new ArrayList<>(4);
    private Rule baseRule = null;

    public StrategyConstructorRuleBuilder(
            IIndicatorResolver indicatorResolver,
            TimeSeries series,
            StrategyPropertyWrapper strategyProperties) {
        this.indicatorResolver = indicatorResolver;
        this.series = series;
        this.strategyProperties = strategyProperties;
    }

    public Rule createRule(final HashMap<Integer, List<StrategyExpressionParserWrapper>> map) {
        this.baseRule = null;
        lossOrTrailingIndicatorWrappers.clear();

        List<Integer> sortedKeys = new ArrayList<>(map.size());
        sortedKeys.addAll(map.keySet());
        sortedKeys.sort(Comparator.reverseOrder());

        sortedKeys.forEach(key -> map.get(key).forEach(this::parseExpression));
        return baseRule;
    }

    public Rule createRule(final List<HashMap<Integer, List<StrategyExpressionParserWrapper>>> list) {
        if (list.size() == 1) {
            return createByTradePropertiesRule(createRule(list.get(0)), upgradeTradeProperties(strategyProperties));
        }

        ChainRule chainRule = new ChainRule();
        ChainRule prevChainRule = null;
        for (int i = 0; i < list.size() - 1; i++) {
            StrategyPropertyWrapper properties = new StrategyPropertyWrapper();
            if (prevChainRule == null) {
                prevChainRule = chainRule;
            }

            Rule ruleLeft = createByTradePropertiesRule(createRule(list.get(i)), upgradeTradeProperties(properties));
            prevChainRule.setRule1(ruleLeft);

            Rule ruleRight = new ChainRule();
            if (i + 1 == list.size() - 1) {
                ruleRight = createByTradePropertiesRule(createRule(list.get(i + 1)), upgradeTradeProperties(properties));
                prevChainRule.setRule2(ruleRight);
                break;
            }
            prevChainRule.setRule2(ruleRight);
            prevChainRule = (ChainRule) ruleRight;
        }
        this.baseRule = chainRule;
        return chainRule;
    }

    private void parseExpression(final StrategyExpressionParserWrapper wrapper) {
        String expression = wrapper.getExpression();
        ExpressionSymbol splitSymbol = ExpressionSymbol.findJoinSymbol(expression);

        if (splitSymbol != null) {
            String[] expressionPairs = expression.split(splitSymbol.getSymbol(), 2);
            if (expressionPairs.length == 0) {
                throw new RuntimeException(String.format("Error parse expression %s\"", expression));
            }

            final StrategyExpressionParserWrapper leftWrapper = StrategyExpressionParserWrapper.builder()
                    .rightOperation(splitSymbol)
                    .leftOperation(wrapper.getLeftOperation())
                    .expression(expressionPairs[0])
                    .build();
            parseLeftPartOfExpression(leftWrapper);

            if (expressionPairs.length == 2) {
                final StrategyExpressionParserWrapper rightWrapper = StrategyExpressionParserWrapper.builder()
                        .rightOperation(splitSymbol)
                        .expression(expressionPairs[1])
                        .build();
                parseRightPartOfExpression(rightWrapper);
            }
        } else {
            parseLeftPartOfExpression(wrapper);
        }
    }

    /**
     * Формируем правило по левой стороне выражения
     */
    private void parseLeftPartOfExpression(final StrategyExpressionParserWrapper wrapper) {
        String expression = wrapper.getExpression();
        Rule indicatorRule = createIndicatorRule(expression);
        indicatorRule = upgradeRule(indicatorRule, wrapper);
        if (this.baseRule == null) {
            this.baseRule = indicatorRule;
        }
        joinRule(this.baseRule, indicatorRule, wrapper);
    }

    /**
     * Подготавливаем и выполняем индикатор и формируем правило с этим индикатором
     *
     * @param expression выражение
     * @return правило индикатора
     */
    private Rule createIndicatorRule(final String expression) {
        String indicatorName;
        String indicatorLogicalValue = "";
        ExpressionSymbol splitSymbol = ExpressionSymbol.findLogicalSymbol(expression);

        if (splitSymbol == null) {
//            throw new BotException(String.format(PARSE_STRATEGY_EXCPRESSION_ERROR, expression));
            indicatorName = expression;
        } else {
            indicatorName = expression.substring(0, expression.indexOf(splitSymbol.getSymbol()));
            indicatorLogicalValue = expression.substring(indicatorName.length() + splitSymbol.getSymbol().length());
        }
        Indicator indicator = Indicator.getByName(indicatorName);
        if (indicator == null) {
            if (splitSymbol != null) {
                indicatorLogicalValue = expression.substring(0, expression.indexOf(splitSymbol.getSymbol()));
                indicatorName = expression.substring(indicatorLogicalValue.length() + splitSymbol.getSymbol().length());
                indicator = Indicator.getByName(indicatorName);
            }

            if (indicator == null) {
                throw new RuntimeException(String.format("Error parse expression %s", expression));
            }
        }
        //Проверям является ли текущий индикатором стоп лоссом или трейлингом и если да то складываем его в коллекцию
        if (indicator.isLossOrTraining()) {
            lossOrTrailingIndicatorWrappers.add(
                    StopLossOrTrailingIndicatorWrapper.builder()
                            .indicator(indicator)
                            .indicatorLogicalValue(indicatorLogicalValue)
                            .build());
            return null;
        }
        return getIndicatorRule(indicator, indicatorLogicalValue, splitSymbol);
    }

    /**
     * Парсинг правой части выражения
     */
    private void parseRightPartOfExpression(final StrategyExpressionParserWrapper wrapper) {
        parseExpression(wrapper);
    }

    /**
     * Объединение правил в логические выражения
     *
     * @param leftRule  правило к которому мы присоединяемся
     * @param rightRule присоединяемое правило
     * @param wrapper   Сущность содержащая операцию присоединения
     */
    private void joinRule(Rule leftRule, Rule rightRule, final StrategyExpressionParserWrapper wrapper) {
        if (rightRule == null) {
            return;
        }

        ExpressionSymbol expressionSymbol = null;

        if (wrapper.getRightOperation() != null) {
            expressionSymbol = wrapper.getRightOperation();
        } else if (wrapper.getLeftOperation() != null) {
            expressionSymbol = wrapper.getLeftOperation();
        }

        if (expressionSymbol == null) {
            return;
        }

        switch (expressionSymbol) {
            case AND:
                if (this.baseRule.hashCode() != rightRule.hashCode()) {
                    this.baseRule = leftRule.and(rightRule);
                }
                return;
            case OR:
                if (this.baseRule.hashCode() != rightRule.hashCode()) {
                    this.baseRule = leftRule.or(rightRule);
                }
                return;
            case NEGATION:
                this.baseRule = rightRule.negation();
                return;
        }
        throw new RuntimeException("Error join rule of expression");
    }

    /**
     * Обновление правила
     *
     * @param upgradeRule правило к которому мы присоединяемся
     * @param wrapper     Сущность содержащая операцию присоединения
     */
    private Rule upgradeRule(Rule upgradeRule, final StrategyExpressionParserWrapper wrapper) {
        if (upgradeRule == null) {
            return upgradeRule;
        }

        ExpressionSymbol expressionSymbol = null;
        if (wrapper.getLeftOperation() != null) {
            expressionSymbol = wrapper.getLeftOperation();
        }

        if (expressionSymbol == null) {
            return upgradeRule;
        }

        switch (expressionSymbol) {
            case NEGATION:
                return upgradeRule.negation();
        }
        return upgradeRule;
    }

    /**
     * Обновление торговых настроек
     */
    private StrategyPropertyWrapper upgradeTradeProperties(StrategyPropertyWrapper strategyProperties) {
        if (CollectionUtils.isEmpty(lossOrTrailingIndicatorWrappers)) {
            return strategyProperties;
        }

        for (StopLossOrTrailingIndicatorWrapper wrapper : lossOrTrailingIndicatorWrappers) {
            switch (wrapper.getIndicator()) {
                case STOP_LOSS:
//                    strategyProperties.setStopLoss(Integer.valueOf(wrapper.getIndicatorLogicalValue()));
                    break;
                case TAKE_PROFIT:
                    strategyProperties.setTakeProfit(new BigDecimal(wrapper.getIndicatorLogicalValue()));
                    break;
                case TRAILING_BUY:
                    strategyProperties.setTrailingBuy(new BigDecimal(wrapper.getIndicatorLogicalValue()));
                    break;
                case TRAILING_SELL:
                    strategyProperties.setTrailingSell(new BigDecimal(wrapper.getIndicatorLogicalValue()));
                    break;
            }
        }
        return strategyProperties;
    }

    /**
     * Сущеость для хранения имени и значения индикаторов трейлинга и стоплосса указанные в правиле
     */
    @Builder
    @Getter
    private static class StopLossOrTrailingIndicatorWrapper {
        private Indicator indicator;
        private String indicatorLogicalValue;
    }

    protected abstract Rule getIndicatorRule(Indicator indicator, String indicatorLogicalValue, ExpressionSymbol splitSymbol);

    protected abstract Rule createByTradePropertiesRule(Rule baseRule, StrategyPropertyWrapper strategyProperties);
}
