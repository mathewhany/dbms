package dbms.util;

import dbms.pages.Row;

import java.util.Hashtable;

public class BinaryExpression implements Expression {
    private final Expression left;
    private final Expression right;
    private final String operator;

    public BinaryExpression(Expression left, Expression right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(Row row) {
        switch (operator.toLowerCase()) {
            case "and":
                return left.evaluate(row) && right.evaluate(row);
            case "or":
                return left.evaluate(row) || right.evaluate(row);
            case "xor":
                return left.evaluate(row) ^ right.evaluate(row);
            default:
                throw new RuntimeException("Unknown operator: " + operator);
        }
    }

    @Override
    public boolean evaluate(Hashtable<String, Range> ranges) {
        switch (operator.toLowerCase()) {
            case "and":
                return left.evaluate(ranges) && right.evaluate(ranges);
            case "or":
                return left.evaluate(ranges) || right.evaluate(ranges);
            case "xor":
                return left.evaluate(ranges) ^ right.evaluate(ranges);
            default:
                throw new RuntimeException("Unknown operator: " + operator);
        }
    }
}
