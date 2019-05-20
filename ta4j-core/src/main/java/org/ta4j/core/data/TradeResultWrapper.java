package org.ta4j.core.data;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.ta4j.core.Rule;

/**
 * @author VKozlov
 * Сущность содержащая ответ обработки индикатора
 */
@ToString(of = "value")
@Data
@Builder
public class TradeResultWrapper {

    /**
     * Действия с ордером
     */
    private TradeAction action;

    /**
     * Значение, вычесленное по алгоритму
     */
    private String value;

    /**
     * Правило входа
     */
    private Rule entryRule;

    /**
     * Правило выхода
     */
    private Rule exitRule;
}
