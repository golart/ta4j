package org.ta4j.core.data.event;

/**
 * @author VKozlov
 * Событие для отключения правила по срабатыванию дргого правила
 */
@FunctionalInterface
public interface DisabledRuleEvent {

    void disabled();
}
