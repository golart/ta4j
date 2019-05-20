package org.ta4j.core.data.strategy;

import lombok.Data;
import org.ta4j.core.Rule;

/**
 * @author VKozlov
 * Сущность для хранения данных при конструировании стратегии
 */
@Data
public class StrategyConstructorWrapper {
    
    private Rule rule;
}
