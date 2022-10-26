package historyobject;

import java.util.*;

/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 *
 */
public class HistoryObject {

	public BitSet p1 = new BitSet();
	public BitSet p2 = new BitSet();

	
	public HistoryObject(BitSet p1, BitSet p2){
		this.p1 = p1;
		this.p2 = p2;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof HistoryObject)) {
			return false;
		}
		HistoryObject other = (HistoryObject) obj;
		return this.p1.equals(other.p1) && this.p2.equals(other.p2);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return p1 + " " + p2;
	}
	
}