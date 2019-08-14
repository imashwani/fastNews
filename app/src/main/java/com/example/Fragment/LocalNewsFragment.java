package com.example.Fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fastnews.MainActivity;
import com.example.fastnews.R;

public class LocalNewsFragment extends Fragment {

    public LocalNewsFragment() {
        // Required empty public constructor
    }

    public static final String ARG_CATEGORY = "category";
    View rootView;
    ViewPager viewPager;
    TabLayout tabLayout;
    ViewPagerAdapter adapter;
    MainActivity activityContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_local_news, container, false);
        viewPager = rootView.findViewById(R.id.view_pager);
        setupViewPager(viewPager);

        tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getChildFragmentManager());
        addToAdapter("business");
        addToAdapter("technology");
        addToAdapter("science");
        addToAdapter("health");
        addToAdapter("entertainment");
        addToAdapter("sports");
        viewPager.setAdapter(adapter);
    }

    void addToAdapter(String category) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CATEGORY, category);
        NewsFragment newsFragment = new NewsFragment();
        newsFragment.setFragmentActionListener(activityContext);
        newsFragment.setArguments(bundle);
        adapter.addFragment(newsFragment, category.toUpperCase().charAt(0) + category.substring(1));
    }

    public void setActivityContext(MainActivity activityContext) {
        this.activityContext = activityContext;
    }
}
