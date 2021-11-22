package pri.lr.lucene_1.util;

public class Arrays {
    public static void sort(String[] a) {
        String aux[] = a.clone();
        mergeSort(aux, a, 0, a.length);
    }

    private static void mergeSort(String[] src, String[] dest, int low, int high) {
        int len = high - low; // [low, high)

        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low && dest[j - 1].compareTo(dest[j]) > 0; j--) {
                   swap(dest, j , j - 1);
                }
            }
            return;
        }

        int mid = (low + high) / 2;
        mergeSort(dest, src, low, mid); // [low, mid)
        mergeSort(dest, src, mid, high); // [mid, high)

        if ((src[mid - 1].compareTo(src[mid]) <= 0)) {
            System.arraycopy(src, low, dest, low, len);
            return;
        }

        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && (src[p].compareTo(src[q]) <= 0)) {
                dest[i] = src[p++];
            } else {
                dest[i] = src[q++];
            }
        }
    }

    private static void swap(String x[], int a, int b) {
        String t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
