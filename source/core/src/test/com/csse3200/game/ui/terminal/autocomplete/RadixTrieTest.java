package com.csse3200.game.ui.terminal.autocomplete;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RadixTrieTest {

    private static RadixTrie t(String... words) {
        RadixTrie trie = new RadixTrie();
        for (String w : words) trie.insert(w);
        return trie;
    }

    private static List<String> L(String... xs) {
        return List.of(xs);
    }

    private static Object callMergeTopK(List<String> a, List<String> b) throws Exception {
        Method m = RadixTrie.class.getDeclaredMethod("mergeTopK", List.class, List.class);
        m.setAccessible(true);
        return m.invoke(null, a, b);
    }

    private static int callLowerBoundLabel(List<Object> bucket, String label) throws Exception {
        Method m = RadixTrie.class.getDeclaredMethod("lowerBoundLabel", List.class, String.class);
        m.setAccessible(true);
        Object res = m.invoke(null, bucket, label);
        return (Integer) res;
    }

    private static List<Object> edgeBucket(String... labels) throws Exception {
        String base = "com.csse3200.game.ui.terminal.autocomplete.RadixTrie$";
        Class<?> nodeCls = Class.forName(base + "Node");
        Class<?> edgeCls = Class.forName(base + "Edge");
        Constructor<?> nodeCtor = nodeCls.getDeclaredConstructor();
        Constructor<?> edgeCtor = edgeCls.getDeclaredConstructor(String.class, nodeCls);
        nodeCtor.setAccessible(true);
        edgeCtor.setAccessible(true);

        List<Object> bucket = new ArrayList<>();
        for (String lab : labels) {
            Object node = nodeCtor.newInstance();
            Object edge = edgeCtor.newInstance(lab, node);
            bucket.add(edge);
        }
        return bucket; // already in caller-provided order (sorted in our tests)
    }

    @Test
    @DisplayName("Empty trie: querying any prefix returns empty; root topK is empty")
    void emptyTrie() {
        RadixTrie t = new RadixTrie();
        assertTrue(t.suggestTopK("anything").isEmpty());
        assertTrue(t.suggestTopK("").isEmpty());
        assertTrue(t.suggestTopK(null).isEmpty());
    }

    @Test
    @DisplayName("insertAll ignores null collection and null/empty elements")
    void insertAllNullsAndEmpties() {
        RadixTrie t = new RadixTrie();

        // Null collection is a no-op
        t.insertAll(null);

        // Collection with null/empty entries is safe
        ArrayList<String> words = new ArrayList<>();
        words.add("alpha");
        words.add(null);
        words.add("");
        words.add("beta");
        t.insertAll(words);

        // Root cache is lexicographic and ignores null/empty
        assertEquals(L("alpha", "beta"), t.suggestTopK(""));
    }

    @Test
    @DisplayName("Global top-5 from root for empty/null prefix")
    void rootCacheForEmptyOrNullPrefix() {
        RadixTrie t = t("c", "b", "a", "aa", "ab", "ac");
        // Global order: a, aa, ab, ac, b, c -> top-5
        List<String> exp = L("a", "aa", "ab", "ac", "b");
        assertEquals(exp, t.suggestTopK(""));
        assertEquals(exp, t.suggestTopK(null));
    }

    @Test
    @DisplayName("Insertion order does not affect lexicographic results")
    void insertionOrderInvariance() {
        String[] words = {"zeta", "gamma", "alpha", "delta", "beta", "epsilon"};

        RadixTrie a = new RadixTrie();
        for (String w : words) a.insert(w);

        RadixTrie b = new RadixTrie();
        b.insert("delta");
        b.insert("epsilon");
        b.insert("beta");
        b.insert("alpha");
        b.insert("gamma");
        b.insert("zeta");

        List<String> exp = L("alpha", "beta", "delta", "epsilon", "gamma");
        assertEquals(exp, a.suggestTopK(""));
        assertEquals(exp, b.suggestTopK(""));
    }

    @Test
    @DisplayName("Top-K is capped at 5 and lexicographic")
    void topKCap() {
        RadixTrie t = t("aa", "ab", "ac", "ad", "ae", "af", "ag");
        assertEquals(L("aa", "ab", "ac", "ad", "ae"), t.suggestTopK("a"));
    }

    @Test
    @DisplayName("Duplicates are ignored (no repeats in caches)")
    void duplicatesIgnored() {
        RadixTrie t = new RadixTrie();
        t.insert("al");
        t.insert("alpha");
        t.insert("alpha");    // duplicate
        t.insert("alphabet");
        t.insert("alpine");
        t.insert("alto");
        t.insert("alto");     // duplicate

        assertEquals(L("al", "alpha", "alphabet", "alpine", "alto"),
                t.suggestTopK("al"));
    }

    @Test
    @DisplayName("Split when adding shorter word after a longer one (cache seeded)")
    void splitShorterAfterLonger() {
        RadixTrie t = t("hello");
        t.insert("he"); // forces split of edge "hello" -> "he" + "llo"

        // 'he' should remain in topK along with 'hello'
        assertEquals(L("he", "hello"), t.suggestTopK("he"));
    }

    @Test
    @DisplayName("Split in the middle: multiple children under common prefix (cache retained)")
    void splitInMiddleCacheRetained() {
        RadixTrie t = t("hello", "helium", "help", "helm", "helicon");

        // Under "hel", lexicographic order of 5 items
        assertEquals(L("helicon", "helium", "hello", "helm", "help"),
                t.suggestTopK("hel"));
    }

    @Test
    @DisplayName("Prefix equals a full word: ensure word appears first (if lexicographically smallest)")
    void prefixEqualsWord() {
        RadixTrie t = t("cat", "catalog", "catch", "cater", "cattle");
        assertEquals(L("cat", "catalog", "catch", "cater", "cattle"),
                t.suggestTopK("cat"));
    }

    @Test
    @DisplayName("Inside-edge (prefix shorter than a compressed edge) returns child cache")
    void insideEdgePrefixReturnsChildCache() {
        RadixTrie t = t("hello");
        assertEquals(L("hello"), t.suggestTopK("hel"));
    }

    @Test
    @DisplayName("Exact edge match path (idx >= 0) is exercised")
    void exactEdgeMatchThenContinue() {
        RadixTrie t = t("he", "hero");
        // 'he' is an exact edge; ensure we descend, not fail
        assertFalse(t.suggestTopK("he").isEmpty());
        // Completing 'her' should find 'hero'
        assertEquals(L("hero"), t.suggestTopK("her"));
    }

    @Test
    @DisplayName("Neighbor probing: second neighbor matches (first diverges)")
    void secondNeighborMatches() {
        // Root has edge "he" (common), under it children "ap" (heap) and "llo" (hello).
        RadixTrie t = t("heap", "hello");
        // Query "hel": left neighbor "ap" mismatches; right neighbor "llo" matches -> out("hello")
        assertEquals(L("hello"), t.suggestTopK("hel"));
    }

    @Test
    @DisplayName("Inside-edge mismatch returns empty (e.g., 'gaZ' vs 'gamma')")
    void insideEdgeMismatchReturnsEmpty() {
        RadixTrie t = t("alpha", "beta", "gamma");
        assertTrue(t.suggestTopK("gaZ").isEmpty());
    }

    @Test
    @DisplayName("clear() wipes entries and caches")
    void clearResetsState() {
        RadixTrie t = t("hello", "help");
        assertFalse(t.suggestTopK("he").isEmpty());
        t.clear();
        assertTrue(t.suggestTopK("he").isEmpty());
        assertTrue(t.suggestTopK("").isEmpty());
    }

    @Test
    @DisplayName("Returned suggestion list is unmodifiable")
    void returnedListIsUnmodifiable() {
        RadixTrie t = t("alpha");
        List<String> res = t.suggestTopK("a");
        assertEquals(L("alpha"), res);
        assertThrows(UnsupportedOperationException.class, () -> res.add("zzz"));
    }

    @Test
    @DisplayName("Lowercase query merges uppercase/lowercase buckets; capped at 5")
    void lowercaseQueryMerges() {
        RadixTrie t = t(
                // Uppercase H*
                "Hea", "Hee", "Hei", "Hel", "Hem",
                // Lowercase h*
                "hea", "hee", "hei", "hel", "hem"
        );
        // Uppercase letters sort before lowercase in String.compareTo.
        assertEquals(L("Hea", "Hee", "Hei", "Hel", "Hem"), t.suggestTopK("he"));
    }

    @Test
    @DisplayName("Uppercase query also merges symmetrically")
    void uppercaseQueryMerges() {
        RadixTrie t = t(
                "Hea", "Hee", "Hei", "Hel", "Hem",
                "hea", "hee", "hei", "hel", "hem"
        );
        assertEquals(L("Hea", "Hee", "Hei", "Hel", "Hem"), t.suggestTopK("He"));
    }

    @Test
    @DisplayName("Non-letter first char: no case-toggle fallback")
    void nonLetterNoFallback() {
        RadixTrie t = t("1alpha", "Alpha");
        // Query starts with '1' -> toggleFirstCase is a no-op; must not include "Alpha"
        assertEquals(L("1alpha"), t.suggestTopK("1"));
    }

    @Test
    @DisplayName("Case-sensitive bucket traversal still works without fallback (one-case path)")
    void oneCaseStillWorks() {
        RadixTrie t = t("Hello", "Help", "helium", "help");
        // The merged result should be lexicographic across both cases
        assertEquals(L("Hello", "Help", "helium", "help"), t.suggestTopK("he"));
    }

    @Test
    void cacheSeedGuardBranch() throws Exception {
        // Prepare an empty topK cache
        ArrayList<String> top = new ArrayList<>();

        // Reflectively invoke the private static method:
        Method m = RadixTrie.class.getDeclaredMethod(
                "cacheSeed", ArrayList.class, List.class);
        m.setAccessible(true);

        // seed == null -> early return (no change)
        m.invoke(null, top, (Object) null);
        assertTrue(top.isEmpty());

        // seed.isEmpty() -> early return (no change)
        m.invoke(null, top, List.of());
        assertTrue(top.isEmpty());

        // non-empty seed -> items merged via cachePush, deduped and sorted
        m.invoke(null, top, List.of("b", "a", "c", "a")); // note duplicate "a"
        assertEquals(List.of("a", "b", "c"), List.copyOf(top));
    }

    @Test
    @DisplayName("tryAdvanceOrReturn: lcp==0 → continue (defensive branch)")
    void tryAdvanceOrReturn_lcpZero_continue() throws Exception {
        String base = "com.csse3200.game.ui.terminal.autocomplete.RadixTrie$";
        Class<?> edgeCls = Class.forName(base + "Edge");
        Class<?> nodeCls = Class.forName(base + "Node");
        Class<?> stepCls = Class.forName(base + "Step");

        var nodeCtor = nodeCls.getDeclaredConstructor();
        nodeCtor.setAccessible(true);
        var edgeCtor = edgeCls.getDeclaredConstructor(String.class, nodeCls);
        edgeCtor.setAccessible(true);

        // Bucket with labels that share no prefix with "hello" → lcp == 0
        Object e0 = edgeCtor.newInstance("zzz", nodeCtor.newInstance());
        Object e1 = edgeCtor.newInstance("yyy", nodeCtor.newInstance());
        List<Object> bucket = new ArrayList<>();
        bucket.add(e0);
        bucket.add(e1);

        var tryM = RadixTrie.class.getDeclaredMethod("tryAdvanceOrReturn", List.class, int.class, String.class);
        tryM.setAccessible(true);

        // ins = 1 → probe indices 0 and 1; both lcp==0 → Step.fail()
        Object step = tryM.invoke(null, bucket, 1, "hello");

        var getAdvance = stepCls.getDeclaredMethod("advance");
        getAdvance.setAccessible(true);
        var getOut = stepCls.getDeclaredMethod("out");
        getOut.setAccessible(true);

        assertNull(getAdvance.invoke(step), "advance() should be null when both neighbors produce lcp==0");
        assertNull(getOut.invoke(step), "out() should be null when both neighbors produce lcp==0");
    }

    @Test
    @DisplayName("toggleFirstCase: null/empty early return (defensive guard)")
    void toggleFirstCase_nullEmpty_guard() throws Exception {
        Method m = RadixTrie.class.getDeclaredMethod("toggleFirstCase", String.class);
        m.setAccessible(true);

        // null -> returns null
        Object r1 = m.invoke(null, new Object[]{null});
        assertNull(r1);

        // "" -> returns same instance
        String empty = "";
        Object r2 = m.invoke(null, empty);
        assertSame(empty, r2);
    }

    @Test
    @DisplayName("insert: neighbor probe sees lcp==0 → continue; falls back to new-leaf path")
    void insert_neighborLoop_lcpZero_continue() throws Exception {
        RadixTrie t = new RadixTrie();

        // Reflect root.children and seed an invalid bucket for key 'a' (edges not starting with 'a').
        Field rootF = RadixTrie.class.getDeclaredField("root");
        rootF.setAccessible(true);
        Object rootNode = rootF.get(t);

        Class<?> nodeCls = Class.forName("com.csse3200.game.ui.terminal.autocomplete.RadixTrie$Node");
        Field childrenF = nodeCls.getDeclaredField("children");
        childrenF.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Character, List<?>> children = (Map<Character, List<?>>) childrenF.get(rootNode);

        Class<?> edgeCls = Class.forName("com.csse3200.game.ui.terminal.autocomplete.RadixTrie$Edge");
        Constructor<?> nodeCtor = nodeCls.getDeclaredConstructor();
        nodeCtor.setAccessible(true);
        Constructor<?> edgeCtor = edgeCls.getDeclaredConstructor(String.class, nodeCls);
        edgeCtor.setAccessible(true);

        Object bogusNext1 = nodeCtor.newInstance();
        Object bogusNext2 = nodeCtor.newInstance();
        Object edgeA = edgeCtor.newInstance("zzz", bogusNext1); // wrong first char
        Object edgeB = edgeCtor.newInstance("yyy", bogusNext2); // wrong first char

        ArrayList<Object> bogusBucket = new ArrayList<>();
        bogusBucket.add(edgeA);
        bogusBucket.add(edgeB);
        // Put the bogus bucket under 'a' so insert("alpha") will look here.
        children.put('a', bogusBucket);

        t.insert("alpha");

        // Public behavior remains correct: we can retrieve "alpha"
        assertEquals(List.of("alpha"), t.suggestTopK("a"));
    }

    @Test
    @DisplayName("bucket == null → return List.of()")
    void bucketNull_returnsEmpty() throws Exception {
        RadixTrie t = new RadixTrie();

        // Call the private, case-sensitive walker directly.
        Method m = RadixTrie.class.getDeclaredMethod("suggestOneCase", String.class);
        m.setAccessible(true);

        Object res = m.invoke(t, "z");
        assertTrue((boolean) Collection.class.getMethod("isEmpty").invoke(res));
    }

    @Test
    @DisplayName("bucket.isEmpty() → return List.of()")
    void bucketEmpty_returnsEmpty() throws Exception {
        var t = new RadixTrie();

        var rootF = RadixTrie.class.getDeclaredField("root");
        rootF.setAccessible(true);
        var rootNode = rootF.get(t);

        var nodeCls = Class.forName("com.csse3200.game.ui.terminal.autocomplete.RadixTrie$Node");
        var childrenF = nodeCls.getDeclaredField("children");
        childrenF.setAccessible(true);

        var childrenObj = childrenF.get(rootNode);
        childrenObj.getClass().getMethod("put", Object.class, Object.class)
                .invoke(childrenObj, 'q', new ArrayList<>());

        var m = RadixTrie.class.getDeclaredMethod("suggestOneCase", String.class);
        m.setAccessible(true);

        assertTrue((boolean) Collection.class.getMethod("isEmpty").invoke(m.invoke(t, "q")));
    }

    @Test
    @DisplayName("mergeTopK: a is empty → returns b (same instance)")
    void merge_emptyLeft_returnsRight_sameInstance() throws Exception {
        List<String> a = List.of();
        List<String> b = new ArrayList<>(List.of("a", "b", "c"));
        Object res = callMergeTopK(a, b);
        assertSame(b, res);
        assertEquals(List.of("a", "b", "c"), res);
    }

    @Test
    @DisplayName("mergeTopK: b is empty → returns a (same instance)")
    void merge_emptyRight_returnsLeft_sameInstance() throws Exception {
        List<String> a = new ArrayList<>(List.of("a", "b"));
        List<String> b = List.of();
        Object res = callMergeTopK(a, b);
        assertSame(a, res);
        assertEquals(List.of("a", "b"), res);
    }

    @Test
    @DisplayName("mergeTopK: dedup + cap at 5")
    void merge_dedup_and_cap() throws Exception {
        // both sorted ascending
        List<String> a = List.of("Hea", "Hee", "Hei");
        List<String> b = List.of("Hee", "Hel", "Hem", "Hex", "Hey");
        Object res = callMergeTopK(a, b);
        assertEquals(List.of("Hea", "Hee", "Hei", "Hel", "Hem"), res);
    }

    @Test
    @DisplayName("mergeTopK: picks in lexicographic order; result is unmodifiable")
    void merge_order_and_unmodifiable() throws Exception {
        List<String> a = List.of("a", "c", "e", "g", "i");
        List<String> b = List.of("a", "b", "d", "f", "h", "j");
        Object res = callMergeTopK(a, b);
        assertEquals(List.of("a", "b", "c", "d", "e"), res);

        // Try to add via reflection; should throw UnsupportedOperationException
        try {
            java.util.List.class.getMethod("add", Object.class).invoke(res, "zzz");
            fail("Expected unmodifiable list");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            assertInstanceOf(UnsupportedOperationException.class, ite.getCause());
        }
    }

    @Test
    @DisplayName("mergeTopK: one list exhausted → continue from the other side")
    void merge_oneSideExhausted_thenOther() throws Exception {
        // b has the smallest items, so it's consumed first, then a fills until K
        List<String> a = List.of("m", "n", "o", "p", "q");
        List<String> b = List.of("a", "b");
        Object res = callMergeTopK(a, b);
        assertEquals(List.of("a", "b", "m", "n", "o"), res);
    }

    @Test
    @DisplayName("lowerBoundLabel: empty bucket → 0")
    void lowerBound_empty_returnsZero() throws Exception {
        List<Object> bucket = edgeBucket(); // empty
        assertEquals(0, callLowerBoundLabel(bucket, "anything"));
    }

    @Test
    @DisplayName("lowerBoundLabel: before first element")
    void lowerBound_beforeFirst() throws Exception {
        List<Object> bucket = edgeBucket("aa", "ab", "ad", "ad", "ae"); // sorted
        assertEquals(0, callLowerBoundLabel(bucket, "a")); // before "aa"
    }

    @Test
    @DisplayName("lowerBoundLabel: exact match (first occurrence among duplicates)")
    void lowerBound_exactFirstOfDuplicates() throws Exception {
        List<Object> bucket = edgeBucket("aa", "ab", "ad", "ad", "ae");
        assertEquals(2, callLowerBoundLabel(bucket, "ad")); // first "ad" at index 2
        assertEquals(1, callLowerBoundLabel(bucket, "ab")); // exact at index 1
        assertEquals(0, callLowerBoundLabel(bucket, "aa")); // exact at index 0
    }

    @Test
    @DisplayName("lowerBoundLabel: in-between insertion point")
    void lowerBound_between() throws Exception {
        List<Object> bucket = edgeBucket("aa", "ab", "ad", "ad", "ae");
        assertEquals(2, callLowerBoundLabel(bucket, "ac")); // between "ab" and "ad"
    }

    @Test
    @DisplayName("lowerBoundLabel: after last element")
    void lowerBound_afterLast() throws Exception {
        List<Object> bucket = edgeBucket("aa", "ab", "ad", "ad", "ae");
        assertEquals(5, callLowerBoundLabel(bucket, "af")); // size of bucket
    }
}
