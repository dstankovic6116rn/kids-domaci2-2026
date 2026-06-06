package app;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Suzuki-Kasami token for a single item's buy mutex.
 *
 * In Suzuki-Kasami there is exactly one token per protected resource (here:
 * per item).  The token physically moves between nodes; whoever holds it
 * is allowed to enter the critical section.
 *
 * Fields:
 *   - itemId: which item this token belongs to (each item has its own token).
 *   - LN:  per-node "Last granted sequence Number".  LN[port] = the last
 *          request seq from `port` that has already been served.
 *   - Q:   FIFO queue of node ports waiting for the token.  Populated by
 *          the token holder when it exits the CS, based on RN[k] vs LN[k].
 *
 * The token travels inside MutexTokenMessage between nodes.
 */
public class MutexToken implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final HashMap<Integer, Integer> LN;
	private final LinkedList<Integer> Q;

	public MutexToken(int itemId) {
		this.itemId = itemId;
		this.LN = new HashMap<>();
		this.Q = new LinkedList<>();
	}

	public MutexToken(int itemId, Map<Integer, Integer> LN, Queue<Integer> Q) {
		this.itemId = itemId;
		// Defensive copies — the token is mutated by each holder, we don't
		// want stale references shared between nodes.
		this.LN = new HashMap<>(LN);
		this.Q = new LinkedList<>(Q);
	}

	public int getItemId() { return itemId; }
	public HashMap<Integer, Integer> getLN() { return LN; }
	public LinkedList<Integer> getQ() { return Q; }
}
