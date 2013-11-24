package logist.history;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * A simple writer for XML files.
 */
public class XMLWriter {

	private Writer writer; // underlying writer
	private Stack<String> stack; // stack of open XML tag names
	private StringBuilder attrs; // current attribute string
	private boolean empty; // whether the current node is empty
	private boolean closed; // whether the current node was closed

	/**
	 * Create an XmlWriter on top of an existing java.io.Writer.
	 */
	public XMLWriter(Writer writer) {
		this.writer = writer;
		this.closed = true;
		this.empty = false;
		this.stack = new Stack<String>();
		this.attrs = new StringBuilder();

		try {
			writer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		} catch (IOException ioEx) {
			throw new XMLWritingException(ioEx);
		}
	}

	/**
	 * Begin to output a tag.
	 * 
	 * @param name
	 *            the name of the tag.
	 */
	public XMLWriter writeTag(String name) {
		try {
			closeOpeningTag();

			newLine();
			writer.write("<");
			writer.write(name);
			stack.add(name);

			closed = false;
			empty = true;

			return this;
		} catch (IOException ioEx) {
			throw new XMLWritingException(ioEx);
		}
	}

	// close off the opening tag
	private void closeOpeningTag() throws IOException {
		if (!closed) {
			writeAttributes();
			writer.write(">");

			closed = true;
			empty = false;
		}
	}

	// write out all current attributes
	private void writeAttributes() throws IOException {
		if (attrs != null) {
			writer.write(attrs.toString());
			attrs.setLength(0);
			empty = false;
		}
	}

	private void newLine() throws IOException {
		int level = stack.size();
		writer.append('\n');
		while (level-- > 0)
			writer.append('\t');
	}

	/**
	 * Write an attribute out for the current tag. Any XML characters in the
	 * value are escaped.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public XMLWriter writeAttribute(String name, String value) {
		attrs.append(" ");
		attrs.append(name);
		attrs.append("=\"");
		attrs.append(escapeXml(value));
		attrs.append("\"");
		return this;
	}

	/**
	 * Write an attribute out for the current tag. The value is converted to a
	 * string using the <tt>toString</tt> method. Any XML characters in the
	 * value are escaped.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public XMLWriter writeAttribute(String name, Object value) {
		return writeAttribute(name, value.toString());
	}

	/**
	 * End the current tag.
	 * 
	 * @throws XMLWritingException
	 *             if there is no open tag
	 */
	public XMLWriter endTag() {
		try {
			if (stack.empty()) {
				flush();
				throw new XMLWritingException(
						"Called endTag too many times. ");
			}

			String name = stack.pop();
			if (name != null) {
				if (empty) {
					writeAttributes();
					writer.write("/>");
				} else {
					newLine();
					writer.write("</");
					writer.write(name);
					writer.write(">");
				}
				empty = false;
				closed = true;
			}
			return this;
		} catch (IOException ioEx) {
			throw new XMLWritingException(ioEx);
		}
	}

	public void flush() {

		try {
			writer.flush();
		} catch (IOException ioEx) {
			throw new XMLWritingException(ioEx);
		}
	}

	
	/**
	 * Close this writer and its underlying writer
	 * 
	 * @throws XMLWritingException
	 *             if there are unclosed tags.
	 */
	public void close() {
		if (!this.stack.empty()) {
			flush();
			throw new XMLWritingException("Unclosed tag " + stack.peek());
		}
		try {
			writer.append('\n');
			writer.flush();
			writer.close();
		} catch (IOException ioEx) {
			throw new XMLWritingException(ioEx);
		}

	}

	/**
	 * Output body text. Any XML characters are escaped.
	 * 
	 * @param text
	 *            the text to be written
	 */
	public XMLWriter writeText(String text) {
		try {
			closeOpeningTag();
			newLine();
			writer.write(escapeXml(text));

			return this;
		} catch (IOException ioEx) {
			throw new XMLWritingException(ioEx);
		}
	}

	/**
	 * Output a comment. Any XML characters are escaped.
	 * 
	 * @param comment
	 *            the text to be written
	 */
	public XMLWriter writeComment(String comment) {
		try {
			closeOpeningTag();
			newLine();
			writer.write("<!-- "+ escapeXml(comment) + " -->");

			return this;
		} catch (IOException ioEx) {
			throw new XMLWritingException(ioEx);
		}
	}
	
	/**
	 * @param string
	 *            the string to be written to an XML file
	 * @return the escaped string
	 */
	public static String escapeXml(String string) {
		string = string.replaceAll("&", "&amp;");
		string = string.replaceAll("<", "&lt;");
		string = string.replaceAll(">", "&gt;");
		string = string.replaceAll("\"", "&quot;");
		string = string.replaceAll("'", "&apos;");
		return string;
	}

	public void writeText(Object object) {
		writeText(object.toString());
	}
}
