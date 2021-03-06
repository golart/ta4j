package org.ta4j.core.trading.rules;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.data.event.DisabledRuleEvent;
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
    private boolean takeProfitThresholdisSatisfied = false;
    /**
     * the current trade
     */
    private Trade supervisedTrade;

    /**
     * Параметр используется ТОЛЬКО В РАСЧЕТАХ ДЛЯ TRAILING SELL!!!!
     */
    private Num takeProfitPercentage;

    /**
     * Событие отмены индикатора если текущее событие выполняется
     */
    private DisabledRuleEvent disabledStopLossRuleEvent;

    public CustomTrailingStopLossRule(ClosePriceIndicator closePrice, Num lossPercentage) {
        this.closePrice = closePrice;
        this.lossPercentage = lossPercentage;
    }

    public CustomTrailingStopLossRule(ClosePriceIndicator closePrice, Num lossPercentage, BigDecimal takeProfitPercentage) {
        this(closePrice, lossPercentage);
        this.takeProfitPercentage = takeProfitPercentage != null ? PrecisionNum.valueOf(takeProfitPercentage) : null;
    }

    public CustomTrailingStopLossRule withThresholdPrice(Num threshold) {
        this.threshold = threshold;
        return this;
    }

    public CustomTrailingStopLossRule withPrevPrice(Num currentExtremum) {
        this.currentExtremum = currentExtremum;
        return this;
    }

    public CustomTrailingStopLossRule withDisabledEvent(DisabledRuleEvent disabledEvent) {
        this.disabledStopLossRuleEvent = disabledEvent;
        return this;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        if (disabledStopLossRuleEvent != null) {
            disabledStopLossRuleEvent.disabled();
        }

        boolean satisfied = false;
        Bar bar = closePrice.getTimeSeries().getBar(index);
        Num currentPrice = closePrice.getValue(index);
        // No trading history or no trade opened, no loss
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                if (!currentTrade.equals(supervisedTrade)) {
                    supervisedTrade = currentTrade;
                    currentExtremum = null;
                    takeProfitThreshold = null;
                    threshold = null;
                    takeProfitThresholdisSatisfied = false;
                }
                if (currentTrade.getEntry().isBuy()) {
                    satisfied = isBuySatisfied(currentPrice, currentTrade.getEntry().getPrice());
                } else {
                    satisfied = isSellSatisfied(currentPrice);
                }

                if (satisfied) {
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
                if (!currentTrade.equals(supervisedTrade)) {
                    supervisedTrade = currentTrade;
                    currentExtremum = null;
                    takeProfitThreshold = null;
                    threshold = null;
                    takeProfitThresholdisSatisfied = false;
                }
                satisfied = isSellSatisfied(currentPrice);
                if (satisfied) {
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
        log.info("TrailingStopLossRule satisfied: {} start time: {},  end time: {}, current price: {}," +
                        " threshold: {}, takeProfitThreshold: {}, takeProfitThresholdisSatisfied: {}, currentExtremum: {}, index {} ",
                satisfied,
                bar.getBeginTime(),
                bar.getEndTime(),
                currentPrice,
                threshold,
                takeProfitThreshold,
                takeProfitThresholdisSatisfied,
                currentExtremum,
                index);
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    protected void traceIsSatisfied(int index, boolean isSatisfied) {
        log.trace("{}#isSatisfied({}): {}", className, index, isSatisfied);
    }

    private boolean isBuySatisfied(Num currentPrice, Num buyPrice) {
        boolean satisfied = false;
        boolean trade = true;

        if (currentExtremum == null) {
            currentExtremum = currentPrice.numOf(Float.MIN_VALUE);
        }
        if (takeProfitThreshold == null && takeProfitPercentage != null && !takeProfitPercentage.isNaN()) {
            takeProfitThreshold = CustomTakeProfitRule.calculateProfitThresholdPrice(buyPrice, takeProfitPercentage);
            trade = currentPrice.isGreaterThanOrEqual(takeProfitThreshold);
        }
        if (currentPrice.isGreaterThan(currentExtremum) && trade) {
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
