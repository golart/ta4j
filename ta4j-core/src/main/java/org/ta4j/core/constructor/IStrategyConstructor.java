package org.ta4j.core.constructor;

import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;

/**
 * @author VKozlov
 * Конструктор статегии из логического выражения
 */
public interface IStrategyConstructor {

    /**
     * Создать стратегию по выражению (стратегия содержит 2 правила на покупку и на продажу)
     *
     * @param ruleToBuyExpression  выражение для правила продажи
     * @param ruleToSellExpression выражение для правила покупки
     * @param series               данные о свечах
     * @return Стартегия
     */
    Strategy createStrategyByExpression(String ruleToBuyExpression,
                                        String ruleToSellExpression,
                                        final TimeSeries series,
                                        final StrategyPropertyWrapper strategyProperty);

    /**
     * Создать правило на покупку
     *
     * @param ruleToBuyExpression выражение для правила продажи
     * @param series              данные о свечах
     * @return Правило покупки
     */
    Rule createRuleToBuy(String ruleToBuyExpression, final TimeSeries series);

    /**
     * Создать правило на покупку
     *
     * @param ruleToSellExpression выражение для правила продажи
     * @param series               данные о свечах
     * @return Правило покупки
     */
    Rule createRuleToSell(String ruleToSellExpression, final TimeSeries series);
}
