package com.pl.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pl.app.model.Department;
import com.pl.app.model.Staff;
import com.pl.app.views.PinnedHeaderExpandableListView;
import com.pl.app.views.StickyLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener,
        PinnedHeaderExpandableListView.OnHeaderUpdateListener, StickyLayout.OnQuitTouchEventListener {
    private PinnedHeaderExpandableListView expandableListView;
    private StickyLayout stickyLayout;
    private ArrayList<Department> departments;
    private ArrayList<List<Staff>> childList;
    private MyExpandableAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_activity);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        expandableListView = (PinnedHeaderExpandableListView) findViewById(R.id.expandablelist);
        stickyLayout = (StickyLayout)findViewById(R.id.sticky_layout);
        initData();

        adapter = new MyExpandableAdapter(this);
        expandableListView.setAdapter(adapter);

        // 展开所有group
        for (int i = 0, count = expandableListView.getCount(); i < count; i++) {
            expandableListView.expandGroup(i);
        }

        expandableListView.setOnHeaderUpdateListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnGroupClickListener(this);
        stickyLayout.setOnQuitTouchEventListener(this);
    }

    private void initData() {
        departments = new ArrayList<>();
        Department department;
        department = new Department("人力资源部", "Herd Rolex");
        departments.add(department);
        department = new Department("产品设计部", "Alex Pop");
        departments.add(department);
        department = new Department("产品开发一部", "James Cheng");
        departments.add(department);
        department = new Department("产品开发二部", "Alon Guan");
        departments.add(department);
        department = new Department("产品测试部", "Alin Dan");
        departments.add(department);

        childList = new ArrayList<List<Staff>>();
        for (int i = 0; i < departments.size(); i++) {
            ArrayList<Staff> childTemp;
            if (i == 0) {
                childTemp = new ArrayList<Staff>();
                for (int j = 0; j < 5; j++) {
                    Staff people = new Staff();
                    people.setName("yy-" + j);
                    people.setAge(35);
                    people.setTitle("sh-" + j);

                    childTemp.add(people);
                }
            } else if (i == 1) {
                childTemp = new ArrayList<Staff>();
                for (int j = 0; j < 8; j++) {
                    Staff people = new Staff();
                    people.setName("ff-" + j);
                    people.setAge(30);
                    people.setTitle("sh-" + j);

                    childTemp.add(people);
                }
            } else {
                childTemp = new ArrayList<Staff>();
                for (int j = 0; j < 12; j++) {
                    Staff people = new Staff();
                    people.setName("hh-" + j);
                    people.setAge(20);
                    people.setTitle("sh-" + j);

                    childTemp.add(people);
                }
            }
            childList.add(childTemp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Toast.makeText(MainActivity.this, childList.get(groupPosition).get(childPosition).getName(), Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        return false;
    }

    @Override
    public View getPinnedHeader() {
        View headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.group_item, null);
        headerView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        return headerView;
    }

    @Override
    public void updatePinnedHeader(View headerView, int firstVisibleGroupPos) {
        Department firstVisibleGroup = (Department) adapter.getGroup(firstVisibleGroupPos);
        TextView textView = (TextView) headerView.findViewById(R.id.group);
        textView.setText(firstVisibleGroup.getName());
    }

    @Override
    public boolean quitTouchEvent(MotionEvent event) {
        if (expandableListView.getFirstVisiblePosition() == 0) {
            View view = expandableListView.getChildAt(0);
            if (view != null && view.getTop() >= 0) {
                return true;
            }
        }
        return false;
    }

    class MyExpandableAdapter extends BaseExpandableListAdapter {
        private LayoutInflater inflater;
        public MyExpandableAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getGroupCount() {
            return departments.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childList.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return departments.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder;
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = inflater.inflate(R.layout.group_item, null);
                holder.departmentName = (TextView)convertView.findViewById(R.id.group);
                holder.imageView = (ImageView)convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }
            holder.departmentName.setText(((Department)getGroup(groupPosition)).getName());
            if (isExpanded)
                holder.imageView.setImageResource(R.mipmap.expanded);
            else
                holder.imageView.setImageResource(R.mipmap.collapse);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.child_item, null);
                holder.textName = (TextView)convertView.findViewById(R.id.name);
                holder.textAge = (TextView)convertView.findViewById(R.id.age);
                holder.staffTitle = (TextView)convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }
            holder.staffTitle.setText(((Staff)getChild(groupPosition, childPosition)).getTitle());
            holder.textName.setText(((Staff)getChild(groupPosition, childPosition)).getName());
            //holder.textAge.setText(((Staff)getChild(groupPosition, childPosition)).getAge());

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    class GroupHolder {
        TextView departmentName;
        ImageView imageView;
    }

    class ChildHolder {
        TextView textName;
        TextView textAge;
        TextView staffTitle;
        ImageView imageView;
    }
}
