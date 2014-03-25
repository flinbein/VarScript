package ru.dpohvar.varscript.utils;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by DPOH-VAR on 01.03.14
 */
public class StringSetUtils {

    public static Set<String> reduceByPattern (Set<String> values, String pattern) {
        Iterator<String> itr = values.iterator();
        while (itr.hasNext()) {
            if (!itr.next().startsWith(pattern)) itr.remove();
        }
        return values;
    }
}
