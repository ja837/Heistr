package com.dawgandpony.pd2skills.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.dawgandpony.pd2skills.BuildObjects.Attachment;
import com.dawgandpony.pd2skills.BuildObjects.Build;
import com.dawgandpony.pd2skills.BuildObjects.Weapon;
import com.dawgandpony.pd2skills.BuildObjects.WeaponBuild;
import com.dawgandpony.pd2skills.Database.DataSourceWeapons;
import com.dawgandpony.pd2skills.Database.MySQLiteHelper;
import com.dawgandpony.pd2skills.Fragments.AttachmentListFragment;
import com.dawgandpony.pd2skills.Fragments.BlankFragment;
import com.dawgandpony.pd2skills.Fragments.BuildListFragment;
import com.dawgandpony.pd2skills.Fragments.TaskFragment;
import com.dawgandpony.pd2skills.Fragments.WeaponListFragment;
import com.dawgandpony.pd2skills.Fragments.WeaponOverallFragment;
import com.dawgandpony.pd2skills.R;

/**
 * Created by Jamie on 11/10/2015.
 */
public class EditWeaponActivity extends AppCompatActivity implements TaskFragment.TaskCallbacks{

    public static final String EXTRA_WEAPON_TYPE = "WeaponType";
    private static final String TAG = "EditWeaponActivity";
    Weapon currentWeapon;
    Build currentBuild;
    long currentWeaponID = -2;
    long currentBuildID = -2;
    boolean activityReady = false;
    boolean infoReady = false;
    int weaponType = -1;
    ArrayList<Weapon> baseWeaponInfo;
    ArrayList<Attachment> baseAttachmentInfo;
    ArrayList<ArrayList<Attachment>> attachmentsSplitUp;
    ArrayList<WeaponsCallbacks> mListeners;
    ArrayList<EditBuildActivity.BuildReadyCallbacks> mBuildListeners;

    TabLayout mTabLayout;
    ViewPager mViewPager;

    private TaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_weapon);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);


        if (savedInstanceState == null){
            currentWeaponID = getIntent().getLongExtra(WeaponListFragment.EXTRA_WEAPON_ID, -1);
            currentBuildID = getIntent().getLongExtra(BuildListFragment.EXTRA_BUILD_ID, -1);
            weaponType = getIntent().getIntExtra(EXTRA_WEAPON_TYPE, -1);
        } else {
            currentWeaponID = savedInstanceState.getLong(WeaponListFragment.EXTRA_WEAPON_ID);
            currentBuildID = savedInstanceState.getLong(BuildListFragment.EXTRA_BUILD_ID);
            weaponType = savedInstanceState.getInt(EXTRA_WEAPON_TYPE);
        }
        activityReady = true;

        if (infoReady){
            onInfoReady();
        }

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_weapon, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(EditBuildActivity.TAG_TASK_FRAGMENT);

        // If we haven't retained the worker fragment, then create it
        // and set this UIFragment as the TaskFragment's target fragment.
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            mTaskFragment.setCurrentBuildID(currentBuildID);
            fm.beginTransaction().add(mTaskFragment, EditBuildActivity.TAG_TASK_FRAGMENT).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(WeaponListFragment.EXTRA_WEAPON_ID, currentWeapon.getId());
        outState.putInt(EXTRA_WEAPON_TYPE, weaponType);
        outState.putLong(EditBuildActivity.BUILD_ID, currentBuild.getId());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = this.getIntent();
                intent.putExtra(WeaponListFragment.EXTRA_WEAPON_ID, currentWeapon.getId());
                this.setResult(RESULT_CANCELED, intent);
                finish();
                return true;
            case R.id.action_equip:
                Intent intent2 = this.getIntent();
                intent2.putExtra(WeaponListFragment.EXTRA_WEAPON_ID, currentWeapon.getId());
                this.setResult(RESULT_OK, intent2);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface ViewPagerLifecycle {

        void onHide();

        void onShow();
    }

    private void setupViewPager(final ViewPager viewPager) {
        mListeners = new ArrayList<>();
        mBuildListeners = new ArrayList<>();
        final Adapter adapter = new Adapter(getSupportFragmentManager());

        adapter.addFragment(new WeaponOverallFragment(), "Overview");

        String[] attachment_types = getResources().getStringArray(R.array.attachment_types);
        for (int i = 0; i < MySQLiteHelper.COLUMNS_ATTACHMENTS.length; i++){
            if (attachmentsSplitUp.get(i).size() > 0) {
                adapter.addFragment(AttachmentListFragment.newInstance(i), attachment_types[i]);
            }
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int currentPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Adapter adapter = (Adapter) mViewPager.getAdapter();
                ViewPagerLifecycle fragmentToShow = (ViewPagerLifecycle) adapter.getItem(position);
                fragmentToShow.onShow();

                ViewPagerLifecycle fragmentToHide = (ViewPagerLifecycle) adapter.getItem(currentPosition);
                fragmentToHide.onHide();

                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onBuildReady(Build build) {

        currentBuild = build;
        currentBuildID = build.getId();
        baseWeaponInfo = currentBuild.getWeaponsFromXML();
        baseAttachmentInfo = currentBuild.getAttachmentsFromXML();

        infoReady = true;

        if (activityReady){
            onInfoReady();
        }
    }


    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    public interface WeaponsCallbacks{
        void onWeaponReady();
    }

    public void listen(Fragment fragment){
        mListeners.add((WeaponsCallbacks) fragment);
        mBuildListeners.add((EditBuildActivity.BuildReadyCallbacks) fragment);
    }

    public void stopListening(Fragment fragment){
        if (mListeners != null) {
            mListeners.remove(fragment);
        }
        if (mBuildListeners != null) {
            mBuildListeners.remove(fragment);
        }
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public Build getCurrentBuild() {
        return currentBuild;
    }

    public ArrayList<Attachment> getPossibleAttachments(int attachmentType){
        return attachmentsSplitUp.get(attachmentType);
    }

    public void updateCurrentWeapon(int attachmentType, int currentAttachmentIndex) {
        DataSourceWeapons dataSourceWeapons = new DataSourceWeapons(this, baseWeaponInfo, baseAttachmentInfo);
        dataSourceWeapons.open();
        if (currentAttachmentIndex != -1){
            Attachment newAttachment = attachmentsSplitUp.get(attachmentType).get(currentAttachmentIndex);
            currentWeapon.getAttachments().add(newAttachment);

            dataSourceWeapons.updateAttachment(currentWeapon.getId(), attachmentType, newAttachment.getPd2());

        } else {

            int attachmentToRemove = -1;
            for (Attachment a : currentWeapon.getAttachments()) {
                if (a.getAttachmentType() == attachmentType) {
                    attachmentToRemove = currentWeapon.getAttachments().indexOf(a);
                }
            }

            currentWeapon.getAttachments().remove(attachmentToRemove);


            dataSourceWeapons.updateAttachment(currentWeapon.getId(), attachmentType, -1 + "");
        }

        dataSourceWeapons.close();

        if (mBuildListeners != null) {
            for (EditBuildActivity.BuildReadyCallbacks b : mBuildListeners){
                b.onBuildUpdated();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mListeners = null;
        mBuildListeners = null;
    }

    public class GetWeaponFromDB extends AsyncTask<Void, Integer, Weapon> {

        private final long id;

        public GetWeaponFromDB(long id) {
            super();
            this.id = id;
        }

        @Override
        protected Weapon doInBackground(Void... params) {
            DataSourceWeapons dataSourceWeapons = new DataSourceWeapons(EditWeaponActivity.this, baseWeaponInfo, baseAttachmentInfo);
            dataSourceWeapons.open();
            Weapon weapon = dataSourceWeapons.getWeapon(id);
            dataSourceWeapons.close();

            return weapon;
        }

        @Override
        protected void onPostExecute(Weapon weapon) {
            super.onPostExecute(weapon);
            currentWeapon = weapon;

            setTitle(weapon.getName());

            attachmentsSplitUp = new ArrayList<>();
            for (int i = Attachment.MOD_AMMO; i <= Attachment.MOD_STAT_BOOST; i++){
                attachmentsSplitUp.add(new ArrayList<Attachment>());
            }
            for (Attachment attachment : baseAttachmentInfo){
                for (String s : currentWeapon.getPossibleAttachments()){
                    if (s.equals(attachment.getPd2())){
                        attachmentsSplitUp.get(attachment.getAttachmentType()).add(attachment);
                    }
                }
            }

            onWeaponReady();
        }
    }

    private void onInfoReady() {
        new GetWeaponFromDB(currentWeaponID).execute();
    }

    private void onWeaponReady() {
        if (mListeners != null){
            for (WeaponsCallbacks listener : mListeners){
                listener.onWeaponReady();
            }
        }

        if (mBuildListeners != null) {
            for (EditBuildActivity.BuildReadyCallbacks b : mBuildListeners){
                b.onBuildReady();
            }
        }

        if (mViewPager != null) {
            setupViewPager(mViewPager);
        }
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
