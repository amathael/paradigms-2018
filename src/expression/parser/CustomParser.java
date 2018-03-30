package expression.parser;

import expression.*;
import expression.parser.grammar.*;

/**
 * Created by isuca in paradigms catalogue
 *
 * @date 22-Mar-18
 * @time 19:06
 */

@SuppressWarnings("WeakerAccess")
public class CustomParser implements Parser {

    private Tokenizer tokenizer;
    private ExpressionGrammar grammar;

    public CustomParser() {
        tokenizer = new Tokenizer();
        grammar = new ExpressionGrammar();

        tokenizer.addToken("(", new Single("LEFT_BRACKET"));
        tokenizer.addToken(")", new Single("RIGHT_BRACKET"));

        initToken("<<", new Single("SHRINK_LEFT", ShrinkLeft.class, null), 0);
        initToken(">>", new Single("SHRINK_RIGHT", ShrinkRight.class, null), 0);

        initToken("|", new Single("VSLASH", BitwiseOr.class, null), 1);
        initToken("^", new Single("CIRCUMFLEX", BitwiseXor.class, null), 2);
        initToken("&", new Single("AMPERSAND", BitwiseAnd.class, null), 3);

        initToken("-", new Single("MINUS", Subtract.class, Negate.class), 4);
        initToken("+", new Single("PLUS", Add.class, null), 4);

        initToken("mod", new Single("MOD", Mod.class, null), 5);
        initToken("/", new Single("SLASH", Divide.class, null), 5);
        initToken("*", new Single("ASTERISK", Multiply.class, null), 5);

        initToken("**", new Single("POWER", Power.class, null), 6);
        initToken("//", new Single("LOG", Log.class, null), 6);

        tokenizer.addToken("~", new Single("TILDE", null, BitwiseNegate.class));
        tokenizer.addToken("abs", new Single("ABS", null, Abs.class));
        tokenizer.addToken("square", new Single("SQUARE", null, Square.class));
        tokenizer.addToken("count", new Single("COUNT", null, BitCount.class));
    }

    public void initToken(String value, Single token, int level) {
        tokenizer.addToken(value, token);
        grammar.addOnLevel(level, token);
    }

    @Override
    public CommonExpression parse(String expression) {
        tokenizer.init(expression);
        return parseLevel(0);
    }

    private CommonExpression parseLevel(int level) {
        if (level == grammar.size()) {
            return parseFactor();
        } else {
            CommonExpression result = parseLevel(level + 1);
            while (grammar.isOnLevel(level, tokenizer.getToken())) {
                Single operation = tokenizer.getToken();
                tokenizer.nextToken();
                result = operation.apply(result, parseLevel(level + 1));
            }
            return result;
        }
    }

    private CommonExpression parseFactor() {
        if (tokenizer.getToken().isUnary()) {
            Single operation = tokenizer.getToken();
            tokenizer.nextToken();
            return operation.apply(parseFactor());
        } else {
            return parseAtom();
        }
    }

    private CommonExpression parseAtom() {
        CommonExpression result;
        Single atom = tokenizer.getToken();
        tokenizer.nextToken();

        if (atom == Tokenizer.CONSTANT) {
            result = new Const(tokenizer.getConstValue());
        } else if (atom == Tokenizer.VARIABLE) {
            result = new Variable(tokenizer.getCustomWord());
        } else if ("LEFT_BRACKET".equals(atom.getName())) {
            result = parseLevel(0);
            assert "RIGHT_BRACKET".equals(tokenizer.getToken().getName()) : "End of expression expected";
            tokenizer.nextToken();
        } else {
            throw new Error("Invalid atom token ".concat(atom.getName()));
        }
        return result;
    }

    /*
    <full>              <- <expression> (<bit sign> <expression>)*
    <expression>        <- <or expression> ("|" <or expression>)*
    <or expression>     <- <xor expression> ("^" <xor expression>)*
    <xor expression>    <- <and expression> ("&" <and expression>)*
    <and_expression>    <- <term> (<term sign> <term>)*
    <term>              <- <signed factor> (<factor sign> <signed factor>)*
    <signed factor>     <- <unary sign>*<factor>
    <factor>            <- <constant> | <variable> | "("<full>")"
    <constant>          <- ("-") "0" | ["1"-"9"]["0"-"9"]*
    <variable>          <- "x" | "y" | "z"

    <unary sign>        <- "-" | "abs" | "square"
    <term sign>         <- "+" | "-"
    <factor sign>       <- "*" | "/" | "mod"
    <bit sign>          <- "<<" | ">>"
    */

}