package com.example.blog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.blog.adapter.ViewpagerAdapter
import com.example.blog.databinding.ActivityMainBinding
import com.example.blog.fragment.HomeFragment
import com.example.blog.fragment.NewPostFragment
import com.example.blog.fragment.ProfileFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // xử lí viewPager
        val fragmentList = arrayListOf<Fragment>(HomeFragment(),NewPostFragment(),ProfileFragment())
        val viewpagerAdapter = ViewpagerAdapter(fragmentList,supportFragmentManager)
        binding.viewPager.adapter = viewpagerAdapter
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                binding.bottomNavigation.menu.getItem(position).setChecked(true)
            }

            override fun onPageScrollStateChanged(state: Int) {}

        })

        // xử lí bottom navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    binding.viewPager.currentItem = 0
                }
                R.id.createPost -> {
                    binding.viewPager.currentItem = 1
                }
                R.id.profile ->{
                    binding.viewPager.currentItem = 2
                }
            }
            false
        }
    }
}