package com.csse3200.game.ui.terminal.autocomplete;

import java.util.*;

/**
 * <p><strong>RadixTrie</strong> is a compressed (radix) trie for command-name autocompletion.
 * Each node caches the lexicographically smallest {@value #K} full-word completions in its
 * subtree, enabling {@link #suggestTopK(String)} to run in <em>O(m)</em> time, where
 * <code>m</code> is the length of the query prefix.</p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Compressed edges (radix) for memory efficiency and fewer pointer traversals.</li>
 *   <li>Per-node <em>top-K</em> cache (K = {@value #K}) for fast suggestions without DFS.</li>
 *   <li>Deterministic, lexicographic ordering of suggestions using {@link String#compareTo(String)}.</li>
 *   <li>Duplicate inserts are ignored; null/empty inputs are safely ignored.</li>
 * </ul>
 *
 * <h2>Complexity</h2>
 * <ul>
 *   <li><b>Insertion:</b> Amortized O(L · log B) where L is word length and B is the
 *       bucket size (edges that share the same first character). Buckets stay sorted.</li>
 *   <li><b>Query:</b> O(m) traversal to the prefix locus, then O(1) to return the cached list.</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>Instances are <strong>not</strong> thread-safe. If you share a trie across threads,
 * synchronize externally around mutations and queries.</p>
 *
 * <h2>Case behavior</h2>
 * <p>Traversal is case-sensitive. By default, {@link #suggestTopK(String)} also performs a
 * <em>first-letter case fallback</em> (e.g., the query {@code "he"} also considers {@code "He"}).
 * Results remain strictly lexicographic by {@link String#compareTo(String)}. If you prefer purely
 * case-sensitive behavior, remove the wrapper logic and call {@link #suggestOneCase(String)}.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * RadixTrie trie = new RadixTrie();
 * trie.insertAll(List.of("help", "heal", "heap", "hello", "helm"));
 * List<String> top = trie.suggestTopK("he");
 * // => ["heal", "heap", "hello", "helm", "help"]
 * }</pre>
 *
 * @implNote The node-level cache stores full words (not suffixes). During splits, caches are
 * seeded from existing children so previously inserted words remain visible.
 * @since 1.0
 */
public class RadixTrie {

    /**
     * Maximum number of completions cached per node and returned by queries.
     */
    private static final int K = 5;

    /**
     * Root of the trie (may be reused across {@link #clear()} to avoid churn).
     */
    private final Node root = new Node();
    
    /**
     * Inserts {@code candidate} into {@code topK} if it belongs within the first {@value #K}
     * lexicographic elements and is not already present. Mutates {@code topK} in-place.
     *
     * @param topK      destination cache (must be sorted ascending)
     * @param candidate word to consider
     */
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

    /**
     * Merge-seeds {@code topK} from an existing {@code seed} list, preserving lexicographic order
     * and the {@value #K} cap.
     *
     * @param topK destination cache
     * @param seed existing sorted seed list (may be null/empty)
     */
    private static void cacheSeed(ArrayList<String> topK, List<String> seed) {
        if (seed == null || seed.isEmpty()) return;
        for (String s : seed) cachePush(topK, s);
    }

    /**
     * Returns the length of the longest common prefix between {@code a} and {@code b}.
     *
     * @param a first string
     * @param b second string
     * @return number of leading characters that match
     */
    private static int lcpLen(String a, String b) {
        int n = Math.min(a.length(), b.length());
        int i = 0;
        while (i < n && a.charAt(i) == b.charAt(i)) i++;
        return i;
    }

    /**
     * Lower-bound index in {@code bucket} for an edge whose label should be {@code label}.
     * The bucket is assumed to be sorted by {@link Edge#label}.
     *
     * @param bucket sorted list of edges
     * @param label  probe label
     * @return insertion index for {@code label}
     */
    private static int lowerBoundLabel(List<Edge> bucket, String label) {
        int lo = 0, hi = bucket.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (bucket.get(mid).label.compareTo(label) < 0) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    /**
     * Binary search in a bucket by comparing {@link Edge#label} to {@code label}.
     *
     * @param bucket sorted list of edges
     * @param label  probe label
     * @return index if found; otherwise {@code -(insertionPoint) - 1}
     */
    private static int edgeBinarySearch(List<Edge> bucket, String label) {
        int lo = 0, hi = bucket.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int cmp = bucket.get(mid).label.compareTo(label);
            if (cmp < 0) lo = mid + 1;
            else if (cmp > 0) hi = mid - 1;
            else return mid;
        }
        return -(lo + 1);
    }

    /**
     * Probes the two neighbor edges around the insertion point while walking a prefix.
     * <ul>
     *   <li>If the entire prefix is consumed inside an edge, returns the child's cache.</li>
     *   <li>If an edge is fully matched but the prefix has remaining characters, requests a descend.</li>
     *   <li>If we diverge mid-edge (mismatch) or no edge shares a prefix, returns failure.</li>
     * </ul>
     *
     * @param bucket bucket of candidate edges (sorted)
     * @param ins    insertion index computed from a failed exact-label search
     * @param rem    remaining (unmatched) prefix
     * @return a {@link Step} describing whether to advance deeper, return results, or fail
     */
    private static Step tryAdvanceOrReturn(List<Edge> bucket, int ins, String rem) {
        int[] candidates = {ins - 1, ins};
        for (int j : candidates) {
            if (j < 0 || j >= bucket.size()) continue;
            Edge e = bucket.get(j);
            int lcp = lcpLen(rem, e.label);
            if (lcp == 0) continue;

            if (lcp == rem.length()) {
                // Prefix fully consumed (at node boundary or mid-edge).
                return Step.out(List.copyOf(e.next.topK));
            }
            if (lcp == e.label.length()) {
                // Edge fully matched; keep descending.
                return Step.advance(new Advance(e.next, rem.substring(lcp)));
            }
            // else: diverged inside edge -> try the other neighbor
        }
        return Step.fail();
    }

    /**
     * Toggles only the first character's case (e.g., {@code he} ↔ {@code He}).
     * If the first character is not alphabetic, returns the input unchanged.
     *
     * @param s input string (may be null/empty)
     * @return string with first character case flipped, or the original if non-letter
     */
    private static String toggleFirstCase(String s) {
        if (s == null || s.isEmpty()) return s;
        char c = s.charAt(0);
        char t = Character.isUpperCase(c) ? Character.toLowerCase(c)
                : (Character.isLowerCase(c) ? Character.toUpperCase(c) : c);
        if (t == c) return s;
        return t + s.substring(1);
    }

    /**
     * Merges two already-sorted suggestion lists into a deduplicated, lexicographic
     * top-{@value #K} list. Neither input is modified.
     *
     * @param a first list (sorted ascending)
     * @param b second list (sorted ascending)
     * @return unmodifiable merged top-K list in ascending order
     */
    private static List<String> mergeTopK(List<String> a, List<String> b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        ArrayList<String> out = new ArrayList<>(K);
        int i = 0, j = 0;
        while (out.size() < K && (i < a.size() || j < b.size())) {
            String pick;
            if (j >= b.size() || (i < a.size() && a.get(i).compareTo(b.get(j)) <= 0)) {
                pick = a.get(i++);
            } else {
                pick = b.get(j++);
            }
            if (out.isEmpty() || !out.getLast().equals(pick)) {
                out.add(pick);
            }
        }
        return List.copyOf(out);
    }

    /**
     * Removes all entries from this trie.
     * <p>The root node object is preserved (to avoid allocation churn), but its state,
     * children, and caches are cleared.</p>
     *
     * @since 1.0
     */
    public void clear() {
        root.children.clear();
        root.topK.clear();
        root.terminal = false;
    }

    /**
     * Inserts all words from the given collection.
     * <ul>
     *   <li>Null collections are ignored.</li>
     *   <li>Null or empty strings in the collection are ignored.</li>
     *   <li>Duplicate words have no effect beyond the first insertion.</li>
     * </ul>
     *
     * @param words collection of words to insert (may be null)
     * @since 1.0
     */
    public void insertAll(Collection<String> words) {
        if (words == null) return;
        for (String w : words) insert(w);
    }

    /**
     * Inserts a single word into the trie.
     * <ul>
     *   <li>If {@code word} is null or empty, the call is a no-op.</li>
     *   <li>If {@code word} already exists, the call is a no-op.</li>
     * </ul>
     *
     * @param word the word to insert (full completion string)
     * @since 1.0
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) return;

        Node node = root;
        // Bubble into caches as we descend.
        cachePush(node.topK, word);
        String rem = word;

        while (!rem.isEmpty()) {
            final char c0 = rem.charAt(0);
            final ArrayList<Edge> bucket = node.children.computeIfAbsent(c0, k -> new ArrayList<>());

            // Exact-label binary search on the bucket.
            int idx = edgeBinarySearch(bucket, rem);
            if (idx >= 0) {
                // Exact edge label match; descend.
                Edge e = bucket.get(idx);
                rem = rem.substring(e.label.length());
                node = e.next;
                cachePush(node.topK, word);
                continue;
            }

            // No exact match: check neighbors around insertion point for shared prefix.
            int ins = -idx - 1;
            boolean advanced = false;

            for (int j : new int[]{ins - 1, ins}) {
                if (j < 0 || j >= bucket.size()) continue;
                Edge e = bucket.get(j);
                int lcp = lcpLen(rem, e.label);
                if (lcp == 0) continue;

                if (lcp < e.label.length()) {
                    // Split existing edge at the common prefix.
                    String common = e.label.substring(0, lcp);
                    String eRest = e.label.substring(lcp);
                    Node mid = new Node();

                    // mid inherits the old child via eRest
                    ArrayList<Edge> mb = mid.children.computeIfAbsent(eRest.charAt(0), k -> new ArrayList<>());
                    mb.add(new Edge(eRest, e.next));

                    // Seed mid cache so earlier completions are retained after the split.
                    cacheSeed(mid.topK, e.next.topK);

                    // Replace e with (common -> mid), keeping bucket sorted by relabeling reposition.
                    Edge commonEdge = new Edge(common, mid);
                    bucket.remove(j);
                    bucket.add(lowerBoundLabel(bucket, common), commonEdge);

                    // Descend into mid and continue with the remainder.
                    node = mid;
                    cachePush(node.topK, word);

                    rem = rem.substring(lcp);
                    if (rem.isEmpty()) {
                        node.terminal = true; // word ends exactly at mid
                        return;
                    }
                } else {
                    // Edge fully matches; descend and continue with the remainder.
                    rem = rem.substring(lcp); // lcp == e.label.length()
                    node = e.next;
                    cachePush(node.topK, word);
                }
                advanced = true;
                break;
            }

            if (advanced) continue;

            // No overlap — create a new leaf edge.
            Node leaf = new Node();
            leaf.terminal = true;
            cachePush(leaf.topK, word);
            bucket.add(ins, new Edge(rem, leaf));
            return;
        }

        // rem is empty => the word ends at the current node
        node.terminal = true;
    }

    /**
     * Returns up to {@value #K} lexicographically smallest completions for the given {@code prefix}.
     * <ul>
     *   <li>If {@code prefix} is {@code null} or empty, returns the global top-K from the root.</li>
     *   <li>If the prefix does not exist in the trie, returns an empty list.</li>
     *   <li>The returned list is unmodifiable and reflects the trie's state at call time.</li>
     * </ul>
     *
     * <p>O(m) where m is the prefix length: this method walks the compressed path and then
     * returns a precomputed cache. It also performs a first-letter case fallback (see “Case behavior”).</p>
     *
     * @param prefix the prefix to complete (may be null or empty)
     * @return an unmodifiable list containing at most {@value #K} suggestions in lexicographic order
     * @since 1.0
     */
    public List<String> suggestTopK(String prefix) {
        // Case-sensitive path
        List<String> a = suggestOneCase(prefix);
        if (prefix == null || prefix.isEmpty()) return a;

        // Alternate: flip only the first char's case and try again (to include H*/h*)
        String alt = toggleFirstCase(prefix);
        if (alt.equals(prefix)) return a; // non-letter first char

        List<String> b = suggestOneCase(alt);

        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        return mergeTopK(a, b); // lexicographic, de-duped, capped at K
    }

    /**
     * Internal, strictly case-sensitive version of {@link #suggestTopK(String)}.
     * Performs an O(m) walk to the prefix locus and returns the node cache.
     *
     * @param prefix prefix to complete (may be null/empty to use root cache)
     * @return unmodifiable list of up to {@value #K} suggestions; empty if no match
     */
    private List<String> suggestOneCase(String prefix) {
        if (prefix == null || prefix.isEmpty()) return List.copyOf(root.topK);

        Node node = root;
        String rem = prefix;

        while (!rem.isEmpty()) {
            List<Edge> bucket = node.children.get(rem.charAt(0));
            if (bucket == null || bucket.isEmpty()) return List.of();

            int idx = edgeBinarySearch(bucket, rem);
            if (idx >= 0) {
                Edge e = bucket.get(idx);
                rem = rem.substring(e.label.length());
                node = e.next;
                continue;
            }

            int ins = -idx - 1;
            Step step = tryAdvanceOrReturn(bucket, ins, rem);
            if (step.out() != null) return step.out();
            if (step.advance() != null) {
                node = step.advance().node();
                rem = step.advance().rem();
                continue;
            }
            return List.of();
        }
        return List.copyOf(node.topK);
    }

    /**
     * Result of probing neighbor edges during prefix traversal:
     * either advance deeper, directly return suggestions, or fail.
     *
     * @param advance non-null when an edge was fully matched and traversal should continue
     * @param out     non-null when the prefix was fully consumed and we can return a cache
     */
    private record Step(Advance advance, List<String> out) {
        /**
         * Factory: request to advance deeper in the trie.
         */
        static Step advance(Advance a) {
            return new Step(a, null);
        }

        /**
         * Factory: return a cache as the final result.
         */
        static Step out(List<String> list) {
            return new Step(null, list);
        }

        /**
         * Factory: neither neighbor matched sufficiently (failure).
         */
        static Step fail() {
            return new Step(null, null);
        }
    }

    /**
     * Instruction to descend in the trie.
     *
     * @param node next node to visit
     * @param rem  remaining (unmatched) prefix after consuming an edge label
     */
    private record Advance(Node node, String rem) {
    }

    /**
     * A trie node holding compressed outgoing edges and a fixed-size cache of the smallest
     * {@value RadixTrie#K} completions in its subtree.
     */
    private static final class Node {
        /**
         * Children grouped by first character of the outgoing edge label. Buckets are kept sorted by label.
         */
        final Map<Character, ArrayList<Edge>> children = new HashMap<>();
        /**
         * Cached lexicographic top-{@value RadixTrie#K} full-word completions reachable from this node.
         */
        final ArrayList<String> topK = new ArrayList<>(K);
        /**
         * True if this node marks the end of a word.
         */
        boolean terminal;
    }

    /**
     * A compressed (radix) edge from a node, storing the whole label segment and the next node.
     * Labels may be rewritten during splits; buckets remain sorted by {@link #label}.
     */
    private static final class Edge {
        /**
         * Compressed edge label (may change during split).
         */
        String label;
        /**
         * Child node reached by this edge.
         */
        Node next;

        Edge(String label, Node next) {
            this.label = label;
            this.next = next;
        }
    }
}
