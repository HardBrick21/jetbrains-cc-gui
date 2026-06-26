package com.github.claudecodegui.session;

import org.junit.Test;
import static org.junit.Assert.*;

public class SessionStateDeleteTest {

    @Test
    public void testDeleteMessageByIndex() {
        SessionState state = new SessionState();

        // Add test messages
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.USER, "Hello"));
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.ASSISTANT, "Hi there"));
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.USER, "How are you?"));

        assertEquals(3, state.getMessages().size());

        // Delete middle message (index 1)
        boolean deleted = state.deleteMessage(1);

        assertTrue(deleted);
        assertEquals(2, state.getMessages().size());
        assertEquals("Hello", state.getMessages().get(0).content);
        assertEquals("How are you?", state.getMessages().get(1).content);
        assertTrue(state.isContextDirty());
    }

    @Test
    public void testDeleteMessageMarksDirty() {
        SessionState state = new SessionState();
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.USER, "Test"));

        assertFalse(state.isContextDirty());

        boolean deleted = state.deleteMessage(0);

        assertTrue(deleted);
        assertTrue(state.isContextDirty());
    }

    @Test
    public void testDeleteInvalidIndex() {
        SessionState state = new SessionState();
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.USER, "Test"));

        // Should not throw, should be no-op
        boolean deletedNegative = state.deleteMessage(-1);
        boolean deletedOob = state.deleteMessage(100);

        assertFalse(deletedNegative);
        assertFalse(deletedOob);
        assertEquals(1, state.getMessages().size());
        assertFalse(state.isContextDirty()); // No valid deletion occurred
    }

    @Test
    public void testClearContextDirty() {
        SessionState state = new SessionState();
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.USER, "Test"));
        state.deleteMessage(0);

        assertTrue(state.isContextDirty());

        state.clearContextDirty();

        assertFalse(state.isContextDirty());
    }

    @Test
    public void testDeleteLastMessage() {
        SessionState state = new SessionState();
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.USER, "Only message"));

        boolean deleted = state.deleteMessage(0);

        assertTrue(deleted);
        assertEquals(0, state.getMessages().size());
        assertTrue(state.isContextDirty());
    }

    @Test
    public void testDeletePreservesMessageOrder() {
        SessionState state = new SessionState();
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.USER, "A"));
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.ASSISTANT, "B"));
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.USER, "C"));
        state.addMessage(new ClaudeSession.Message(ClaudeSession.Message.Type.ASSISTANT, "D"));

        boolean deleted = state.deleteMessage(1); // Delete "B"

        assertTrue(deleted);
        assertEquals(3, state.getMessages().size());
        assertEquals("A", state.getMessages().get(0).content);
        assertEquals("C", state.getMessages().get(1).content);
        assertEquals("D", state.getMessages().get(2).content);
    }
}
