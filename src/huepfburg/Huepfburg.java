package huepfburg;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import connection.Connection;
import historyobject.HistoryObject;
import node.Node;

/**
 * @author <a href="mailto:Leon.Havel@Materna.DE">Leon Havel</a>
 *
 */
public class Huepfburg {

	/**
	 * Path to the test values
	 */
	public final static Path testValuePath = Paths.get("C:/USR/workspace/huepfburg/src/testValues1.txt/");
	private static List<Node> nodeList = new ArrayList<>();
	private static List<Connection> connectionList = new ArrayList<>();
	private static List<HistoryObject> historyObjects = new ArrayList<>();
	private static List<String> p1Paths = new ArrayList<>();
	private static List<String> p2Paths = new ArrayList<>();
	private static int endNodeId;
	private static int steps;
	private static int numberOfNodes;
	private static boolean possible = true;
	private static boolean connectionListEmpty = true;

	public final static void main(String[] args) {
		try {
			final long startTime = System.currentTimeMillis();
			final List<String> values = Files.readAllLines(testValuePath);
//			String[] split = values.get(0).split(" ");
			numberOfNodes = Integer.parseInt(values.get(0).split(" ")[0]);
			
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
			if(historyObjects.contains(historyObject)) {
				System.out.println("This is impossible.");
				System.out.println("Loops at state: " + historyObject);
				possible = false;
				return;
			}
			historyObjects.add(historyObject);
			
			//Count the steps
			steps++;
			
			//Set bits accordingly
			final List<Node> toChange = new ArrayList<>();
			flipBits(p1, toChange);
			flipBits(p2, toChange);
		}while(!p1.intersects(p2));
		historyObjects.add(new HistoryObject((BitSet)p1.clone(),(BitSet)p2.clone()));
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
		player.stream().forEach(indexThatIsTrue -> {
			final Node node = getNode(indexThatIsTrue);
			if(node.getId() != -1) {
				toChange.addAll(getNeighbors(node));
			}
			player.clear(indexThatIsTrue);
		});
		toChange.stream().forEach(nodeNeighbor -> {
			player.set(nodeNeighbor.getId());
		});
		toChange.clear();
	}
	
	public final static void buildPath(BitSet bitSet, String path, int depth) {
		final int newDepth = depth + 1;
		final BitSet nodesId = (BitSet)bitSet.clone();
		 String nodeIdInCurlyBraces = nodesId.toString();
		 nodeIdInCurlyBraces = nodeIdInCurlyBraces.replace('{', ' ');
		 nodeIdInCurlyBraces = nodeIdInCurlyBraces.replace('}', ' ');
		 nodeIdInCurlyBraces = nodeIdInCurlyBraces.trim();
		if( nodeIdInCurlyBraces.isEmpty() ) {
			return;
		}
		final List<Node> neighbors = getNeighbors(getNode(Integer.parseInt(nodeIdInCurlyBraces)));
		//Keine Ahnung warum es nicht klappt. Irgendwas mit depth funktioniert nicht richtig und er findet nicht den  optimalsten Pfad.... debug
		for( final Node node : neighbors ) {
			final int nodeId = node.getId();
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
	
	public final static void finish() {
		System.out.println("Zielknoten: " + endNodeId);
		System.out.println("Schritte: " + steps);
		System.out.println("Spieler 1: " + p1Paths.get(0));
		System.out.println("Spieler 2: " + p2Paths.get(0));
	}
	
	private final static Node getEndNode() {
		//Letztes historyObject vergleichen und gucken welcher Node/bit gleich ist
		final int historyObjectsSize = historyObjects.size();
		final HistoryObject lastHistoryObject = historyObjects.get(historyObjectsSize-1);
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
			return getNode(Integer.parseInt(nodeNumber.split(", ")[0]));
		}
		else {
			return getNode(Integer.parseInt(nodeNumber));
		}
	}
	
	public final static List<Node> getNeighbors(Node node) {
		final List<Node> result = new ArrayList<>();
		if(connectionListEmpty) {
			System.err.println("No connections available");
			System.exit(1);
		}
		final List<Connection> clonedConnectionList = new ArrayList<>(connectionList);
		// add the neighbors to result
		for( Connection connection : clonedConnectionList ) {
			if( connection.getSource().equals(node) ) {
				result.add(connection.getTarget());
			}
		}
		return result;
	}
	
	//gets node if it already exists or creates it
	public final static Node getNode(int id) {
		for( Node node : nodeList ) {
			if( node.getId() == id ) {
				return node;
			}
		}
		//test
		//Can happen if the node is a dead end
		return new Node(-1);
	}
	
	public final static void makeNodesAndConnections(String line) {
		final String[] ids = line.split(" ");
		final String knoten = ids[0];
		final String zielKnoten = ids[1];
		final Node newNode = new Node(Integer.parseInt(knoten));
		//HashMap would be faster
		if (!nodeList.contains(newNode)) {
			nodeList.add(newNode);
		}
		final Connection newConnection = new Connection(new Node(Integer.parseInt(knoten)), new Node(Integer.parseInt(zielKnoten)));
		if( !connectionList.contains(newConnection) ) {
			connectionList.add(newConnection);
			connectionListEmpty = false;
		}
	}
}
