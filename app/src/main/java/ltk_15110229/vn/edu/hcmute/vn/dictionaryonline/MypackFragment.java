package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.SearchView;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class MypackFragment extends Fragment {
    private SearchView searchView;
    ListView listViewPackage;
    FloatingActionButton fltButton;
    ArrayList<Package> packageArrayList;
    PackageAdapter2 packageAdapter;
    private FirebaseAuth mAuth;
    DatabaseReference mData;
    String TAG = "MainActivity";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypack, container, false);

        //khởi tạo database reference
        mData = FirebaseDatabase.getInstance().getReference();

        //ánh xạ
        searchView = (SearchView) view.findViewById(R.id.searchPackage);
        listViewPackage = (ListView) view.findViewById(R.id.listviewPackage);
        fltButton = (FloatingActionButton) view.findViewById(R.id.floatingButton);


        //khởi tạo adapter và set cho list view
        packageArrayList = new ArrayList<>();
        packageAdapter = new PackageAdapter2(this.getActivity(), R.layout.item_listview_package, packageArrayList);
        listViewPackage.setAdapter(packageAdapter);

        //load danh sách dữ liệu package
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        String url;
        if(readPreference() == "VE"){
            url = "http://"+Constant.API_URL+":80/androidwebservice/getPackages.php?accountid=" + user.getUid() + "&package=packagevn";
        }else {
            url = "http://"+Constant.API_URL+":80/androidwebservice/getPackages.php?accountid=" + user.getUid() + "&package=package";
        }
        GetData(url);

        //sự kiện longclick lên 1 dòng để xóa
        listViewPackage.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Package pack = (Package) listViewPackage.getItemAtPosition(position); // lấy object dòng hiện tại
                ShowAlertDialog(user.getUid(), pack, position);
                return true;
            }
        });

        //sự kiện click 1 dòng trên listview
        listViewPackage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Package pack = (Package) listViewPackage.getItemAtPosition(position); // lấy object dòng hiện tại
                Intent intent = new Intent(MypackFragment.this.getActivity(), PackageInfoActivity.class);
                intent.putExtra("pack", pack);
                startActivity(intent);
            }
        });

        //xử lí sự kiện trong search view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                packageAdapter.getFilter().filter(query);
                return false;
            }
        });

        //sự kiện click vào floating button thêm package
        fltButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = mAuth.getCurrentUser();
                dialogAddPackage(user.getUid());
            }
        });


        return view; // trả về layout chứa giao diện của fragment
    }
    //mở dialog thêm package mới
    private void dialogAddPackage (final String uid){
        final Dialog dialog = new Dialog(MypackFragment.this.getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_package);

        dialog.setCanceledOnTouchOutside(true);

        final EditText edtPackName = (EditText) dialog.findViewById(R.id.edtPackName);
        final Button btnAddPack = (Button) dialog.findViewById(R.id.btnAddPack);


        btnAddPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = edtPackName.getText().toString().trim();

                //kiểm tra tên phải ít nhất 6 kí tự
                if(name.length() < 6){
                    Toast.makeText(MypackFragment.this.getActivity(), "Name at least 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    if(!(name.contains(".") || name.contains("#") || name.contains("$") || name.contains("[") || name.contains("]"))) {
                        if(readPreference() == "VE"){   //nếu từ điển VE
                            //thêm vào firebase
                            mData.child(uid).child("V-E_dict").child(name).child("0").setValue("0", new DatabaseReference.CompletionListener() {
                                //kiểm tra nếu lưu đc vào firebase (tức tên hợp lệ) thì thực hiện lưu vào mysql
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) { //nếu lưu thành công
                                        //lấy ngày hiện tại
                                        Calendar calendar = Calendar.getInstance();
                                        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd ");
                                        String strDate = mdformat.format(calendar.getTime());

                                        //thêm vào mysql
                                        String url = "http://"+Constant.API_URL+":80/androidwebservice/insertPackagevn.php?";
                                        InsertPackageMysql(url, name.trim(), strDate, uid);

                                        Toast.makeText(MypackFragment.this.getActivity(), "Add successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else { //nếu EV
                            //thêm vào firebase
                            mData.child(uid).child("E-V_dict").child(name).child("0").setValue("0", new DatabaseReference.CompletionListener() {
                                //kiểm tra nếu lưu đc vào firebase (tức tên hợp lệ) thì thực hiện lưu vào mysql
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) { //nếu lưu thành công
                                        //lấy ngày hiện tại
                                        Calendar calendar = Calendar.getInstance();
                                        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd ");
                                        String strDate = mdformat.format(calendar.getTime());

                                        //thêm vào mysql
                                        String url = "http://"+Constant.API_URL+":80/androidwebservice/insertPackage.php?";
                                        InsertPackageMysql(url, name.trim(), strDate, uid);

                                        Toast.makeText(MypackFragment.this.getActivity(), "Add successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    }else {
                        Toast.makeText(MypackFragment.this.getActivity(), "Invalid name!", Toast.LENGTH_SHORT).show();
                    }

                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    GetData("http://"+Constant.API_URL+":80/androidwebservice/getPackages.php?accountid=" + user.getUid() + "&package=package");
                        dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    //đọc dữ liệu từ đoạn JSON đc viết từ file PHP và đổ về mảng wordArrayList
    private void GetData(String url){
        RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Lấy dữ liệu từng object rồi thêm vào wordArrayList
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
                        Log.d(TAG, "error2: " +  error.toString());
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    //dialog xác nhận xoá gói
    public void ShowAlertDialog(final String uid, final Package pack, final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(MypackFragment.this.getActivity());
        builder.setTitle("kt Dictionary");
        builder.setMessage(getResources().getString(R.string.confirmDeletePackage));
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
                    String url = "http://"+Constant.API_URL+":80/androidwebservice/deletePackagevn.php";
                    DeletePackageMysql(url, Integer.toString(pack.getId()));

                    //delete khỏi firebase
                    mData.child(uid).child("V-E_dict").child(pack.getName().trim()).removeValue();
                }else {
                    //delete khỏi mysql
                    String url = "http://"+Constant.API_URL+":80/androidwebservice/deletePackage.php";
                    DeletePackageMysql(url, Integer.toString(pack.getId()));

                    //delete khỏi firebase
                    mData.child(uid).child("E-V_dict").child(pack.getName().trim()).removeValue();
                }

                //remove package vừa xóa và load lại listview
                packageArrayList.remove(position);
                packageAdapter.notifyDataSetChanged();

                Toast.makeText(MypackFragment.this.getActivity(), "Delete successfully", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
    //xử lí xóa package trong mysql
    private void DeletePackageMysql(String url, final String packid){
        RequestQueue requestQueue = Volley.newRequestQueue(MypackFragment.this.getActivity());
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
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    //xử lí thêm package trong mysql
    private void InsertPackageMysql(String url, final String name, final String strDate, final String account_id){
        RequestQueue requestQueue = Volley.newRequestQueue(MypackFragment.this.getActivity());
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
                params.put("name", name.trim());
                params.put("created_date", strDate);
                params.put("account_id", account_id.trim());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public String readPreference(){
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("dataDict", MODE_PRIVATE);
        return sharedPreferences.getString("dict","EV");
    }
}
