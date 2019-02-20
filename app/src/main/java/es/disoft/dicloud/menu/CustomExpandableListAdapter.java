package es.disoft.dicloud.menu;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import es.disoft.dicloud.R;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context                         context;
    private List<MenuModel>                 listDataHeader;
    private Map<MenuModel, List<MenuModel>> listDataChild;

    CustomExpandableListAdapter(Context context,
                                       List<MenuModel> listDataHeader,
                                       Map<MenuModel, List<MenuModel>> listChildData) {

        this.context        = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild  = listChildData;
    }

    @Override
    public MenuModel getChild(int groupPosition, int childPosititon) {
        return listDataChild.get(listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition,
                             int childPosition,
                             boolean isLastChild,
                             View convertView,
                             ViewGroup parent) {

        String childText = getChild(groupPosition, childPosition).menuName;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView       = li.inflate(R.layout.list_group_child, null);
        }

        TextView txtListChild = convertView.findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        if (listDataChild.get(listDataHeader.get(groupPosition)) == null) return 0;
        else return listDataChild.get(listDataHeader.get(groupPosition)).size();
    }

    @Override
    public MenuModel getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();

    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition,
                             boolean isExpanded,
                             View convertView,
                             ViewGroup parent) {

        String headerTitle    = getGroup(groupPosition).menuName;
        Integer headerIconId  = getGroup(groupPosition).iconPathId;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView       = li.inflate(R.layout.list_group_header, null);
        }

        int color = ResourcesCompat.getColor(context.getResources(), R.color.menuItems, null);
        int arrow = -1;
        if (getGroup(groupPosition).hasChildren) {
            if (isExpanded) {
                arrow = R.drawable.ic_expand_less;
                color = ResourcesCompat.getColor(context.getResources(), R.color.menuItemsFocus, null);
            } else {
                arrow = R.drawable.ic_expand_more;
            }
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setText(headerTitle);
        lblListHeader.setTextColor(color);

        ImageView imgIconListHeader = convertView.findViewById(R.id.imgIconListHeader);
        if (headerIconId != null) {
            imgIconListHeader.setImageResource(headerIconId);
            imgIconListHeader.setColorFilter(color);
        } else {
            imgIconListHeader.setImageBitmap(null);
            imgIconListHeader.setColorFilter(null);
        }

        ImageView imgArrowListHeader = convertView.findViewById(R.id.imgArrowListHeader);
        if (getGroup(groupPosition).hasChildren) {
            imgArrowListHeader.setImageResource(arrow);
            imgArrowListHeader.setColorFilter(color);
        } else {
            imgArrowListHeader.setImageBitmap(null);
            imgArrowListHeader.setColorFilter(null);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}