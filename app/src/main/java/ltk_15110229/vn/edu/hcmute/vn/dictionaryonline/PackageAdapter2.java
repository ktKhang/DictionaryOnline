package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class PackageAdapter2 extends BaseAdapter implements Filterable {
    private Context context;
    private int layout;
    private ArrayList<Package> packageList;
    CustomFilter filter;
    private ArrayList<Package> filterList ;

    public PackageAdapter2(Context context, int layout, ArrayList<Package> packageList) {
        this.context = context;
        this.layout = layout;
        this.packageList = packageList;
        this.filterList = packageList;
    }
    @Override
    public int getCount() {
        return packageList.size();
    }

    @Override
    public Object getItem(int position) {
        return packageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return packageList.indexOf(getItem(position));
    }

    private class ViewHolder{
        TextView txtPackName;
        TextView txtCreatedDate;
        TextView txtNumberWords;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            holder.txtPackName = (TextView) convertView.findViewById(R.id.txtNamePack);
            holder.txtCreatedDate = (TextView) convertView.findViewById(R.id.txtCreatedDatePack);
            holder.txtNumberWords = (TextView) convertView.findViewById(R.id.txtNumberWordsPack);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Package pack = packageList.get(position);
        holder.txtPackName.setText(pack.getName());
        holder.txtCreatedDate.setText(pack.getCreated_date());
        holder.txtNumberWords.setText(Integer.toString(pack.getNumber_words()));

        return convertView;
    }

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

                ArrayList<Package> filters = new ArrayList<Package>();

                //get specific items
                for (int i=0; i<filterList.size(); i++){
                    if (filterList.get(i).getName().toUpperCase().contains(constraint)){
                        Package p = new Package(filterList.get(i).getId(), filterList.get(i).getName(),
                                filterList.get(i).getCreated_date(), filterList.get(i).getNumber_words());
                        filters.add(p);
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
            packageList = (ArrayList<Package>) results.values;
            notifyDataSetChanged();
        }
    }
}
