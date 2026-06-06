package cli.command;

import app.AppConfig;
import app.ServentInfo;
import servent.message.SubscribeRequestMessage;
import servent.message.util.MessageUtil;

/**
 * CLI command: subscribe [host:port]
 *
 * Subscribes THIS node to the node at the given address.
 * After subscribing, whenever that remote node lists a new item, this node
 * will receive a MARKET_NOTIFICATION and print [MARKET-NOTIFICATION].
 *
 * What happens:
 * 1. Parse "host:port" from args.
 * 2. Send a SUBSCRIBE_REQUEST directly to that node.
 * 3. The remote node (SubscribeRequestHandler) stores us in its subscribers
 * set.
 *
 * Example: subscribe localhost:1200
 * (Subscribe this node to the node listening on port 1200.)
 */
public class SubscribeCommand implements CLICommand {

	@Override
	public String commandName() {
		return "subscribe";
	}

	@Override
	public void execute(String args) {
		if (args == null || args.isEmpty()) {
			AppConfig.timestampedErrorPrint("Usage: subscribe [host:port]");
			return;
		}
		// Split on the last ':' to handle IPv6 or "localhost:PORT" alike.
		int colonPos = args.lastIndexOf(':');
		if (colonPos == -1) {
			AppConfig.timestampedErrorPrint("Usage: subscribe [host:port]");
			return;
		}
		String host = args.substring(0, colonPos);
		String portStr = args.substring(colonPos + 1);
		int port;
		try {
			port = Integer.parseInt(portStr.trim());
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Invalid port: " + portStr);
			return;
		}

		int myPort = AppConfig.myServentInfo.getListenerPort();
		if (port == myPort) {
			AppConfig.timestampedErrorPrint("Cannot subscribe to yourself.");
			return;
		}

		ServentInfo target = new ServentInfo(host, port);
		SubscribeRequestMessage msg = new SubscribeRequestMessage(myPort, port);
		MessageUtil.sendMessage(msg);
		AppConfig.timestampedStandardPrint("Sent subscribe request to node "
				+ target.getChordId() + " (port " + port + ").");
	}
}
