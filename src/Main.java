import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Main {

    static class BSTNode {
        int key;
        BSTNode left;
        BSTNode right;

        BSTNode(int key) {
            this.key = key;
        }
    }

    static class BST {
        private BSTNode root;

        public void insert(int key) {
            root = insertRecursive(root, key);
        }

        private BSTNode insertRecursive(BSTNode node, int key) {
            if (node == null) {
                return new BSTNode(key);
            }

            if (key < node.key) {
                node.left = insertRecursive(node.left, key);
            } else if (key > node.key) {
                node.right = insertRecursive(node.right, key);
            }
            return node;
        }

        public boolean search(int key) {
            BSTNode current = root;

            while (current != null) {
                if (key == current.key) {
                    return true;
                } else if (key < current.key) {
                    current = current.left;
                } else {
                    current = current.right;
                }
            }
            return false;
        }

        public void delete(int key) {
            root = deleteRecursive(root, key);
        }

        private BSTNode deleteRecursive(BSTNode node, int key) {
            if (node == null) {
                return null;
            }

            if (key < node.key) {
                node.left = deleteRecursive(node.left, key);
            } else if (key > node.key) {
                node.right = deleteRecursive(node.right, key);
            } else {
                if (node.left == null) {
                    return node.right;
                }
                if (node.right == null) {
                    return node.left;
                }

                BSTNode successor = findMinimum(node.right);
                node.key = successor.key;
                node.right = deleteRecursive(node.right, successor.key);
            }
            return node;
        }

        private BSTNode findMinimum(BSTNode node) {
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }
    }

    // Separate chaining: each bucket stores a linked list of keys.
    static class HashTable {
        private final List<LinkedList<Integer>> buckets;

        HashTable(int capacity) {
            buckets = new ArrayList<>(capacity);
            for (int i = 0; i < capacity; i++) {
                buckets.add(new LinkedList<>());
            }
        }

        private int hash(int key) {
            return Math.floorMod(key, buckets.size());
        }

        public void insert(int key) {
            LinkedList<Integer> bucket = buckets.get(hash(key));
            if (!bucket.contains(key)) {
                bucket.add(key);
            }
        }

        public boolean search(int key) {
            return buckets.get(hash(key)).contains(key);
        }

        public void delete(int key) {
            buckets.get(hash(key)).remove(Integer.valueOf(key));
        }
    }

    interface Task {
        void run();
    }

    private static double measureMicroseconds(Task task) {
        long start = System.nanoTime();
        task.run();
        long end = System.nanoTime();
        return (end - start) / 1000.0;
    }

    public static void main(String[] args) {
        int[] sizes = {100, 1000, 10000};
        Random random = new Random(42);

        try (FileWriter csv = new FileWriter("results.csv")) {
            csv.write("elements,structure,insertion_us,search_us,deletion_us\n");

            System.out.println("BST and Hash Table performance experiment");
            System.out.println("Times are measured in microseconds (us).\n");

            for (int n : sizes) {
                List<Integer> keys = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                    keys.add(i);
                }
                Collections.shuffle(keys, random);

                BST bst = new BST();
                double bstInsert = measureMicroseconds(() -> {
                    for (int key : keys) {
                        bst.insert(key);
                    }
                });

                final int[] bstFound = {0};
                double bstSearch = measureMicroseconds(() -> {
                    for (int key : keys) {
                        if (bst.search(key)) {
                            bstFound[0]++;
                        }
                    }
                });

                double bstDelete = measureMicroseconds(() -> {
                    for (int key : keys) {
                        bst.delete(key);
                    }
                });

                HashTable table = new HashTable(n * 2 + 1);
                double hashInsert = measureMicroseconds(() -> {
                    for (int key : keys) {
                        table.insert(key);
                    }
                });

                final int[] hashFound = {0};
                double hashSearch = measureMicroseconds(() -> {
                    for (int key : keys) {
                        if (table.search(key)) {
                            hashFound[0]++;
                        }
                    }
                });

                double hashDelete = measureMicroseconds(() -> {
                    for (int key : keys) {
                        table.delete(key);
                    }
                });

                System.out.printf("Elements: %d%n", n);
                System.out.printf("BST       insert: %.2f us, search: %.2f us, delete: %.2f us%n",
                        bstInsert, bstSearch, bstDelete);
                System.out.printf("HashTable insert: %.2f us, search: %.2f us, delete: %.2f us%n%n",
                        hashInsert, hashSearch, hashDelete);

                csv.write(String.format(java.util.Locale.US,
                        "%d,BST,%.2f,%.2f,%.2f%n", n, bstInsert, bstSearch, bstDelete));
                csv.write(String.format(java.util.Locale.US,
                        "%d,Hash Table,%.2f,%.2f,%.2f%n", n, hashInsert, hashSearch, hashDelete));
            }

            System.out.println("Measured results saved to results.csv");
        } catch (IOException e) {
            System.out.println("Could not write results.csv: " + e.getMessage());
        }
    }
}
