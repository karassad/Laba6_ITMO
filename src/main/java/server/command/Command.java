package server.command;

import shared.Request;
import shared.Response;

/**
 * Interface for server commands.
 */
public interface Command {
    Response execute(Request request);
    String getName();
}
