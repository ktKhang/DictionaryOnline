package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class WordAdapter2 extends BaseAdapter implements Filterable {
    private Context context;
    private int layout;
    private ArrayList<Word> wordList;
    WordAdapter2.CustomFilter filter;
    private ArrayList<Word> filterList ;

    public WordAdapter2(Context context, int layout, ArrayList<Word> wordList) {
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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final WordAdapter2.ViewHolder holder;
        if (convertView == null){
            holder = new WordAdapter2.ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            holder.txtWord = (TextView) convertView.findViewById(R.id.txtWord2);
            convertView.setTag(holder);
        }else{
            holder = (WordAdapter2.ViewHolder) convertView.getTag();
        }
        final Word word = wordList.get(position);
        holder.txtWord.setText(word.getWord());

        return convertView;
    }

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
                                filterList.get(i).getDetail1(), filterList.get(i).getDetail2());
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
