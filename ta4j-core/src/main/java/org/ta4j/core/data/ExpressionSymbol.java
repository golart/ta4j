package org.ta4j.core.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ta4j.core.num.Num;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * @author VKozlov
 * Операции доступные при составлении выражений для конструктора
 */
@AllArgsConstructor
public enum ExpressionSymbol {

    AND("&&", (num, num2) -> true),
    OR("or", (num, num2) -> true),
    NEGATION("!", (num, num2) -> true),
    MORE_OR_EQUAL(">=", Num::isGreaterThanOrEqual),
    MORE(">", Num::isGreaterThan),
    LESS_OR_EQUAL("=<", Num::isLessThanOrEqual),
    LESS("<", Num::isLessThan),
    EQUAL("==", Num::isEqual),
    EQUAL_SECOND("=", Num::isEqual),
    RULE_CHAIN("->", (num, num2) -> true);

    @Getter
    private String symbol;
    @Getter
    private BiPredicate<Num, Num> expressionOperation;

    public static ExpressionSymbol findJoinSymbol(final String expression) {
       return Stream.of(AND, OR, NEGATION)
               .map(expressionSymbol -> new AbstractMap.SimpleEntry<>(expressionSymbol, expression.indexOf(expressionSymbol.getSymbol())))
               .filter(esise -> esise.getValue() > 0)
               .min(Comparator.comparing(AbstractMap.SimpleEntry::getValue))
               .map(AbstractMap.SimpleEntry::getKey)
               .orElse(null);
    }

    public static ExpressionSymbol findLogicalSymbol(final String expression) {
        return Stream.of(MORE_OR_EQUAL, MORE, LESS_OR_EQUAL, LESS, EQUAL, EQUAL_SECOND).filter(expressionSymbol ->
                expression.contains(expressionSymbol.getSymbol())).findFirst().orElse(null);
    }

    public static boolean isRuleChain(final String expression) {
        return Stream.of(RULE_CHAIN).anyMatch(expressionSymbol ->
                expression.contains(expressionSymbol.getSymbol()));
    }
}
