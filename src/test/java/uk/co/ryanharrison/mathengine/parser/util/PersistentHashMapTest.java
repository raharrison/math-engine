package uk.co.ryanharrison.mathengine.parser.util;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PersistentHashMap}.
 */
class PersistentHashMapTest {

    // ==================== Creation ====================

    @Test
    void emptyMapIsEmpty() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.empty();

        assertThat(map.isEmpty()).isTrue();
        assertThat(map.size()).isZero();
    }

    @Test
    void fromCreatesMapWithEntries() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("a", 1, "b", 2));

        assertThat(map.isEmpty()).isFalse();
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get("a")).isEqualTo(1);
        assertThat(map.get("b")).isEqualTo(2);
    }

    @Test
    void fromWithEmptyMapCreatesEmptyPersistentMap() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of());

        assertThat(map.isEmpty()).isTrue();
        assertThat(map.size()).isZero();
    }

    // ==================== Basic Operations ====================

    @Test
    void assocAddsNewEntry() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.empty();

        PersistentHashMap<String, Integer> updated = map.assoc("key", 42);

        assertThat(updated.get("key")).isEqualTo(42);
        assertThat(updated.containsKey("key")).isTrue();
    }

    @Test
    void assocDoesNotModifyOriginal() {
        PersistentHashMap<String, Integer> original = PersistentHashMap.empty();

        PersistentHashMap<String, Integer> updated = original.assoc("key", 42);

        assertThat(original.containsKey("key")).isFalse();
        assertThat(original.get("key")).isNull();
        assertThat(updated.get("key")).isEqualTo(42);
    }

    @Test
    void assocOverwritesExistingValue() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("key", 1));

        PersistentHashMap<String, Integer> updated = map.assoc("key", 2);

        assertThat(map.get("key")).isEqualTo(1);
        assertThat(updated.get("key")).isEqualTo(2);
    }

    @Test
    void assocAllAddsMultipleEntries() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("a", 1));

        PersistentHashMap<String, Integer> updated = map.assocAll(Map.of("b", 2, "c", 3));

        assertThat(updated.get("a")).isEqualTo(1);
        assertThat(updated.get("b")).isEqualTo(2);
        assertThat(updated.get("c")).isEqualTo(3);
        assertThat(updated.size()).isEqualTo(3);
    }

    @Test
    void assocAllWithEmptyMapReturnsSameInstance() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("a", 1));

        PersistentHashMap<String, Integer> updated = map.assocAll(Map.of());

        assertThat(updated).isSameAs(map);
    }

    @Test
    void assocAllDoesNotModifyOriginal() {
        PersistentHashMap<String, Integer> original = PersistentHashMap.from(Map.of("a", 1));

        original.assocAll(Map.of("b", 2));

        assertThat(original.containsKey("b")).isFalse();
        assertThat(original.size()).isEqualTo(1);
    }

    // ==================== Dissoc (Removal) ====================

    @Test
    void dissocRemovesEntry() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("a", 1, "b", 2));

        PersistentHashMap<String, Integer> updated = map.dissoc("a");

        assertThat(updated.containsKey("a")).isFalse();
        assertThat(updated.get("a")).isNull();
        assertThat(updated.containsKey("b")).isTrue();
    }

    @Test
    void dissocDoesNotModifyOriginal() {
        PersistentHashMap<String, Integer> original = PersistentHashMap.from(Map.of("a", 1));

        PersistentHashMap<String, Integer> updated = original.dissoc("a");

        assertThat(original.containsKey("a")).isTrue();
        assertThat(original.get("a")).isEqualTo(1);
        assertThat(updated.containsKey("a")).isFalse();
    }

    @Test
    void dissocShadowsParentValue() {
        PersistentHashMap<String, Integer> parent = PersistentHashMap.from(Map.of("a", 1));
        PersistentHashMap<String, Integer> child = parent.assoc("b", 2);

        PersistentHashMap<String, Integer> withRemoval = child.dissoc("a");

        assertThat(withRemoval.containsKey("a")).isFalse();
        assertThat(withRemoval.get("a")).isNull();
        assertThat(withRemoval.containsKey("b")).isTrue();
    }

    // ==================== Lookup ====================

    @Test
    void getReturnsNullForMissingKey() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.empty();

        assertThat(map.get("missing")).isNull();
    }

    @Test
    void getFindsValueInParentChain() {
        PersistentHashMap<String, Integer> root = PersistentHashMap.from(Map.of("a", 1));
        PersistentHashMap<String, Integer> child = root.assoc("b", 2);
        PersistentHashMap<String, Integer> grandchild = child.assoc("c", 3);

        assertThat(grandchild.get("a")).isEqualTo(1);
        assertThat(grandchild.get("b")).isEqualTo(2);
        assertThat(grandchild.get("c")).isEqualTo(3);
    }

    @Test
    void containsKeyReturnsFalseForMissingKey() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("a", 1));

        assertThat(map.containsKey("missing")).isFalse();
    }

    @Test
    void containsKeyFindsKeyInParentChain() {
        PersistentHashMap<String, Integer> root = PersistentHashMap.from(Map.of("a", 1));
        PersistentHashMap<String, Integer> child = root.assoc("b", 2);

        assertThat(child.containsKey("a")).isTrue();
        assertThat(child.containsKey("b")).isTrue();
    }

    // ==================== Shadowing ====================

    @Test
    void childValueShadowsParentValue() {
        PersistentHashMap<String, Integer> parent = PersistentHashMap.from(Map.of("key", 1));
        PersistentHashMap<String, Integer> child = parent.assoc("key", 2);

        assertThat(parent.get("key")).isEqualTo(1);
        assertThat(child.get("key")).isEqualTo(2);
    }

    @Test
    void multipleShadowingLevels() {
        PersistentHashMap<String, Integer> level0 = PersistentHashMap.from(Map.of("x", 0));
        PersistentHashMap<String, Integer> level1 = level0.assoc("x", 1);
        PersistentHashMap<String, Integer> level2 = level1.assoc("x", 2);
        PersistentHashMap<String, Integer> level3 = level2.assoc("x", 3);

        assertThat(level0.get("x")).isEqualTo(0);
        assertThat(level1.get("x")).isEqualTo(1);
        assertThat(level2.get("x")).isEqualTo(2);
        assertThat(level3.get("x")).isEqualTo(3);
    }

    // ==================== toMap ====================

    @Test
    void toMapFlattensChain() {
        PersistentHashMap<String, Integer> root = PersistentHashMap.from(Map.of("a", 1));
        PersistentHashMap<String, Integer> child = root.assoc("b", 2);
        PersistentHashMap<String, Integer> grandchild = child.assoc("c", 3);

        Map<String, Integer> flattened = grandchild.toMap();

        assertThat(flattened).containsExactlyInAnyOrderEntriesOf(Map.of("a", 1, "b", 2, "c", 3));
    }

    @Test
    void toMapReflectsShadowing() {
        PersistentHashMap<String, Integer> parent = PersistentHashMap.from(Map.of("x", 1, "y", 2));
        PersistentHashMap<String, Integer> child = parent.assoc("x", 10);

        Map<String, Integer> flattened = child.toMap();

        assertThat(flattened).containsExactlyInAnyOrderEntriesOf(Map.of("x", 10, "y", 2));
    }

    @Test
    void toMapReflectsDissoc() {
        PersistentHashMap<String, Integer> parent = PersistentHashMap.from(Map.of("a", 1, "b", 2));
        PersistentHashMap<String, Integer> child = parent.dissoc("a");

        Map<String, Integer> flattened = child.toMap();

        assertThat(flattened).containsExactlyInAnyOrderEntriesOf(Map.of("b", 2));
    }

    @Test
    void toMapReturnsIndependentCopy() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("a", 1));

        Map<String, Integer> flattened = map.toMap();
        flattened.put("b", 2);

        assertThat(map.containsKey("b")).isFalse();
    }

    // ==================== keySet ====================

    @Test
    void keySetReturnsAllKeys() {
        PersistentHashMap<String, Integer> root = PersistentHashMap.from(Map.of("a", 1));
        PersistentHashMap<String, Integer> child = root.assoc("b", 2);

        Set<String> keys = child.keySet();

        assertThat(keys).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void keySetExcludesDissociatedKeys() {
        PersistentHashMap<String, Integer> parent = PersistentHashMap.from(Map.of("a", 1, "b", 2));
        PersistentHashMap<String, Integer> child = parent.dissoc("a");

        Set<String> keys = child.keySet();

        assertThat(keys).containsExactly("b");
    }

    @Test
    void keySetWithShadowingDoesNotDuplicateKeys() {
        PersistentHashMap<String, Integer> parent = PersistentHashMap.from(Map.of("x", 1));
        PersistentHashMap<String, Integer> child = parent.assoc("x", 2);

        Set<String> keys = child.keySet();

        assertThat(keys).containsExactly("x");
    }

    // ==================== Size and Empty ====================

    @Test
    void sizeReflectsAllEntries() {
        PersistentHashMap<String, Integer> root = PersistentHashMap.from(Map.of("a", 1));
        PersistentHashMap<String, Integer> child = root.assoc("b", 2);

        assertThat(child.size()).isEqualTo(2);
    }

    @Test
    void sizeAccountsForShadowing() {
        PersistentHashMap<String, Integer> parent = PersistentHashMap.from(Map.of("x", 1));
        PersistentHashMap<String, Integer> child = parent.assoc("x", 2);

        assertThat(child.size()).isEqualTo(1);
    }

    @Test
    void sizeAccountsForDissoc() {
        PersistentHashMap<String, Integer> parent = PersistentHashMap.from(Map.of("a", 1, "b", 2));
        PersistentHashMap<String, Integer> child = parent.dissoc("a");

        assertThat(child.size()).isEqualTo(1);
    }

    @Test
    void isEmptyAfterDissocAll() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("a", 1));
        PersistentHashMap<String, Integer> empty = map.dissoc("a");

        assertThat(empty.isEmpty()).isTrue();
    }

    // ==================== toString ====================

    @Test
    void toStringShowsEntries() {
        PersistentHashMap<String, Integer> map = PersistentHashMap.from(Map.of("a", 1));

        assertThat(map.toString()).contains("a").contains("1");
    }

    // ==================== Structural Sharing ====================

    @Test
    void chainedMapsShareStructure() {
        PersistentHashMap<String, Integer> map1 = PersistentHashMap.from(Map.of("a", 1));
        PersistentHashMap<String, Integer> map2 = map1.assoc("b", 2);
        PersistentHashMap<String, Integer> map3 = map1.assoc("c", 3);

        // map2 and map3 both branch from map1
        // They should both be able to see "a" from the shared parent
        assertThat(map2.get("a")).isEqualTo(1);
        assertThat(map3.get("a")).isEqualTo(1);

        // But they have different local additions
        assertThat(map2.get("b")).isEqualTo(2);
        assertThat(map2.get("c")).isNull();
        assertThat(map3.get("c")).isEqualTo(3);
        assertThat(map3.get("b")).isNull();
    }

    @Test
    void branchingDoesNotAffectOtherBranches() {
        PersistentHashMap<String, Integer> root = PersistentHashMap.from(Map.of("x", 0));
        PersistentHashMap<String, Integer> branch1 = root.assoc("x", 1);
        PersistentHashMap<String, Integer> branch2 = root.assoc("x", 2);

        assertThat(root.get("x")).isEqualTo(0);
        assertThat(branch1.get("x")).isEqualTo(1);
        assertThat(branch2.get("x")).isEqualTo(2);
    }
}
