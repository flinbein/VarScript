package ru.dpohvar.varscript.extension;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class CollectionExt {

    public static <T> T rnd(List<T> self) {
        if (self.isEmpty()) return null;
        int index = (int) ( Math.random() * self.size() );
        return self.get(index);
    }

    public static <T> T rnd(Collection<T> self) {
        if (self.isEmpty()) return null;
        int index = (int) ( Math.random() * self.size() );
        Iterator<T> iterator = self.iterator();
        while (index --> 0) iterator.next();
        return iterator.next();
    }

    public static <T> T popRnd(List<T> self) {
        if (self.isEmpty()) return null;
        int index = (int) ( Math.random() * self.size() );
        return self.remove(index);
    }

    public static <T> T popRnd(Collection<T> self) {
        if (self.isEmpty()) return null;
        int index = (int) ( Math.random() * self.size() );
        Iterator<T> iterator = self.iterator();
        while (index --> 0) iterator.next();
        T result = iterator.next();
        iterator.remove();
        return result;
    }

    public static <T> List<T> rnd(Collection<T> self, int size) {
        List<T> result = new ArrayList<T>();
        List<T> temp = new ArrayList<T>(self);
        while(size --> 0 && temp.size() > 0) {
            result.add(popRnd(temp));
        }
        return result;
    }

    public static <T extends List> T shuffle(T self) {
        Collections.shuffle(self);
        return self;
    }

    public static <T> int count(Collection<T> self, T object) {
        return Collections.frequency(self, object);
    }

}
