package ru.dpohvar.varscript.extension

import org.bukkit.util.Vector

import java.lang.reflect.Method

/**
 * Created by DPOH-VAR on 06.03.14
 */

@SuppressWarnings("GroovyUnusedDeclaration")
class VectorExtension<Z extends Vector> {

    public static Z add(Z self, double x, double y, double z) {
        self.setX(self.getX() + x)
        self.setY(self.getY() + x)
        self.setZ(self.getZ() + x)
        self
    }

    public static Vector plus(Vector self, Vector val) {
        self.clone().add val
    }

    public static Vector minus(Vector self, Vector val) {
        self.clone().subtract val
    }

    public static Vector div(Vector self, Vector val) {
        self.clone().divide val
    }

    private static Method m_multiply_v = Vector.getMethod "multiply", Vector

    public static Vector multiply(Vector self, Vector val) {
        def result = m_multiply_v.invoke self.clone(), val
        result as Vector
    }

    public static Vector mul(Vector self, Vector val) {
        def result = m_multiply_v.invoke self, val
        result as Vector
    }

    private static Method m_multiply_i = Vector.getMethod "multiply", int

    public static Vector multiply(Vector self, int val) {
        def result = m_multiply_i.invoke self.clone(), val
        result as Vector
    }

    public static Vector mul(Vector self, int val) {
        def result = m_multiply_i.invoke self, val
        result as Vector
    }

    private static Method m_multiply_d = Vector.getMethod "multiply", double

    public static Vector multiply(Vector self, double val) {
        def result = m_multiply_d.invoke self.clone(), val
        result as Vector
    }

    public static Vector mul(Vector self, double val) {
        def result = m_multiply_d.invoke self, val
        result as Vector
    }

    private static Method m_multiply_f = Vector.getMethod "multiply", float

    public static Vector multiply(Vector self, float val) {
        def result = m_multiply_f.invoke self.clone(), val
        result as Vector
    }

    public static Vector mul(Vector self, float val) {
        def result = m_multiply_f.invoke self, val
        result as Vector
    }

    public static Vector power(Vector self, Vector val) {
        self.clone().crossProduct val
    }

    public static Vector cross(Vector self, Vector val) {
        self.clone().crossProduct val
    }

    public static Vector cross(Vector self, double x, double y, double z) {
        self.clone().crossProduct new Vector(x, y, z)
    }

    public static Vector getNorm(Vector self) {
        self.clone().normalize()
    }

    public static Vector mod(Vector self, double val) {
        def result = m_multiply_d.invoke self.clone().normalize(), val
        result as Vector
    }

    public static double or(Vector self, Vector val) {
        self.distance val
    }

    public static float xor(Vector self, Vector val) {
        self.angle val
    }

    public static int getBx(Vector self) {
        self.blockX
    }

    public static int getBy(Vector self) {
        self.blockY
    }

    public static int getBz(Vector self) {
        self.blockZ
    }

    public static double getLen(Vector self) {
        self.length()
    }

    public static Z setLen(Z self, double len) {
        self.normalize().multiply len
        self
    }


}
