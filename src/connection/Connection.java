package connection;

import node.Node;

/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 *
 */
public class Connection {

	public Node source;
	public Node target;

	public Connection(Node source, Node target) {
		this.source = source;
		this.target = target;
	}

	public Node getSource() {
		return source;
	}

	public void setSource(Node source) {
		this.source = source;
	}

	public Node getTarget() {
		return target;
	}

	public void setTarget(Node target) {
		this.target = target;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Connection)) {
			return false;
		}
		Connection connection = (Connection) obj;
		if (this.getSource() == connection.getSource() && this.getTarget() == connection.getTarget()) {
			return true;
		} else {
			return false;
		}
	}
}
