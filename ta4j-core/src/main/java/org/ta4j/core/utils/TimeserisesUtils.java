package org.ta4j.core.utils;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.data.Period;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.util.Collection;
import java.util.Map;

/**
 * @author VKozlov
 */
public abstract class TimeserisesUtils {

    /**
     * Сделать клон свечи и выставить новую цену
     *
     * @param cloneBar   клонируемая свеча
     * @param closePrice цена закрытия
     * @param period     период свечи
     * @return склонированный объект
     */
    public static Bar cloneBarWithNewPrice(final Bar cloneBar,
                                           final Num closePrice,
                                           final Period period) {
        return new BaseBar(
                period.getDuration(),
                cloneBar.getEndTime().plus(period.getDuration()),
                cloneBar.getOpenPrice(),
                cloneBar.getMaxPrice(),
                cloneBar.getMinPrice(),
                closePrice,
                cloneBar.getVolume(),
                PrecisionNum.valueOf(0));
    }

}
