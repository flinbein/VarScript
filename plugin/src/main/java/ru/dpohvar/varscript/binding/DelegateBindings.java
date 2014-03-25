package ru.dpohvar.varscript.binding;

import ru.dpohvar.varscript.utils.StringSetUtils;

import javax.script.Bindings;
import java.util.*;

/**
 * Это биндинги для Workspace
 * Внутри содержится HashMap
 * При просмотре значения, если оно не найдено в хешмапе,
 * то ищется в параметре view
 */
public class DelegateBindings implements Bindings {

    private final Map<String,Object> map = new HashMap<>();
    private final Map<String,Object> view;

    public DelegateBindings(Map<String, Object> view) {
        this.view = view;
    }

    public Object put(String name, Object value) {
        checkKey(name);
        return map.put(name,value);
    }

    public void putAll(Map<? extends String, ?> toMerge) {
        if (toMerge == null) {
            throw new NullPointerException("toMerge map is null");
        }
        for (Map.Entry<? extends String, ?> entry : toMerge.entrySet()) {
            String key = entry.getKey();
            checkKey(key);
            put(key, entry.getValue());
        }
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        checkKey(key);
        return map.containsKey(key) || view.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value) || view.containsValue(value);
    }

    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public Object get(Object key) {
        checkKey(key);
        if (map.containsKey(key)) return map.get(key);
        return view.get(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Object remove(Object key) {
        checkKey(key);
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection<Object> values() {
        return map.values();
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException("key can not be null");
        }
        if (!(key instanceof String)) {
            throw new ClassCastException("key should be a String");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("key can not be empty");
        }
    }


    public Set<String> findCompletion(String pattern) {
        Set<String> result = new HashSet<>();
        result.addAll(map.keySet());
        result.addAll(view.keySet());
        StringSetUtils.reduceByPattern(result, pattern);
        return result;
    }
}
