package org.ta4j.core.strategy;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.constructor.StrategyConstructor;
import org.ta4j.core.data.Period;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.utils.TimeserisesUtils;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author VKozlov
 * Тест конструктора стратегий
 */
public class StrategyInfoStopLossAndTakeProfitTest  {

    private StrategyConstructor strategyConstructor;

    private TimeSeries timeSeries;
    private Period period;

    private final String RULE_TO_BUY = "RSI < 2";
    private final String RULE_TO_SELL = "RSI > 99";

    @Before
    public void setUp() {
        period = Period.PERIOD_1HRS;
        strategyConstructor = new StrategyConstructor();
    }

    @Test
    public void testStrategyStopLoss() {
        timeSeries = CsvTradesLoader.loadSeries("backtesting_POLONIEX_XRP_BTC_2019-04-20-00___2019-04-29-00_1HRS.csv", period);
        StrategyPropertyWrapper strategyProperty = new StrategyPropertyWrapper();
        strategyProperty.setStopLoss(BigDecimal.valueOf(5d));

        Strategy strategy = strategyConstructor.createStrategyByExpression(RULE_TO_BUY, RULE_TO_SELL, timeSeries, strategyProperty);
        TradingRecord tradingRecord = new BaseTradingRecord();

        Rule sellRule = strategy.getExitRule();
        Bar lastBar = timeSeries.getLastBar();
        Num stopLossPrice = lastBar.getClosePrice()
                .minus(calculatePercentageOfPrice(lastBar.getClosePrice(), strategyProperty.getStopLoss().add(BigDecimal.valueOf(5d))));

        timeSeries.addBar(TimeserisesUtils.cloneBarWithNewPrice(lastBar, stopLossPrice, period));
        Strategy strategyWithNewBar = strategyConstructor.createStrategyByExpression(RULE_TO_BUY, RULE_TO_SELL, timeSeries, strategyProperty);
        Rule newSellRule = strategyWithNewBar.getExitRule();


        tradingRecord.enter(1, timeSeries.getBar(timeSeries.getEndIndex() - 3).getClosePrice(), PrecisionNum.valueOf(1));
        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex() - 1).getClosePrice().toString());
        assertFalse(sellRule.isSatisfied(timeSeries.getEndIndex() - 1, tradingRecord));
        System.out.println("result  " + sellRule.isSatisfied(timeSeries.getEndIndex() - 1, tradingRecord));
        tradingRecord.exit(1);

        tradingRecord.enter(2, timeSeries.getBar(timeSeries.getEndIndex() - 2).getClosePrice(), PrecisionNum.valueOf(1));
        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex()).getClosePrice().toString());
        assertTrue(newSellRule.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        System.out.println("result " + newSellRule.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        tradingRecord.exit(2);

        timeSeries.addBar(TimeserisesUtils.cloneBarWithNewPrice(timeSeries.getLastBar(), timeSeries.getBar(timeSeries.getEndIndex() - 3).getClosePrice(), period));
        Strategy strategyWithNewBar2 = strategyConstructor.createStrategyByExpression(RULE_TO_BUY, RULE_TO_SELL, timeSeries, strategyProperty);
        Rule newSellRule2 = strategyWithNewBar2.getExitRule();

        tradingRecord.enter(3, timeSeries.getBar(timeSeries.getEndIndex() - 1).getClosePrice(), PrecisionNum.valueOf(1));
        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex()).getClosePrice().toString());
        assertFalse(newSellRule2.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        System.out.println("result " + newSellRule2.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        tradingRecord.exit(3);
    }

    @Test
    public void testStrategyTakeProfit() {
        timeSeries = CsvTradesLoader.loadSeries("backtesting_POLONIEX_XRP_BTC_2019-04-20-00___2019-04-29-00_1HRS.csv", period);
        StrategyPropertyWrapper strategyProperty = new StrategyPropertyWrapper();
        strategyProperty.setTakeProfit(BigDecimal.valueOf(5d));

        Strategy strategy = strategyConstructor.createStrategyByExpression(RULE_TO_BUY, RULE_TO_SELL, timeSeries, strategyProperty);
        TradingRecord tradingRecord = new BaseTradingRecord();

        Rule sellRule = strategy.getExitRule();
        Bar lastBar = timeSeries.getLastBar();
        Num takeProfitPrice = lastBar.getClosePrice()
                .plus(calculatePercentageOfPrice(lastBar.getClosePrice(), strategyProperty.getTakeProfit().add(BigDecimal.valueOf(5d))));

        timeSeries.addBar(TimeserisesUtils.cloneBarWithNewPrice(lastBar, takeProfitPrice, period));
        Strategy strategyWithNewBar = strategyConstructor.createStrategyByExpression(RULE_TO_BUY, RULE_TO_SELL, timeSeries, strategyProperty);
        Rule newSellRule = strategyWithNewBar.getExitRule();

        tradingRecord.enter(1, timeSeries.getBar(timeSeries.getEndIndex() - 3).getClosePrice(), PrecisionNum.valueOf(1));
        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex() - 1).getClosePrice().toString());
        assertFalse(sellRule.isSatisfied(timeSeries.getEndIndex() - 1, tradingRecord));
        System.out.println("result false " + sellRule.isSatisfied(timeSeries.getEndIndex() - 1, tradingRecord));
        tradingRecord.exit(1);

        tradingRecord.enter(2, timeSeries.getBar(timeSeries.getEndIndex() - 2).getClosePrice(), PrecisionNum.valueOf(1));
        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex()).getClosePrice().toString());
        assertTrue(newSellRule.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        System.out.println("result true " + sellRule.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        tradingRecord.exit(2);

        timeSeries.addBar(TimeserisesUtils.cloneBarWithNewPrice(timeSeries.getLastBar(), lastBar.getClosePrice(), period));
        Strategy strategyWithNewBar2 = strategyConstructor.createStrategyByExpression(RULE_TO_BUY, RULE_TO_SELL, timeSeries, strategyProperty);
        Rule newSellRule2 = strategyWithNewBar2.getExitRule();

        tradingRecord.enter(3, timeSeries.getBar(timeSeries.getEndIndex() - 1).getClosePrice(), PrecisionNum.valueOf(1));
        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex()).getClosePrice().toString());
        assertFalse("result false", newSellRule2.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        System.out.println("result false " + newSellRule2.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        tradingRecord.exit(3);
    }

    @Test
    public void testStrategyTrailingSell() {
        timeSeries = CsvTradesLoader.loadSeries("backtesting_POLONIEX_XRP_BTC_2019-04-20-00___2019-04-29-00_1HRS.csv", period);
        StrategyPropertyWrapper strategyProperty = new StrategyPropertyWrapper();
        strategyProperty.setTrailingSell(BigDecimal.valueOf(5d));

        Strategy strategy = strategyConstructor.createStrategyByExpression(RULE_TO_BUY, RULE_TO_SELL, timeSeries, strategyProperty);
        TradingRecord tradingRecord = new BaseTradingRecord();

        Rule sellRule = strategy.getExitRule();
        Bar lastBar = timeSeries.getLastBar();
        Num takeProfitPrice = lastBar.getClosePrice()
                .plus(calculatePercentageOfPrice(lastBar.getClosePrice(), strategyProperty.getTrailingSell()));
        System.out.println("takeProfitPrice " + takeProfitPrice.toString());

        timeSeries.addBar(TimeserisesUtils.cloneBarWithNewPrice(lastBar, takeProfitPrice, period));
        timeSeries.addBar(TimeserisesUtils.cloneBarWithNewPrice(timeSeries.getLastBar(), takeProfitPrice
                .plus(calculatePercentageOfPrice(takeProfitPrice, BigDecimal.valueOf(5d))), period));
        timeSeries.addBar(TimeserisesUtils.cloneBarWithNewPrice(timeSeries.getLastBar(), takeProfitPrice, period));
        timeSeries.addBar(TimeserisesUtils.cloneBarWithNewPrice(timeSeries.getLastBar(), lastBar.getClosePrice(), period));

        Strategy strategyWithNewBar = strategyConstructor.createStrategyByExpression(RULE_TO_BUY, RULE_TO_SELL, timeSeries, strategyProperty);
        Rule newSellRule = strategyWithNewBar.getExitRule();

        tradingRecord.enter(1, timeSeries.getBar(timeSeries.getEndIndex() - 4).getClosePrice(), PrecisionNum.valueOf(1));
        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex() - 3).getClosePrice().toString());
        assertFalse(sellRule.isSatisfied(timeSeries.getEndIndex() - 3, tradingRecord));
        System.out.println("result " + sellRule.isSatisfied(timeSeries.getEndIndex() - 3, tradingRecord));

        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex() - 2).getClosePrice().toString());
        assertFalse(newSellRule.isSatisfied(timeSeries.getEndIndex() - 2, tradingRecord));
        System.out.println("result " + sellRule.isSatisfied(timeSeries.getEndIndex() - 2, tradingRecord));

        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex() - 1).getClosePrice().toString());
        assertFalse(newSellRule.isSatisfied(timeSeries.getEndIndex() - 1, tradingRecord));
        System.out.println("result " + sellRule.isSatisfied(timeSeries.getEndIndex() - 1, tradingRecord));

        System.out.println("Prev price " + tradingRecord.getCurrentTrade().getEntry().getPrice().toString());
        System.out.println("Current price " + timeSeries.getBar(timeSeries.getEndIndex()).getClosePrice().toString());
        assertTrue(newSellRule.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
        System.out.println("result " + sellRule.isSatisfied(timeSeries.getEndIndex(), tradingRecord));
    }

    @Test
    public void testStrategySellWithChainRule() {
        timeSeries = CsvTradesLoader.loadSeries("backtesting_BINANCE_XRP_BTC_2019-05-10-00___2019-05-13-14_30MIN.csv", period);
        StrategyPropertyWrapper strategyProperty = new StrategyPropertyWrapper();

        String ruleToBuy = "RSI < 30";
        String ruleToSell = "RSI > 50 -> TRAILING_SELL == 5";

        Strategy strategy = strategyConstructor.createStrategyByExpression(
                ruleToBuy,
                ruleToSell,
                timeSeries,
                strategyProperty);

        TimeSeriesManager seriesManager = new TimeSeriesManager(timeSeries);
        TradingRecord tradingRecord = seriesManager.run(strategy, Order.OrderType.BUY, PrecisionNum.valueOf(0.5));

        assertEquals(tradingRecord.getTradeCount(), 2);
        assertEquals(tradingRecord.getLastTrade().getEntry().getIndex(), 58);
        assertEquals(tradingRecord.getLastTrade().getExit().getIndex(), 91);
    }

    private Num calculatePercentageOfPrice(final Num price, BigDecimal percentage) {
        return price.dividedBy(PrecisionNum.valueOf(100))
                .multipliedBy(PrecisionNum.valueOf(percentage));
    }

}
