package hamburg.haw.polyshift.Menu;

import java.util.Comparator;
import java.util.HashMap;

import hamburg.haw.polyshift.Game.Block;

public class ScoreComparator implements Comparator<HashMap> {

	public int compare(HashMap lhs, HashMap rhs) {
        String lhs_string = (String) lhs.get("score");
        String lhs_win_string = (String) lhs.get("win");
        String lhs_loss_string = (String) lhs.get("loss");
        Integer lhs_win = Integer.parseInt(lhs_win_string);
        Integer lhs_loss = Integer.parseInt(lhs_loss_string);
        Integer lhs_score = Integer.parseInt(lhs_string);
        String rhs_win_string = (String) rhs.get("win");
        String rhs_loss_string = (String) rhs.get("loss");
        Integer rhs_win = Integer.parseInt(rhs_win_string);
        Integer rhs_loss = Integer.parseInt(rhs_loss_string);
        String rhs_string = (String) rhs.get("score");
        Integer rhs_score = Integer.parseInt(rhs_string);
        if(lhs_score == rhs_score && lhs_win + lhs_loss != 0 && rhs_win + rhs_loss != 0){
                return Integer.compare(lhs_win - lhs_loss, rhs_win - rhs_loss);
        }else if(lhs_score == rhs_score && lhs_win - lhs_loss == rhs_win - rhs_loss
                && lhs_win + lhs_loss != 0 && rhs_win + rhs_loss != 0){
                return Integer.compare(lhs_win, rhs_win);
        }else{
                return Integer.compare(lhs_score, rhs_score);
        }
	}

}
