package node;

/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 *
 */
public class Node {

	public int id;

	public Node(int id) {
		this.id = id;

	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Node)) {
			return false;
		}
		Node node = (Node) obj;
		if (this.getId() == node.getId()) {
			return true;
		} else {
			return false;
		}
	}

}