package com.csse3200.game.ui.terminal.autocomplete;

import java.util.*;

/**
 * Micro BK-tree for fuzzy lookup (Levenshtein distance).
 * Intended for threshold 1. Returns up to K results in lexicographic order.
 */
public class BKTree {
    private static final int K = 5;

    private static final class Node {
        final String term;
        final Map<Integer, Node> children = new HashMap<>();

        Node(String t) {
            term = t;
        }
    }

    private Node root;

    public void insert(String term) {
        if (term == null || term.isEmpty()) return;
        if (root == null) {
            root = new Node(term);
            return;
        }
        Node cur = root;
        int d;
        while (true) {
            d = distLE2(term, cur.term); // compute full distance (cheap for short commands)
            if (d == 0) return;
            Node nxt = cur.children.get(d);
            if (nxt == null) {
                cur.children.put(d, new Node(term));
                return;
            }
            cur = nxt;
        }
    }

    public List<String> searchWithin(String query, int threshold) {
        if (root == null || query == null) return Collections.emptyList();
        ArrayList<String> out = new ArrayList<>();
        Deque<Node> dq = new ArrayDeque<>();
        dq.add(root);
        while (!dq.isEmpty()) {
            Node n = dq.pop();
            int d = distLE2(query, n.term);
            if (d <= threshold) out.add(n.term);
            int lo = d - threshold, hi = d + threshold;
            for (Map.Entry<Integer, Node> e : n.children.entrySet()) {
                int key = e.getKey();
                if (key >= lo && key <= hi) dq.add(e.getValue());
            }
        }
        Collections.sort(out);
        if (out.size() > K) return out.subList(0, K);
        return out;
    }

    /**
     * Fast Levenshtein with early exit for distance > 2 (tiny commands).
     */
    private static int distLE2(String a, String b) {
        // small optimization: if |len(a)-len(b)| > 2, bail early with >2
        int la = a.length(), lb = b.length();
        int diff = Math.abs(la - lb);
        if (diff > 2) return diff; // already > 2
        // classic DP but trimmed because la,lb are tiny (command words)
        int[] prev = new int[lb + 1];
        int[] cur = new int[lb + 1];
        for (int j = 0; j <= lb; j++) prev[j] = j;
        for (int i = 1; i <= la; i++) {
            cur[0] = i;
            char ca = a.charAt(i - 1);
            int rowMin = cur[0];
            for (int j = 1; j <= lb; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
                rowMin = Math.min(rowMin, cur[j]);
            }
            // early exit: if minimal in row already exceeds 2, we can stop
            if (rowMin > 2) return rowMin;
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[lb];
    }
}
