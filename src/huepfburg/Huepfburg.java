package huepfburg;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;

import connection.*;
import historyobject.*;
import node.*;


/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 */
public final class Huepfburg {
	private final static List<Node> nodeList = new ArrayList<>();
	private final static List<Connection> connectionList = new ArrayList<>();
	private final static List<HistoryObject> historyObjects = new ArrayList<>();
	private final static List<String> p1Paths = new ArrayList<>();
	private final static List<String> p2Paths = new ArrayList<>();
	private static boolean connectionHashMapEmpty = true;
	private static int endNodeId;
	private static int steps;
	private static int numberOfNodes;

	public final static void main(String[] args) {
		try {
			//read test file
			List<String> values = readTestFile();

			//get number of nodes
			String string = values.get(0);
			numberOfNodes = Integer.parseInt(string.substring(0, string.indexOf(' ')));

			// remove first element in list (number of nodes, connections)
			values.remove(0);

			// convert lines to nodes and connections
			makeNodesAndConnections(values);

			//simulate each player taking steps
			walk();

			//present findings
			finish();
		}
		catch( Exception e1 ) {
			System.err.println("Fehler beim verarbeiten der Test-Datei");
			System.exit(1);
		}
	}

	/**
	 * Simulate two players taking steps in a graph.
	 */
	private static void walk() {
		BitSet p1 = new BitSet(numberOfNodes);
		p1.set(1);
		BitSet p2 = new BitSet(numberOfNodes);
		p2.set(2);
		steps = 0;
		do {
			//For infinite loop detection
			HistoryObject historyObject = new HistoryObject((BitSet)p1.clone(), (BitSet)p2.clone());
			if( historyObjects.contains(historyObject) ) {
				System.out.println("This is impossible.");
				System.out.println("Loops at state: " + historyObject);
				System.exit(0);
			}
			historyObjects.add(historyObject);

			//Count steps
			steps++;

			//Set bits accordingly
			List<Node> toChange = new ArrayList<>();
			flipBits(p1, toChange);
			flipBits(p2, toChange);
		}while( !p1.intersects(p2) );
		historyObjects.add(new HistoryObject((BitSet)p1.clone(), (BitSet)p2.clone()));
		endNodeId = getEndNode().getId();
		//For paths
		BitSet bitSet1 = new BitSet(numberOfNodes);
		bitSet1.set(1);
		buildPath(bitSet1, "1", 0);
		BitSet bitSet2 = new BitSet(numberOfNodes);
		bitSet2.set(2);
		buildPath(bitSet2, "2", 0);
	}

	/**
	 * For each index of the <code>BitSet</code> that is set to true, we get the corresponding <code>Node</code> and that <code>Node's</code> the neighbors.<br>
	 * Then we "flip" each index that was set to true, to false.<br>
	 * And finally, we set for each Neighbor the corresponding index to true.
	 * 
	 * @param player
	 * @param toChange
	 */
	private static void flipBits(BitSet player, List<Node> toChange) {
		player.stream().forEach(trueIndex -> {
			Node node = getNode(trueIndex);
			if( node.getId() != -1 ) {
				toChange.addAll(getNeighbors(node));
			}
			player.clear(trueIndex);
		});
		for( Node neighbor : toChange ) {
			player.set(neighbor.getId());
		}
		toChange.clear();
	}

	/**
	 * Recursively "walk" down a path.<br>
	 * We know the number of steps it takes and the id of the final <code>Node</code>.
	 * 
	 * @param bitSet
	 * @param path
	 * @param depth
	 */
	private static void buildPath(BitSet bitSet, String path, int depth) {
		int newDepth = depth + 1;
		BitSet nodesId = (BitSet)bitSet.clone();
		String nodeIdinCB = nodesId.toString();
		nodeIdinCB = nodeIdinCB.replace('{', ' ');
		nodeIdinCB = nodeIdinCB.replace('}', ' ');
		nodeIdinCB = nodeIdinCB.trim();
		if( nodeIdinCB.isEmpty() ) {
			return;
		}
		List<Node> neighbors = getNeighbors(getNode(Integer.parseInt(nodeIdinCB)));
		for( Node node : neighbors ) {
			int nodeId = node.getId();
			if( newDepth == steps ) {
				if( nodeId == endNodeId ) {
					if( path.startsWith("1") ) {
						p1Paths.add(path + " -> " + nodeId);
					}
					else {
						p2Paths.add(path + " -> " + nodeId);
					}
					return;
				}
				continue;
			}
			nodesId.clear();
			nodesId.set(nodeId);
			buildPath(nodesId, path + " -> " + nodeId, newDepth);
		}
		return;
	}


	private static void finish() {
		System.out.println("Zielknoten: " + endNodeId);
		System.out.println("Schritte: " + steps);
		System.out.println("Spieler 1: " + p1Paths.get(0));
		System.out.println("Spieler 2: " + p2Paths.get(0));
	}

	/**
	 * We get the node by checking at which index both <code>BitSet</code> are set to true.<br>
	 * There could be multiple but we just get the first.
	 * 
	 * @return
	 */
	private static Node getEndNode() {
		int historyObjectsSize = historyObjects.size();
		HistoryObject lastHistoryObject = historyObjects.get(historyObjectsSize - 1);
		BitSet intersection = (BitSet)lastHistoryObject.p1.clone();
		intersection.and(lastHistoryObject.p2);
		String nodeNumberInCurlyBraces = intersection.toString();
		String nodeNumber = nodeNumberInCurlyBraces.replace('{', ' ');
		nodeNumber = nodeNumber.replace('}', ' ');
		nodeNumber = nodeNumber.trim();
		//If there are more possible end nodes...
		if( nodeNumber.contains(",") ) {
			//just get the first
			return getNode(Integer.parseInt(nodeNumber.substring(0, nodeNumber.indexOf(','))));
		}
		else {
			return getNode(Integer.parseInt(nodeNumber));
		}
	}


	/**
	 * We get every "Target" of a <code>Connection</code> with the <code>Node</code> as the "Source" of the <code>Connection</code>.
	 * 
	 * @param node
	 * @return
	 */
	private static List<Node> getNeighbors(Node node) {
		List<Node> result = new ArrayList<>();
		if( connectionHashMapEmpty ) {
			System.err.println("No connections available");
			System.exit(3);
		}
		for( Connection connection : connectionList ) {
			if( connection.getSource().equals(node) ) {
				result.add(connection.getTarget());
			}
		}
		return result;
	}


	/**
	 * Get the <code>Node</code> from the Node-List if a <code>Node</code> with that Id exists.<br>
	 * Otherwise return a <code>Node</code> with the Id -1.
	 * @param id
	 * @return
	 */
	private static Node getNode(int id) {
		for( Node node : nodeList ) {
			if( node.getId() == id ) {
				return node;
			}
		}
		return new Node(-1);
	}


	/**
	 * This method converts every line in a test file into <code>Node's</code> and <code>Connection's</code>.<br>
	 * <pre>Line:"1 2" to <code>Node</code> with Id 1</pre> and <code>Connection</code> with <pre><code>Node</code> Id 1 and <code>Node</code> Id 2</pre></pre>
	 * 
	 * @param values
	 */
	private static void makeNodesAndConnections(List<String> values) {
		for( String line : values ) {
			int emptySpaceIndex = line.indexOf(' ');
			String node = line.substring(0, emptySpaceIndex);
			//+1 to not get the empty space
			String target = line.substring(emptySpaceIndex + 1, line.length());
			Node newNode = new Node(Integer.parseInt(node));
			if( !nodeList.contains(newNode) ) {
				nodeList.add(newNode);
			}
			Connection newConnection = new Connection(newNode, new Node(Integer.parseInt(target)));
			connectionList.add(newConnection);
			connectionHashMapEmpty = false;
		}
	}

	/**
	 * Let the user choose the test file and converts the lines into an <code>ArrayList&ltString&gt</code>
	 * 
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	
	private static List<String> readTestFile() throws IOException, FileNotFoundException {
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

		int returnValue = jfc.showOpenDialog(null);
		File selectedFile = null;
		if( returnValue == JFileChooser.APPROVE_OPTION ) {
			selectedFile = jfc.getSelectedFile();
		}
		else {
			System.out.println("Please choose a test file.");
			System.exit(0);
		}
		//read test file
		List<String> values = new ArrayList<>();
		try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile), StandardCharsets.UTF_8));) {
			String line;
			while( (line = in.readLine()) != null ) {
				values.add(line);
			}
		}
		return values;
	}
}