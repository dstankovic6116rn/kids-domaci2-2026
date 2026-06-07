package cli.command;

import app.AppConfig;
import app.ServentInfo;
import servent.message.SubscribeRequestMessage;
import servent.message.util.MessageUtil;

/**
 * subscribe [host:port]
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
		// Split on the last : to handle localhost:PORT.
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
