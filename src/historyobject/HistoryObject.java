package historyobject;

import java.util.*;

/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 *
 */
public final class HistoryObject {

	public final BitSet p1;
	public final BitSet p2;

	
	public HistoryObject(BitSet p1, BitSet p2){
		this.p1 = p1;
		this.p2 = p2;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if( this == obj ) {
			return true;
		}
		if( !(obj instanceof HistoryObject) ) {
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
