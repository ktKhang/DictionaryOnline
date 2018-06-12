package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class WordAdapter extends BaseAdapter implements Filterable{
    private Context context;
    private int layout;
    private ArrayList<Word> wordList;
    CustomFilter filter;
    private ArrayList<Word> filterList ;

    public WordAdapter(Context context, int layout, ArrayList<Word> wordList) {
        this.context = context;
        this.layout = layout;
        this.wordList = wordList;
        this.filterList = wordList;
    }

    @Override
    public int getCount() {
        return wordList.size();
    }

    @Override
    public Object getItem(int position) {
        return wordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return wordList.indexOf(getItem(position));
    }

    private class ViewHolder{
        TextView txtWord;
        ImageView imgAdd0;
        ImageView imgAdd1;
    }
    //xử lí load dữ liệu lên 1 view item và trả về convertView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            holder.txtWord = (TextView) convertView.findViewById(R.id.txtWord);
            holder.imgAdd0 = (ImageView) convertView.findViewById(R.id.imgAdd0);
            holder.imgAdd1 = (ImageView) convertView.findViewById(R.id.imgAdd1);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final Word word = wordList.get(position);

        holder.txtWord.setText(word.getWord());
        if(word.getInpack() > 0) {
            holder.imgAdd0.setVisibility(View.INVISIBLE);
            holder.imgAdd1.setVisibility(View.VISIBLE);
        }
        else {
            holder.imgAdd0.setVisibility(View.VISIBLE);
            holder.imgAdd1.setVisibility(View.INVISIBLE);
        }

        //set sự kiện khi click vào imgAdd0 hoặc imgAdd1
        holder.imgAdd0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                holder.imgAdd0.setVisibility(View.INVISIBLE);
                holder.imgAdd1.setVisibility(View.VISIBLE);
                //chuyển sang AddToPackageActivity
                Intent intent = new Intent(context, AddToPackageActivity.class);
                intent.putExtra("wordInfo", word);
                context.startActivity(intent);
            }
        });
        holder.imgAdd1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //chuyển sang AddToPackageActivity
                Intent intent = new Intent(context, AddToPackageActivity.class);
                intent.putExtra("wordInfo", word);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

//    //Nhận gtri trả về từ
//    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == REQUEST_CODE_INPUT && resultCode == RESULT_OK && data!= null){
//
//            Log.d(TAG, "resultdt: " +  data.getStringExtra("check"));
//            check = data.getStringExtra("check");
//        }else {
//            Toast.makeText(context, "Error ", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "errordt: ");
//        }
//    }

    //
    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new CustomFilter();
        }

        return filter;
    }

    //inner class
    class CustomFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if(constraint != null & constraint.length() >0){
                //constraint to upper
                constraint = constraint.toString().toUpperCase();

                ArrayList<Word> filters = new ArrayList<Word>();

                //get specific items
                for (int i=0; i<filterList.size(); i++){
                    if (filterList.get(i).getWord().toUpperCase().contains(constraint)){
                        Word w = new Word(filterList.get(i).getId(), filterList.get(i).getWord(),
                                filterList.get(i).getDetail1(), filterList.get(i).getDetail2(), filterList.get(i).getInpack());
                        filters.add(w);
                    }
                }
                results.count = filters.size();
                results.values = filters;

            }else {
                results.count = filterList.size();
                results.values = filterList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            wordList = (ArrayList<Word>) results.values;
            notifyDataSetChanged();
        }
    }

}
