package connection;

import node.*;


/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 */
public record Connection (Node source, Node target){

	public Node getSource() {
		return source;
	}

	public Node getTarget() {
		return target;
	}
	@Override
	public String toString() {
		return source + " " + target;
	}
}