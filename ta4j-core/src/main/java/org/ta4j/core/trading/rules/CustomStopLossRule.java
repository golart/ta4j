package org.ta4j.core.trading.rules;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

/**
 * @author VKozlov
 */
@Slf4j
public class CustomStopLossRule extends org.ta4j.core.trading.rules.StopLossRule {

    private org.slf4j.Logger tradeLogger = org.slf4j.LoggerFactory.getLogger("backtest-logger");

    private ClosePriceIndicator closePrice;
    private TimeSeries series;
    private Num lossRatioThreshold;

    public CustomStopLossRule(ClosePriceIndicator closePrice, Num lossPercentage, final TimeSeries series) {
        super(closePrice, lossPercentage);
        this.series = series;
        this.closePrice = closePrice;
        this.lossRatioThreshold = series.numOf(100).minus(lossPercentage).dividedBy(series.numOf(100));
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = super.isSatisfied(index, tradingRecord);

        if (tradingRecord.getCurrentTrade().isOpened()) {
            Num entryPrice = tradingRecord.getCurrentTrade().getEntry().getPrice();
            Num currentPrice = closePrice.getValue(index);
            Num threshold = entryPrice.multipliedBy(lossRatioThreshold);
            Bar bar = series.getBar(index);
            if (satisfied) {
                setEventType(tradingRecord, String.format("satisfied by stop loss " +
                                "start time: %s " +
                                "end time: %s " +
                                "current price: %s " +
                                "threshold: %s",
                        bar.getBeginTime(),
                        bar.getEndTime(),
                        currentPrice,
                        threshold));
            }
            log.info("StopLossRule satisfied: {} start time: {},  end time: {}, current price: {}, threshold: {}, index {} ",
                    satisfied,
                    bar.getBeginTime(),
                    bar.getEndTime(),
                    currentPrice,
                    threshold,
                    index);
        }

        return satisfied;
    }
}
