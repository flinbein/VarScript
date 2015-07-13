package ru.dpohvar.varscript.extension;

import org.bukkit.util.Vector;

public class VectorExt {

    public static Vector add(Vector self, double x, double y, double z) {
        self.setX(self.getX() + x);
        self.setY(self.getY() + y);
        self.setZ(self.getZ() + z);
        return self;
    }

    public static Vector plus(Vector self, Vector val) {
        return self.clone().add(val);
    }

    public static Vector minus(Vector self, Vector val) {
        return self.clone().subtract(val);
    }

    public static Vector div(Vector self, Vector val) {
        return self.clone().divide(val);
    }

    public static Vector div(Vector self, Double val) {
        return self.clone().multiply(1 / val);
    }

    public static Vector div(Vector self, int val) {
        return self.clone().multiply(1.0/val);
    }

    public static Vector multiply(Vector self, Vector val) {
        return self.clone().multiply(val);
    }

    public static Vector mul(Vector self, Vector val) {
        return self.clone().multiply(val);
    }

    public static Vector multiply(Vector self, int val) {
        return self.clone().multiply(val);
    }

    public static Vector mul(Vector self, int val) {
        return self.clone().multiply(val);
    }

    public static Vector multiply(Vector self, double val) {
        return self.clone().multiply(val);
    }

    public static Vector mul(Vector self, double val) {
        return self.clone().multiply(val);
    }

    public static Vector multiply(Vector self, float val) {
        return self.clone().multiply(val);
    }

    public static Vector mul(Vector self, float val) {
        return self.clone().multiply(val);
    }

    public static Vector power(Vector self, Vector val) {
        return self.clone().crossProduct(val);
    }

    public static Vector cross(Vector self, Vector val) {
        return self.clone().crossProduct(val);
    }

    public static Vector cross(Vector self, double x, double y, double z) {
        return self.clone().multiply(new Vector(x,y,z));
    }

    public static Vector getNorm(Vector self) {
        return self.clone().normalize();
    }

    public static Vector mod(Vector self, double val) {
        return self.clone().normalize().multiply(val);
    }

    public static double or(Vector self, Vector val) {
        return self.distance(val);
    }

    public static float xor(Vector self, Vector val) {
        return self.angle(val);
    }

    public static int getBx(Vector self) {
        return self.getBlockX();
    }

    public static int getBy(Vector self) {
        return self.getBlockY();
    }

    public static int getBz(Vector self) {
        return self.getBlockZ();
    }

    public static double getYawRad(Vector vector){
        return Math.atan2(-vector.getBlockX(),vector.getZ());
    }

    public static double getYaw(Vector vector){
        return getYawRad(vector) / Math.PI * 180;
    }

    public static double getPitchRad(Vector vector){
        double len = Math.sqrt(vector.getX()*vector.getX()+vector.getZ()*vector.getZ());
        return Math.atan2(-vector.getY(),len);
    }

    public static double getPitch(Vector vector){
        return getPitchRad(vector) / Math.PI * 180;
    }

    public static void setYawRad(Vector vector, double rad){
        double len = Math.sqrt(vector.getX()*vector.getX()+vector.getZ()*vector.getZ());
        vector.setX(-Math.sin(rad)*len);
        vector.setZ(Math.cos(rad)*len);
    }

    public static void setYaw(Vector vector, double deg){
        setYawRad(vector, deg / 180 * Math.PI);
    }

    public static void setPitchRad(Vector vector, double rad){
        double horLength = Math.sqrt(vector.getX()*vector.getX()+vector.getZ()*vector.getZ());
        double verLength = Math.sqrt(horLength*horLength+vector.getY()*vector.getY());
        vector.setY(-Math.sin(rad)*verLength);
        horLength = Math.cos(rad)*verLength;
        vector.setX(vector.getX()*horLength);
        vector.setZ(vector.getZ()*horLength);
    }

    public static void setPitch(Vector vector, double deg){
        setPitchRad(vector, deg / 180 * Math.PI);
    }


    public static double getLen(Vector self) {
        return self.length();
    }

    public static Vector setLen(Vector self, double len) {
        return self.normalize().multiply(len);
    }
}
