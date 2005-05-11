package jaxp;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.Duration;

public class DatatypeAPITest {
    public static void main (String[] args) throws Exception {
        DatatypeFactory df = DatatypeFactory.newInstance();
        // my work number in milliseconds:
        Duration myPhone = df.newDuration(9054133519l);
        // my approximate life span up to the conference
        Duration myLife = df.newDuration(true, 29, 2, 15, 13, 45, 0);
        int compareVal = myPhone.compare(myLife);
        switch (compareVal) {
        case DatatypeConstants.LESSER:
            System.out.println("There are fewer milliseconds in my phone number than my lifespan.");
            break;
        case DatatypeConstants.EQUAL:
            System.out.println("The same number of milliseconds are in my phone number and my lifespan.");
            break;
        case DatatypeConstants.GREATER:
            System.out.println("There are more milliseconds in my phone number than my lifespan.");
            break;
        case DatatypeConstants.INDETERMINATE:
            System.out.println("The comparison could not be carried out.");
        }
        
        // create a yearmonthDuration
        Duration ymDuration = df.newDurationYearMonth("P12Y10M");
        System.out.println("P12Y10M is of type: " + ymDuration.getXMLSchemaType());
        
        // try to fool the factory!
        try {
            ymDuration = df.newDurationYearMonth("P12Y10M1D");
        }
        catch(IllegalArgumentException e) {
            System.out.println("'duration': P12Y10M1D is not 'yearMonthDuration'!!!");
        }
        
        XMLGregorianCalendar xgc = df.newXMLGregorianCalendar();
        xgc.setYear(1975);
        xgc.setMonth(DatatypeConstants.AUGUST);
        xgc.setDay(11);
        xgc.setHour(6);
        xgc.setMinute(44);
        xgc.setSecond(0);
        xgc.setMillisecond(0);
        xgc.setTimezone(5);
        xgc.add(myPhone);
        System.out.println("The approximate end of the number of milliseconds in my phone number was " + xgc);
        
        // create a new XMLGregorianCalendar using the string format of xgc.
        XMLGregorianCalendar xgc_copy = df.newXMLGregorianCalendar(xgc.toXMLFormat());
        
        //should be equal-if not what happened!!
        if(xgc_copy.compare(xgc) != DatatypeConstants.EQUAL) {
            System.out.println("oooops!");
        }
        else {
            System.out.println("Very good: " + xgc +  " is values are equal to " + xgc_copy);
        }
    }
}

