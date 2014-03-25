package ru.dpohvar.varscript.groovy.nbt

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode;

import java.util.*;

/**
 * Created by DPOH-VAR on 15.01.14
 */

@SuppressWarnings(["GroovyUnusedDeclaration", "GroovyAssignabilityCheck"])
@CompileStatic
public class NBTList implements List<Object> {

    private final List<Object> handleList
    private final Object handle

    public static NBTList forNBT(Object tag){
        return new NBTList(tag, false)
    }

    public NBTList(Object tag, boolean ignored) {
        handle = tag
        handleList = NBTUtils.fieldNBTTagListList.of(handle).get() as List
        assert NBTUtils.rcNBTTagList.realClass.isInstance(tag)
    }

    public NBTList(Collection collection) {
        this()
        addAll collection
    }

    public NBTList() {
        this(NBTUtils.conNBTTagList.create(), true)
    }

    @Override
    public boolean equals(Object t){
        return t instanceof NBTList && handle.equals(((NBTList) t).handle);
    }

    public List<Object> getHandleList(){
        return handleList;
    }

    public Object getHandle(){
        return handle;
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    public byte getType(){
        if (handleList.empty) return 0
        else return handle.@type
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    private void setType(byte type){
        handle.@type = type
    }

    private Object convertToListType(Object obj) {
        switch (type) {
            case 1: return NBTUtils.createTag(obj as byte)
            case 2: return NBTUtils.createTag(obj as short)
            case 3: return NBTUtils.createTag(obj as int)
            case 4: return NBTUtils.createTag(obj as long)
            case 5: return NBTUtils.createTag(obj as float)
            case 6: return NBTUtils.createTag(obj as double)
            case 7: return NBTUtils.createTag(obj as byte[])
            case 8: return NBTUtils.createTag(obj as String)
            case 9: return NBTUtils.createTag(obj as List)
            case 10: return NBTUtils.createTag(obj as Map)
            case 11: return NBTUtils.createTag(obj as int[])
            default:
                def tag = NBTUtils.createTag(obj)
                type = NBTUtils.methodNBTTypeId.of(tag) as byte;
                return tag
        }
    }

    @Override
    public NBTList clone(){
        return forNBT( handle.clone() )
    }

    @Override
    public int size() {
        return handleList.size()
    }

    @Override
    public boolean isEmpty() {
        return !handleList
    }

    @Override
    public boolean contains(Object o) {
        return handleList.contains(NBTUtils.createTag(o));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public NBTIterator iterator() {
        return new NBTIterator(handleList.listIterator());
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Object[] toArray() {
        Object[] result = new Object[size()]
        int i=0
        for (Object t: this) result[i++] = t
        return result
    }

    @Override
    @SuppressWarnings("unchecked,NullableProblems")
    public <T> T[] toArray(T[] a) {
        int size = size()
        if (size > a.length) size = a.length
        for (int i=0; i<size; i++){
            a[i] = (T) get(i)
        }
        return a

    }

    @Override
    public boolean add(Object o) {
        return handleList.add(convertToListType(o));
    }

    @Override
    public boolean remove(Object o) {
        return handleList.remove(convertToListType(o));
    }

    @Override
    public boolean containsAll( Collection<?> c) {
        def e = c.find {
            ! handleList.contains( NBTUtils.createTag(it) )
        }
        e == null
    }


    @Override
    public boolean addAll(Collection<?> c) {
        boolean modified = false
        for (Object t: c) {
            modified |=  handleList.add(convertToListType(t));
        }
        return modified;
    }

    @Override
    public boolean addAll(int index,@SuppressWarnings("NullableProblems") Collection<?> c) {
        for (Object t: c) {
            Object tag = NBTUtils.createTag t;
            handleList.add index++, convertToListType(t)
        }
        return !c.empty
    }

    @Override
    public boolean removeAll(@SuppressWarnings("NullableProblems") Collection<?> c) {
        boolean modified = false;
        for (Object t: c) {
            modified |= handleList.remove(convertToListType(t));
        }
        return modified;

    }

    @Override
    public boolean retainAll(@SuppressWarnings("NullableProblems") Collection<?> c) {
        boolean modified = false;
        Iterator itr = iterator();
        while (itr.hasNext()) {
            if (!c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        handleList.clear();
    }

    @Override
    public Object get(int index) {
        return NBTUtils.getValue(handleList.get(index));
    }

    @Override
    public Object set(int index, Object element) {
        Object oldTag = handleList.set(index, convertToListType(element));
        return NBTUtils.getValue(oldTag);
    }

    @Override
    public void add(int index, Object element) {
        Object tag = NBTUtils.createTag(element);
        handleList.add(index, convertToListType(element));
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public Object remove(int index) {
        NBTUtils.getValue(handleList.remove(index));
    }

    @Override
    public int indexOf(Object o) {
        return handleList.indexOf(NBTUtils.createTag(o));
    }

    @Override
    public int lastIndexOf(Object o) {
        return handleList.lastIndexOf(NBTUtils.createTag(o));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public NBTIterator listIterator() {
        return new NBTIterator(handleList.listIterator());

    }

    @Override
    @SuppressWarnings("NullableProblems")
    public NBTIterator listIterator(int index) {
        return new NBTIterator(handleList.listIterator(index));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public NBTSubList subList(int fromIndex, int toIndex) {
        return new NBTSubList(this,fromIndex,toIndex);
    }

    public class NBTIterator implements ListIterator<Object>{

        protected ListIterator<Object> iterator;

        private NBTIterator(ListIterator<Object> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Object next() {
            return NBTUtils.getValue(iterator.next());
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public Object previous() {
            return NBTUtils.getValue(iterator.previous());
        }

        @Override
        public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

        @Override
        public void set(Object o) {
            Object tag = NBTUtils.createTag(o);
            iterator.set(convertToListType(o));
        }

        @Override
        public void add(Object o) {
            Object tag = NBTUtils.createTag(o);
            iterator.add(convertToListType(o));
        }
    }

    public class NBTSubList extends NBTList {
        private final NBTList list;
        private final int offset;
        private int size;

        private NBTSubList(NBTList list, int fromIndex, int toIndex) {
            this.list = list;
            this.offset = fromIndex;
            size = toIndex - fromIndex;
            if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex = $fromIndex");
            if (toIndex > list.size()) throw new IndexOutOfBoundsException("toIndex = $toIndex");
            if (fromIndex > toIndex) throw new IllegalArgumentException("fromIndex($fromIndex) > toIndex($toIndex)");
        }

        @Override
        public Object set(int index, Object element) {
            rangeCheck index
            list.set index+offset, element
        }

        @Override
        public Object get(int index) {
            rangeCheck index
            list.get index+offset
        }

        @Override
        public int size() {
            size
        }

        @Override
        public void add(int index, Object element) {
            rangeCheckForAdd index
            list.add index + offset, element
            size++
        }

        @Override
        @CompileStatic(TypeCheckingMode.SKIP)
        public Object remove(int index) {
            rangeCheck index
            list.remove index+offset
        }

        @Override
        public boolean addAll(Collection<?> c) {
            addAll size, c
        }

        @Override
        public boolean addAll(int index, Collection<?> c) {
            rangeCheckForAdd index
            int cSize = c.size()
            if (cSize==0) return false
            list.addAll offset+index, c
            size += cSize
            return true
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public NBTIterator iterator() {
            listIterator()
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public NBTIterator listIterator(final int index) {
            rangeCheckForAdd index

            return new NBTIterator(list.listIterator(index+offset)) {

                @Override
                public boolean hasNext() {
                    nextIndex() < size
                }

                @Override
                public Object next() {
                    if (hasNext()) iterator.next()
                    else throw new NoSuchElementException()
                }

                @Override
                public boolean hasPrevious() {
                    previousIndex() >= 0
                }

                @Override
                public Object previous() {
                    if (hasPrevious()) iterator.previous()
                    else throw new NoSuchElementException()
                }

                @Override
                public int nextIndex() {
                    iterator.nextIndex() - offset
                }

                @Override
                public int previousIndex() {
                    iterator.previousIndex() - offset
                }

                @Override
                public void remove() {
                    iterator.remove()
                    size--;
                }

                @Override
                public void set(Object e) {
                    iterator.set e
                }

                @Override
                public void add(Object e) {
                    iterator.add e
                    size++
                }
            }
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public NBTSubList subList(int fromIndex, int toIndex) {
            new NBTSubList(this, fromIndex, toIndex)
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            "Index: $index, Size: $size"
        }
    }

    @Override
    public String toString() {
        NBTIterator itr = iterator()
        if (! itr.hasNext()) return '[]'
        StringBuilder sb = new StringBuilder()
        sb << '['
        for (;;) {
            sb << itr.next()
            if (! itr.hasNext()) return sb << ']' as String
            sb << ', '
        }
    }

}
