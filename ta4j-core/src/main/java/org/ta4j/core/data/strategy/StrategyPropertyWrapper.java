package org.ta4j.core.data.strategy;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author VKozlov
 */
@Data
public class StrategyPropertyWrapper implements PeriodTradeProperties {

    private String ruleToBuy;

    private String ruleToSell;

    private Integer period;

    private String candlePeriod;

    private BigDecimal stopLoss;

    private BigDecimal takeProfit;

    private BigDecimal trailingSell;

    private BigDecimal trailingSellPrice;

    private BigDecimal trailingBuy;

    private BigDecimal trailingBuyPrice;

    private BigDecimal prevPrice;


}
