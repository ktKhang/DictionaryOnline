package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddToPackageActivity extends AppCompatActivity {
    ListView listviewAddWord;
    ArrayList<Package> packageArrayList;
    PackageAdapter packageAdapter;
    private FirebaseAuth mAuth;
    DatabaseReference mData;
    private String TAG = "AddToPackageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_package);

        //khởi tạo database reference
        mData = FirebaseDatabase.getInstance().getReference();

        listviewAddWord = (ListView) findViewById(R.id.listviewAddWord);

        //nhận dữ liệu từ intent
        Intent intent = getIntent();
        final Word word = (Word) intent.getSerializableExtra("wordInfo");

        //khởi tạo adapter và set cho list view
        packageArrayList = new ArrayList<>();
        packageAdapter = new PackageAdapter(this, R.layout.item_listview_package_not_added, packageArrayList);
        listviewAddWord.setAdapter(packageAdapter);

        //load danh sách package (các package chưa chứa từ cần thêm)
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        String url;
        if (readPreference()=="VE") {
            url = "http://"+Constant.API_URL+":80/androidwebservice/getPackagesNotAdded.php?accountid=" + user.getUid()
                    + "&wordid=" + word.getId() + "&package=packagevn&pack_vocab=pack_vocab_vn";
        }else {
            url = "http://"+Constant.API_URL+":80/androidwebservice/getPackagesNotAdded.php?accountid=" + user.getUid()
                    + "&wordid=" + word.getId() + "&package=package&pack_vocab=pack_vocab";
        }
        GetData(url);

        //sự kiện click 1 dòng listview
        listviewAddWord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Package pack = (Package) listviewAddWord.getItemAtPosition(position); // lấy object dòng hiện tại

                showAlertDialog(user.getUid(),word, pack, position);
            }
        });


        //hiện nút back về activity cũ
        if(getSupportActionBar()!= null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    //sự kiện click nút quay về
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //đọc dữ liệu từ đoạn JSON đc viết từ file PHP và đổ về mảng packageArrayList
    private void GetData(String url){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Lấy dữ liệu từng object rồi thêm vào packageArrayList
                        for (int i=0; i<response.length() ; i++){
                            try {
                                JSONObject object = response.getJSONObject(i);
                                packageArrayList.add(new Package(object.getInt("ID"),
                                        object.getString("Name"),
                                        object.getString("Created_date"),
                                        object.getInt("Number_words")));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        packageAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "errora: " +  error.toString());
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    //dialog xác nhận thêm từ vào gói
    public void showAlertDialog(final String uid, final Word word, final Package pack, final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("kt Dictionary");
        builder.setMessage(getResources().getString(R.string.confirmAddToPackage));
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
                if (readPreference() == "VE") { //nếu từ điển VE
                    //thêm vào mysql
                    String url = "http://"+Constant.API_URL+":80/androidwebservice/insertWordToPackagevn.php";
                    addWordToPackageMysql(url, Integer.toString(word.getId()), Integer.toString(pack.getId()));
                    //thêm vào firebase
                    mData.child(uid).child("V-E_dict").child(pack.getName()).child(Integer.toString(word.getId())).setValue(word.getWord());
                }else { // nếu EV
                    //thêm vào mysql
                    String url = "http://"+Constant.API_URL+":80/androidwebservice/insertWordToPackage.php";
                    addWordToPackageMysql(url, Integer.toString(word.getId()), Integer.toString(pack.getId()));
                    //thêm vào firebase
                    mData.child(uid).child("E-V_dict").child(pack.getName()).child(Integer.toString(word.getId())).setValue(word.getWord());
                }
                //remove package vừa thêm và load lại listview
                packageArrayList.remove(position);
                packageAdapter.notifyDataSetChanged();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    //xử lí thêm word vào package
    private void addWordToPackageMysql(String url, final String wordid, final String packid){
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
                params.put("wordid", wordid.trim());
                params.put("packid", packid.trim());
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
