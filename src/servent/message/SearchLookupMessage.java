package servent.message;

/**
 * Forwarded hop-by-hop until it arrives
 * at the node whose key range includes nameKey.
 */
public class SearchLookupMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// Product name
	private final String name;

	// Chord key for the name
	private final int nameKey;

	// Port of the node that started the search
	private final int originPort;

	public SearchLookupMessage(int senderPort, int receiverPort, String name, int nameKey, int originPort) {
		super(MessageType.SEARCH_LOOKUP, senderPort, receiverPort);
		this.name = name;
		this.nameKey = nameKey;
		this.originPort = originPort;
	}

	public String getName() {
		return name;
	}

	public int getNameKey() {
		return nameKey;
	}

	public int getOriginPort() {
		return originPort;
	}
}
