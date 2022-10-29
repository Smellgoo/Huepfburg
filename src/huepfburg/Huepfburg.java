package huepfburg;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import connection.*;
import historyobject.*;
import node.*;

/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 *
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
	private static long startTime;

	public final static void main(String[] args) {
		try {
			startTime = System.currentTimeMillis();
			//read test file
			List<String> values = new ArrayList<>();
			try( final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("src/testValues4.txt/"), StandardCharsets.UTF_8)); ){
				String line;
				while ((line = in.readLine()) != null) {
					values.add(line);
				}
			}
			
			//get number of nodes
			String string = values.get(0);
			numberOfNodes = Integer.parseInt(string.substring(0, string.indexOf(' ')));
			
			// remove first element in list (number of nodes, connections)
			values.remove(0);

			// convert lines to nodes and connections
			makeNodesAndConnections(values);
			
			//walk
			walk();

			//present findings
			finish();
			
			System.out.println("Total execution time: " + (System.currentTimeMillis() - startTime));
		}
		catch( IOException e) {
			System.err.println("Fehler beim einlesen der Testdatei");
			System.exit(1);
		}
	}
	
	public static void walk() {
		BitSet p1 = new BitSet(numberOfNodes);
		p1.set(1);
		BitSet p2 = new BitSet(numberOfNodes);
		p2.set(2);
		steps = 0;
		do {
			//For Paths
			HistoryObject historyObject = new HistoryObject((BitSet)p1.clone(),(BitSet)p2.clone());
			if(historyObjects.contains(historyObject)) {
				System.out.println("This is impossible.");
				System.out.println("Loops at state: " + historyObject);
				System.out.println("Total execution time: " + (System.currentTimeMillis() - startTime));
				System.exit(0);
			}
			historyObjects.add(historyObject);
			
			//Count the steps
			steps++;
			
			//Set bits accordingly
			List<Node> toChange = new ArrayList<>();
			flipBits(p1, toChange);
			flipBits(p2, toChange);
		}while(!p1.intersects(p2));
		historyObjects.add(new HistoryObject((BitSet)p1.clone(),(BitSet)p2.clone()));
		endNodeId = getEndNode().getId();
		BitSet bitSet1 = new BitSet(numberOfNodes);
		bitSet1.set(1);
		buildPath(bitSet1, "1", 0);
		BitSet bitSet2 = new BitSet(numberOfNodes);
		bitSet2.set(2);
		buildPath(bitSet2, "2", 0);
	}

	private static void flipBits(BitSet player, List<Node> toChange) {
		//For each bit that is true -> get the index (which is the nodeId) -> get the corresponding node and get the neighbors
		//get the id of those neighbors and flip the corresponding bits from false to true. And flip the orignal node to false
		player.stream().forEach(trueIndex -> {
			Node node = getNode(trueIndex);
			if(node.getId() != -1) {
				toChange.addAll(getNeighbors(node));
			}
			player.clear(trueIndex);
		});
		for( Node neighbor : toChange ) {
			player.set(neighbor.getId());
		}
		toChange.clear();
	}
	
	public static void buildPath(BitSet bitSet, String path, int depth) {
		int newDepth = depth + 1;
		BitSet nodesId = (BitSet)bitSet.clone();
		String nodeIdinCB = nodesId.toString();
		nodeIdinCB = nodeIdinCB.replace('{', ' ');
		nodeIdinCB = nodeIdinCB.replace('}', ' ');
		nodeIdinCB = nodeIdinCB.trim();
		if (nodeIdinCB.isEmpty()) {
			return;
		}
		List<Node> neighbors = getNeighbors(getNode(Integer.parseInt(nodeIdinCB)));
		//Keine Ahnung warum es nicht klappt. Irgendwas mit depth funktioniert nicht richtig und er findet nicht den  optimalsten Pfad.... debug
		for( Node node : neighbors ) {
			int nodeId = node.getId();
			//are you done?
			if( newDepth == steps ) {
				if( nodeId == endNodeId ) {
					//Ist nicht der kuerzeste sondern der erste
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
	
	public static void finish() {
		System.out.println("Zielknoten: " + endNodeId);
		System.out.println("Schritte: " + steps);
		System.out.println("Spieler 1: " + p1Paths.get(0));
		System.out.println("Spieler 2: " + p2Paths.get(0));
	}
	
	private static Node getEndNode() {
		//Letztes historyObject vergleichen und gucken welcher Node/bit gleich ist
		int historyObjectsSize = historyObjects.size();
		HistoryObject lastHistoryObject = historyObjects.get(Integer.valueOf(historyObjectsSize-1));
		BitSet intersection = (BitSet)lastHistoryObject.p1.clone();
		intersection.and(lastHistoryObject.p2);
		String nodeNumberInCurlyBraces = intersection.toString();
		//Zielnode
		String nodeNumber = nodeNumberInCurlyBraces.replace('{', ' ');
		nodeNumber = nodeNumber.replace('}', ' ');
		nodeNumber = nodeNumber.trim();
		//What if there are more possible end nodes
		if( nodeNumber.contains(",") ) {
			//just get the first
			return getNode(Integer.parseInt(nodeNumber.substring(0, nodeNumber.indexOf(','))));
		}
		else {
			return getNode(Integer.parseInt(nodeNumber));
		}
	}
	
	public static List<Node> getNeighbors(Node node) {
		List<Node> result = new ArrayList<>();
		if(connectionHashMapEmpty) {
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
	
	//gets node if it exists or return a -1 object
	public static Node getNode(int id) {
		for( Node node : nodeList ) {
			if( node.getId() == id ) {
				return node;
			}
		}
		return new Node(-1);
	}
	
	public static void makeNodesAndConnections(List<String> values) {
		for( String line : values ) {
			int emptySpaceIndex = line.indexOf(' ');
			String node = line.substring(0, emptySpaceIndex);
			//+1 to not get the empty space
			String target = line.substring(emptySpaceIndex+1, line.length());
			Node newNode = new Node(Integer.parseInt(node));
			if(! nodeList.contains(newNode) ) {
				nodeList.add(newNode);
			}
			Connection newConnection = new Connection(newNode, new Node(Integer.parseInt(target)));
			connectionList.add(newConnection);
			connectionHashMapEmpty = false;
		}
	}
}
