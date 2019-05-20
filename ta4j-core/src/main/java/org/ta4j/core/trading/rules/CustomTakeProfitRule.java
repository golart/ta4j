package org.ta4j.core.trading.rules;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.utils.StringUtils;

/**
 * @author VKozlov
 */
@Slf4j
public class CustomTakeProfitRule extends org.ta4j.core.trading.rules.StopGainRule {

    private ClosePriceIndicator closePrice;
    private TimeSeries series;
    private Num lossRatioThreshold;

    public CustomTakeProfitRule(ClosePriceIndicator closePrice, Num lossPercentage, final TimeSeries series) {
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
            Bar bar = series.getBar(index);
            setEventType(tradingRecord, String.format("satisfied by take profit loss " +
                            "start time: %s " +
                            "end time: %s " +
                            "current price: %s " +
                            "threshold: %s ",
                    bar.getBeginTime(),
                    bar.getEndTime(),
                    currentPrice,
                    threshold));
        }
        return satisfied;
    }
}
