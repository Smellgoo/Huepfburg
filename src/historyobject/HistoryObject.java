package historyobject;

import java.util.*;


/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 */
public record HistoryObject (BitSet p1, BitSet p2) {
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return p1 + " " + p2;
	}
}
