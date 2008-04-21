package net.bzresults.astmgr.utils;

import java.util.Collection;
import java.util.List;

public class CollectionUtils {

	public static boolean isEmpty(Collection c) {
		return (c == null || c.isEmpty());
	}
	
	public static boolean isNotEmpty(Collection c) {
		return !isEmpty(c);
	}
	
    public static final boolean isInBounds(Collection c, int pos) {
    	if(isNotEmpty(c)) {
    		return (pos >= 0 && c.size() > pos);
    	}
    	
    	return false;
    }
    
    public static final boolean isNotInBounds(Collection c, int pos) {
    	return !isInBounds(c, pos);
    }
    
    public static final <O> List<O> moveTo(List<O> l, int from, int to) {
    	if(isNotInBounds(l, from) && isNotInBounds(l, to)) {
    		//throw new IndexOutOfBoundsException("Position " + from + "->" + to + " is out of bounds of list");
    		// invalid arguments just return
    		return l;
    	}
    	
    	O obj = l.get(from);
    	// moving left in the list
    	// so we remove the item first and then add it 
    	// b/c the index of the remove item will shift to the right
    	if(from > to) {
    		l.remove(from);
    		l.add(to, obj);
    	} else {
    		// we use to + 1 b/c we want it added after the element
    		l.add(to + 1, obj);
    		l.remove(from);
    	}
    	
    	return l;
    }
}
