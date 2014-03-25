package ru.dpohvar.varscript.utils;

public class EnumUtils {

    /**
     * Get enum by name
     * @param enums enums
     * @param name name
     * @param <T> enum class
     * @return matched enum or null
     */
    public static <T extends Enum<T>> T match(T[] enums, String name) {
        for (T e: enums) if (e.name().equals(name)) return e;
        for (T e: enums) if (e.name().equalsIgnoreCase(name)) return e;
        for (T e: enums) if (e.name().startsWith(name)) return e;
        for (T e: enums) if (e.name().toLowerCase().startsWith(name.toLowerCase())) return e;
        return null;
    }

    public static <T extends Enum<T>> T match(Class<T> clazz, String name) {
        try{
            @SuppressWarnings("unchecked")
            T[] enums = (T[]) clazz.getMethod("values").invoke(null);
            return match(enums, name);
        } catch (Exception e) {
            return null;
        }
    }
}
