package rr.industries.util.sql;

/**
 * @author Sam
 */
public class Value {
    /*private String value;
    private boolean isQueried;
    private boolean isBlank;

    private <T> Value(T raw, boolean isQueried) {
        if (raw instanceof Boolean)
            value = ((Boolean) raw ? "1" : "0");
        else
            value = String.valueOf(raw);
        this.isQueried = isQueried;
    }

    public boolean shouldQuery() {
        return isQueried;
    }

    public static <T> Value of(T raw, boolean isQueried) {
        return new Value(raw, isQueried);
    }

    public static Value empty() {
        return new Value(null, false).makeBlank();
    }

    private Value makeBlank() {
        this.isBlank = true;
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

    public boolean isBlank() {
        return isBlank;
    }*/
}
