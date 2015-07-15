package com.dawgandpony.pd2skills.Activities;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dawgandpony.pd2skills.Build;
import com.dawgandpony.pd2skills.Fragments.BlankFragment;
import com.dawgandpony.pd2skills.Consts.Trees;
import com.dawgandpony.pd2skills.R;
import com.dawgandpony.pd2skills.Fragments.SkillTreeFragment;
import com.dawgandpony.pd2skills.Skill;
import com.dawgandpony.pd2skills.SkillBuild;
import com.dawgandpony.pd2skills.SkillTree;
import com.dawgandpony.pd2skills.Tier;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;

/**
 * Created by Jamie on 15/07/2015.
 */
public class EditBuildActivity extends MaterialNavigationDrawer {

    private Build currentBuild;

    @Override
    public void init(Bundle savedInstanceState) {
        setDrawerHeaderImage(R.drawable.payday_2_logo);

        MaterialSection secInfamy = newSection("Infamy", new BlankFragment());

        MaterialSection secMas = newSection("Mastermind", SkillTreeFragment.newInstance(Trees.MASTERMIND));
        MaterialSection secEnf = newSection("Enforcer", SkillTreeFragment.newInstance(Trees.ENFORCER));
        MaterialSection secTech = newSection("Technician", SkillTreeFragment.newInstance(Trees.TECHNICIAN));
        MaterialSection secGhost = newSection("Ghost", SkillTreeFragment.newInstance(Trees.GHOST));
        MaterialSection secFugi = newSection("Fugitive", SkillTreeFragment.newInstance(Trees.FUGITIVE));

        MaterialSection secPD = newSection("Perk Deck", new BlankFragment());

        MaterialSection secPrimary = newSection("Primary Weapon", new BlankFragment());
        MaterialSection secSecondaty = newSection("Secondary Weapon", new BlankFragment());

        MaterialSection secArmour = newSection("Armour", new BlankFragment());

        MaterialSection secAbout = newSection("About", R.drawable.ic_info_black_24dp, new BlankFragment());
        MaterialSection secSettings = newSection("Settings", R.drawable.ic_settings_black_24dp, new BlankFragment());









        this.addSubheader("Infamy & Perk Deck");
        addSection(secInfamy);
        addSection(secPD);
        this.addSubheader("Skills");
        addSection(secMas);
        addSection(secEnf);
        addSection(secTech);
        addSection(secGhost);
        addSection(secFugi);
        //this.addSubheader("Perk Deck");
        //addSection(secPD);
        this.addSubheader("Weapons");
        addSection(secPrimary);
        addSection(secSecondaty);
        this.addSubheader("Armour");
        addSection(secArmour);

        addBottomSection(secAbout);
        addBottomSection(secSettings);

        new GetSkillsFromXMLandDBTask().execute((long) 0);
    }

    private class GetSkillsFromXMLandDBTask extends AsyncTask<Long, Integer, SkillBuild> {

        @Override
        protected SkillBuild doInBackground(Long... ids) {

            SkillBuild skillBuild = new SkillBuild();
            XmlResourceParser xmlParser = null;

            //Go get the xml for all the trees
            for (int i = Trees.MASTERMIND; i <= Trees.FUGITIVE; i++) {
                //Get the xml for the correct tree
                switch (i) {
                    case Trees.MASTERMIND:
                        xmlParser = getResources().getXml(R.xml.mastermind);
                        break;
                    case Trees.ENFORCER:
                        xmlParser = getResources().getXml(R.xml.enforcer);
                        break;
                    case Trees.TECHNICIAN:
                        xmlParser = getResources().getXml(R.xml.technician);
                        break;
                    case Trees.GHOST:
                        xmlParser = getResources().getXml(R.xml.ghost);
                        break;
                    case Trees.FUGITIVE:
                        xmlParser = getResources().getXml(R.xml.fugitive);
                        break;
                    default:
                        Toast.makeText(getBaseContext(), "Something went wrong while we were retrieving the skills, defaulting to Mastermind", Toast.LENGTH_LONG).show();
                        break;
                }


                SkillTree currentSkillTree = null;

                try {
                    int eventType = xmlParser.getEventType();

                    Tier currentTier = null;
                    Skill currentSkill = null;
                    String currentTag = "";

                    while (eventType != XmlPullParser.END_DOCUMENT) {

                        if (eventType == XmlPullParser.START_DOCUMENT) {
                            Log.d("XML", "Start Document");
                        } else if (eventType == XmlPullParser.START_TAG) {

                            Log.d("XML", "Start tag " + xmlParser.getName());
                            currentTag = xmlParser.getName();

                            switch (xmlParser.getName()){
                                case "skill_tree":
                                    currentSkillTree = new SkillTree();
                                    break;
                                case "tier":
                                    currentTier = new Tier();
                                    break;
                                case "skill":
                                    currentSkill = new Skill();
                                    break;
                            }

                        } else if (eventType == XmlPullParser.END_TAG) {
                            Log.d("XML", "End tag " + xmlParser.getName());
                            currentTag = xmlParser.getName();
                            switch (xmlParser.getName()){
                                case "skill_tree":
                                    skillBuild.getSkillTrees().add(currentSkillTree);
                                    break;
                                case "tier":
                                    currentSkillTree.getTierList().add(currentTier);
                                    break;
                                case "skill":
                                    currentTier.getSkillsInTier().add(currentSkill);
                                    break;
                            }


                        } else if (eventType == XmlPullParser.TEXT) {
                            String text = xmlParser.getText();
                            Log.d("XML", "Text " + text);
                            Log.d("Current Tag", currentTag + "");
                            switch (currentTag.toString()){
                                case "tree_name":
                                    currentSkillTree.setName(text);
                                    break;
                                case "tierNumber":
                                    currentTier.setNumber(Integer.parseInt(text));
                                    break;
                                case "point_requirement":
                                    currentTier.setPointRequirement(Integer.parseInt(text));
                                    break;
                                case "name":
                                    currentSkill.setName(text);
                                    break;
                                case "normal":
                                    currentSkill.setNormalDescription(text);
                                    break;
                                case "ace":
                                    currentSkill.setAceDescription(text);
                                    break;
                                default:
                                    Log.d("XML", "currentTag didnt match anything!");
                                    break;


                            }
                        }
                        eventType = xmlParser.next();
                    }

                    Log.d("XML", "End Document");
                } catch (Exception e) {
                    Log.e("Error", e.toString());
                    xmlParser.close();
                    //Toast.makeText(getBaseContext(), "Something went wrong while we were retrieving the skills", Toast.LENGTH_SHORT).show();
                }

            }
            xmlParser.close();
            Log.d("Result from XML", skillBuild.toString());
            return skillBuild;
        }
    }

}
