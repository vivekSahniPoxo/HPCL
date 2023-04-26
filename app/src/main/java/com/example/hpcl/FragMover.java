package com.example.hpcl;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class FragMover {

    public static void replaceFrag(FragmentManager fm, Fragment fragment, Integer main) {
        fm.beginTransaction().replace(main, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    public static void replaceFragStack(FragmentManager fm, Fragment fragment, Integer main) {
        fm.beginTransaction()
                .replace(main, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public static void addFrag(FragmentManager fm, Fragment fragment, Integer containerId) {
        fm.beginTransaction()
                .add(containerId, fragment, fragment.getClass().getSimpleName())
                .commit();
    }



    public static void addFragToStack(FragmentManager fm, Fragment fragment, Integer main) {
        fm.beginTransaction()
                .add(main, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }


}