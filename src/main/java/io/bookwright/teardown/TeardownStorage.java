package io.bookwright.teardown;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Per-test LIFO queue of cleanup actions. Steps push an action for every
 * entity they create; {@link TeardownExtension} drains the queue after each test
 * in reverse creation order.
 *
 * <p>ThreadLocal storage matches the JUnit parallel model: each test method
 * runs on one worker thread.</p>
 */
public class TeardownStorage {

    public record TeardownAction(String name, Runnable action) {
    }

    private static final ThreadLocal<Deque<TeardownAction>> ACTIONS = ThreadLocal.withInitial(ArrayDeque::new);

    public void push(String name, Runnable action) {
        ACTIONS.get().addLast(new TeardownAction(name, action));
    }

    TeardownAction pollLast() {
        return ACTIONS.get().pollLast();
    }

    static void clear() {
        ACTIONS.remove();
    }
}
