package org.ta4j.core.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Действая с ордером вычесленное по алгоритму
 */
@Getter
@AllArgsConstructor
public enum TradeAction {

    BUY, //купить 
    SELL, //продать 
    NOTHING; //ничего не делать
}
