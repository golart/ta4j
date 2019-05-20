/*******************************************************************************
 *   The MIT License (MIT)
 *
 *   Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2018 Ta4j Organization 
 *   & respective authors (see AUTHORS)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy of
 *   this software and associated documentation files (the "Software"), to deal in
 *   the Software without restriction, including without limitation the rights to
 *   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *   the Software, and to permit persons to whom the Software is furnished to do so,
 *   subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *   FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.ta4j.core.trading.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Rule;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.utils.StringUtils;

/**
 * An abstract trading {@link Rule rule}.
 */
public abstract class AbstractRule implements Rule {

    /**
     * The logger
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The class name
     */
    protected final String className = getClass().getSimpleName();

    /**
     * Traces the isSatisfied() method calls.
     *
     * @param index       the bar index
     * @param isSatisfied true if the rule is satisfied, false otherwise
     */
    protected void traceIsSatisfied(int index, boolean isSatisfied) {
        log.trace("{}#isSatisfied({}): {}", className, index, isSatisfied);
    }

    protected void setEventType(TradingRecord tradingRecord, String eventType) {
        if (tradingRecord == null) {
            return;
        }
        
        Trade currentTrade = tradingRecord.getCurrentTrade();
        if (currentTrade != null) {
            if (tradingRecord.getLastOrder() == null || tradingRecord.getLastOrder().isSell()) {
                String prevEvent = "";
                if (!StringUtils.isEmpty(tradingRecord.getCurrentTrade().getEventTypeBuy())) {
                    prevEvent = tradingRecord.getCurrentTrade().getEventTypeBuy() + " -> ";
                }
                tradingRecord.getCurrentTrade().setEventTypeBuy(prevEvent + eventType);
            } else if (tradingRecord.getLastOrder() != null && tradingRecord.getLastOrder().isBuy()) {
                String prevEvent = "";
                if (!StringUtils.isEmpty(tradingRecord.getCurrentTrade().getEventTypeSell())) {
                    prevEvent = tradingRecord.getCurrentTrade().getEventTypeSell() + " -> ";
                }
                tradingRecord.getCurrentTrade().setEventTypeSell(prevEvent + eventType);
                
            }
        }
    }
}
