package com.jamieadkins.heistr.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.jamieadkins.heistr.BuildObjects.Build;
import com.jamieadkins.heistr.BuildObjects.WeaponBuild;
import com.jamieadkins.heistr.Consts.Trees;
import com.jamieadkins.heistr.Database.DataSourceBuilds;
import com.jamieadkins.heistr.Dialogs.PD2SkillsExportDialog;
import com.jamieadkins.heistr.Dialogs.RenameDialog;
import com.jamieadkins.heistr.Fragments.ArmourFragment;
import com.jamieadkins.heistr.Fragments.BlankFragment;
import com.jamieadkins.heistr.Fragments.BuildListFragment;
import com.jamieadkins.heistr.Fragments.InfamyFragment;
import com.jamieadkins.heistr.Fragments.PerkDeckFragment;
import com.jamieadkins.heistr.Fragments.ViewPagerFragments.NewSkillTreeParentFragment;
import com.jamieadkins.heistr.Fragments.ViewPagerFragments.SkillTreeParentFragment;
import com.jamieadkins.heistr.Fragments.TaskFragment;
import com.jamieadkins.heistr.Fragments.ViewPagerFragments.WeaponListParentFragment;
import com.jamieadkins.heistr.R;
import com.jamieadkins.heistr.utils.URLEncoder;

import java.util.ArrayList;

import static android.nfc.NdefRecord.createMime;

public class EditBuildActivity extends AppCompatActivity implements TaskFragment.TaskCallbacks,
        RenameDialog.RenameDialogListener, NfcAdapter.CreateNdefMessageCallback {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    public static final String TAG_TASK_FRAGMENT = "task_fragment";
    protected final static String BUILD_ID = "BuildID";
    private final static String FRAGMENT_INDEX = "FragmentInd";
    public final static String SKILL_TREE_INDEX = "SkillTreeInd";
    public final static String MASTERMIND_INDEX = "MastermindInd";
    public final static String ENFORCER_INDEX = "EnforcerInd";
    public final static String TECHNICIAN_INDEX = "TechnicianInd";
    public final static String GHOST_INDEX = "GhostInd";
    public final static String FUGITIVE_INDEX = "FugitiveInd";
    public final static String WEAPON_LIST_INDEX = "WeaponInd";
    private final static String ACTIVITY_START = "StartAct";
    public static final int WEAPON_EDIT_REQUEST = 505;  // The request code
    private static final int NFC_PERMISSIONS = 506;  // The request code
    private static final String TAG = EditBuildActivity.class.getSimpleName();

    private Intent intent;
    private Build currentBuild;
    private long currentBuildID;
    private TaskFragment mTaskFragment;
    private ArrayList<BuildReadyCallbacks> mListCallbacks;

    private int currentFragment = R.id.nav_mastermind;
    private int currentSkillTree = Trees.MASTERMIND;
    private int currentWeaponList = WeaponBuild.PRIMARY;

    private int[] currentSkillTreeIndex = new int[Trees.FUGITIVE + 1];

    private NfcAdapter mNfcAdapter;

    private ProgressBar mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitBuildId(savedInstanceState);

        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getInt(FRAGMENT_INDEX);
            currentSkillTree = savedInstanceState.getInt(SKILL_TREE_INDEX);
            currentWeaponList = savedInstanceState.getInt(WEAPON_LIST_INDEX);

            currentSkillTreeIndex[Trees.MASTERMIND] = savedInstanceState.getInt(MASTERMIND_INDEX);
            currentSkillTreeIndex[Trees.ENFORCER] = savedInstanceState.getInt(ENFORCER_INDEX);
            currentSkillTreeIndex[Trees.TECHNICIAN] = savedInstanceState.getInt(TECHNICIAN_INDEX);
            currentSkillTreeIndex[Trees.GHOST] = savedInstanceState.getInt(GHOST_INDEX);
            currentSkillTreeIndex[Trees.FUGITIVE] = savedInstanceState.getInt(FUGITIVE_INDEX);
        }

        setContentView(R.layout.activity_edit_build2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        loadFragment(currentFragment);

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            // Register callback
            mNfcAdapter.setNdefPushMessageCallback(this, this);
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // NFC should be auto granted, but doesn't hurt to check.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.NFC)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.NFC}, NFC_PERMISSIONS);
        }

        String url = URLEncoder.encodeBuild(this, currentBuild);
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/vnd.com.jamieadkins.heistr", url.getBytes())
                        ,NdefRecord.createApplicationRecord("com.jamieadkins.heistr")
                });
        return msg;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mListCallbacks == null) {
            mListCallbacks = new ArrayList<>();
        }

        InitRetainedFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(BUILD_ID, currentBuild.getId());
        savedInstanceState.putInt(FRAGMENT_INDEX, currentFragment);
        savedInstanceState.putInt(SKILL_TREE_INDEX, currentSkillTree);
        savedInstanceState.putInt(WEAPON_LIST_INDEX, currentWeaponList);

        savedInstanceState.putInt(MASTERMIND_INDEX, currentSkillTreeIndex[Trees.MASTERMIND]);
        savedInstanceState.putInt(ENFORCER_INDEX, currentSkillTreeIndex[Trees.ENFORCER]);
        savedInstanceState.putInt(TECHNICIAN_INDEX, currentSkillTreeIndex[Trees.TECHNICIAN]);
        savedInstanceState.putInt(GHOST_INDEX, currentSkillTreeIndex[Trees.GHOST]);
        savedInstanceState.putInt(FUGITIVE_INDEX, currentSkillTreeIndex[Trees.FUGITIVE]);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            finish();
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_build, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_URL:
                DialogFragment dialog = PD2SkillsExportDialog.newInstance(URLEncoder.encodeBuild(this, currentBuild));
                dialog.show(getSupportFragmentManager(), "PD2SkillsExportDialogFragment");
                return true;
            case R.id.action_rename:
                showRenameBuildDialog();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        setTitle(menuItem.getTitle());
                        loadFragment(menuItem.getItemId());
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }


    @Override
    public void onDialogRenameBuild(DialogFragment dialog, String name, SparseBooleanArray buildPositions) {
        DataSourceBuilds dataSourceBuilds = new DataSourceBuilds(this);
        dataSourceBuilds.open();
        dataSourceBuilds.renameBuild(currentBuild.getId(), name);
        dataSourceBuilds.close();
    }

    @Override
    public void onPreExecute() {
        mLoadingBar = (ProgressBar) findViewById(R.id.pbLoading);
        mLoadingBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onBuildReady(Build build) {
        if (mLoadingBar != null) {
            mLoadingBar.setVisibility(View.GONE);
        }

        currentBuild = build;
        currentBuildID = build.getId();
        if (mListCallbacks == null) {
            mListCallbacks = new ArrayList<>();
        } else {
            for (BuildReadyCallbacks b : mListCallbacks) {
                b.onBuildReady();
            }
        }
    }

    public interface BuildReadyCallbacks {
        void onBuildReady();

        void onBuildUpdated();
    }

    public interface WeaponsCallbacks {
        void onWeaponsReady();
    }

    private void InitRetainedFragment() {
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If we haven't retained the worker fragment, then create it
        // and set this UIFragment as the TaskFragment's target fragment.
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            mTaskFragment.setCurrentBuildID(currentBuildID);
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
        //mTaskFragment.start(currentBuildID, newBuildName);
    }

    private void InitBuildId(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            currentBuildID = savedInstanceState.getLong(BUILD_ID);
        } else {
            intent = getIntent();
            final String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action)) {
                Log.d("Intents", intent.getData().toString());
                DataSourceBuilds dataSourceBuilds = new DataSourceBuilds(this);
                dataSourceBuilds.open();
                Build b = dataSourceBuilds.createAndInsertBuild("PD2Skills Build", 0, intent.getData().toString(), -1);
                dataSourceBuilds.close();
                currentBuildID = b.getId();

                showRenameBuildDialog();
            } else if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                        NfcAdapter.EXTRA_NDEF_MESSAGES);
                // only one message sent during the beam
                NdefMessage msg = (NdefMessage) rawMsgs[0];
                String url = new String(msg.getRecords()[0].getPayload());

                DataSourceBuilds dataSourceBuilds = new DataSourceBuilds(this);
                dataSourceBuilds.open();
                Build b = dataSourceBuilds.createAndInsertBuild("PD2Skills Build", 0, url, -1);
                dataSourceBuilds.close();
                currentBuildID = b.getId();

                showRenameBuildDialog();
            } else {
                long id = intent.getLongExtra(BuildListFragment.EXTRA_BUILD_ID, Build.NEW_BUILD);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String key = getString(R.string.current_build_id);

                if (id == Build.NEW_BUILD) {
                    id = preferences.getLong(key, Build.NEW_BUILD);
                }

                currentBuildID = id;
                preferences.edit().putLong(key, id).apply();
            }
        }
    }

    private void showRenameBuildDialog() {
        DialogFragment dialog = RenameDialog.newInstance(true, null);
        dialog.show(getSupportFragmentManager(), "RenameBuildDialogFragment");
    }

    public void listenIn(Fragment f) {
        mListCallbacks.add((BuildReadyCallbacks) f);
    }

    public void stopListening(Fragment f) {
        if (mListCallbacks != null) {
            mListCallbacks.remove(f);
        }
    }

    public Build getCurrentBuild() {
        return currentBuild;
    }


    public void loadFragment(int id) {
        currentFragment = id;
        Fragment fragment;
        switch (id) {
            case R.id.nav_mastermind:
                fragment = NewSkillTreeParentFragment.newInstance(Trees.MASTERMIND, currentSkillTreeIndex[Trees.MASTERMIND]);
                break;
            case R.id.nav_enforcer:
                fragment = NewSkillTreeParentFragment.newInstance(Trees.ENFORCER, currentSkillTreeIndex[Trees.ENFORCER]);
                break;
            case R.id.nav_technician:
                fragment = NewSkillTreeParentFragment.newInstance(Trees.TECHNICIAN, currentSkillTreeIndex[Trees.TECHNICIAN]);
                break;
            case R.id.nav_ghost:
                fragment = NewSkillTreeParentFragment.newInstance(Trees.GHOST, currentSkillTreeIndex[Trees.GHOST]);
                break;
            case R.id.nav_fugitive:
                fragment = NewSkillTreeParentFragment.newInstance(Trees.FUGITIVE, currentSkillTreeIndex[Trees.FUGITIVE]);
                break;
            case R.id.nav_infamy:
                fragment = InfamyFragment.newInstance();
                break;
            case R.id.nav_perk_deck:
                fragment = PerkDeckFragment.newInstance();
                break;
            case R.id.nav_armour:
                fragment = ArmourFragment.newInstance();
                break;
            case R.id.nav_weapons:
                fragment = WeaponListParentFragment.newInstance(currentWeaponList);
                break;
            case R.id.nav_home:
                fragment = null;
                finish();
                break;
            case R.id.nav_settings:
                fragment = null;
                Intent settingsIntent = new Intent(EditBuildActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            default:
                fragment = new BlankFragment();
                break;
        }


        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.contentFragment, fragment);
            transaction.commit();
        }

        mNavigationView.setCheckedItem(id);
    }

    public void updateCurrentSkillTree(int tree) {
        currentSkillTree = tree;
    }

    public void updateCurrentWeaponList(int weaponList) {
        currentWeaponList = weaponList;
    }

    public void updateCurrentSkillTreeIndex(int tree, int index) {
        currentSkillTreeIndex[tree] = index;
    }

    public int getCurrentSkillTreeIndex(int skillTree) {
        return currentSkillTreeIndex[skillTree];
    }
}
