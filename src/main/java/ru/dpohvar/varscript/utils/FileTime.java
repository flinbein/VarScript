package ru.dpohvar.varscript.utils;

import java.io.File;

public class FileTime {

    public final File file;
    public final Long time;

    public FileTime(File file) {
        this.file = file;
        this.time = file.lastModified();
    }

    @Override
    public int hashCode() {
        return file.hashCode() ^ time.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileTime &&
                ((FileTime) obj).file.equals(file) &&
                ((FileTime) obj).time.equals(time);
    }

    @Override
    public String toString() {
        return file + ":" + time;
    }
}
