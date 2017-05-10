import java.util.LinkedList;

/*
 * Contain a packet that traverses the network
 */
class Packet {

	// The final router
	private int					destination;
	// The origin router
	private int					source;
	// The path this packet takes
	private LinkedList<Integer>	path	= new LinkedList<Integer>();

	/*
	 * Instantiate a packet, given source and destination
	 */
	Packet(int s, int d) {
		source = s;
		destination = d;
	}

	/*
	 * Return the packet's source.
	 */
	int getSource() {
		return source;
	}

	/*
	 * Return the packet's destination.
	 */
	int getDestination() {
		return destination;
	}

	/*
	 * Record the current location as the packet traverses the network.
	 */
	void Record(int router) {
		path.add(router);
	}

	/*
	 * Print the route the packet took through the network.
	 */
	void Print() {
		System.out.println("Packet source=" + source + " destination=" + destination);
		System.out.print("    path: ");
		for (int i = 0; i < path.size(); i++) {
			System.out.print(" " + path.get(i));
		}
		System.out.println();
	}
}