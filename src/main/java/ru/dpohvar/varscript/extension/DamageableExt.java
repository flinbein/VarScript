package ru.dpohvar.varscript.extension;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

public class DamageableExt {

    public static double getHp(Damageable self) {
        return self.getHealth();
    }

    public static void setHp(Damageable self, double hp) {
        if (hp > self.getMaxHealth()) hp = self.getMaxHealth();
        self.setHealth(hp);
    }

    public static <D extends Damageable> D kill(D self) {
        self.setHealth(0);
        return self;
    }

    public static <D extends Damageable> D dmg(D self, double hp, Entity damager) {
        self.damage(hp, damager);
        return self;
    }

    public static <D extends Damageable> D dmg(D self, double hp) {
        self.damage(hp);
        return self;
    }

    public static double getMaxhp(Damageable self) {
        return self.getMaxHealth();
    }

    public static void setMaxhp(Damageable self, double hp) {
        self.setMaxHealth(hp);
    }

    public static <D extends Damageable> D resetMaxhp(D self) {
        self.resetMaxHealth();
        return self;
    }

    public static <D extends Damageable> D heal(D self) {
        self.setHealth(self.getMaxHealth());
        return self;
    }

    public static <D extends Damageable> D heal(D self, double val) {
        double hp = self.getHealth() + val;
        setHp(self, hp);
        return self;
    }

}
