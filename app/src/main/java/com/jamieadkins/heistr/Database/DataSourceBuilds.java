package com.jamieadkins.heistr.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jamieadkins.heistr.BuildObjects.Attachment;
import com.jamieadkins.heistr.BuildObjects.Build;
import com.jamieadkins.heistr.BuildObjects.MeleeWeapon;
import com.jamieadkins.heistr.BuildObjects.SkillBuild;
import com.jamieadkins.heistr.BuildObjects.Weapon;
import com.jamieadkins.heistr.BuildObjects.WeaponBuild;
import com.jamieadkins.heistr.Consts.Trees;
import com.jamieadkins.heistr.utils.URLEncoder;

import java.util.ArrayList;

/**
 * Created by Jamie on 16/07/2015.
 */
public class DataSourceBuilds {


    private Context context;
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] buildColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_NAME,
            MySQLiteHelper.COLUMN_SKILL_BUILD_ID,
            MySQLiteHelper.COLUMN_WEAPON_BUILD_ID,
            MySQLiteHelper.COLUMN_INFAMY_ID,
            MySQLiteHelper.COLUMN_PERK_DECK,
            MySQLiteHelper.COLUMN_ARMOUR};

    ArrayList<Weapon> baseWeaponInfo;
    ArrayList<MeleeWeapon> meleeWeaponInfo;
    ArrayList<Attachment> baseAttachmentInfo;
    ArrayList<Attachment> overrideAttachmentInfo;

    public DataSourceBuilds(Context context) {
        dbHelper = new MySQLiteHelper(context);
        this.context = context;
        this.baseWeaponInfo = WeaponBuild.getWeaponsFromXML(context.getResources());
        this.meleeWeaponInfo = MeleeWeapon.getMeleeWeaponsFromXML(context.getResources());
        this.baseAttachmentInfo = Attachment.getAttachmentsFromXML(context.getResources());
        this.overrideAttachmentInfo = Attachment.getAttachmentOverrides(context.getResources());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();

    }

    public Build createAndInsertBuild(String name, int infamies, String url, long templateID) {
        Log.d("Build DB", "Creating new build");
        int perkDeck = 0;
        int armour = 0;

        Build template = null;
        template = URLEncoder.decodeURL(context, url);
        if (template == null) {
            if (templateID > 0) {
                template = getBuild(templateID);
            }
        }

        DataSourceSkills dataSourceSkills = new DataSourceSkills(context);
        DataSourceWeapons dataSourceWeapons = new DataSourceWeapons(context);
        dataSourceSkills.open();
        dataSourceWeapons.open();
        long skillBuildID;
        long weaponBuildID;
        if (template != null) {
            long templateSkillBuildID = template.getSkillBuild().getId();
            long templateWeaponBuildID = template.getWeaponBuild().getId();

            if (templateSkillBuildID == Build.PD2SKILLS) {
                //PD2skills URL
                skillBuildID = dataSourceSkills.createAndInsertSkillBuild(template.getSkillBuild()).getId();
            } else {
                //template
                skillBuildID = dataSourceSkills.createAndInsertSkillBuild(templateSkillBuildID).getId();
            }

            if (templateWeaponBuildID == Build.PD2SKILLS) {
                //TODO: When Pd2skill URL is enetered
                weaponBuildID = dataSourceWeapons.createAndInsertWeaponBuild().getId();
            } else {
                //TODO: template selected
                weaponBuildID = dataSourceWeapons.createAndInsertWeaponBuild().getId();
            }

            if (infamies < template.getInfamyID() || url.length() > 0) {
                infamies = (int) template.getInfamyID();
            }
            perkDeck = template.getPerkDeck();
            armour = template.getArmour();
        } else {
            skillBuildID = dataSourceSkills.createAndInsertSkillBuild().getId();
            weaponBuildID = dataSourceWeapons.createAndInsertWeaponBuild().getId();
        }
        dataSourceSkills.close();
        dataSourceWeapons.close();

        ContentValues buildValues = new ContentValues();
        buildValues.put(MySQLiteHelper.COLUMN_NAME, name);
        buildValues.put(MySQLiteHelper.COLUMN_SKILL_BUILD_ID, skillBuildID);
        buildValues.put(MySQLiteHelper.COLUMN_WEAPON_BUILD_ID, weaponBuildID);
        buildValues.put(MySQLiteHelper.COLUMN_INFAMY_ID, infamies);
        buildValues.put(MySQLiteHelper.COLUMN_PERK_DECK, perkDeck);
        buildValues.put(MySQLiteHelper.COLUMN_ARMOUR, armour);


        long buildID = database.insert(MySQLiteHelper.TABLE_BUILDS, null, buildValues);


        Cursor cursorBuild = database.query(MySQLiteHelper.TABLE_BUILDS,
                buildColumns, MySQLiteHelper.COLUMN_ID + " = " + buildID, null,
                null, null, null);
        cursorBuild.moveToFirst();

        Build newBuild = cursorToBuild(cursorBuild);
        cursorBuild.close();


        Log.d("Build DB", "Build created: " + newBuild.getId() + "");
        return newBuild;


    }


    public Build getBuild(long id) {
        Build build;

        Cursor cursorBuild = database.query(MySQLiteHelper.TABLE_BUILDS,
                buildColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null,
                null, null, null);

        if (cursorBuild.moveToFirst()) {

            build = cursorToBuild(cursorBuild);
            cursorBuild.close();


        } else {
            cursorBuild.close();
            build = null;
        }


        return build;
    }

    public ArrayList<Build> getAllBuilds() {
        ArrayList<Build> builds = new ArrayList<>();

        Cursor cursorBuild = database.query(MySQLiteHelper.TABLE_BUILDS,
                buildColumns, null, null,
                null, null, null);


        if (cursorBuild.moveToFirst()) {

            while (!cursorBuild.isAfterLast()) {
                Build build = cursorToBuild(cursorBuild);
                builds.add(build);
                cursorBuild.moveToNext();
            }


        }

        cursorBuild.close();

        Log.d("DB", "Got all builds for db, there were " + builds.size());
        return builds;


    }

    public void DeleteBuild(long id) {
        database.delete(MySQLiteHelper.TABLE_BUILDS, MySQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public void updateInfamy(long buildID, long infamyID) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_INFAMY_ID, infamyID);

        database.update(MySQLiteHelper.TABLE_BUILDS, values, MySQLiteHelper.COLUMN_ID + " = " + buildID, null);
        Log.d("DB", "Infamy updated for build " + buildID + " to " + infamyID);
    }

    public void updatePerkDeck(long buildID, long selected) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PERK_DECK, selected);

        database.update(MySQLiteHelper.TABLE_BUILDS, values, MySQLiteHelper.COLUMN_ID + " = " + buildID, null);
        Log.d("DB", "Perk Deck updated for build " + buildID + " to " + selected);
    }

    public void updateArmour(long buildID, long selected) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ARMOUR, selected);

        database.update(MySQLiteHelper.TABLE_BUILDS, values, MySQLiteHelper.COLUMN_ID + " = " + buildID, null);
        Log.d("DB", "Armour updated for build " + buildID + " to " + selected);
    }

    private Build cursorToBuild(Cursor cursorBuild) {

        Build build = new Build();
        long buildID = cursorBuild.getLong(0);
        String name = cursorBuild.getString(1);
        long skillBuildID = cursorBuild.getLong(2);
        long weaponBuildID = cursorBuild.getLong(3);
        long infamyID = cursorBuild.getLong(4);
        int perkDeck = cursorBuild.getInt(5);
        int armour = cursorBuild.getInt(6);


        build.setId(buildID);
        build.setName(name);
        ArrayList<Boolean> infamyList = DataSourceInfamies.idToInfamy(infamyID);
        build.setInfamies(infamyList);
        build.setPerkDeck(perkDeck);
        build.setArmour(armour);


        SkillBuild skillBuildFromXML = SkillBuild.getSkillBuildFromXML(context.getResources());
        SkillBuild skillBuildFromDB = SkillBuild.getSkillBuildFromDB(skillBuildID, context);
        SkillBuild mergedSkillBuild = SkillBuild.mergeBuilds(skillBuildFromXML, skillBuildFromDB);

        int count = Trees.MASTERMIND;
        for (boolean infamy : infamyList) {
            mergedSkillBuild.setInfamyBonus(count, infamy);
            count++;

            if (infamy) {
                mergedSkillBuild.setInfamyBonus(Trees.FUGITIVE, true);
            }
        }

        build.setWeaponsFromXML(baseWeaponInfo);
        build.setMeleeWeaponsFromXML(meleeWeaponInfo);
        build.setAttachmentsFromXML(baseAttachmentInfo);
        build.setAttachmentsOverridesFromXML(overrideAttachmentInfo);

        DataSourceWeapons dataSourceWeapons = new DataSourceWeapons(context);
        dataSourceWeapons.open();
        WeaponBuild weaponBuildFromDB = dataSourceWeapons.getWeaponBuild(weaponBuildID);
        dataSourceWeapons.close();

        build.setSkillBuild(mergedSkillBuild);
        build.setWeaponBuild(weaponBuildFromDB);


        return build;
    }


    public void renameBuild(long id, String newName) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_NAME, newName);

        database.update(MySQLiteHelper.TABLE_BUILDS, values, MySQLiteHelper.COLUMN_ID + " = " + id, null);
        Log.d("DB", "Name updated for build " + id + " to " + newName);
    }

    public void updateWeaponBuild(long id, int weaponType, long weaponID) {
        ContentValues values = new ContentValues();
        switch (weaponType) {
            case WeaponBuild.PRIMARY:
                values.put(MySQLiteHelper.COLUMN_PRIMARY_WEAPON, weaponID);
                break;
            case WeaponBuild.SECONDARY:
                values.put(MySQLiteHelper.COLUMN_SECONDARY_WEAPON, weaponID);
                break;
        }

        database.update(MySQLiteHelper.TABLE_WEAPON_BUILDS, values, MySQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public void updateMeleeWeapon(long id, MeleeWeapon meleeWeapon) {
        ContentValues values = new ContentValues();
        if (meleeWeapon != null) {
            values.put(MySQLiteHelper.COLUMN_MELEE_WEAPON, meleeWeapon.getPd2());
        } else {
            values.putNull(MySQLiteHelper.COLUMN_MELEE_WEAPON);
        }

        database.update(MySQLiteHelper.TABLE_WEAPON_BUILDS, values, MySQLiteHelper.COLUMN_ID + " = " + id, null);
    }
}
