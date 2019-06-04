package org.ta4j.core.data.strategy;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.ta4j.core.data.ExpressionSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VKozlov
 * Сущность для хранения данных при парсинге логического выражения стратегии
 */
@Builder
@Getter
@Setter
public class StrategyExpressionParserWrapper {

    private String expression;

    private ExpressionSymbol rightOperation;

    private ExpressionSymbol leftOperation;
}
