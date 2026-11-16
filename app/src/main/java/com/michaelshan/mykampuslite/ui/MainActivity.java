package com.michaelshan.mykampuslite.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.michaelshan.mykampuslite.R;
import com.michaelshan.mykampuslite.databinding.ActivityMainBinding;
import com.michaelshan.mykampuslite.ui.dosen.DosenFragment;
import com.michaelshan.mykampuslite.ui.mahasiswa.MahasiswaFragment;

/**
 * MainActivity adalah host tunggal navigasi aplikasi dengan dua tab menggunakan ViewPager2.
 * Mengikuti prinsip pemisahan tanggung jawab (Separation of Concerns):
 * tidak memiliki logic bisnis, validasi, maupun akses database langsung.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupViewPagerAndTabs();
    }

    private void setupViewPagerAndTabs() {
        AkademikPagerAdapter pagerAdapter = new AkademikPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Mahasiswa");
            } else {
                tab.setText("Dosen");
            }
        }).attach();
    }

    /**
     * Adapter ViewPager2 untuk mengelola tab Mahasiswa dan Dosen.
     */
    private static class AkademikPagerAdapter extends FragmentStateAdapter {

        public AkademikPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new MahasiswaFragment();
            } else {
                return new DosenFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
