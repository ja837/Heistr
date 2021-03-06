package com.jamieadkins.heistr.utils;

import android.content.Context;
import android.util.Log;

import com.jamieadkins.heistr.BuildObjects.Build;
import com.jamieadkins.heistr.BuildObjects.NewSkillSubTree;
import com.jamieadkins.heistr.BuildObjects.NewSkillTree;
import com.jamieadkins.heistr.BuildObjects.Skill;
import com.jamieadkins.heistr.BuildObjects.SkillBuild;
import com.jamieadkins.heistr.BuildObjects.SkillTier;
import com.jamieadkins.heistr.BuildObjects.SkillTree;
import com.jamieadkins.heistr.BuildObjects.WeaponBuild;
import com.jamieadkins.heistr.Consts.Trees;
import com.jamieadkins.heistr.Database.DataSourceInfamies;
import com.jamieadkins.heistr.R;

import java.util.ArrayList;

/**
 * Created by Jamie on 05/09/2015.
 */
public class URLEncoder {

    final private static String baseURL = "http://pd2skills.com/#/v3/";

    final private static int tierIndex = 0;
    final private static int skillIndex = 1;

    public static String encodeBuild(Context context, Build build) {
        String url = baseURL;

        SkillBuild skillBuild = build.getSkillBuild();

        //region Skills
        int treeNumber = Trees.MASTERMIND;
        for (NewSkillTree tree : skillBuild.getNewSkillTrees()) {
            switch (treeNumber) {
                case Trees.MASTERMIND:
                    url += "m";
                    break;
                case Trees.ENFORCER:
                    url += "e";
                    break;
                case Trees.TECHNICIAN:
                    url += "t";
                    break;
                case Trees.GHOST:
                    url += "g";
                    break;
                case Trees.FUGITIVE:
                    url += "f";
                    break;
            }

            ArrayList<NewSkillSubTree> subTrees = tree.getSubTrees();

            for (NewSkillSubTree subTree : subTrees) {
                for (Skill skill : subTree.getSkillsInSubTree()) {
                    if (skill.getTaken() == Skill.NORMAL) {
                        url += skill.getPd2SkillsSymbol();
                    } else if (skill.getTaken() == Skill.ACE) {
                        url += skill.getPd2SkillsSymbol().toUpperCase();
                    }

                }
            }

            url += ":";
            treeNumber++;
        }
        //endregion

        //region Infamy
        url += "i";
        if (build.getInfamies().get(Trees.MASTERMIND)) {
            url += "b";
        }
        if (build.getInfamies().get(Trees.ENFORCER)) {
            url += "c";
        }
        if (build.getInfamies().get(Trees.TECHNICIAN)) {
            url += "d";
        }
        if (build.getInfamies().get(Trees.GHOST)) {
            url += "e";
        }
        url += "a:";
        //endregion

        //region Perkdeck
        String perkDeckShort = context.getResources().getStringArray(R.array.perkDecksURL)[build.getPerkDeck()];
        url += "p" + perkDeckShort.toUpperCase() + "8::";
        //endregion


        //region Primary Weapon
        //url+="w2:";
        //endregion

        //region Secondary Weapon
        //url+="s7:";
        //endregion

        //region Armour
        url += "a" + context.getResources().getStringArray(R.array.armourURLNumbers)[build.getArmour()];
        //end region


        return url;
    }

    public static Build decodeURL(Context context, String url) {
        Build b = new Build();
        String base = "";
        String remaining = "";
        if (url.length() >= baseURL.length()) {
            base = url.substring(0, baseURL.length());
            remaining = url.substring(base.length());
        }

        if (base.equals(baseURL)) {
            Log.d("URL DECODER", remaining);

            b.setSkillBuild(SkillBuild.newNonDBInstance());
            b.setWeaponBuild(new WeaponBuild());
            b.getWeaponBuild().setId(Build.PD2SKILLS);

            while (!remaining.equals("")) {
                int end = remaining.indexOf(":");
                switch (remaining.charAt(0)) {
                    case 'm': //mastermind tree
                        NewSkillTree treeM = getNewSkillTreeFromLetters(remaining);
                        b.getSkillBuild().getNewSkillTrees().set(Trees.MASTERMIND, treeM);

                        break;
                    case 'e': //enforcer
                        NewSkillTree treeE = getNewSkillTreeFromLetters(remaining);
                        b.getSkillBuild().getNewSkillTrees().set(Trees.ENFORCER, treeE);
                        break;
                    case 't': //tech
                        NewSkillTree treeT = getNewSkillTreeFromLetters(remaining);
                        b.getSkillBuild().getNewSkillTrees().set(Trees.TECHNICIAN, treeT);
                        break;
                    case 'g': //ghost
                        NewSkillTree treeG = getNewSkillTreeFromLetters(remaining);
                        b.getSkillBuild().getNewSkillTrees().set(Trees.GHOST, treeG);
                        break;
                    case 'f': //fugitive
                        NewSkillTree treeF = getNewSkillTreeFromLetters(remaining);
                        b.getSkillBuild().getNewSkillTrees().set(Trees.FUGITIVE, treeF);
                        break;
                    case 'i': //infamy
                        b.setInfamies(DataSourceInfamies.idToInfamy(findInfamies(remaining)));
                        break;
                    case 'p': //perkdeck
                        String[] perkDecks = context.getResources().getStringArray(R.array.perkDecksURL);
                        final int perkDeck = findPerkDeck(remaining, perkDecks);
                        b.setPerkDeck(perkDeck);
                        break;
                    case 'a': //armour
                        String[] armours = context.getResources().getStringArray(R.array.armourURLNumbers);

                        String fromUrl = String.valueOf(remaining.charAt(1));
                        int realArmour = java.util.Arrays.asList(armours).indexOf(fromUrl);
                        b.setArmour(realArmour);
                        remaining = "";
                        break;
                    case ':': //end

                        break;
                }

                if (remaining.length() > 0) {
                    remaining = remaining.substring(end + 1);
                }

            }
        } else {
            b = null;
        }

        return b;
    }

    private static int findPerkDeck(String remaining, String[] possiblePerkDecks) {
        char[] chars = remaining.toCharArray();
        int deck = 0;
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                if (Character.toUpperCase(chars[i]) == chars[i]) {
                    deck = java.util.Arrays.asList(possiblePerkDecks).indexOf(Character.toString(Character.toLowerCase(chars[i])));
                }
            }
        }
        return deck;
    }

    public static int findInfamies(String remaining) {
        int end = remaining.indexOf(":");
        String infamies = remaining.substring(1, end);
        int id = 1;
        while (infamies.length() > 0) {
            if (infamies.charAt(0) == 'b') { //Mastermind
                id += 8;
            }
            if (infamies.charAt(0) == 'c') { //Enforcer
                id += 4;
            }
            if (infamies.charAt(0) == 'd') { //Tech
                id += 2;
            }
            if (infamies.charAt(0) == 'e') { //Ghost
                id += 1;
            }
            if (infamies.length() > 1) {
                infamies = infamies.substring(1);
            } else {
                infamies = "";
            }

        }

        return id;
    }

    //6 qrs
    //5 nop
    //4 klm
    //3 hij
    //2 efg
    //1 bcd
    //0 -a-
    private static SkillTree getSkillTreeFromLetters(String url) {
        int end = url.indexOf(":");
        String treeString = url.substring(1, end);
        SkillTree tree = SkillTree.newNonDBInstance();
        Log.d("Tree from URL", treeString);


        for (char character : treeString.toCharArray()) {
            int[] pos = tierIndexFromLetter(Character.toLowerCase(character));
            Skill skill = new Skill();
            if (character == Character.toLowerCase(character)) {
                skill.setTaken(Skill.NORMAL);
            } else {
                skill.setTaken(Skill.ACE);
            }

            tree.getTierList().get(pos[tierIndex]).getSkillsInTier().set(pos[skillIndex], skill);
        }

        return tree;
    }

    private static NewSkillTree getNewSkillTreeFromLetters(String url) {
        int end = url.indexOf(":");
        String treeString = url.substring(1, end);
        NewSkillTree tree = NewSkillTree.newNonDBInstance();
        Log.d("Tree from URL", treeString);


        for (char character : treeString.toCharArray()) {
            int[] pos = subtreeIndexFromLetter(Character.toLowerCase(character));
            Skill skill = new Skill();
            if (character == Character.toLowerCase(character)) {
                skill.setTaken(Skill.NORMAL);
            } else {
                skill.setTaken(Skill.ACE);
            }

            tree.getSubTrees().get(pos[tierIndex]).getSkillsInSubTree().set(pos[skillIndex], skill);
        }

        return tree;
    }

    private static int[] tierIndexFromLetter(char letter) {
        int numValue = (int) letter - 95;

        int tier = 0;
        int skill = 0;
        if (letter != 'a') {
            tier = numValue / 3;
            skill = numValue % 3;
        }
        Log.d("URL Letter conversion", letter + " turned into tier " + tier + ", skill " + skill);
        return new int[]{tier, skill};
    }

    private static int[] subtreeIndexFromLetter(char letter) {
        int numValue = (int) letter - 95;

        int subtree = 0;
        int tier = 0;
        int skill = 0;
        if (numValue < 6) {
            subtree = 0;
            tier = numValue / 3;
            skill = numValue % 3;
        } else if (numValue < 13) {
            subtree = 1;
            tier = numValue / 3;
            skill = numValue % 3;
        } else {
            subtree = 2;
        }
        Log.d("URL Letter conversion", letter + " turned into tier " + tier + ", skill " + skill + ", subtree " + subtree);
        return new int[]{tier, skill, subtree};
    }


}
