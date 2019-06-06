package org.ta4j.core.constructor;

import org.ta4j.core.data.IndicatorRequestWrapper;
import org.ta4j.core.data.TradeResultWrapper;
import org.ta4j.core.utils.CollectionUtils;

import java.util.List;

/**
 * @author VKozlov
 * Реализация индикатора
 */
public interface IIndicatorHandler {

    TradeResultWrapper execute(final IndicatorRequestWrapper requestWrapper);

    default String getParam(int index, List<String> params) {
        if (CollectionUtils.isEmpty(params) || index >= params.size()) {
            return null;
        }
        return params.get(index);
    }
}
