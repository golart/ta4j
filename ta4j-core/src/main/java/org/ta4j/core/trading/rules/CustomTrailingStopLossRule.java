package org.ta4j.core.trading.rules;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.math.BigDecimal;

/**
 * @author VKozlov
 * Правило для подсчета покупки/продажи по трейлингу
 */
@Slf4j
public class CustomTrailingStopLossRule extends AbstractRule {

    /**
     * The close price indicator
     */
    private final ClosePriceIndicator closePrice;
    /**
     * the loss-distance as percentage
     */
    private final Num lossPercentage;
    /**
     * the current price extremum
     */
    private Num currentExtremum = null;
    /**
     * the current threshold
     */
    private Num threshold = null;
    private Num takeProfitThreshold = null;
    /**
     * the current trade
     */
    private Trade supervisedTrade;

    /**
     * Параметр используется ТОЛЬКО В РАСЧЕТАХ ДЛЯ TRAILING SELL!!!!
     */
    private Num takeProfitPercentage;

    public CustomTrailingStopLossRule(ClosePriceIndicator closePrice, Num lossPercentage) {
        this.closePrice = closePrice;
        this.lossPercentage = lossPercentage;
    }

    public CustomTrailingStopLossRule(ClosePriceIndicator closePrice, Num lossPercentage, BigDecimal takeProfitPercentage) {
        this(closePrice, lossPercentage);
        this.takeProfitPercentage = takeProfitPercentage != null ? PrecisionNum.valueOf(takeProfitPercentage) : PrecisionNum.valueOf(0);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no loss
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                if (!currentTrade.equals(supervisedTrade)) {
                    supervisedTrade = currentTrade;
                    currentExtremum = null;
                    takeProfitThreshold = null;
                    threshold = null;
                }
                Num currentPrice = closePrice.getValue(index);
                if (currentTrade.getEntry().isBuy()) {
                    satisfied = isBuySatisfied(currentPrice);
                } else {
                    satisfied = isSellSatisfied(currentPrice);
                }

                if (satisfied) {
                    setEventType(tradingRecord, String.format("satisfied by trailing " +
                                    "current price: %s " +
                                    "buy price: %s " +
                                    "threshold: %s",
                            currentPrice,
                            currentTrade.getEntry().getPrice(),
                            threshold));
                }
            } else if (currentTrade.isNew()) {
                Num currentPrice = closePrice.getValue(index);
                satisfied = isSellSatisfied(currentPrice);
                if (satisfied) {
                    setEventType(tradingRecord, String.format("satisfied by trailing " +
                                    "current price: %s " +
                                    "buy price: %s " +
                                    "threshold: %s",
                            currentPrice,
                            currentTrade.getEntry().getPrice(),
                            threshold));
                }
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    protected void traceIsSatisfied(int index, boolean isSatisfied) {
        log.trace("{}#isSatisfied({}): {}", className, index, isSatisfied);
    }

    private boolean isBuySatisfied(Num currentPrice) {
        boolean satisfied = false;
        if (currentExtremum == null) {
            currentExtremum = currentPrice.numOf(Float.MIN_VALUE);
        }
        if (takeProfitThreshold == null) {
            takeProfitThreshold = currentPrice.multipliedBy(currentPrice.numOf(100).minus(takeProfitPercentage).dividedBy(currentPrice.numOf(100)));
        }
        if (currentPrice.isGreaterThan(currentExtremum) && currentPrice.isGreaterThanOrEqual(takeProfitPercentage)) {
            currentExtremum = currentPrice;
            Num lossRatioThreshold = currentPrice.numOf(100).minus(lossPercentage).dividedBy(currentPrice.numOf(100));
            threshold = currentExtremum.multipliedBy(lossRatioThreshold);
        }
        if (threshold != null) {
            satisfied = currentPrice.isLessThanOrEqual(threshold);
        }
        return satisfied;
    }

    private boolean isSellSatisfied(Num currentPrice) {
        boolean satisfied = false;
        if (currentExtremum == null) {
            currentExtremum = currentPrice.numOf(Float.MAX_VALUE);
        }
        if (currentPrice.isLessThan(currentExtremum)) {
            currentExtremum = currentPrice;
            Num lossRatioThreshold = currentPrice.numOf(100).plus(lossPercentage).dividedBy(currentPrice.numOf(100));
            threshold = currentExtremum.multipliedBy(lossRatioThreshold);
        }
        if (threshold != null) {
            satisfied = currentPrice.isGreaterThanOrEqual(threshold);
        }
        return satisfied;
    }
}
