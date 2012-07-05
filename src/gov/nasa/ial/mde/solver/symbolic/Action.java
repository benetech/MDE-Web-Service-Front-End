/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

public class Action {
    
    /** An Operator. */
    public static final int CORRUPTED = -2,
                            NO_OP = -1,
                            U_MINUS = 0,
                            SUM = 1,
                            RECIPROCAL = 2,
                            PRODUCT = 3,
                            POWER = 4,
                            SQRT = 5,
                            EXPONENTIAL = 6,
                            LOG = 7,
                            SINE = 8,
                            COSINE = 9,
                            TANGENT = 10,
                            ABS = 11,
                            FIRST_FUNCTION = SQRT;

    /** Evaluator object. */
    static final OperatorObject[] EVALUATOR = {
                            new UnaryMinusObject(),
                            new SumObject(),
                            new ReciprocalObject(),
                            new ProductObject(),
                            new PowerObject(),
                            new SqrtObject(),
                            new ExpObject(),
                            new LogObject(),
                            new SineObject(),
                            new CosineObject(),
                            new TangentObject(),
                            new AbsObject() };

    /** Symbolic function name. */
    static final String[] FNAMES = { "-", "+", "/", "*", "^", "sqrt", "exp", "log", "sin", "cos", "tan", "abs" };

    /**
     * Constructs an Action.
     */
    public Action() {
        super();
    }

    /**
     * Returns the index to the first match in the list to the target.
     * 
     * @param target the tartget string to match.
     * @param list the list of string to compare against.
     * @return the index to the first match in the list.
     */
    public int findFirst(String target, String[] list) {
       // System.out.println ("Target = "+target);
        for (int i = 0; i < list.length; i++) {
           // System.out.println ("Checking "+list[i]);
            if (target.startsWith(list[i])) {
                return i;
            }
        } // end for i

        return NO_OP;
    } // end findFirst
    
} // end class Action

class OperatorObject {
    double eval(ParseNode[] values) {
        return 0.0;
    } // end eval
} // end operatorObjectk

class UnaryMinusObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return -values[0].eval();
    } // end eval
} // end class UnaryMinusObject

class SumObject extends OperatorObject {
    double eval(ParseNode[] values) {
        double s = 0.0;

        for (int i = 0; i < values.length; i++)
            s += values[i].eval();
        return s;
    } // end eval
} // end class SumObject

class ReciprocalObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return 1.0 / values[0].eval();
    } // end eval
} // end reciprocalObjec

class ProductObject extends OperatorObject {
    double eval(ParseNode[] values) {
        double p = 1.0;

        for (int i = 0; i < values.length; i++)
            p *= values[i].eval();
        return p;
    } // end eval
} // end ProductObject

class PowerObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return Math.pow(values[0].eval(), values[1].eval());
    } // end eval
} // end PowerObject

class SqrtObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return Math.sqrt(values[0].eval());
    } // end eval
} // end SqrtObject

class ExpObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return Math.exp(values[0].eval());
    } // end eval
} // end ExpObject

class LogObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return Math.log(values[0].eval());
    } // end eval
} // end LogObject

class SineObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return Math.sin(values[0].eval());
    } // end eval
} // end SineObject

class CosineObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return Math.cos(values[0].eval());
    } // end eval
} // end CosineObject

class TangentObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return Math.tan(values[0].eval());
    } // end eval
} // end TangentObject

class AbsObject extends OperatorObject {
    double eval(ParseNode[] values) {
        return Math.abs(values[0].eval());
    } // end eval
} // end AbsObject
