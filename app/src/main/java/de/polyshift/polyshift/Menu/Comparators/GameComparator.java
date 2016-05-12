package de.polyshift.polyshift.Menu.Comparators;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashMap;

public class GameComparator implements Comparator<HashMap> {

	public int compare(HashMap lhs, HashMap rhs) {
        String lhs_string = (String) lhs.get("timestamp");
        Timestamp lhs_time = Timestamp.valueOf(lhs_string);
        String rhs_string = (String) rhs.get("timestamp");
        Timestamp rhs_time = Timestamp.valueOf(rhs_string);
		return Long.compare(lhs_time.getTime(), rhs_time.getTime());
	}

}
