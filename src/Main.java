import java.io.*;
import java.util.*;

public class Main {

    // Узел B-дерева с подсчётом операций
    static class BTreeNode {
        int[] keys;
        int t;
        BTreeNode[] children;
        int n;
        boolean leaf;

        BTreeNode(int t, boolean leaf) {
            this.t = t;
            this.leaf = leaf;
            keys = new int[2 * t - 1];
            children = new BTreeNode[2 * t];
            n = 0;
        }

        // Поиск ключа в поддереве
        BTreeNode search(int key) {
            int i = 0;
            while (i < n && key > keys[i]) {
                BTree.opCount++;
                i++;
            }
            if (i < n) BTree.opCount++;
            if (i < n && keys[i] == key) {
                BTree.opCount++;
                return this;
            }
            if (leaf) {
                return null;
            }
            return children[i].search(key);
        }

        // Вставка в неполный узел
        void insertNonFull(int key) {
            int i = n - 1;
            if (leaf) {
                while (i >= 0 && keys[i] > key) {
                    BTree.opCount++;
                    keys[i + 1] = keys[i];
                    BTree.opCount++;
                    i--;
                }
                if (i >= 0) BTree.opCount++;
                keys[i + 1] = key;
                BTree.opCount++;
                n++;
                BTree.opCount++;
            } else {
                while (i >= 0 && keys[i] > key) {
                    BTree.opCount++;
                    i--;
                }
                if (i >= 0) BTree.opCount++;
                i++;
                if (children[i].n == 2 * t - 1) {
                    BTree.opCount++;
                    splitChild(i, children[i]);
                    if (keys[i] < key) {
                        BTree.opCount++;
                        i++;
                    }
                }
                children[i].insertNonFull(key);
            }
        }

        // Разбиение переполненного ребёнка
        void splitChild(int i, BTreeNode y) {
            BTree.opCount++;
            BTreeNode z = new BTreeNode(y.t, y.leaf);
            z.n = t - 1;
            BTree.opCount++;
            for (int j = 0; j < t - 1; j++) {
                BTree.opCount++;
                z.keys[j] = y.keys[j + t];
                BTree.opCount++;
            }
            if (!y.leaf) {
                for (int j = 0; j < t; j++) {
                    BTree.opCount++;
                    z.children[j] = y.children[j + t];
                    BTree.opCount++;
                }
            }
            y.n = t - 1;
            BTree.opCount++;
            for (int j = n; j >= i + 1; j--) {
                BTree.opCount++;
                children[j + 1] = children[j];
                BTree.opCount++;
            }
            children[i + 1] = z;
            BTree.opCount++;
            for (int j = n - 1; j >= i; j--) {
                BTree.opCount++;
                keys[j + 1] = keys[j];
                BTree.opCount++;
            }
            keys[i] = y.keys[t - 1];
            BTree.opCount++;
            n++;
            BTree.opCount++;
        }

        // Удаление ключа
        void remove(int key) {
            int idx = findKey(key);
            BTree.opCount++;
            if (idx < n && keys[idx] == key) {
                if (leaf) removeFromLeaf(idx);
                else removeFromNonLeaf(idx);
            } else {
                if (leaf) return;
                boolean last = (idx == n);
                if (children[idx].n < t) fill(idx);
                if (last && idx > n) children[idx - 1].remove(key);
                else children[idx].remove(key);
            }
        }

        int findKey(int key) {
            int idx = 0;
            while (idx < n && keys[idx] < key) {
                BTree.opCount++;
                idx++;
            }
            return idx;
        }

        void removeFromLeaf(int idx) {
            for (int i = idx + 1; i < n; i++) {
                BTree.opCount++;
                keys[i - 1] = keys[i];
                BTree.opCount++;
            }
            n--;
            BTree.opCount++;
        }

        void removeFromNonLeaf(int idx) {
            int k = keys[idx];
            BTree.opCount++;
            if (children[idx].n >= t) {
                int pred = getPred(idx);
                keys[idx] = pred;
                BTree.opCount++;
                children[idx].remove(pred);
            } else if (children[idx + 1].n >= t) {
                int succ = getSucc(idx);
                keys[idx] = succ;
                BTree.opCount++;
                children[idx + 1].remove(succ);
            } else {
                merge(idx);
                children[idx].remove(k);
            }
        }

        int getPred(int idx) {
            BTreeNode cur = children[idx];
            while (!cur.leaf) {
                BTree.opCount++;
                cur = cur.children[cur.n];
            }
            return cur.keys[cur.n - 1];
        }

        int getSucc(int idx) {
            BTreeNode cur = children[idx + 1];
            while (!cur.leaf) {
                BTree.opCount++;
                cur = cur.children[0];
            }
            return cur.keys[0];
        }

        void fill(int idx) {
            if (idx != 0 && children[idx - 1].n >= t) borrowFromPrev(idx);
            else if (idx != n && children[idx + 1].n >= t) borrowFromNext(idx);
            else {
                if (idx != n) merge(idx);
                else merge(idx - 1);
            }
        }

        void borrowFromPrev(int idx) {
            BTreeNode child = children[idx];
            BTreeNode sibling = children[idx - 1];
            for (int i = child.n - 1; i >= 0; i--) {
                BTree.opCount++;
                child.keys[i + 1] = child.keys[i];
                BTree.opCount++;
            }
            if (!child.leaf) {
                for (int i = child.n; i >= 0; i--) {
                    BTree.opCount++;
                    child.children[i + 1] = child.children[i];
                    BTree.opCount++;
                }
            }
            child.keys[0] = keys[idx - 1];
            BTree.opCount++;
            if (!child.leaf) child.children[0] = sibling.children[sibling.n];
            BTree.opCount++;
            keys[idx - 1] = sibling.keys[sibling.n - 1];
            BTree.opCount++;
            child.n++;
            BTree.opCount++;
            sibling.n--;
            BTree.opCount++;
        }

        void borrowFromNext(int idx) {
            BTreeNode child = children[idx];
            BTreeNode sibling = children[idx + 1];
            child.keys[child.n] = keys[idx];
            BTree.opCount++;
            if (!child.leaf) child.children[child.n + 1] = sibling.children[0];
            BTree.opCount++;
            keys[idx] = sibling.keys[0];
            BTree.opCount++;
            for (int i = 1; i < sibling.n; i++) {
                BTree.opCount++;
                sibling.keys[i - 1] = sibling.keys[i];
                BTree.opCount++;
            }
            if (!sibling.leaf) {
                for (int i = 1; i <= sibling.n; i++) {
                    BTree.opCount++;
                    sibling.children[i - 1] = sibling.children[i];
                    BTree.opCount++;
                }
            }
            child.n++;
            BTree.opCount++;
            sibling.n--;
            BTree.opCount++;
        }

        void merge(int idx) {
            BTreeNode child = children[idx];
            BTreeNode sibling = children[idx + 1];
            child.keys[t - 1] = keys[idx];
            BTree.opCount++;
            for (int i = 0; i < sibling.n; i++) {
                BTree.opCount++;
                child.keys[i + t] = sibling.keys[i];
                BTree.opCount++;
            }
            if (!child.leaf) {
                for (int i = 0; i <= sibling.n; i++) {
                    BTree.opCount++;
                    child.children[i + t] = sibling.children[i];
                    BTree.opCount++;
                }
            }
            for (int i = idx + 1; i < n; i++) {
                BTree.opCount++;
                keys[i - 1] = keys[i];
                BTree.opCount++;
            }
            for (int i = idx + 2; i <= n; i++) {
                BTree.opCount++;
                children[i - 1] = children[i];
                BTree.opCount++;
            }
            child.n += sibling.n + 1;
            BTree.opCount++;
            n--;
            BTree.opCount++;
        }
    }

    // Само дерево с глобальным счётчиком оперций
    static class BTree {
        BTreeNode root;
        int t;
        public static long opCount = 0;

        BTree(int t) {
            this.t = t;
            this.root = null;
        }

        void resetOpCount() {
            opCount = 0;
        }

        long getOpCount() {
            return opCount;
        }

        void insert(int key) {
            resetOpCount();
            if (root == null) {
                root = new BTreeNode(t, true);
                root.keys[0] = key; opCount++;
                root.n = 1; opCount++;
            } else {
                if (root.n == 2 * t - 1) {
                    BTreeNode s = new BTreeNode(t, false);
                    s.children[0] = root; opCount++;
                    s.splitChild(0, root);
                    int i = (s.keys[0] < key) ? 1 : 0; opCount++;
                    s.children[i].insertNonFull(key);
                    root = s; opCount++;
                } else {
                    root.insertNonFull(key);
                }
            }
        }

        BTreeNode search(int key) {
            resetOpCount();
            return root == null ? null : root.search(key);
        }

        void remove(int key) {
            resetOpCount();
            if (root != null) {
                root.remove(key);
                if (root.n == 0) {
                    root = root.leaf ? null : root.children[0];
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        final int N = 10000;
        Random rnd = new Random();
        int[] data = new int[N];
        for (int i = 0; i < N; i++) data[i] = rnd.nextInt();

        BTree tree = new BTree(50);

        long[] insertTimes = new long[N];
        long[] insertOps = new long[N];
        for (int i = 0; i < N; i++) {
            long start = System.nanoTime();
            tree.insert(data[i]);
            insertTimes[i] = System.nanoTime() - start;
            insertOps[i] = tree.getOpCount();
        }

        // Поиск 100 случайных элементов
        List<Integer> idxList = new ArrayList<>();
        for (int i = 0; i < N; i++) idxList.add(i);
        Collections.shuffle(idxList);
        final int S = 100;
        long[] searchTimes = new long[S];
        long[] searchOps = new long[S];
        for (int i = 0; i < S; i++) {
            int key = data[idxList.get(i)];
            long start = System.nanoTime();
            tree.search(key);
            searchTimes[i] = System.nanoTime() - start;
            searchOps[i] = tree.getOpCount();
        }

        // Удаление 1000 случайных элементов
        Collections.shuffle(idxList);
        final int D = 1000;
        long[] deleteTimes = new long[D];
        long[] deleteOps = new long[D];
        for (int i = 0; i < D; i++) {
            int key = data[idxList.get(i)];
            long start = System.nanoTime();
            tree.remove(key);
            deleteTimes[i] = System.nanoTime() - start;
            deleteOps[i] = tree.getOpCount();
        }

        // Средние показатели
        double avgInsertTime = Arrays.stream(insertTimes).average().orElse(0);
        double avgInsertOps = Arrays.stream(insertOps).average().orElse(0);
        double avgSearchTime = Arrays.stream(searchTimes).average().orElse(0);
        double avgSearchOps = Arrays.stream(searchOps).average().orElse(0);
        double avgDeleteTime = Arrays.stream(deleteTimes).average().orElse(0);
        double avgDeleteOps = Arrays.stream(deleteOps).average().orElse(0);

        System.out.println("Average insert time (ns): " + avgInsertTime);
        System.out.println("Average insert ops: " + avgInsertOps);
        System.out.println("Average search time (ns): " + avgSearchTime);
        System.out.println("Average search ops: " + avgSearchOps);
        System.out.println("Average delete time (ns): " + avgDeleteTime);
        System.out.println("Average delete ops: " + avgDeleteOps);

        // Сохранение в CSV
        try (PrintWriter pw = new PrintWriter(new FileWriter("C://Users/solo2/OneDrive/Belgeler/Java projects/Files/BTree.txt"))) {
            pw.println("type,index,key,time_ns,ops");
            for (int i = 0; i < N; i++) {
                pw.printf("insert,%d,%d,%d,%d%n", i, data[i], insertTimes[i], insertOps[i]);
            }
            for (int i = 0; i < S; i++) {
                pw.printf("search,%d,%d,%d,%d%n", i, data[idxList.get(i)], searchTimes[i], searchOps[i]);
            }
            for (int i = 0; i < D; i++) {
                pw.printf("delete,%d,%d,%d,%d%n", i, data[idxList.get(i)], deleteTimes[i], deleteOps[i]);
            }
        }
    }
}
