package connection;

import node.*;

/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 *
 */
public final class Connection {

	private final Node source;
	private final Node target;

	public Connection(Node source, Node target) {
		this.source = source;
		this.target = target;
	}

	public Node getSource() {
		return source;
	}

	public Node getTarget() {
		return target;
	}

	@Override
	public boolean equals(Object obj) {

		if( this == obj ) {
			return true;
		}
		if( !(obj instanceof Connection) ) {
			return false;
		}
		Connection connection = (Connection) obj;
		if( this.getSource() == connection.getSource() && this.getTarget() == connection.getTarget() ) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return source + " " + target;
	}
}
