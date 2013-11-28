package logist.config;

/**
 * An exception that is thrown when an XML configuration file could not be
 * parsed.
 * 
 * @author Robin Steiger
 */
public class ParserException extends Exception {
    private static final long serialVersionUID = -9019554669489983951L;

    public static ParserException missingTag(String tagName, String parent) {
        return new ParserException("Tag " + parent + " has no '" + tagName
                + "' element");
    }

    public static ParserException missingAttribute(String attName, Object parent) {
        return new ParserException("Tag " + parent + " has no '" + attName
                + "' attribute");
    }

    public static ParserException missingSet(String setName, Object parent) {
        return new ParserException("Tag " + parent + " must set a " + setName);
    }

    public static ParserException missing(String name) {
        return new ParserException("Could not find " + name);
    }

    public static ParserException duplicate(String type, String name) {
        return new ParserException("Duplicate " + type + ": '" + name + "'");
    }

    public static ParserException badFormat(String value, Object attr,
            String type) {
        return new ParserException("Value '" + value + "' of attribute " + attr
                + " is not of type " + type);
    }

    public static ParserException unknownKey(String key, Object parent) {
        return new ParserException("Unknown attribute '" + key + "' of "
                + parent);
    }

    public static ParserException badClass(Class<?> found, String expected) {
        return new ParserException("Expected class of type '" + expected
                + "' but found '" + found.getName() + "'");
    }

    ParserException(String message) {
        super(message);
    }

//	ParserException(Throwable t) {
//		super(t);
//	}

    ParserException(String message, Throwable t) {
        super(message, t);
    }
}
