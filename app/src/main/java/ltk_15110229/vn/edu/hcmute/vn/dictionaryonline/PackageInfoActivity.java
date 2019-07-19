package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PackageInfoActivity extends AppCompatActivity {
    private ListView listViewWord;
    ArrayList<Word> wordArrayList;
    WordAdapter2 wordAdapter;
    private SearchView search_view2;
    private TextView txtNamePackInfo;
    private FirebaseAuth mAuth;
    DatabaseReference mData;
    private FloatingActionButton fab, fab_testing, fab_notification;
    boolean displayFab = false;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_info);

        //khởi tạo database reference
        mData = FirebaseDatabase.getInstance().getReference();

        txtNamePackInfo = (TextView) findViewById(R.id.txtNamePackInfo);
        listViewWord = (ListView) findViewById(R.id.listviewWord);
        search_view2 = (SearchView) findViewById(R.id.search_view2);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_testing = (FloatingActionButton) findViewById(R.id.fab_testing);
        fab_notification = (FloatingActionButton) findViewById(R.id.fab_notification);

        //ẩn 2 nút fab
        fab_notification.hide();
        fab_testing.hide();

        //nhận dữ liệu từ intent
        Intent intent = getIntent();
        final Package pack = (Package) intent.getSerializableExtra("pack");
        txtNamePackInfo.setText(pack.getName());

        //hiện nút back về activity cũ, đặt lại tiêu đề cho activity
        if(getSupportActionBar()!= null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(pack.getName());
        }

        //khởi tạo adapter và set cho list view
        wordArrayList = new ArrayList<>();
        wordAdapter = new WordAdapter2(this, R.layout.item_listview_word_inpackage, wordArrayList);
        listViewWord.setAdapter(wordAdapter);

        //load danh sách word trong package
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        String url;
        if(readPreference() == "VE"){
            url = "http://"+Constant.API_URL+":80/androidwebservice/getWordsInPackage.php?package_id=" + pack.getId()
                    + "&pack_vocab=pack_vocab_vn&word=wordvn";
        }else {
            url = "http://"+Constant.API_URL+":80/androidwebservice/getWordsInPackage.php?package_id=" + pack.getId()
                    + "&pack_vocab=pack_vocab&word=word";
        }
        GetData(url);

        //xử lí sự kiện trong search view
        search_view2.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                wordAdapter.getFilter().filter(query);
                return false;
            }
        });

        //xử lí sự kiện long click trên 1 dòng item để xóa từ đó
        listViewWord.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Word word = (Word) listViewWord.getItemAtPosition(position); // lấy object dòng hiện tại
                ShowAlertDialog( mAuth.getUid(), word, pack, position);
                return true;
            }
        });

        //sự kiện click 1 dòng item trên listview, chuyển dữ liệu sang activity WordInfoActivity
        listViewWord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Word w = (Word) listViewWord.getItemAtPosition(position);   //lấy object dòng hiện tại

                Intent intent = new Intent(PackageInfoActivity.this, WordInfoActivity.class);
                intent.putExtra("wordInfo", w);

                startActivity(intent);
            }
        });

        //Sự kiện đặt thông báo từ vựng hàng ngày
        fab_notification.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //kiểm tra có từ trong gói thì mới được đặt thông báo
                if(wordArrayList.size() > 0) {
                    Calendar calendar = Calendar.getInstance();

                    //calendar.set(Calendar.HOUR_OF_DAY, 12);
                    //calendar.set(Calendar.MINUTE, 35);
                    calendar.set(Calendar.SECOND, 1);

                    //truyền ds các từ trong gói
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", (Serializable) wordArrayList);
//                    Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                    Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                    intent.putExtra("wordArrayList", args);

//                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100,
//                            intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                            AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);

                    alarmManager.setInexactRepeating(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + 5000,
                            5000,
                            pendingIntent
                    );

                    Toast.makeText(PackageInfoActivity.this, getResources().getString(R.string.notificationSuccess), Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(PackageInfoActivity.this, getResources().getString(R.string.notificationError), Toast.LENGTH_LONG).show();
                }

                return true;
            }
        });

        //click nút fab_notification
        fab_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PackageInfoActivity.this, getResources().getString(R.string.notification), Toast.LENGTH_LONG).show();
            }
        });

        //sự kiện click nút fab sẽ hiện 2 nút kia
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (displayFab == false){
                    fab_notification.show();
                    fab_testing.show();
                    displayFab = true;
                }else {
                    fab_notification.hide();
                    fab_testing.hide();
                    displayFab = false;
                }
            }
        });

    }

    //đọc dữ liệu từ đoạn JSON đc viết từ file PHP và đổ về mảng wordArrayList
    private void GetData(String url){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Lấy dữ liệu từng object rồi thêm vào wordArrayList
                        for (int i=0; i<response.length() ; i++){
                            try {
                                JSONObject object = response.getJSONObject(i);
                                wordArrayList.add(new Word(object.getInt("ID"),
                                        object.getString("Word"),
                                        object.getString("Detail1"),
                                        object.getString("Detail2")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        wordAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "error1: " +  error.toString());
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    //sự kiện click nút quay về
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    //dialog xác nhận xoá từ ra khỏi gói
    public void ShowAlertDialog(final String uid, final Word word, final Package pack, final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("kt Dictionary");
        builder.setMessage(getResources().getString(R.string.confirmDeleteWord));
        builder.setCancelable(true);
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(readPreference() == "VE"){
                    //delete khỏi mysql
                    String url = "http://"+Constant.API_URL+":80/androidwebservice/deleteWordvn.php";
                    DeleteWordMysql(url, Integer.toString(pack.getId()), Integer.toString(word.getId()));

                    //delete khỏi firebase
                    mData.child(uid).child("V-E_dict").child(pack.getName()).child(Integer.toString(word.getId())).removeValue();
                }else {
                    //delete khỏi mysql
                    String url = "http://"+Constant.API_URL+":80/androidwebservice/deleteWord.php";
                    DeleteWordMysql(url, Integer.toString(pack.getId()), Integer.toString(word.getId()));

                    //delete khỏi firebase
                    mData.child(uid).child("E-V_dict").child(pack.getName()).child(Integer.toString(word.getId())).removeValue();
                }

                //remove word vừa xóa và load lại listview
                wordArrayList.remove(position);
                wordAdapter.notifyDataSetChanged();

                Toast.makeText(PackageInfoActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
    //xử lí xóa word trong package mysql
    private void DeleteWordMysql(String url, final String packid, final String wordid){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //truyền tham số
                params.put("packid", packid.trim());
                params.put("wordid", wordid.trim());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public String readPreference(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("dataDict", MODE_PRIVATE);
        return sharedPreferences.getString("dict","EV");
    }
}
