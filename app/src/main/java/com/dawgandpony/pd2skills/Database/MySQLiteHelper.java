package com.dawgandpony.pd2skills.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dawgandpony.pd2skills.BuildObjects.Attachment;

/**
 * Created by Jamie on 15/07/2015.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pd2skills.db";
    private static final int DATABASE_VERSION = 22;

    //region Skills
    public static final String TABLE_SKILL_BUILDS = "tbSkillBuilds";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";

    private static final String CREATE_SKILL_BUILD_TABLE = "create table if not exists "
            + TABLE_SKILL_BUILDS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NAME
            + " text);";

    public static final String TABLE_SKILL_TIERS = "tbSkillTrees";
    public static final String COLUMN_SKILL_BUILD_ID = "skillBuildID";
    public static final String COLUMN_TREE = "tree";
    public static final String COLUMN_TIER = "tier";

    public static final String[] COLUMNS_SKILLS = new String[]{"skill1",
            "skill2",
            "skill3"};

    private static final String CREATE_SKILL_TIER_TABLE = "create table if not exists "
            + TABLE_SKILL_TIERS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_SKILL_BUILD_ID
            + " integer," + COLUMN_TREE
            + " integer," + COLUMN_TIER
            + " integer," + COLUMNS_SKILLS[0]
            + " integer," + COLUMNS_SKILLS[1]
            + " integer," + COLUMNS_SKILLS[2]
            + " integer);";
    //endregion

    //region Builds
    public static final String TABLE_BUILDS = "tbBuilds";
    public static final String COLUMN_WEAPON_BUILD_ID = "weaponBuildID";
    public static final String COLUMN_INFAMY_ID = "infamy";
    public static final String COLUMN_PERK_DECK = "perkdeck";
    public static final String COLUMN_ARMOUR = "armour";

    private static final String CREATE_BUILD_TABLE = "create table if not exists "
            + TABLE_BUILDS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NAME
            + " text, "  + COLUMN_SKILL_BUILD_ID
            + " integer, " + COLUMN_WEAPON_BUILD_ID
            + " integer, " + COLUMN_INFAMY_ID
            + " integer, " + COLUMN_PERK_DECK
            + " integer, " + COLUMN_ARMOUR
            + " integer);";
    //endregion

    //region Weapons
    public static final String TABLE_WEAPON_BUILDS = "tbWeaponBuilds";
    public static final String COLUMN_PRIMARY_WEAPON = "primaryW";
    public static final String COLUMN_SECONDARY_WEAPON = "secondaryW";
    public static final String COLUMN_MELEE_WEAPON = "meleeW";

    private static final String CREATE_WEAPON_BUILD_TABLE = "create table if not exists "
            + TABLE_WEAPON_BUILDS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_PRIMARY_WEAPON
            + " integer, " + COLUMN_SECONDARY_WEAPON
            + " integer, " + COLUMN_MELEE_WEAPON
            + " integer);";

    public static final String TABLE_WEAPONS = "tbWeapons";
    public static final String COLUMN_WEAPON_TYPE = "weaponType";
    public static final String COLUMN_PD2SKILLS_ID= "pd2skillsID";
    public static final String[] COLUMNS_ATTACHMENTS = new String[]{"barrel", "barrelExt",
            "bayonet", "modCustom", "modExtra", "foregrip", "gadget", "grip", "lReceiver",
            "magazine", "sight", "slide", "stock", "suppressor", "uReceiver"};


    private static final String CREATE_WEAPON_TABLE = "create table if not exists "
            + TABLE_WEAPONS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_PD2SKILLS_ID
            + " integer," + COLUMN_WEAPON_TYPE
            + " integer," + COLUMN_NAME
            + " text," + COLUMNS_ATTACHMENTS[Attachment.MOD_BARREL]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_BARREL_EXTENSION]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_BAYONET]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_CUSTOM]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_EXTRA]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_FOREGRIP]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_GADGET]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_GRIP]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_LOWER_RECEIVER]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_MAGAZINE]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_SIGHT]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_SLIDE]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_STOCK]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_SUPPRESSOR]
            + " integer," + COLUMNS_ATTACHMENTS[Attachment.MOD_UPPER_RECEIVER]
            + " integer);";
    //endregion



    //region Infamies
    public static final String TABLE_INFAMY = "tbInfamy";
    public static final String COLUMN_INFAMY_MASTERMIND = "mastermind";
    public static final String COLUMN_INFAMY_ENFORCER = "enforcer";
    public static final String COLUMN_INFAMY_TECHNICIAN = "technician";
    public static final String COLUMN_INFAMY_GHOST = "ghost";

    private static final String CREATE_INFAMY_TABLE = "create table if not exists "
            + TABLE_INFAMY + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_INFAMY_MASTERMIND
            + " integer, "  + COLUMN_INFAMY_ENFORCER
            + " integer," + COLUMN_INFAMY_TECHNICIAN
            + " integer," + COLUMN_INFAMY_GHOST
            + " integer);";
    //endregion


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SKILL_BUILD_TABLE);
        db.execSQL(CREATE_SKILL_TIER_TABLE);
        db.execSQL(CREATE_BUILD_TABLE);
        db.execSQL(CREATE_INFAMY_TABLE);
        db.execSQL(CREATE_WEAPON_BUILD_TABLE);
        db.execSQL(CREATE_WEAPON_TABLE);
        InitInfamies(db);
        //AddBaseWeapons(db);
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy some old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUILDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SKILL_BUILDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SKILL_TIERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFAMY);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEAPON_BUILDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEAPONS);
        onCreate(db);
    }

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private void InitInfamies(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 2; j++){
                for (int k = 0; k < 2; k++){
                    for (int l = 0; l < 2; l++){
                        values.put(COLUMN_INFAMY_MASTERMIND, i);
                        values.put(COLUMN_INFAMY_ENFORCER, j);
                        values.put(COLUMN_INFAMY_TECHNICIAN, k);
                        values.put(COLUMN_INFAMY_GHOST, l);
                        db.insert(TABLE_INFAMY, null, values);
                    }
                }
            }
        }
    }

    private void AddBaseWeapons(SQLiteDatabase db) {
        long[] weaponIDs = new long[3];

        for (int weapon = 0; weapon < 3; weapon++){

            ContentValues values = new ContentValues();

            for (String weaponColumn : DataSourceWeapons.getWeaponColumns()) {
                values.put(weaponColumn, -1);
            }
            values.remove(MySQLiteHelper.COLUMN_ID);
            values.put(MySQLiteHelper.COLUMN_WEAPON_TYPE, weapon);
            switch ( weapon){
                case 0:
                    values.put(MySQLiteHelper.COLUMN_NAME, "example");
                    values.put(MySQLiteHelper.COLUMN_PD2SKILLS_ID, 10);
                    break;
                case 1:
                    values.put(MySQLiteHelper.COLUMN_NAME, "example");
                    values.put(MySQLiteHelper.COLUMN_PD2SKILLS_ID, 25);
                    break;
                case 2:
                    values.put(MySQLiteHelper.COLUMN_NAME, "example");
                    values.put(MySQLiteHelper.COLUMN_PD2SKILLS_ID, 100000);
                    break;
            }

            weaponIDs[weapon] = db.insert(MySQLiteHelper.TABLE_WEAPONS, null, values);
            Log.d("Weapon inserted DB", weaponIDs[weapon] + "");
        }

    }


}
