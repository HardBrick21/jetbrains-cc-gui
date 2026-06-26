package com.github.claudecodegui.handler;

import com.github.claudecodegui.handler.core.BaseMessageHandler;
import com.github.claudecodegui.handler.core.HandlerContext;
import com.github.claudecodegui.session.ClaudeSession;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;

/**
 * Handles delete_message requests from the frontend.
 * Deletes a message at the specified index and updates the session state.
 */
public class DeleteMessageHandler extends BaseMessageHandler {

    private static final Logger LOG = Logger.getInstance(DeleteMessageHandler.class);

    private static final String[] SUPPORTED_TYPES = {
            "delete_message"
    };

    public DeleteMessageHandler(HandlerContext context) {
        super(context);
    }

    @Override
    public String[] getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public boolean handle(String type, String content) {
        if (!"delete_message".equals(type)) {
            return false;
        }
        handleDeleteMessage(content);
        return true;
    }

    private void handleDeleteMessage(String content) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            JsonObject payload = gson.fromJson(content, JsonObject.class);

            if (payload == null || !payload.has("messageIndex")) {
                LOG.warn("[DeleteMessage] Missing messageIndex in payload");
                sendError("Missing messageIndex");
                return;
            }

            int messageIndex = payload.get("messageIndex").getAsInt();

            ClaudeSession session = context.getSession();
            if (session == null) {
                LOG.warn("[DeleteMessage] Session is null");
                sendError("Session not initialized");
                return;
            }

            boolean deleted = session.deleteMessage(messageIndex);

            JsonObject response = new JsonObject();
            response.addProperty("success", deleted);
            response.addProperty("deletedIndex", messageIndex);
            response.addProperty("remainingCount", session.getMessages().size());

            if (!deleted) {
                response.addProperty("error", "Invalid message index: " + messageIndex);
            }

            callJavaScript("onMessageDeleted", response.toString());

            LOG.info("[DeleteMessage] " + (deleted ? "Deleted" : "Failed to delete") +
                     " message at index " + messageIndex);

        } catch (Exception e) {
            LOG.error("[DeleteMessage] Error handling delete request: " + e.getMessage(), e);
            sendError("Internal error: " + e.getMessage());
        }
    }

    private void sendError(String errorMessage) {
        JsonObject response = new JsonObject();
        response.addProperty("success", false);
        response.addProperty("error", errorMessage);
        callJavaScript("onMessageDeleted", response.toString());
    }
}
