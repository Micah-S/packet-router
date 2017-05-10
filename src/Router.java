import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/*
 * This runnable routes packets as they traverse the network.
 */
class Router implements Runnable {

	private LinkedList<Packet>	list	= new LinkedList<Packet>();
	private int					routes[];
	private Router				routers[];
	private int					routerNum;
	private static boolean		end		= false;
	private static Set<Integer>	packets	= new HashSet<Integer>();	// counts total packets that
																	// have found their home

	Router(int rts[], Router rtrs[], int num) {
		routes = rts;
		routers = rtrs;
		routerNum = num;
	}

	/*
	 * Add a packet to this router. Synchronizes over the list before adding, and notifies anything
	 * synchronized on the list after doing so
	 */
	public void addWork(Packet p) {
		synchronized (list) {
			list.add(p);
			list.notifyAll();
		}
	}

	/*
	 * End the thread, once no more packets are outstanding.
	 */
	public void end() {
		synchronized (packets) {
			// wait for the processed packet count to be equal to the number of originally created
			// packets
			while (packets.size() < routing.getPacketCount()) {
				try {
					packets.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			end = true;
		}

		// notify list so we can break free from the run loop
		synchronized (list) {
			list.notifyAll();
		}
	}

	public void networkEmpty() {

	}

	/*
	 * Process packets. Starts out by synchronizing over list, and waiting for new packets to
	 * arrive. When they do, the router records its number in the packet, and checks where it needs
	 * to go. If the packet isn't at the correct router, it becomes sent to that router instead. If
	 * the packet is at the correct router, it will keep the packet in list and continue the loop.
	 */
	public void run() {

		// keeps track of the number of packets that were sent to this router as the destination
		int i = 0;

		// holds the current packet we are processing
		Packet p;

		while (!end) {
			// reset p to null
			p = null;

			synchronized (list) {

				// if true, there aren't any new packets to process, and we shouldn't end
				while (list.size() <= i && !end) {
					try {
						list.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				// set p to the newly received packet
				try {
					p = list.get(i);
				} catch (Exception ex) {
				}

				if (p != null) {
					p.Record(routerNum); // record this router

					// if this isn't the correct router...
					if (p.getDestination() != routerNum) {
						// remove it from our list, and notify the list since we no longer need it
						list.remove(i);
						list.notifyAll();
					} else if (p.getDestination() == routerNum) {
						// if this is the correct router...
						i++; // because it's time to move on

						// add this packets hash to set, to keep track of count
						synchronized (packets) {
							packets.add(p.hashCode());
						}
						p = null; // so it doesn't get sent off again below
						list.notifyAll();

						// when the final packet has been added, we notify so end() can properly
						// execute
						if (packets.size() >= routing.getPacketCount()) {
							synchronized (packets) {
								packets.notifyAll();
							}
						}
					}
				}
			}
			// if p is still set, that means it needs to get forwarded to the next router
			if (p != null) {
				routers[routes[p.getDestination()]].addWork(p);
			}
		}
	}
}