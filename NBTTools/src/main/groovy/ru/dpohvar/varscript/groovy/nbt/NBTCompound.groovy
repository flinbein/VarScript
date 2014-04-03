package ru.dpohvar.varscript.groovy.nbt

import groovy.transform.CompileStatic

/**
 * Created by DPOH-VAR on 14.01.14
 */
@SuppressWarnings(["GroovyUnusedDeclaration", "GroovyUnusedCatchParameter"])
@CompileStatic
public class NBTCompound implements Map<String, Object> {

    private final Map<String, Object> handleMap;
    final Object handle;

    public static NBTCompound forNBT(Object tag) {
        return new NBTCompound(tag, true);
    }

    public NBTCompound(Object tag, boolean ignored) {
        handle = tag
        handleMap = NBTUtils.fieldNBTTagCompoundMap.of(tag).get() as Map<String, Object>
        assert NBTUtils.rcNBTTagCompound.getRealClass().isInstance(tag)
    }

    public NBTCompound() {
        this(NBTUtils.conNBTTagCompound.create(), true)
    }

    public NBTCompound(Map values) {
        this()
        this.putAll(values)
    }

    public Object getHandle() {
        return handle;
    }

    public Object getHandleMap() {
        return handleMap;
    }

    @Override
    public boolean equals(Object t) {
        t instanceof NBTCompound && handle.equals(t.@handle);
    }

    public void merge(Map<String, Object> values) {
        values.each { String key, Object value ->
            if (!containsKey(key)) {
                put(key, value)
                return
            }
            def val = get key
            if (val instanceof NBTCompound && value instanceof Map) {
                val.merge value
            } else {
                put key, value
            }
        }
    }

    @Override
    public NBTCompound clone() {
        forNBT(handle.clone())
    }

    @Override
    public int size() {
        handleMap.size()
    }

    @Override
    public boolean isEmpty() {
        handleMap.isEmpty()
    }

    @Override
    public boolean containsKey(Object key) {
        handleMap.containsKey(key)
    }

    @Override
    public boolean containsValue(Object value) {
        Object tag = NBTUtils.createTag value
        handleMap.containsValue tag
    }

    @Override
    public Object get(Object key) {
        NBTUtils.getValue handleMap.get(key)
    }

    @Override
    public Object put(String key, Object value) {
        Object tag = NBTUtils.createTag value
        Object oldTag = handleMap.put key, tag
        NBTUtils.getValue oldTag
    }

    @Override
    public Object remove(Object key) {
        def oldTag = handleMap.remove key
        NBTUtils.getValue oldTag
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        m.each { String k, Object v -> put(k, v) }
    }

    @Override
    public void clear() {
        handleMap.clear()
    }

    @Override
    public Set<String> keySet() {
        handleMap.keySet()
    }

    @Override
    public Collection<Object> values() {
        new NBTValues(handleMap.values())
    }

    @Override
    public NBTEntrySet entrySet() {
        new NBTEntrySet(handleMap.entrySet())
    }

    public class NBTValues extends AbstractCollection<Object> {

        Collection<Object> handle

        public NBTValues(Collection<Object> values) {
            handle = values
        }

        @Override
        public Iterator<Object> iterator() {
            new NBTValuesIterator(handle.iterator());
        }

        @Override
        public int size() {
            handle.size()
        }

        public class NBTValuesIterator implements Iterator<Object> {

            private Iterator<Object> handle

            public NBTValuesIterator(Iterator<Object> iterator) {
                handle = iterator
            }

            @Override
            public boolean hasNext() {
                handle.hasNext()
            }

            @Override
            public Object next() {
                NBTUtils.getValue handle.next();
            }

            @Override
            public void remove() {
                handle.remove()
            }
        }
    }

    public class NBTEntrySet extends AbstractSet<Map.Entry<String, Object>> {

        private Set<Map.Entry<String, Object>> entries;

        public NBTEntrySet(Set<Map.Entry<String, Object>> entries) {
            this.entries = entries;
        }

        @Override
        public NBTIterator iterator() {
            new NBTIterator(entries.iterator());
        }

        @Override
        public int size() {
            entries.size();
        }

        public class NBTIterator implements Iterator<Map.Entry<String, Object>> {

            private Iterator<Map.Entry<String, Object>> iterator;

            public NBTIterator(Iterator<Map.Entry<String, Object>> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                iterator.hasNext();
            }

            @Override
            public NBTEntry next() {
                new NBTEntry(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            public class NBTEntry implements Map.Entry<String, Object> {

                private Map.Entry<String, Object> entry;

                public NBTEntry(Map.Entry<String, Object> entry) {
                    this.entry = entry;
                }

                @Override
                public String getKey() {
                    entry.getKey();
                }

                @Override
                public Object getValue() {
                    NBTUtils.getValue entry.getValue()
                }

                @Override
                public Object setValue(Object value) {
                    Object tag = NBTUtils.createTag value
                    Object oldTag = entry.setValue tag
                    NBTUtils.getValue oldTag
                }
            }
        }
    }

    public String toString() {
        NBTEntrySet.NBTIterator i = entrySet().iterator();
        if (!i.hasNext()) return "{}";
        StringBuilder sb = new StringBuilder().append('{');
        for (; ;) {
            NBTEntrySet.NBTIterator.NBTEntry e = i.next();
            Object val = e.getValue();
            sb.append(e.getKey()).append('=');
            if (val instanceof byte[]) {
                sb.append("int[" + ((byte[]) val).length + ']');
            } else if (val instanceof int[]) {
                sb.append("byte[" + ((int[]) val).length + ']');
            } else {
                sb.append(val);
            }
            if (!i.hasNext()) return sb.append('}').toString();
            sb.append(", ");
        }
    }


    public byte getByte(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).byteValue();
        if (val instanceof String) try {
            return (byte) Long.parseLong((String) val);
        } catch (e) {
            try {
                return (byte) Double.parseDouble((String) val);
            } catch (ignored) {
            }
        }
        return 0;
    }

    public short getShort(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).shortValue();
        if (val instanceof String) try {
            return (short) Long.parseLong((String) val);
        } catch (e) {
            try {
                return (short) Double.parseDouble((String) val);
            } catch (ignored) {
            }
        }
        return 0;
    }

    public int getInt(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) try {
            return (int) Long.parseLong((String) val);
        } catch (Exception e) {
            try {
                return (int) Double.parseDouble((String) val);
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    public long getLong(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) try {
            return Long.parseLong((String) val);
        } catch (Exception e) {
            try {
                return (long) Double.parseDouble((String) val);
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    public float getFloat(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).floatValue();
        if (val instanceof String) try {
            return (float) Double.parseDouble((String) val);
        } catch (Exception ignored) {
        }
        return 0;
    }

    public double getDouble(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) try {
            return Double.parseDouble((String) val);
        } catch (Exception ignored) {
        }
        return 0;
    }

    public String getString(String key) {
        Object val = get(key);
        if (val == null) return "";
        else return val.toString();
    }

    /**
     * get NBTCompound or create new one
     * Example: new NBTCompound().compound("display").list("Lore").add("lore1")
     * @param key key
     * @return existing or created compound
     */
    public NBTCompound compound(String key) {
        Object val = get key
        if (val instanceof NBTCompound) return val as NBTCompound
        NBTCompound compound = new NBTCompound()
        put key, compound
        return compound
    }

    /**
     * get NBTList or create new one
     * Example: new NBTCompound().compound("display").list("Lore").add("lore1")
     * @param key key
     * @return existing or created list
     */
    public NBTList list(String key) {
        Object val = get key
        if (val instanceof NBTList) return val as NBTList
        NBTList list = new NBTList()
        put key, list
        return list
    }

    /**
     * @see #put(String, Object)
     * @param key key
     * @param value value
     * @return this
     */
    public NBTCompound putHere(String key, Object value) {
        Object tag = NBTUtils.createTag value
        handleMap.put key, tag
        return this
    }

}










