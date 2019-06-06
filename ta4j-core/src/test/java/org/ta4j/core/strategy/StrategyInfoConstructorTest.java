package org.ta4j.core.strategy;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.CsvTradesLoader;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.constructor.StrategyConstructor;
import org.ta4j.core.data.Period;
import org.ta4j.core.data.strategy.StrategyPropertyWrapper;

/**
 * @author VKozlov
 * Тест конструктора стратегий
 */
public class StrategyInfoConstructorTest {

    private StrategyConstructor strategyConstructor;

    @Before
    public void setUp() {
        strategyConstructor = new StrategyConstructor();
    }

    @Test
    public void testStrategyRuleConstructor() {
        TimeSeries timeSeries = CsvTradesLoader.loadSeries("backtesting_POLONIEX_XRP_BTC_2019-04-20-00___2019-04-29-00_1HRS.csv", Period.PERIOD_1HRS);

        String testBuyRuleExpression = "((RSI > 65) && (MACD == true) && (((STOCH > 40) or (STOCH > 70)) && MACD == true))";
        String testSellRuleExpression = "((RSI < 65) && (MACD == true) && (((STOCH < 40) or (STOCH < 70)) && MACD == true))";
        Strategy strategy1 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression,
                testSellRuleExpression, timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule1 = strategy1.getEntryRule();
        Rule testSellRule1 = strategy1.getExitRule();

        String testBuyRuleExpression2 = "(RSI > 65 && MACD == true && ((STOCH > 40 or STOCH > 70) && STOCH == true) && RSI > 40)";
        String testSellRuleExpression2 = "(RSI < 65 && MACD == true && ((STOCH < 40 or STOCH < 70) && STOCH == true) && RSI < 40)";
        Strategy strategy2 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression2,
                testSellRuleExpression2,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule2 = strategy2.getEntryRule();
        Rule testSellRule2 = strategy2.getExitRule();

        String testBuyRuleExpression3 = "RSI > 15 && MACD == true && (STOCH == true && (STOCH > 20 or STOCH < 40))";
        String testSellRuleExpression3 = "RSI < 15 && MACD == true && (STOCH == true && (STOCH > 20 or STOCH < 40))";
        Strategy strategy3 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression3,
                testSellRuleExpression3,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule3 = strategy3.getEntryRule();
        Rule testSellRule3 = strategy3.getExitRule();

        String testBuyRuleExpression4 = "RSI > 55 && MACD == true && STOCH > 50";
        String testSellRuleExpression4 = "RSI < 15 && MACD == true && STOCH > 20";
        Strategy strategy4 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression4,
                testSellRuleExpression4,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule4 = strategy4.getEntryRule();
        Rule testSellRule4 = strategy4.getExitRule();

        String testBuyRuleExpression5 = "(RSI < 15 && ((STOCH > 20 or STOCH < 20) && RSI < 40 && (STOCH > 20 or STOCH < 20)) && MACD == true && (STOCH == true && (STOCH > 20 or STOCH < 40)))";
        String testSellRuleExpression5 = "(RSI > 60 && ((STOCH > 50 or STOCH < 20) && RSI > 70 && (STOCH > 20 or STOCH < 20)) && MACD == true && (STOCH == true && (STOCH > 20 or STOCH < 40)))";
        Strategy strategy5 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression5,
                testSellRuleExpression5,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule5 = strategy5.getEntryRule();
        Rule testSellRule5 = strategy5.getExitRule();

        String testBuyRuleExpression6 = "STOCH";
        String testSellRuleExpression6 = "STOCH";
        Strategy strategy6 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression6,
                testSellRuleExpression6,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule6 = strategy6.getEntryRule();
        Rule testSellRule6 = strategy6.getExitRule();

        String testBuyRuleExpression7 = "!STOCH";
        String testSellRuleExpression7 = "!STOCH";
        Strategy strategy7 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression7,
                testSellRuleExpression7,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule7 = strategy7.getEntryRule();
        Rule testSellRule7 = strategy7.getExitRule();

        String testBuyRuleExpression8 = "!STOCH && MACD";
        String testSellRuleExpression8 = "!STOCH && MACD";
        Strategy strategy8 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression8,
                testSellRuleExpression8,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule8 = strategy8.getEntryRule();
        Rule testSellRule8 = strategy8.getExitRule();

        String testBuyRuleExpression9 = "RSI";
        String testSellRuleExpression9 = "RSI";
        Strategy strategy9 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression9,
                testSellRuleExpression9,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule9 = strategy9.getEntryRule();
        Rule testSellRule9 = strategy9.getExitRule();


        String testBuyRuleExpression10 = "!RSI";
        String testSellRuleExpression10 = "!RSI";
        Strategy strategy10 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression10,
                testSellRuleExpression10,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule10 = strategy10.getEntryRule();
        Rule testSellRule10 = strategy10.getExitRule();


        String testBuyRuleExpression11 = "RSI or STOCH or MACD";
        String testSellRuleExpression11 = "RSI or STOCH or MACD";
        Strategy strategy11 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression11,
                testSellRuleExpression11,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule11 = strategy11.getEntryRule();
        Rule testSellRule11 = strategy11.getExitRule();


        String testBuyRuleExpression12 = "(RSI < 30) -> (STOCH or MACD)";
        String testSellRuleExpression12 = "(RSI > 70) -> (STOCH or MACD)";
        Strategy strategy12 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression12,
                testSellRuleExpression12,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule12 = strategy12.getEntryRule();
        Rule testSellRule12 = strategy12.getExitRule();


        String testBuyRuleExpression13 = "(RSI < 30) -> (STOCH or MACD) -> (MACD) -> RSI -> MACD";
        String testSellRuleExpression13 = "(RSI > 70) -> (STOCH or MACD) -> (MACD) -> RSI -> MACD";
        Strategy strategy13 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression13,
                testSellRuleExpression13,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule13 = strategy13.getEntryRule();
        Rule testSellRule13 = strategy13.getExitRule();


        String testBuyRuleExpression14 = "(RSI < 30) -> TRAILING_BUY == 14";
        String testSellRuleExpression14 = "(RSI > 70) -> TRAILING_BUY == 14";
        Strategy strategy14 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression14,
                testSellRuleExpression14,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule14 = strategy14.getEntryRule();
        Rule testSellRule14 = strategy14.getExitRule();


        String testBuyRuleExpression15 = "(RSI < 30) -> TAKE_PROFIT == 14 && STOP_LOSS == 17";
        String testSellRuleExpression15 = "(RSI > 60) -> TAKE_PROFIT == 14 && STOP_LOSS == 17";
        Strategy strategy15 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression15,
                testSellRuleExpression15,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule15 = strategy15.getEntryRule();
        Rule testSellRule15 = strategy15.getExitRule();


        String testBuyRuleExpression16 = "STOCH < 30 or MACD == true && RSI < 40";
        String testSellRuleExpression16 = "STOCH > 70 or MACD == true && RSI > 50";
        Strategy strategy16 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression16,
                testSellRuleExpression16,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule16 = strategy16.getEntryRule();
         Rule testSellRule16 = strategy16.getExitRule();


        String testBuyRuleExpression17 = "STOCH[14, 4] < 30";
        String testSellRuleExpression17 = "STOCH[14, 7] > 70";
        Strategy strategy17 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression17,
                testSellRuleExpression17,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule17 = strategy17.getEntryRule();
        Rule testSellRule17 = strategy17.getExitRule();


        String testBuyRuleExpression18 = "STOCH[19, 4] < 30 && MACD[12, 26, 9] == true";
        String testSellRuleExpression18 = "STOCH[15, 7] > 70 && MACD[12, 26, 9] == true";
        Strategy strategy18 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression18,
                testSellRuleExpression18,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule18 = strategy18.getEntryRule();
        Rule testSellRule18 = strategy18.getExitRule();


        String testBuyRuleExpression19 = "STOCH[19, 4] < 30 && MACD == true";
        String testSellRuleExpression19 = "STOCH[15, 7] > 70 && MACD == true";
        Strategy strategy19 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression19,
                testSellRuleExpression19,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule19 = strategy19.getEntryRule();
        Rule testSellRule19 = strategy19.getExitRule();


        String testBuyRuleExpression20 = "RSI[14] < 30 && MACD == true -> TRAILING_BUY == 2";
        String testSellRuleExpression20 = "STOCH[14] > 70 && MACD == true -> TRAILING_SELL == 2";
        Strategy strategy20 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression20,
                testSellRuleExpression20,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule20 = strategy20.getEntryRule();
        Rule testSellRule20 = strategy20.getExitRule();


        String testBuyRuleExpression21 = "CCI[20] < 20";
        String testSellRuleExpression21 = "CCI[20] > 70";
        Strategy strategy21 = strategyConstructor.createStrategyByExpression(
                testBuyRuleExpression21,
                testSellRuleExpression21,
                timeSeries, new StrategyPropertyWrapper());
        Rule testBuyRule21 = strategy21.getEntryRule();
        Rule testSellRule21 = strategy21.getExitRule();
    }
}
