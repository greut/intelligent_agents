package logist.config;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

/**
 * A wrapper class for <tt>jdom.org.Element</tt> providing a concise interface
 * and shortcuts for frequently used functionality.
 *
 * @author Robin Steiger
 */
public class XMLTag {

    private final String elemName;
    private final Element elem;

    static XMLTag loadXMLFromFile(String filename) throws ParserException {
        return loadXMLFromFile(new File(filename));
    }

    static XMLTag loadXMLFromFile(File filename) throws ParserException {
        try {
            //System.out.println("Reading " + filename);

            // loads the stream from a file
            FileInputStream stream = new FileInputStream(filename);

            // creates a builder
            SAXBuilder documentBuilder = new SAXBuilder();

            // loads the document from the builder
            Document document = documentBuilder.build(new InputSource(stream));

            // return the top level element
            return new XMLTag("<root>", document.getRootElement());

        } catch (FileNotFoundException e) {
            throw new ParserException("Config file " + filename.getAbsolutePath()
                    + " does not exists.");
        } catch (JDOMException jdomEx) {
            throw new ParserException("Failed to parse " + filename, jdomEx);
        } catch (IOException ioEx) {
            throw new ParserException("Failed to read " + filename, ioEx);
        }
    }

    private XMLTag(String elemName, Element elem) {
        this.elemName = elemName;
        this.elem = elem;
    }

    public String toString() {
        return elemName;
    }

    // public String name() {
    // return elemName;
    // }

    /**
     * Returns a child of given name and verifies that there is exactly one such
     * child.
     *
     * @param tagName
     *            - the name of the child
     * @return a child tag
     * @throws ParserException
     *             - if name is not a unique child
     */
    @SuppressWarnings("unchecked")
    XMLTag getUniqueChild(String tagName) throws ParserException {

        // get the list of elements
        List<Element> children = elem.getChildren(tagName);

        // Verify that there is exactly one child
        if (children == null || children.isEmpty())
            throw ParserException.missingTag(tagName, elemName);
        else if (children.size() > 1)
            throw ParserException.duplicate("tag", tagName);

        // retrieve element
        return new XMLTag(tagName, children.get(0));
    }

    @SuppressWarnings("unchecked")
    boolean hasChild(String tagName) {
        // get the list of elements
        List<Element> children = elem.getChildren(tagName);

        // Verify that there is exactly one child
        return (children != null && !children.isEmpty());
    }

    /**
     * Returns all child tags with that name.
     *
     * @param tagName
     *            - the name of the children
     * @return all children with that name
     * @throws ParserException
     *             - if there is no child of the given name
     */
    @SuppressWarnings("unchecked")
    List<XMLTag> getAllChildren(String tagName) throws ParserException {

        // get the list of elements
        List<Element> children = elem.getChildren(tagName);

        // Verify that there is at least one child
        if (children == null || children.isEmpty())
            throw ParserException.missingTag(tagName, elemName);

        // wrap elements
        List<XMLTag> tagList = new ArrayList<XMLTag>(children.size());
        for (Element child : children)
            tagList.add(new XMLTag(tagName, child));

        return tagList;
    }

    /**
     * Reads an attribute if it exists
     *
     * @param attributeName
     *            - the name of the attribute
     * @return an attribute
     * @throws ParserException
     *             - if no such attribute exists
     */
    private Attribute getAttribute(String attributeName) throws ParserException {
        Attribute attribute = elem.getAttribute(attributeName);

        if (attribute == null) // || (value = attribute.getValue()).isEmpty())
            throw ParserException.missingAttribute(attributeName, elemName);

        return attribute;
    }

    /*
     * Parse an attribute of this tag as String
     *
     * @param attributeName
     * @return a non-empty String
     * @throws ParserException
     *             - if the attribute does not exist or is empty
     */
    // String getStringAttribute(String attributeName) throws ParserException {
    // String attribute = getAttribute(attributeName).getValue();
    //
    // if (attribute.isEmpty())
    // throw ParserException.badFormat(attribute, attributeName,
    // "(non-empty) String");
    //
    // return attribute;
    // }

    /*
     * Parse an attribute of this tag as an integer
     *
     * @param attributeName
     * @return an integer
     * @throws ParserException
     *             - if the attribute does not exist or is not an integer
     */
    // int getIntAttribute(String attributeName) throws ParserException {
    // try {
    // return getAttribute(attributeName).getIntValue();
    // } catch (DataConversionException dcEx) {
    // throw ParserException.badFormat(attributeName, elemName, "int");
    // }
    // }

    /*
     * Parse an attribute of this tag as a double
     *
     * @param attributeName
     * @return a double
     * @throws ParserException
     *             - if the attribute does not exist or is not a double
     */
    // double getDoubleAttribute(String attributeName)
    // throws ParserException {
    // try {
    // return getAttribute(attributeName).getDoubleValue();
    // } catch (DataConversionException dcEx) {
    // throw ParserException.badFormat(attributeName, elemName, "double");
    // }
    // }

    /*
     * Parse an attribute of this tag as a long
     *
     * @param attributeName
     * @return a long
     * @throws ParserException
     *             - if the attribute does not exist or is not a long
     */
    // long getLongAttribute(String attributeName) throws ParserException {
    // try {
    // return getAttribute(attributeName).getLongValue();
    // } catch (DataConversionException dcEx) {
    // throw ParserException.badFormat(attributeName, elemName, "long");
    // }
    // }

    /*
     * Parse an attribute of this tag as a color
     *
     * @param attributeName
     * @return a color
     * @throws ParserException
     * @throws ParserException
     *             - if the attribute does not exist or is not a color
     */
    // Color getColorAttribute(String attributeName) throws ParserException {
    // try {
    // return Color.decode(getAttribute(attributeName).getValue());
    // } catch (NumberFormatException nfEx) {
    // throw ParserException.badFormat(attributeName, elemName, "color");
    // }
    // }

    /*
     * Parse an attribute of this tag as a class
     *
     * @param <E>
     * @param attributeName
     * @param clazz
     * @return
     * @throws ParserException
     */
    // public <E> E getClassAttribute(String attributeName, Class<E> clazz)
    // throws ParserException {
    // try {
    // // return getAttribute(attributeName).getClass();
    // return clazz.cast(
    // Class.forName(getAttribute(attributeName).getValue()).newInstance() );
    // } catch (InstantiationException iEx) {
    // throw ParserException.badFormat(attributeName, elemName, "class");
    // } catch (IllegalAccessException iaEx) {
    // throw ParserException.badFormat(attributeName, elemName, "class");
    // } catch (ClassNotFoundException cnfEx) {
    // throw ParserException.badFormat(attributeName, elemName, "class");
    // }
    // }

    /*
     * Parse an attribute of this tag as a class
     *
     * @param <E>
     * @param attributeName
     * @param clazz
     * @return
     * @throws ParserException
     */
    // public Class<?> getClassAttribute(String attributeName) throws
    // ParserException {
    // try {
    // return Class.forName(getAttribute(attributeName).getValue(), false,
    // ClassLoader.getSystemClassLoader());
    // } catch (ClassNotFoundException cnfEx) {
    // throw ParserException.badFormat(attributeName, elemName, "class");
    // }
    // }


    <T> T getAttribute(String attributeName, Class<T> type)
            throws ParserException {
        String value = getAttribute(attributeName).getValue();
        return convert(value, elemName, type);
    }

    // public File getFileAttribute(String attributeName) throws ParserException
    // {
    // return new File(getAttribute(attributeName).getValue());
    // }

    /**
     * Returns whether this tag has a given attribute
     *
     * @param attributeName
     *            the name of the attribute
     */
    boolean hasAttribute(String attributeName) {
        return (elem.getAttribute(attributeName) != null);
    }

    @SuppressWarnings("unchecked")
    Map<String, String> getAttributes(Map<String, String> map) {

        for (Attribute attr : (List<Attribute>) elem.getAttributes())
            map.put(attr.getName(), attr.getValue());

        return map;
    }

    static <T extends Enum<T>> T convert(String value, Object parent,
            Class<T> enumType) throws ParserException {
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException iaEx) {
            throw ParserException.unknownKey(value, parent);
        }
    }

    public static <T> T convert(Map<String, String> map, String name,
            Object parent, Class<? extends T> type, T def)
            throws ParserException {
        String value = map.get(name);
        if (value == null) {
            if (def == null)
                throw ParserException.missingSet(name, parent);
            else
                return def;
        }

        return convert(value, parent, type);
    }

    public static <T> T convert(String value, Object attr,
            Class<? extends T> type) throws ParserException {
        try {
            if (type == String.class) {
                if (value.isEmpty())
                    throw ParserException.badFormat(value, attr,
                            "(non-empty) String");
                return type.cast(value);
            } else if (type == Boolean.class) {
                return type.cast(Boolean.parseBoolean(value));
            } else if (type == Integer.class) {
                return type.cast(Integer.decode(value));
            } else if (type == Long.class) {
                return type.cast(Long.decode(value));
            } else if (type == Color.class) {
                return type.cast(Color.decode(value));
            } else if (type == Double.class) {
                return type.cast(Double.parseDouble(value));
            } else if (type == File.class) {
                return type.cast(new File(value));
            } else if (type == ClassLoader.class) {
                String[] paths = value.split(";");
                URL[] urls = new URL[paths.length];

                for (int i = 0; i < paths.length; i++) {
                    try {
                        File file = new File(paths[i]);
                        urls[i] = file.toURI().toURL();

                        if (!file.exists())
                            throw new ParserException("Class-path '"
                                    + paths[i] + "' does not exist");

                    } catch (MalformedURLException muEx) {
                        throw new ParserException("Invalid class-path '"
                                + paths[i] + "'", muEx);
                    }
                }
                return type.cast(new URLClassLoader(urls));
            } else
                throw new UnsupportedOperationException(value + " " + attr + " " + type);

        } catch (NumberFormatException nfEx) {
            throw ParserException.badFormat(value, attr, type.getSimpleName());
        }
    }

}
