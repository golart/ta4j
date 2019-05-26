package org.ta4j.core.trading.rules;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
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
    @Getter
    @Setter
    private Num currentExtremum = null;
    /**
     * the current threshold
     */
    @Getter
    @Setter
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

    public CustomTrailingStopLossRule withThresholdPrice(Num threshold) {
        this.threshold = threshold;
        return this;
    }

    public CustomTrailingStopLossRule withPrevPrice(Num currentExtremum) {
        this.currentExtremum = currentExtremum;
        return this;
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
                    satisfied = isBuySatisfied(currentPrice, currentTrade.getEntry().getPrice());
                } else {
                    satisfied = isSellSatisfied(currentPrice);
                }

                if (satisfied) {
                    Bar bar = closePrice.getTimeSeries().getBar(index);
                    setEventType(tradingRecord, String.format("satisfied by trailing " +
                                    "start time: %s " +
                                    "end time: %s " +
                                    "current price: %s " +
                                    "buy price: %s " +
                                    "threshold: %s",
                            bar.getBeginTime(),
                            bar.getEndTime(),
                            bar.getClosePrice(),
                            currentTrade.getEntry().getPrice(),
                            threshold));
                }
            } else if (currentTrade.isNew()) {
                Num currentPrice = closePrice.getValue(index);
                satisfied = isSellSatisfied(currentPrice);
                if (satisfied) {
                    Bar bar = closePrice.getTimeSeries().getBar(index);
                    setEventType(tradingRecord, String.format("satisfied by trailing " +
                                    "start time: %s " +
                                    "end time: %s " +
                                    "current price: %s " +
                                    "threshold: %s",
                            bar.getBeginTime(),
                            bar.getEndTime(),
                            bar.getClosePrice(),
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

    private boolean isBuySatisfied(Num currentPrice, Num buyPrice) {
        boolean satisfied = false;
        if (currentExtremum == null) {
            currentExtremum = currentPrice.numOf(Float.MIN_VALUE);
        }
        if (takeProfitThreshold == null) {
            takeProfitThreshold = CustomTakeProfitRule.calculateProfitThresholdPrice(buyPrice, takeProfitPercentage);
        }
        if (currentPrice.isGreaterThan(currentExtremum) && currentPrice.isGreaterThanOrEqual(takeProfitThreshold)) {
            currentExtremum = currentPrice;
            Num lossRatioThreshold = currentPrice.numOf(100).minus(lossPercentage).dividedBy(currentPrice.numOf(100));
            threshold = currentExtremum.multipliedBy(lossRatioThreshold);
        }
        if (threshold != null) {
            satisfied = currentPrice.isLessThanOrEqual(threshold);
        }
        return satisfied;
    }
    
    public static Num calculateSellThresholdPrice(Num currentPrice, Num buyPrice, Num lossPercentage, Num takeProfitPercentage) {
        
        if (takeProfitPercentage != null && !takeProfitPercentage.isEqual(PrecisionNum.valueOf(0))) {            
            Num takeProfitPrice = CustomTakeProfitRule.calculateProfitThresholdPrice(buyPrice, takeProfitPercentage);
            
            if (currentPrice.isGreaterThanOrEqual(takeProfitPrice)) {
                Num lossRatioThreshold = currentPrice.numOf(100).minus(lossPercentage).dividedBy(currentPrice.numOf(100));
                return currentPrice.multipliedBy(lossRatioThreshold);
            }
            return null;
        }
        Num lossRatioThreshold = currentPrice.numOf(100).minus(lossPercentage).dividedBy(currentPrice.numOf(100));
        return currentPrice.multipliedBy(lossRatioThreshold);
    }
    
    public static Num calculateBuyThresholdPrice(Num currentPrice, Num lossPercentage) {
        Num lossRatioThreshold = currentPrice.numOf(100).plus(lossPercentage).dividedBy(currentPrice.numOf(100));
        return currentPrice.multipliedBy(lossRatioThreshold);
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
