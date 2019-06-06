package org.ta4j.core.backtest;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.ProfitLossCriterion;
import org.ta4j.core.constructor.StrategyConstructor;
import org.ta4j.core.data.Period;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;
import org.ta4j.core.num.PrecisionNum;

import java.math.BigDecimal;
import java.util.stream.IntStream;

/**
 * @author VKozlov
 * Бектестинг с использованием стратегии
 */
@Slf4j
public class BackTestingTest {

    private org.slf4j.Logger tradeLogger = org.slf4j.LoggerFactory.getLogger("backtest-logger");

    private StrategyConstructor strategyConstructor;

    @Before
    public void setUp() {
        strategyConstructor = new StrategyConstructor();
    }

    @Test
    public void backTesting() {

        TimeSeries timeSeries = CsvTradesLoader.loadSeries("backtesting_BINANCE_LTC_BTC_2019-05-30__2019-06-02_5MIN.csv", Period.PERIOD_5MIN);
        timeSeries.setRemovedBarsCount(14);

        StrategyPropertyWrapper strategyPropertyWrapper = new StrategyPropertyWrapper();
        strategyPropertyWrapper.setStopLoss(BigDecimal.valueOf(2));

        String ruleToBuy = "STOCH < 30 -> TRAILING_BUY == 0.5";
        String ruleToSell = "RSI > 70 or STOCH > 70 -> TRAILING_SELL == 1";

        Strategy strategy = strategyConstructor.createStrategyByExpression(
                ruleToBuy,
                ruleToSell,
                timeSeries, strategyPropertyWrapper);

        TimeSeriesManager seriesManager = new TimeSeriesManager(timeSeries);
        TradingRecord tradingRecord = seriesManager.runWithCounterCurrencyAmount(strategy, Order.OrderType.BUY,
                PrecisionNum.valueOf(10));
        logBackTestResult(tradingRecord, timeSeries);
    }

    private void logBackTestResult(TradingRecord tradingRecord, TimeSeries timeSeries) {
        tradeLogger.info("\nOrders created by period: \n");
        IntStream.range(1, tradingRecord.getTrades().size()).forEach(i -> {
            Trade trade = tradingRecord.getTrades().get(i);

//            ZonedDateTime startDateTime = TimeUtils.toLocalDateTime(backtestProperties.getTimeStart()).toInstant(ZoneOffset.UTC).atOffset(ZoneOffset.UTC).toZonedDateTime();
            Order orderToBuy = trade.getEntry();
            Order orderToSell = trade.getExit();
            Bar barBuy = timeSeries.getBar(orderToBuy.getIndex());
            Bar barSell = timeSeries.getBar(orderToSell.getIndex());

            tradeLogger.info("========== position ==============");
            tradeLogger.info("----- BUY -----");
            tradeLogger.info("Order amount {}", orderToBuy.getAmount());
            tradeLogger.info("Order price {}", orderToBuy.getPrice());
            tradeLogger.info("Order type {}", orderToBuy.getType().name());
            tradeLogger.info("Order candle start date {} and end date {}", barBuy.getBeginTime(), barBuy.getEndTime());
            tradeLogger.info("Order event type: {}", trade.getEventTypeBuy());
            tradeLogger.info("----- SELL -----");
            tradeLogger.info("Order amount {}", orderToSell.getAmount());
            tradeLogger.info("Order price {}", orderToSell.getPrice());
            tradeLogger.info("Order type {}", orderToSell.getType().name());
            tradeLogger.info("Order candle start date {} and end date {}", barSell.getBeginTime(), barSell.getEndTime());
            tradeLogger.info("Order event type: {}", trade.getEventTypeSell());
            tradeLogger.info("========== position end ===========\n\n");
        });
//        tradeLogger.info("Take profit for the strategy: {} \n", new TotalProfitCriterion().calculate(timeSeries, tradingRecord));
        tradeLogger.info("Profit loss for the strategy: {} \n", new ProfitLossCriterion().calculate(timeSeries, tradingRecord));
    }
}
