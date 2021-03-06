package com.jamieadkins.heistr.BuildObjects;

import com.jamieadkins.heistr.utils.Maths;

import java.util.ArrayList;

/**
 * Created by Jamie on 20/09/2015.
 */
public class Weapon {

    public static final int DAMAGE = 0;
    public static final int ACCURACY = 0;
    public static final int STABILITY = 0;
    public static final int CONCEALMENT = 0;
    public static final int THREAT = 0;

    long id = -1;
    long pd2skillsID = -1;
    String name = "Prim";
    String weaponName = "Prim2";
    String pd2 = "wpn";
    int weaponType = WeaponBuild.PRIMARY;
    int weaponSubType = -1;

    int rateOfFire = 0;
    int totalAmmo = 0;
    int magSize = 0;
    float damage = 0;
    float accuracy = 0;
    float stability = 0;
    int concealment = 0;
    int threat = 0;
    String firemode = "auto";
    ArrayList<String> possibleAttachmentIDs;
    ArrayList<Long> attachmentIDs;
    ArrayList<Attachment> attachments;

    public String getWeaponName() {
        return weaponName;
    }

    public void setWeaponName(String weaponName) {
        this.weaponName = weaponName;
    }

    public int getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(int weaponType) {
        this.weaponType = weaponType;
    }

    public int getWeaponSubType() {
        return weaponSubType;
    }

    public void setWeaponSubType(int weaponSubType) {
        this.weaponSubType = weaponSubType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPd2skillsID() {
        return pd2skillsID;
    }

    public void setPd2skillsID(long pd2skillsID) {
        this.pd2skillsID = pd2skillsID;
    }

    public String getFiremode() {
        return firemode;
    }

    public void setFiremode(String firemode) {
        this.firemode = firemode;
    }

    public int getROF() {
        return rateOfFire;
    }

    public int getAttachmentRof() {
        return 0;
    }

    public int getSkillRof() {
        return 0;
    }

    public void setROF(int rateOfFire) {
        this.rateOfFire = rateOfFire;
    }

    public int getBaseAmmo() {
        return totalAmmo;
    }

    public int getAttachmentAmmo() {
        int magSize = 0;
        for (Attachment attachment : attachments) {
            magSize += attachment.getTotalAmmo();
        }
        return magSize;
    }

    public int getSkillAmmo(SkillStatChangeManager skillStatChangeManager) {
        return getTotalAmmo(skillStatChangeManager) - getBaseAmmo() - getAttachmentAmmo();
    }

    public int getTotalAmmo(SkillStatChangeManager skillStatChangeManager) {
        float ammoAdj = totalAmmo + getAttachmentAmmo();

        float mult = 1;

        for (SkillStatModifier modifier : skillStatChangeManager.getModifiers()) {
            for (int weaponType : modifier.getWeaponTypes()) {
                if (doWeaponTypesMatch(weaponType)) {
                    mult += modifier.getAmmoMult() - 1;
                }
            }
        }

        return (int) Maths.round(ammoAdj * mult);
    }

    public void setTotalAmmo(int totalAmmo) {
        this.totalAmmo = totalAmmo;
    }

    public int getBaseMagSize() {
        return magSize;
    }

    public int getAttachmentMagSize() {
        int magSize = 0;
        for (Attachment attachment : attachments) {
            magSize += attachment.getMagsize();
        }
        return magSize;
    }

    public int getSkillMagSize(SkillStatChangeManager skillStatChangeManager) {
        int magSize = 0;
        for (SkillStatModifier modifier : skillStatChangeManager.getModifiers()) {
            for (int weaponType : modifier.getWeaponTypes()) {
                if (doWeaponTypesMatch(weaponType)) {
                    magSize += modifier.getMag();
                }
            }
        }
        return magSize;
    }

    public int getMagSize(SkillStatChangeManager skillStatChangeManager) {
        return magSize + getAttachmentMagSize() + getSkillMagSize(skillStatChangeManager);
    }

    public void setMagSize(int magSize) {
        this.magSize = magSize;
    }

    public float getBaseDamage() {
        return damage;
    }

    public float getAttachmentDamage() {
        float damageAdj = 0;
        for (Attachment attachment : attachments) {
            damageAdj += attachment.getDamage();
        }
        return damageAdj;
    }

    public float getSkillDamage(SkillStatChangeManager skillStatChangeManager) {
        return Maths.round(getDamage(skillStatChangeManager) - damage - getAttachmentDamage());
    }

    public float getDamage(SkillStatChangeManager skillStatChangeManager) {
        float damageAdj = damage + getAttachmentDamage();

        float mult = 1;

        for (SkillStatModifier modifier : skillStatChangeManager.getModifiers()) {
            for (int weaponType : modifier.getWeaponTypes()) {
                if (doWeaponTypesMatch(weaponType)) {
                    damageAdj += modifier.getDamage();
                    mult += modifier.getDamageMult() - 1;
                }
            }
        }

        return Maths.round(damageAdj * mult);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getBaseAccuracy() {
        return accuracy;
    }

    public float getAttachmentAccuracy() {
        float acc = 0;
        for (Attachment attachment : attachments) {
            acc += attachment.getAccuracy();
        }
        return acc;
    }

    public float getSkillAccuracy(SkillStatChangeManager skillStatChangeManager) {
        int skills = 0;

        for (SkillStatModifier modifier : skillStatChangeManager.getModifiers()) {
            for (int weaponType : modifier.getWeaponTypes()) {
                if (doWeaponTypesMatch(weaponType)) {
                    skills += modifier.getAccuracy();
                }
            }
        }

        return skills;
    }

    public float getAccuracy(SkillStatChangeManager skillStatChangeManager) {
        float acc = accuracy + getAttachmentAccuracy() + getSkillAccuracy(skillStatChangeManager);
        return acc;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getBaseStability() {
        return stability;
    }

    public float getAttachmentStability() {
        float stab = 0;
        for (Attachment attachment : attachments) {
            stab += attachment.getStability();
        }
        return stab;
    }

    public float getSkillStability(SkillStatChangeManager skillStatChangeManager) {
        int skills = 0;

        for (SkillStatModifier modifier : skillStatChangeManager.getModifiers()) {
            for (int weaponType : modifier.getWeaponTypes()) {
                if (doWeaponTypesMatch(weaponType)) {
                    skills += modifier.getStability();
                }
            }
        }

        return skills;
    }

    public float getStability(SkillStatChangeManager skillStatChangeManager) {
        float stab = stability + getAttachmentStability() + getSkillStability(skillStatChangeManager);
        return stab;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setStability(float stability) {
        this.stability = stability;
    }

    public int getBaseConcealment() {
        return concealment;
    }

    public int getAttachmentConcealment() {
        int con = 0;
        for (Attachment attachment : attachments) {
            con += attachment.getConcealment();
        }
        return con;
    }

    public int getSkillConcealment(SkillStatChangeManager skillStatChangeManager) {
        int con = 0;
        for (SkillStatModifier modifier : skillStatChangeManager.getModifiers()) {
            for (int weaponType : modifier.getWeaponTypes()) {
                if (doWeaponTypesMatch(weaponType)) {
                    con += modifier.getConcealment();
                }
            }
        }
        return con;
    }

    public int getConcealment(SkillStatChangeManager skillStatChangeManager) {
        return concealment + getAttachmentConcealment() + getSkillConcealment(skillStatChangeManager);
    }

    public void setConcealment(int concealment) {
        this.concealment = concealment;
    }

    public int getBaseThreat() {
        return threat;
    }

    public int getThreat() {
        int t = 0;
        t += threat;
        for (Attachment attachment : attachments) {
            t += attachment.getThreat();
        }
        return t;
    }

    public void setThreat(int threat) {
        this.threat = threat;
    }

    public ArrayList<String> getPossibleAttachments() {
        return possibleAttachmentIDs;
    }

    public void setPossibleAttachments(ArrayList<String> possibleAttachments) {
        this.possibleAttachmentIDs = possibleAttachments;
    }

    public ArrayList<Long> getAttachmentIDs() {
        return attachmentIDs;
    }

    public void setAttachmentIDs(ArrayList<Long> attachments) {
        this.attachmentIDs = attachments;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getPd2() {
        return pd2;
    }

    public void setPd2(String pd2) {
        this.pd2 = pd2;
    }

    public boolean isSilenced() {
        for (Attachment attachment : attachments) {
            if (attachment.getSubtype().equals("silencer")) {
                return true;
            }
        }

        return false;
    }

    public boolean isSingleShot() {
        if (firemode.equals("single")) {
            return true;
        }

        for (Attachment attachment : attachments) {
            if (attachment.getPd2().equals("wpn_fps_upg_i_singlefire")) {
                return true;
            }
        }

        return false;
    }

    private boolean doWeaponTypesMatch(int attachmentType) {
        if (weaponSubType == attachmentType || attachmentType == SkillStatChangeManager.WEAPON_TYPE_ALL) {
            return true;
        }

        if (attachmentType == SkillStatChangeManager.WEAPON_TYPE_SILENCED && isSilenced()) {
            return true;
        }

        if (attachmentType == SkillStatChangeManager.WEAPON_TYPE_SINGLE_SHOT && isSingleShot()) {
            return true;
        }

        return false;
    }
}
