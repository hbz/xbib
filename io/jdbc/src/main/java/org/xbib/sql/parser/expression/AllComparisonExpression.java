package org.xbib.sql.parser.expression;

import org.xbib.sql.parser.statement.select.SubSelect;

public class AllComparisonExpression implements Expression {

    private SubSelect subSelect;
	
	public AllComparisonExpression(SubSelect subSelect) {
		this.subSelect = subSelect;
	}
	
	public SubSelect GetSubSelect() {
		return subSelect;
	}
	
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

}
