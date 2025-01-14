package Helper;

public class NumberHelper {
    /**
     * Compares two values numerically. The value returned is identical to what would be returned by:
     *      Type.valueOf(x).compareTo(Type.valueOf(y))
     * @param x
     * @param y
     * @return
     */
    public static int compareValues(Number x, Number y) {
        if(x instanceof Integer && y instanceof Integer){
            return Integer.compare(x.intValue(), y.intValue());
        } else if(x instanceof Long && y instanceof Long){
            return Long.compare(x.longValue(), y.longValue());
        } else if(x instanceof Short && y instanceof Short){
            return Short.compare(x.shortValue(), y.shortValue());
        } else if(x instanceof Double) {
            if (checkForDoubleCompatibility(y)) {
                return Double.compare(x.doubleValue(), y.doubleValue());
            }
        } else if(y instanceof Double) {
            if (checkForDoubleCompatibility(y)) {
                return Double.compare(x.doubleValue(), y.doubleValue());
            }
        }
        throw new IllegalArgumentException("Vector values must be comparable");
    }

    public static Double divideValuesAsDoubles(Number x, Number y) {
        return x.doubleValue() / y.doubleValue();
    }

    public static Double multiplyNumberWithDouble(Number value, Double d) {
        if(value instanceof Integer){
            return value.intValue() * d;
        } else if(value instanceof Long){
            return value.longValue() * d;
        } else if(value instanceof Short){
            return value.shortValue() * d;
        } else if(value instanceof Double){
            return value.doubleValue() * d;
        } else {
            throw new IllegalArgumentException("Values must be compatible.");
        }
    }

    public static Number addValues(Number value1, Number value2) {
        if(value1 instanceof Integer && value2 instanceof Integer){
            return value1.intValue() + value2.intValue();
        } else if(value1 instanceof Long && value2 instanceof Long){
            return value1.longValue() + value2.longValue();
        } else if(value1 instanceof Short && value2 instanceof Short){
            return value1.shortValue() + value2.shortValue();
        } else if(value1 instanceof Double) {
            if (checkForDoubleCompatibility(value2)) {
                return value1.doubleValue() + value2.doubleValue();
            }
        } else if(value2 instanceof Double) {
            if (checkForDoubleCompatibility(value1)) {
                return value1.doubleValue() + value2.doubleValue();
            }
        }
        throw new IllegalArgumentException("Values must be compatible.");
    }

    private static boolean checkForDoubleCompatibility(Number value) {
        return value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Double;
    }

    public static Number subtractValues(Number value1, Number value2) {
        if(value1 instanceof Integer && value2 instanceof Integer){
            return value1.intValue() - value2.intValue();
        } else if(value1 instanceof Long && value2 instanceof Long){
            return value1.longValue() - value2.longValue();
        } else if(value1 instanceof Short && value2 instanceof Short){
            return value1.shortValue() - value2.shortValue();
        } else if(value1 instanceof Double) {
            if (checkForDoubleCompatibility(value2)) {
                return value1.doubleValue() - value2.doubleValue();
            }
        } else if(value2 instanceof Double) {
            if (checkForDoubleCompatibility(value1)) {
                return value1.doubleValue() - value2.doubleValue();
            }
        }
        throw new IllegalArgumentException("Values must be compatible.");
    }
}
