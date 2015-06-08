package hamburg.haw.polyshift.Menu;

import java.util.Comparator;
import java.util.HashMap;

import hamburg.haw.polyshift.Game.Block;

public class ScoreComparator implements Comparator<HashMap> {

	public int compare(HashMap lhs, HashMap rhs) {
        String lhs_string = (String) lhs.get("score");
        Integer lhs_score = Integer.parseInt(lhs_string);
        String rhs_string = (String) rhs.get("score");
        Integer rhs_score = Integer.parseInt(rhs_string);
		return Integer.compare(lhs_score, rhs_score);
	}

}
