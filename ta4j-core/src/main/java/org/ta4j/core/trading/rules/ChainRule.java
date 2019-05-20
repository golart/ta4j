
package org.ta4j.core.trading.rules;

import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.trading.rules.AbstractRule;

/**
 * Цепочка правил
 * <p>
 * Второе правило не выполняется пока не выполнится первое.
 * После выполнения первого оно игнорируется и выполняется только 2ое
 */
public class ChainRule extends AbstractRule {

	private Rule rule1;
	private boolean rule1Satisfied = false;

	private Rule rule2;

	/**
	 * Constructor.
	 *
	 * @param rule1 a trading rule
	 * @param rule2 another trading rule
	 */
	public ChainRule(Rule rule1, Rule rule2) {
		this.rule1 = rule1;
		this.rule2 = rule2;
	}
	public ChainRule() {
	}

	@Override
	public boolean isSatisfied(int index, TradingRecord tradingRecord) {
		boolean satisfied;
		if (!rule1Satisfied) {
			satisfied = rule1.isSatisfied(index, tradingRecord);
			if (satisfied) {
				rule1Satisfied = true;
			}
			traceIsSatisfied(index, satisfied);
			return false;
		} else {
			satisfied = rule2.isSatisfied(index, tradingRecord);
			traceIsSatisfied(index, satisfied);
		}
		if (satisfied) {
			rule1Satisfied = false;
		}
		return satisfied;
	}

	public void setRule1(Rule rule1) {
		this.rule1 = rule1;
	}

	public void setRule2(Rule rule2) {
		this.rule2 = rule2;
	}
}
