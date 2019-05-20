package org.ta4j.core.constructor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.ta4j.core.data.ExpressionSymbol;
import org.ta4j.core.data.strategy.StrategyExpressionParserWrapper;
import org.ta4j.core.utils.CollectionUtils;
import org.ta4j.core.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author VKozlov
 * Класс для парсинга выражения и преобразования его структуры в иерархию или же стек для обработки выражений по приоритетам ( приоритет указывается в скобках)
 */
public class StrategyConstructorExpressionParser {

    private final String LEFT_BRACKET = "(";
    private final String RIGHT_BRACKET = ")";

    public List<HashMap<Integer, List<StrategyExpressionParserWrapper>>> parseExpression(String expression) {

        if (ExpressionSymbol.isRuleChain(expression)) {
            return Stream.of(expression.split(ExpressionSymbol.RULE_CHAIN.getSymbol()))
                    .map(ex -> {
                        ExpressionConstructorBuildWrapper buildWrapper = createBuildWrapper(ex);
                        parseExpressionInParentheses(buildWrapper);
                        return buildWrapper.getMap();
                    })
                    .collect(Collectors.toList());
        }

        ExpressionConstructorBuildWrapper buildWrapper = createBuildWrapper(expression);
        parseExpressionInParentheses(buildWrapper);
        return Collections.singletonList(buildWrapper.getMap());
    }

    /**
     * Создаем объект для хранения всех сущностей необходимых для парсинга выражения
     *
     * @param expression - строка с правилом
     */
    private ExpressionConstructorBuildWrapper createBuildWrapper(String expression) {
        String expressionWithoutSpaces = expression.replace(" ", "");
        return ExpressionConstructorBuildWrapper.builder()
                .level(0)
                .parserWrapper(StrategyExpressionParserWrapper.builder()
                        .expression(expressionWithoutSpaces)
                        .build())
                .stack(new Stack<>())
                .map(new HashMap<>())
                .build();
    }

    /**
     * Парсим значение логического выражения по левой и правой скобке
     */
    private void parseExpressionInParentheses(ExpressionConstructorBuildWrapper buildWrapper) {
        StrategyExpressionParserWrapper parserWrapper = buildWrapper.getParserWrapper();
        Integer level = buildWrapper.getLevel();

        String expression = parserWrapper.getExpression();

        if (StringUtils.isEmpty(expression)) {
            return;
        }

        String[] expressionParts = expression.split("\\" + LEFT_BRACKET);

        for (String expressionPart : expressionParts) {
            if (!StringUtils.isEmpty(expressionPart)) {
                level = level + 1;
                String[] expressionRightsParts = expressionPart.split("\\" + RIGHT_BRACKET);
                for (String expressionRightPart : expressionRightsParts) {

                    ExpressionSymbol expressionSymbol = null;
                    //Проверяем есть ли логический символ после скобкой и если он найден то ставим его
                    for (ExpressionSymbol es : ExpressionSymbol.values()) {
                        if (expressionPart.endsWith(es.getSymbol())) {
                            expressionSymbol = es;
                            if (expressionRightPart.contains(expressionSymbol.getSymbol())) {
                                expressionRightPart = expressionRightPart
                                        .substring(0, expressionRightPart.lastIndexOf(expressionSymbol.getSymbol()));
                            }
                            break;
                        }
                    }
                    if (!StringUtils.isEmpty(expressionRightPart)) {
                        level = level - 1;
                    }

                    StrategyExpressionParserWrapper newWrapper = StrategyExpressionParserWrapper.builder()
                            .expression(expressionRightPart)
                            .rightOperation(expressionSymbol)
                            .build();
                    buildWrapper.setParserWrapper(newWrapper);
                    buildHierarchyByExpression(buildWrapper);
                }
            } else {
                level = level + 1;
            }
        }
    }

    /**
     * Строим иерархию приоритетов выражения и складываем в стек
     */
    private void buildHierarchyByExpression(ExpressionConstructorBuildWrapper buildWrapper) {

        StrategyExpressionParserWrapper parserWrapper = buildWrapper.getParserWrapper();
        Stack<StrategyExpressionParserWrapper> stack = buildWrapper.getStack();
        HashMap<Integer, List<StrategyExpressionParserWrapper>> map = buildWrapper.getMap();
        Integer level = buildWrapper.getLevel();

        String expression = buildWrapper.getParserWrapper().getExpression();
        if (StringUtils.isEmpty(expression)) {
            return;
        }

        //Проверяем есть ли логический символ перед скобкой и если он найден то ставим его
        for (ExpressionSymbol es : ExpressionSymbol.values()) {
            if (expression.startsWith(es.getSymbol())) {
                expression = expression.substring(expression.indexOf(es.getSymbol()) + es.getSymbol().length());
                parserWrapper.setExpression(expression);
                parserWrapper.setLeftOperation(es);
                break;
            }
        }

        if (parserWrapper.getLeftOperation() == null && parserWrapper.getRightOperation() == null && stack.size() > 0) {
            StrategyExpressionParserWrapper prevWrapper = stack.get(stack.size() - 1);
            parserWrapper.setLeftOperation(prevWrapper.getRightOperation());
            parserWrapper.setRightOperation(prevWrapper.getLeftOperation());
        }

        stack.push(parserWrapper);
        if (CollectionUtils.isEmpty(map.get(level))) {
            map.put(level, new ArrayList<>());
        }
        map.get(level).add(parserWrapper);
    }

    @Builder
    @Getter
    private static class ExpressionConstructorBuildWrapper {
        /**
         * Текущая часть выражения
         */
        @Setter
        private StrategyExpressionParserWrapper parserWrapper;
        /**
         * Текуший уровень выражения (уровень вложенности указывается в скобках)
         */
        private Integer level;
        /**
         * Части выражения отсортированные по уровню вложенности
         */
        private HashMap<Integer, List<StrategyExpressionParserWrapper>> map;
        /**
         * Стек куда складываются все части выражения с каждой интерацией парсинга
         */
        private Stack<StrategyExpressionParserWrapper> stack;
    }
}
