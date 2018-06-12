package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PackageAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private ArrayList<Package> packageList;

    public PackageAdapter(Context context, int layout, ArrayList<Package> packageList) {
        this.context = context;
        this.layout = layout;
        this.packageList = packageList;
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
            holder.txtPackName = (TextView) convertView.findViewById(R.id.txtPackName);
            holder.txtCreatedDate = (TextView) convertView.findViewById(R.id.txtCreatedDate);
            holder.txtNumberWords = (TextView) convertView.findViewById(R.id.txtNumberWords);
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
}
