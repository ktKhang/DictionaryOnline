package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class HomeFragment extends Fragment {
    private ListView listViewWord;
    ArrayList<Word> wordArrayList;
    WordAdapter wordAdapter;
    private FirebaseAuth mAuth;
    private SearchView search_view;
    private String dict;
    String TAG = "MainActivity";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        listViewWord = (ListView) view.findViewById(R.id.listviewWord);
        search_view = (SearchView) view.findViewById(R.id.search_view2);

        //khởi tạo adapter và set cho list view
        wordArrayList = new ArrayList<>();
        wordAdapter = new WordAdapter(this.getActivity(), R.layout.item_listview_word, wordArrayList);
        listViewWord.setAdapter(wordAdapter);

        //load danh sách dữ liệu word
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String url;
        if (readPreference()=="VE") {
            url = "http://172.20.10.9:8080/androidwebservice/getdata.php?accountid=" + user.getUid() + "&tblword=wordvn";
        }else {
            url = "http://172.20.10.9:8080/androidwebservice/getdata.php?accountid=" + user.getUid() + "&tblword=word";
        }
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
        return view; // trả về layout chứa giao diện của fragment
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
                                wordArrayList.add(new Word(object.getInt("ID"),
                                        object.getString("Word"),
                                        object.getString("Detail1"),
                                        object.getString("Detail2"),
                                        object.getInt("Inpack")));
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
