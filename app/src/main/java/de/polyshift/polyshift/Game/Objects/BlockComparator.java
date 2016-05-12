package de.polyshift.polyshift.Game.Objects;

import java.util.Comparator;

/**
 * Stellt eine Methode zum Sortieren von Blöcken zur Verfügung
 *
 * @author helmsa
 *
 */

public class BlockComparator implements Comparator<Block> {

	public int compare(Block lhs, Block rhs) {
		return Integer.compare(lhs.y, rhs.y);
	}

}
