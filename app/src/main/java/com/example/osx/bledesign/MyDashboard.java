package com.example.osx.bledesign;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by osx on 23/12/15.
 */
public class MyDashboard extends Fragment {

    View view = null;

    @Bind(R.id.sliding_tabs)
    TabLayout sliding_tabs;

    @Bind(R.id.viewpager)
    ViewPager viewpager;

    @Bind(R.id.btn_add)
    ImageView btn_add;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, null);
        ButterKnife.bind(this, view);
        MyAdapter adapter = new MyAdapter(getChildFragmentManager());
        viewpager.setAdapter(adapter);
        sliding_tabs.setupWithViewPager(viewpager);
        getActivity().setTitle(getString(R.string.my_dashboard));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_add)
    public void Add() {
        ((MainActivity) getActivity()).FragmentTransactions(new AddTracker(), "addtracker");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.ic_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
        System.out.println("inside on prepare optionsmenu addtracker");
    }
}
