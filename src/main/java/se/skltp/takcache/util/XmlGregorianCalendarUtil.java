package se.skltp.takcache.util;

import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class XmlGregorianCalendarUtil {
	private static final DatatypeFactory datatypeFactory;

	static {
		try {
			datatypeFactory = getDatatypeFactory();
		} catch (DatatypeConfigurationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private XmlGregorianCalendarUtil() {
    // Private Utility class
  }
	
	private static DatatypeFactory getDatatypeFactory() throws DatatypeConfigurationException {
		return DatatypeFactory.newInstance();
	}

	public static XMLGregorianCalendar getNowAsXMLGregorianCalendar() {
		GregorianCalendar now = new GregorianCalendar();
		return datatypeFactory.newXMLGregorianCalendar(now);
	}

	public static boolean isTimeWithinInterval(XMLGregorianCalendar time, XMLGregorianCalendar from, XMLGregorianCalendar to){
		return time.compare(from) != DatatypeConstants.LESSER && time.compare(to) != DatatypeConstants.GREATER;
	}

}
