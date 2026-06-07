package app;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * In Suzuki-Kasami there is exactly one token per protected resource.
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
		this.LN = new HashMap<>(LN);
		this.Q = new LinkedList<>(Q);
	}

	public int getItemId() {
		return itemId;
	}

	public HashMap<Integer, Integer> getLN() {
		return LN;
	}

	public LinkedList<Integer> getQ() {
		return Q;
	}
}
