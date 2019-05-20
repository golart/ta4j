package org.ta4j.core.constructor;

import org.ta4j.core.data.Indicator;

public interface IIndicatorResolver {

    IIndicatorHandler resolveIndicator(final Indicator indicator);
}
