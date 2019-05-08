package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class HomeFragment extends Fragment {
    private ListView listViewWord;
    ArrayList<Word> wordArrayList;
    WordAdapter wordAdapter;
    private FirebaseAuth mAuth;
    private SearchView search_view;
    private String dict;
    FloatingActionButton fltButton;
    String TAG = "MainActivity";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        listViewWord = (ListView) view.findViewById(R.id.listviewWord);
        search_view = (SearchView) view.findViewById(R.id.search_view2);
        fltButton = (FloatingActionButton) view.findViewById(R.id.btnAddWord);

        Log.d("khang1", "k1");
        //khởi tạo adapter và set cho list view
        wordArrayList = new ArrayList<>();
        wordAdapter = new WordAdapter(this.getActivity(), R.layout.item_listview_word, wordArrayList);
        listViewWord.setAdapter(wordAdapter);
        Log.d("khang2", "k2");
        //load danh sách dữ liệu word
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String url;
        Log.d("khang3", "k3");
        if (readPreference()=="VE") {
            url = "http://"+Constant.API_URL+":80/androidwebservice/getdata.php?accountid=" + user.getUid() + "&tblword=wordvn";
        }else {
            url = "http://"+Constant.API_URL+":80/androidwebservice/getdata.php?accountid=" + user.getUid() + "&tblword=word";
        }
        Log.d("khang4", "k4");
        GetData(url);

        //xử lí sự kiện trong search view
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

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

        //sự kiện click 1 dòng item trên listview, chuyển dữ liệu sang activity WordInfoActivity
        listViewWord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Word w = (Word) listViewWord.getItemAtPosition(position);   //lấy object dòng hiện tại

                Intent intent = new Intent(HomeFragment.this.getActivity(), WordInfoActivity.class);
                intent.putExtra("wordInfo", w);

                startActivity(intent);
            }
        });

        //sự kiện click vào floating button thêm word
        fltButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = mAuth.getCurrentUser();
                dialogAddWord(user.getUid());
            }
        });

        return view; // trả về layout chứa giao diện của fragment
    }

    //mở dialog thêm package mới
    private void dialogAddWord (final String uid){
        final Dialog dialog = new Dialog(HomeFragment.this.getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_word);

        dialog.setCanceledOnTouchOutside(true);

        final EditText edtWord = (EditText) dialog.findViewById(R.id.edtAddWordName);
        final EditText edtTrans = (EditText) dialog.findViewById(R.id.edtAddWordTrans);
        final Button btnAddWord = (Button) dialog.findViewById(R.id.btnAddWord);

        btnAddWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String word = edtWord.getText().toString().trim();
                final String trans = edtTrans.getText().toString().trim();

                    if(!(word.contains(".") || word.contains("#") || word.contains("$") || word.contains("[") || word.contains("]"))) {
                        //thêm vào mysql
                        String url = "http://"+Constant.API_URL+":80/androidwebservice/insertWord.php?";
                        InsertWordMysql(url, word.trim(), trans.trim(), uid);

                        Toast.makeText(HomeFragment.this.getActivity(), "Add successfully", Toast.LENGTH_SHORT).show();

                    }else {
                        Toast.makeText(HomeFragment.this.getActivity(), "Invalid name!", Toast.LENGTH_SHORT).show();
                    }

                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                GetData("http://"+Constant.API_URL+":80/androidwebservice/getdata.php?accountid=" + user.getUid() + "&tblword=word");
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    //xử lí thêm word
    private void InsertWordMysql(String url, final String word, final String trans, final String account_id){
        RequestQueue requestQueue = Volley.newRequestQueue(HomeFragment.this.getActivity());
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

                Log.d("word", word);
                Log.d("trans", trans);
                Log.d("acc", account_id);
                //truyền tham số
                params.put("word", word.trim());
                params.put("trans", trans);
                params.put("account_id", account_id.trim());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    //đọc dữ liệu từ đoạn JSON đc viết từ file PHP và đổ về mảng wordArrayList
    private void GetData(String url){
        RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity());
        Log.d("khang5", "k5");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("khang6", response.toString());
                        //Lấy dữ liệu từng object rồi thêm vào wordArrayList
                        for (int i=0; i<response.length() ; i++){
                            try {
                                JSONObject object = response.getJSONObject(i);
                                wordArrayList.add(new Word(object.getInt("ID"),
                                        object.getString("Word"),
                                        object.getString("Detail1"),
                                        object.getString("Detail2"),
                                        object.getInt("Inpack")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d("khang", wordArrayList.toString());
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

    public String readPreference(){
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("dataDict", MODE_PRIVATE);
        return sharedPreferences.getString("dict","EV");
    }


//    //nhận kết quả của HomeActivity và chuyển đến hàm gọi lại trong word adapter
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        wordAdapter.onActivityResult(requestCode, resultCode, data);
////        super.onActivityResult(requestCode, resultCode, data);
//    }
}
