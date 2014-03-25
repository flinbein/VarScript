package ru.dpohvar.varscript.groovy.extension

import groovy.transform.CompileStatic
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity

/**
 * Created by DPOH-VAR on 06.03.14
 */

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
class DamageableExtension<D extends Damageable> {

    /**
     * get entity's health
     * @param self
     * @return health
     */
    public static double getHp(D self){
        self.health
    }

    /**
     * set entity's health
     * @param self
     * @param hp health
     * @return self
     */
    public static D setHp(D self, double hp){
        if (hp > self.maxHealth) hp = self.maxHealth
        self.setHealth(hp)
        self
    }

    /**
     * kill entity
     * @param self
     * @return self
     */
    public static D kill(D self){
        self.health = 0
        self
    }

    /**
     * damage entity
     * @param self
     * @param hp amount of damage to deal
     * @param entity entity which to attribute this damage from
     * @return self
     */
    public static D dmg(D self, double hp, Entity entity=null){
        self.damage hp, entity
        self
    }

    /**
     * damage entity
     * @param self
     * @param hp amount of damage to deal
     * @return self
     */
    public static D setDmg(D self, double hp){
        dmg self, hp
    }

    /**
     * gets the maximum health this entity has.
     * @param self
     * @return maximum health
     */
    public static double getMaxHp(D self){
        self.maxHealth
    }

    /**
     * sets the maximum health this entity can have
     * @param self
     * @param hp amount of health to set the maximum to
     * @return self
     */
    public static D setMaxHp(D self, double hp){
        self.setMaxHealth(hp)
        self
    }

    /**
     * resets the max health to the original amount
     * @param self
     * @return self
     */
    public static D resetMaxHp(D self){
        self.resetMaxHealth()
        self
    }

    /**
     * set health of entity to maximum
     * @param self
     * @return self
     */
    public static D heal(D self){
        self.setHealth(self.maxHealth)
        self
    }

}
