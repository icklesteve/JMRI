package jmri.jmrit.logixng;

/**
 * An enum with the values "is" and "is not"
 *
 * @author Daniel Bergqvist 2019
 */
public enum Is_IsNot_Enum {

    Is(Bundle.getMessage("IsIsNotEnum_Is")),
    IsNot(Bundle.getMessage("IsIsNotEnum_IsNot"));

    private final String _text;

    private Is_IsNot_Enum(String text) {
        this._text = text;
    }

    public Is_IsNot_Enum getOpposite() {
        switch (this) {
            case Is: return IsNot;
            case IsNot: return Is;
            default: throw new UnsupportedOperationException("Unknown enum: "+this.name());
        }
    }

    @Override
    public String toString() {
        return _text;
    }

}
