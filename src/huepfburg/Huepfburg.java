package huepfburg;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import connection.*;
import historyobject.*;
import node.*;

/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 *
 */
public class Huepfburg {

	private final static Path testValuePath = Paths.get("src/testValues1.txt/");
	private final static HashMap<Integer, Node> nodeList = new HashMap<>();
	private final static HashMap<Integer, Connection> connectionList = new HashMap<>();
	private final static HashMap<Integer, HistoryObject> historyObjects = new HashMap<>();
	private final static HashMap<Integer, String> p1Paths = new HashMap<>();
	private final static HashMap<Integer, String> p2Paths = new HashMap<>();
	private static int endNodeId;
	private static int steps;
	private static int numberOfNodes;
	private static boolean possible = true;
	private static boolean connectionHashMapEmpty = true;

	public final static void main(String[] args) {
		try {
			final List<String> values = Files.readAllLines(testValuePath);
			final long startTime = System.currentTimeMillis();
			String string = values.get(0);
			numberOfNodes = Integer.parseInt(string.substring(0, string.indexOf(' ')));
			
			// remove first element in list (number of nodes, connections)
			values.remove(0);

			// convert lines to nodes and connections
			for( String line : values ) {
				makeNodesAndConnections(line);
			}
			
			//walk
			walk();

			if( possible ) {
				//present findings
				finish();
			}
			System.out.println("Total execution time: " + (System.currentTimeMillis() - startTime));
		}
		catch( IOException e) {
			System.err.println("Fehler beim einlesen der Testdatei");
			System.exit(1);
		}
	}
	
	public final static void walk() {
		//zwei loops einer laeuft den geraden  einer den ungeraden, die loops muessen sich schneiden
		final BitSet p1 = new BitSet(numberOfNodes);
		p1.set(1);
		final BitSet p2 = new BitSet(numberOfNodes);
		p2.set(2);
		steps = 0;
		do {
			//For Paths
			final HistoryObject historyObject = new HistoryObject((BitSet)p1.clone(),(BitSet)p2.clone());
			if(historyObjects.containsValue(historyObject)) {
				System.out.println("This is impossible.");
				System.out.println("Loops at state: " + historyObject);
				possible = false;
				return;
			}
			historyObjects.put(Integer.valueOf(steps), historyObject);
			
			//Count the steps
			steps++;
			
			//Set bits accordingly
			final List<Node> toChange = new ArrayList<>();
			flipBits(p1, toChange);
			flipBits(p2, toChange);
		}while(!p1.intersects(p2));
		historyObjects.put(Integer.valueOf(steps),new HistoryObject((BitSet)p1.clone(),(BitSet)p2.clone()));
		endNodeId = getEndNode().getId();
		final BitSet bitSet1 = new BitSet(numberOfNodes);
		bitSet1.set(1);
		buildPath(bitSet1, "1", 0);
		final BitSet bitSet2 = new BitSet(numberOfNodes);
		bitSet2.set(2);
		buildPath(bitSet2, "2", 0);
	}

	private final static void flipBits(final BitSet player, final List<Node> toChange) {
		//For each bit that is true -> get the index (which is the nodeId) -> get the corresponding node and get the neighbors
		//get the id of those neighbors and flip the corresponding bits from false to true. And flip the orignal node to false
		player.stream().forEach(trueIndex -> {
			final Node node = getNode(trueIndex);
			if(node.getId() != -1) {
				toChange.addAll(getNeighbors(node));
			}
			player.clear(trueIndex);
		});
		toChange.stream().forEach(neighbor -> {
			player.set(neighbor.getId());
		});
		toChange.clear();
	}
	
	public final static void buildPath(BitSet bitSet, String path, int depth) {
		final int newDepth = depth + 1;
		final BitSet nodesId = (BitSet)bitSet.clone();
		 String nodeIdinCB = nodesId.toString();
		 nodeIdinCB = nodeIdinCB.replace('{', ' ');
		 nodeIdinCB = nodeIdinCB.replace('}', ' ');
		 nodeIdinCB = nodeIdinCB.trim();
		if( nodeIdinCB.isEmpty() ) {
			return;
		}
		final List<Node> neighbors = getNeighbors(getNode(Integer.parseInt(nodeIdinCB)));
		//Keine Ahnung warum es nicht klappt. Irgendwas mit depth funktioniert nicht richtig und er findet nicht den  optimalsten Pfad.... debug
		for( final Node node : neighbors ) {
			final int nodeId = node.getId();
			//are you done?
			if( newDepth == steps ) {
				if( nodeId == endNodeId ) {
					//Ist nicht der kuerzeste sondern der erste
					if( path.startsWith("1") ) {
						p1Paths.put(Integer.valueOf(newDepth),path + " -> " + nodeId);
					}
					else {
						p2Paths.put(Integer.valueOf(newDepth),path + " -> " + nodeId);
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
	
	public final static void finish() {
		System.out.println("Zielknoten: " + endNodeId);
		System.out.println("Schritte: " + steps);
		System.out.println("Spieler 1: " + p1Paths.values().toString());
		System.out.println("Spieler 2: " + p2Paths.values().toString());
	}
	
	private final static Node getEndNode() {
		//Letztes historyObject vergleichen und gucken welcher Node/bit gleich ist
		final int historyObjectsSize = historyObjects.size();
		final HistoryObject lastHistoryObject = historyObjects.get(Integer.valueOf(historyObjectsSize-1));
		final BitSet intersection = (BitSet)lastHistoryObject.p1.clone();
		intersection.and(lastHistoryObject.p2);
		final String nodeNumberInCurlyBraces = intersection.toString();
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
	
	public final static List<Node> getNeighbors(Node node) {
		final List<Node> result = new ArrayList<>();
		if(connectionHashMapEmpty) {
			System.err.println("No connections available");
			System.exit(1);
		}
		for( Connection connection : connectionList.values() ) {
			if( connection.getSource().equals(node) ) {
				result.add(connection.getTarget());
			}
		}
		return result;
//		final HashMap<Integer, List<Connection>> clonedConnectionHashMap = new HashMap<>(connectionList);
//		// add the neighbors to result
//		List<Connection> values = clonedConnectionHashMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
	}
	
	//gets node if it exists or return a -1 object
	public final static Node getNode(int id) {
		for( Node node : nodeList.values() ) {
			if( node.getId() == id ) {
				return node;
			}
		}
		return new Node(-1);
	}
	
	public final static void makeNodesAndConnections(String line) {
		int emptySpaceIndex = line.indexOf(' ');
		final String node = line.substring(0, emptySpaceIndex);
		//+1 to not get the empty space
		final String target = line.substring(emptySpaceIndex+1, line.length());
		final Node newNode = new Node(Integer.parseInt(node));
		//HashMap would be faster
		if (!nodeList.containsValue(newNode)) {
			nodeList.put(Integer.valueOf(nodeList.size()),newNode);
		}
		final Connection newConnection = new Connection(new Node(Integer.parseInt(node)), new Node(Integer.parseInt(target)));
		if( !connectionList.containsValue(newConnection) ) {
			connectionList.put(Integer.valueOf(connectionList.size()), newConnection);
			connectionHashMapEmpty = false;
		}
	}
}
