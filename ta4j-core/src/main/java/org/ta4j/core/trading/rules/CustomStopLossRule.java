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

        Num entryPrice = tradingRecord.getCurrentTrade().getEntry().getPrice();
        Num currentPrice = closePrice.getValue(index);
        Num threshold = entryPrice.multipliedBy(lossRatioThreshold);
        if (satisfied) {
            setEventType(tradingRecord, String.format("satisfied by stop loss " +
                            "current price: %s " +
                            "threshold: %s",
                    currentPrice,
                    threshold));
        }
        return satisfied;
    }
}
