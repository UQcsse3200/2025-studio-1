package com.csse3200.game.ui.terminal.autocomplete;

import java.util.*;

/**
 * Compressed (radix) trie with per-node cached top-5 completions.
 * Insert all command names once; suggestions are then O(m + k) where:
 *  - m = prefix traversal, k = number of edges inspected on the path.
 * Returns at most 5 suggestions in lexicographic order.
 */
public class RadixTrie {
    private static final int K = 5;

    private static final class Node {
        // Children keyed by edge-label first char; values kept in sorted order by label
        private final TreeMap<Character, List<Edge>> children = new TreeMap<>();
        private boolean terminal = false;
        // Cached top-5 (lexicographic) completions from this subtree
        private final ArrayList<String> topK = new ArrayList<>(K);
    }

    private static final class Edge {
        String label; // compressed edge label
        Node next;
        Edge(String label, Node next) { this.label = label; this.next = next; }
    }

    private final Node root = new Node();

    public void clear() {
        // not strictly necessary for GC, but explicit if you rebuild often
    }

    public void insert(String word) {
        if (word == null || word.isEmpty()) return;
        insertInternal(root, word, word);
    }

    private void insertInternal(Node node, String remaining, String full) {
        if (remaining.isEmpty()) {
            if (!node.terminal) {
                node.terminal = true;
                cachePush(node.topK, full);
            }
            return;
        }
        char c0 = remaining.charAt(0);
        List<Edge> bucket = node.children.computeIfAbsent(c0, k -> new ArrayList<>());

        // lower_bound by edge label (list kept sorted)
        int idx = Collections.binarySearch(bucket, new Edge(remaining, null),
                Comparator.comparing(e -> e.label));
        if (idx >= 0) {
            // exact label match (rare unless same word reinserted)
            Edge e = bucket.get(idx);
            insertIntoEdge(node, e, remaining, full);
        } else {
            // find insertion point
            int ins = -idx - 1;
            // check neighbors for shared prefix
            int near = ins > 0 ? ins - 1 : ins;
            boolean handled = false;
            for (int j = Math.max(0, near - 1); j <= Math.min(bucket.size() - 1, near + 1); j++) {
                Edge e = bucket.get(j);
                int lcp = lcpLen(remaining, e.label);
                if (lcp > 0) {
                    handled = true;
                    // split if partial match
                    if (lcp < e.label.length()) {
                        // split existing edge
                        String common = e.label.substring(0, lcp);
                        String eRest  = e.label.substring(lcp);
                        Node mid = new Node();
                        // mid inherits e.next via new edge eRest
                        mid.children.computeIfAbsent(eRest.charAt(0), k -> new ArrayList<>())
                                .add(new Edge(eRest, e.next));
                        // replace e with common->mid
                        e.label = common;
                        e.next  = mid;
                        // keep children’s buckets sorted
                        sortBucket(mid.children.get(eRest.charAt(0)));
                    }
                    // consume lcp and continue down
                    String remRest = remaining.substring(lcp);
                    if (remRest.isEmpty()) {
                        if (!e.next.terminal) {
                            e.next.terminal = true;
                            cachePush(e.next.topK, full);
                        }
                    } else {
                        insertInternal(e.next, remRest, full);
                    }
                    break;
                }
            }
            if (!handled) {
                // no overlap — create a new edge
                Node nxt = new Node();
                bucket.add(ins, new Edge(remaining, nxt));
                cachePush(nxt.topK, full);
                nxt.terminal = true; // because remaining fully consumed by the new edge
            }
        }
        // update node's topK cache from child insert
        cachePush(node.topK, full);
        // keep bucket sorted
        sortBucket(bucket);
    }

    private static void insertIntoEdge(Node parent, Edge e, String remaining, String full) {
        int lcp = lcpLen(remaining, e.label);
        if (lcp == e.label.length()) {
            // edge fully matches; continue with rest
            String remRest = remaining.substring(lcp);
            if (remRest.isEmpty()) {
                if (!e.next.terminal) {
                    e.next.terminal = true;
                    cachePush(e.next.topK, full);
                }
            } else {
                new RadixTrie().insertInternal(e.next, remRest, full); // delegate
            }
        } else if (lcp > 0) {
            // split edge at lcp
            String common = e.label.substring(0, lcp);
            String eRest  = e.label.substring(lcp);
            Node mid = new Node();
            // existing edge becomes mid child
            mid.children.computeIfAbsent(eRest.charAt(0), k -> new ArrayList<>())
                    .add(new Edge(eRest, e.next));
            sortBucket(mid.children.get(eRest.charAt(0)));
            // update original edge to common->mid
            e.label = common;
            e.next  = mid;

            String remRest = remaining.substring(lcp);
            if (remRest.isEmpty()) {
                if (!mid.terminal) {
                    mid.terminal = true;
                    cachePush(mid.topK, full);
                }
            } else {
                new RadixTrie().insertInternal(mid, remRest, full);
            }
        } else {
            // no common prefix with this edge, caller handles other edges/bucket insert
            // (should not reach here with the current calling pattern)
        }
        // bubble caches
        cachePush(parent.topK, full);
    }

    public List<String> suggestTopK(String prefix) {
        if (prefix == null) prefix = "";
        Node node = root;
        String rem = prefix;
        while (!rem.isEmpty()) {
            char c0 = rem.charAt(0);
            List<Edge> bucket = node.children.get(c0);
            if (bucket == null || bucket.isEmpty()) return Collections.emptyList();

            // lower_bound style: binary search by label
            int idx = lowerBound(bucket, rem);
            boolean advanced = false;
            // check idx and neighbors for LCP progress
            for (int j = Math.max(0, idx - 1); j <= Math.min(bucket.size() - 1, idx + 1); j++) {
                Edge e = bucket.get(j);
                int lcp = lcpLen(rem, e.label);
                if (lcp == 0) continue;
                if (lcp < rem.length()) {
                    if (lcp == e.label.length()) {
                        // edge fully consumed; go down
                        node = e.next;
                        rem = rem.substring(lcp);
                        advanced = true;
                        break;
                    } else {
                        // prefix falls inside this edge: no deeper node yet
                        return e.next.topK.isEmpty() ? Collections.emptyList()
                                : new ArrayList<>(e.next.topK);
                    }
                } else {
                    // whole prefix consumed on/within this edge
                    if (lcp < e.label.length()) {
                        // inside the edge
                        return e.next.topK.isEmpty() ? Collections.emptyList()
                                : new ArrayList<>(e.next.topK);
                    } else {
                        // exactly at the node below
                        node = e.next;
                        rem = "";
                        advanced = true;
                        break;
                    }
                }
            }
            if (!advanced) return Collections.emptyList();
        }
        return new ArrayList<>(node.topK);
    }

    // --- helpers ---

    private static void cachePush(ArrayList<String> topK, String candidate) {
        int pos = Collections.binarySearch(topK, candidate);
        if (pos >= 0) return; // already present
        int ins = -pos - 1;
        if (topK.size() < K) {
            topK.add(ins, candidate);
        } else if (ins < K) {
            topK.add(ins, candidate);
            topK.remove(K);
        }
    }

    private static int lcpLen(String a, String b) {
        int n = Math.min(a.length(), b.length());
        int i = 0;
        while (i < n && a.charAt(i) == b.charAt(i)) i++;
        return i;
    }

    private static void sortBucket(List<Edge> bucket) {
        bucket.sort(Comparator.comparing(e -> e.label));
    }

    private static int lowerBound(List<Edge> bucket, String rem) {
        int lo = 0, hi = bucket.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (bucket.get(mid).label.compareTo(rem) < 0) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }
}
