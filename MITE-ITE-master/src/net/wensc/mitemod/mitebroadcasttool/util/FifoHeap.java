package net.wensc.mitemod.mitebroadcasttool.util;

import java.util.LinkedList;

public class FifoHeap<T> {
    private LinkedList<T> queue;

    public FifoHeap() {
        queue = new LinkedList<>();
    }

    public void offer(T element) {
        queue.offer(element);
    }

    public T poll() {
        return queue.poll();
    }

    public T peek() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
