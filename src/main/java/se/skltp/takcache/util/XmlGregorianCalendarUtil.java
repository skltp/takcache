package se.skltp.takcache.util;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class XmlGregorianCalendarUtil {
	private static DatatypeFactory datatypeFactory = getDatatypeFactory();
	
	private static DatatypeFactory getDatatypeFactory() {
		try {
			return DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("Could not create DatatypeFactory", e);
		}
	}

	public static final XMLGregorianCalendar getNowAsXMLGregorianCalendar() {
		GregorianCalendar now = (GregorianCalendar) GregorianCalendar.getInstance();
		return datatypeFactory.newXMLGregorianCalendar(now);
	}

	public static boolean isTimeWithinInterval(XMLGregorianCalendar time, XMLGregorianCalendar from, XMLGregorianCalendar to){
		return time.compare(from) != DatatypeConstants.LESSER && time.compare(to) != DatatypeConstants.GREATER;
	}

}
