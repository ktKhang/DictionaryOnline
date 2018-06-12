package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    BottomNavigationView navigation;
    String TAG = "HomeActivity";
    String myFragmentTag = "null";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        //khởi tạo toolbar mới thay cho actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //tạo file shared preference và lưu giá trị từ điển mặc định là EV
        sharedPreferences = getSharedPreferences("dataDict", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("dict","EV");
        editor.commit();

        //chạy HomeFragment đầu tiên
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameContent, new HomeFragment(), "myFragmentTag").commit();

        //Khởi tạo và set sự kiện bottom navigation
        navigation = (BottomNavigationView) findViewById(R.id.bottomNav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Khởi tạo Navigation
        @SuppressLint("WrongViewCast") DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set avatar, name, email
        View headerView = navigationView.getHeaderView(0);
        TextView txtName = (TextView) headerView.findViewById(R.id.txtName);
        TextView txtEmail = (TextView) headerView.findViewById(R.id.txtEmail);
        ImageView imgAvatar = (ImageView) headerView.findViewById(R.id.imgAvatar);

        FirebaseUser user = mAuth.getInstance().getCurrentUser();
        txtName.setText(user.getDisplayName().toString());
        txtEmail.setText(user.getEmail().toString());
        //imgAvatar.setProfileId(Profile.getCurrentProfile().getId());
        Picasso.get().load(user.getPhotoUrl()).into(imgAvatar);



    }

    // kiểm tra ng dùng nếu chưa đăng nhập thì out ra giao diện login
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            updateUI();
        }
    }

    private void updateUI() {
        Intent homeIntent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(homeIntent);
        finish();
    }

    //Sự kiện khi nhấn vào nút back trên thiết bị
    @Override
    public void onBackPressed() {
        @SuppressLint("WrongViewCast") DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Sự kiện khi click vào mỗi item trong NavigationView
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_EV) {
            //chọn từ điển anh - việt
            sharedPreferences = getSharedPreferences("dataDict", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("dict","EV");
            editor.commit();

            //restart lại fragment
            Fragment frg = null;
            frg = getFragmentManager().findFragmentByTag("myFragmentTag");
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(frg);
            ft.attach(frg);
            ft.commit();
        } else if (id == R.id.nav_VE) {
            sharedPreferences = getSharedPreferences("dataDict", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("dict","VE");
            editor.commit();

            //restart lại fragment
            Fragment frg = null;
            frg = getFragmentManager().findFragmentByTag("myFragmentTag");
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(frg);
            ft.attach(frg);
            ft.commit();

        } else if (id == R.id.nav_home) {
            navigation.getMenu().getItem(0).setChecked(true);
            //chạy HomeFragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameContent, new HomeFragment()).commit();

        }else if (id == R.id.nav_mypack) {
            navigation.getMenu().getItem(1).setChecked(true);
            //chạy MypackFragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameContent, new MypackFragment()).commit();

        }else if (id == R.id.nav_trans) {
            navigation.getMenu().getItem(2).setChecked(true);
            //chạy TranslateFragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameContent, new TranslateFragment()).commit();
        }else if (id == R.id.nav_appInfo) {

        } else if (id == R.id.nav_logout) {
            mAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();

            updateUI();
        }

        @SuppressLint("WrongViewCast") DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //xử lí sự kiện select navigation item
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = null;

//            HomeFragment homeFragment = new HomeFragment();
//            MypackFragment mypackFragment = new MypackFragment();
//            TranslateFragment translateFragment = new TranslateFragment();

            switch (item.getItemId()) {
                case R.id.navHome:
                    fragment = new HomeFragment();
                    break;
                case R.id.navMypack:
                    fragment = new MypackFragment();
                    break;
                case R.id.navTrans:
                    fragment = new TranslateFragment();
                    break;
            }
            transaction.replace(R.id.frameContent, fragment,"myFragmentTag").commit();
            return true;
        }
    };

//    //nhận kết quả trả về từ AddToPackageActivity và truyền đến hàm gọi lại trong HomeFragment
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        FragmentManager fm = getFragmentManager();
//        HomeFragment homeFragment = (HomeFragment)fm.findFragmentById(R.id.frameContent);
//        homeFragment.onActivityResult(requestCode, resultCode, data);
//    }
}
