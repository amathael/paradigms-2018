package expression;

import expression.exceptions.DoubleUnsupportedException;
import expression.exceptions.EvaluationException;

/**
 * Created by isuca in paradigms catalogue
 *
 * @date 23-Mar-18
 * @time 16:15
 */

public class CheckedBitCount extends AbstractUnaryOperation {

    public CheckedBitCount(CommonExpression argument) {
        super(argument);
    }

    @Override
    protected int eval(int value) {
        return Integer.bitCount(value);
    }

    @Override
    protected double eval(double value) throws EvaluationException {
        throw new DoubleUnsupportedException();
    }

}
