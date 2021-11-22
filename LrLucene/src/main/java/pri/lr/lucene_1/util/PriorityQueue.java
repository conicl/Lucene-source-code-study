package pri.lr.lucene_1.util;

public abstract class PriorityQueue {
    private Object[] heap;
    private int size;

    abstract protected boolean lessThan(Object a, Object b);

    protected final void initialize(int maxSize) {
        size = 0;
        int heapSize = (maxSize * 2) + 1;
        heap = new Object[heapSize];
    }

    public final void put(Object element) {
        size ++;
        heap[size] = element;
        upHeap();
    }

    public final Object top() {
        if (size > 0) {
            return heap[1];
        } else {
            return null;
        }
    }

    public final Object pop() {
        if (size > 0) {
            Object result = heap[1];
            heap[1] = heap[size];
            heap[size] = null;
            size--;
            downHeap();
            return result;
        } else {
            return null;
        }
    }

    public final void adjustTop() {
        downHeap();
    }

    public final int size() {
        return size;
    }

    public final void clear() {
        for(int i = 0; i < size ; i++) {
            heap[i] = null;
        }
        size = 0;
    }

    private final void upHeap() {
        int i = size;
        Object node = heap[i];

        int j = i >>> 1; // the last non-leaf node
        while (j > 0 && lessThan(node, heap[j])) {
            heap[i] = heap[j];
            i = j;
            j = j >>> 1;
        }
        heap[i] = node;
    }

    private final void downHeap() {
        int i = 1;
        Object node = heap[i];
        int j = i << 1;
        int k = j + 1;
        if (k <= size && lessThan(heap[k], heap[j])){
            j = k;
        }
        while (j <= size && lessThan(heap[j], node)) {
            heap[i] = heap[j];
            i = j;
            j = i << 1;
            k = j + 1;
            if (k <= size && lessThan(heap[k], heap[j])) {
                j = k;
            }
        }
        heap[i] = node;
    }
}
