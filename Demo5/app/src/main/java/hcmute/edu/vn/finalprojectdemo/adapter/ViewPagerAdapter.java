package hcmute.edu.vn.finalprojectdemo.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import hcmute.edu.vn.finalprojectdemo.fragment.AudioFragment;
import hcmute.edu.vn.finalprojectdemo.fragment.ImageFragment;
import hcmute.edu.vn.finalprojectdemo.fragment.SearchFragment;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {


    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ImageFragment();
            case 1:
                return new AudioFragment();
            default:
                return new SearchFragment();
        }
    }
    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    public CharSequence getPageTitle(int position){
        String title="";
        switch (position){
            case 0:
                title ="Image";
                break;
            case 1:
                title ="Audio";
                break;
            default:
                title ="Search";
                break;
        }
        return title;
    }


}
