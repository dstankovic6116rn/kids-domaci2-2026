package servent.message;

/**
 * Sent by a searching node toward the node responsible for hash(productName).
 * That node looks up its local name index, filters by exact name, and sends
 * a SearchLookupReplyMessage directly back to originPort.
 *
 * Routing: forwarded hop-by-hop (see SearchLookupHandler) until it arrives
 * at the node whose key range includes nameKey.
 *
 * originPort is the port of the node that originally triggered the search
 * command.  It is preserved through all forwarding hops so the reply can
 * go directly back to the requester in a single hop, bypassing the ring.
 */
public class SearchLookupMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// The exact product name the user searched for.
	private final String name;

	// Pre-computed Chord key for the name: Math.floorMod(name.hashCode(), CHORD_SIZE).
	private final int nameKey;

	// Port of the node that initiated the search — reply goes here directly.
	private final int originPort;

	public SearchLookupMessage(int senderPort, int receiverPort, String name, int nameKey, int originPort) {
		super(MessageType.SEARCH_LOOKUP, senderPort, receiverPort);
		this.name = name;
		this.nameKey = nameKey;
		this.originPort = originPort;
	}

	public String getName() { return name; }
	public int getNameKey() { return nameKey; }
	public int getOriginPort() { return originPort; }
}
