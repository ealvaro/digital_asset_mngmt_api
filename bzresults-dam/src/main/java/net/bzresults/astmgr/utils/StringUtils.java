package net.bzresults.astmgr.utils;

public class StringUtils {
    public static String removeNonAlphaNumeric(String source) {
        return (source != null) ? source.replaceAll("\\W", "") : source;
    }
    
    public static final boolean isEmpty(String s) {
        return (s == null || s.trim().length() == 0);
    }
    
    public static final boolean isNotEmpty(String s) {
    	return !isEmpty(s);
    }
    
    public static final boolean isEqual(String s1, String s2) {
    	if(s1 != null && s2 != null) {
    		return s1.equals(s2);
    	} else if(s1 == null && s2 == null) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static final boolean isNotEqual(String s1, String s2) {
    	return !isEqual(s1, s2);
    }
}
