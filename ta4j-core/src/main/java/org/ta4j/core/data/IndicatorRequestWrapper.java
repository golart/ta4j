package org.ta4j.core.data;

import lombok.Builder;
import lombok.Data;
import org.ta4j.core.TimeSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VKozlov
 * Сущность содержащая данные для индикатора
 */
@Data
@Builder
public class IndicatorRequestWrapper {

    /**
     * Значения для покупки
     */
    private String buyValue;

    /**
     * Значение для продажи
     */
    private String sellValue;

    /**
     * Параметры выражения
     */
    private List<String> params = new ArrayList<>();

    /**
     * Логический символ для правила покупки
     */
    private ExpressionSymbol expressionSymbolToBuyRule;
    /**
     * Логический символ для правила продажи
     */
    private ExpressionSymbol expressionSymbolToSellRule;
    
    /**
     * Значения свечей
     */
    private TimeSeries timeSeries;
}
