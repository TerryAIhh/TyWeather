package com.cjlu.tyweather.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.cjlu.tyweather.R;
import com.cjlu.tyweather.base.BaseFragment;
import com.cjlu.tyweather.bean.WeatherBean;
import com.cjlu.tyweather.databinding.FragmentCityWeatherBinding;
import com.cjlu.tyweather.db.DbManager;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CityWeatherFragment extends BaseFragment implements View.OnClickListener {
    private FragmentCityWeatherBinding binding;
    String url1 = "http://api.map.baidu.com/telematics/v3/weather?location=";
    String url2 = "&output=json&ak=FkPhtMBK0HTIQNh7gG4cNUttSTyr0nzo";
    private List<WeatherBean.ResultsBean.IndexBean> indexList;
    String city;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_city_weather, container, false);
        // 设置点击事件监听
        binding.fragTvDressIndex.setOnClickListener(this);
        binding.fragTvCarIndex.setOnClickListener(this);
        binding.fragTvColdIndex.setOnClickListener(this);
        binding.fragTvSportIndex.setOnClickListener(this);
        binding.fragTvRaysIndex.setOnClickListener(this);
        changeBg();
        // 可以通过 activity 传值获取到当前 fragment 城市
        Bundle bundle = getArguments();
        city = bundle.getString("city");
        String url = url1 + city + url2;
        // 调用 BaseFragment 获取数据方法
        loadData(url);
        return binding.getRoot();
    }

    @Override
    public void onSuccess(String result) {
        // 解析并展示数据
        showData(result);
        // 更新数据
        int i = DbManager.updateInfoByCity(city, result);
        if (i <= 0) {
            // 更新失败，没有这条记录，则增加城市记录
            DbManager.addCityInfo(city, result);
        }
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        String lastResult = DbManager.queryInfoByCity(city);
        if (!TextUtils.isEmpty(lastResult)) {
            showData(lastResult);
        }
    }

    // 更新背景图
    public void changeBg() {
        SharedPreferences bg_pref = getActivity().getSharedPreferences("bg_pref", MODE_PRIVATE);
        int bg_num = bg_pref.getInt("bg", 2);
        switch (bg_num) {
            case 0:
                binding.fragLayout.setBackgroundResource(R.color.colorAccent);
                break;
            case 1:
                binding.fragLayout.setBackgroundResource(R.color.pink);
                break;
            case 2:
                binding.fragLayout.setBackgroundResource(R.color.gray_bg);
                break;
        }
    }

    @SuppressLint("InflateParams")
    private void showData(String result) {
        WeatherBean weatherBean = new Gson().fromJson(result, WeatherBean.class);
        WeatherBean.ResultsBean resultsBean = weatherBean.getResults().get(0);
        // 获取指数信息集合列表
        indexList = resultsBean.getIndex();
        // 设置今日天气信息
        WeatherBean.ResultsBean.WeatherDataBean todayDataBean = resultsBean.getWeather_data().get(0);
        binding.fragTvWind.setText(todayDataBean.getWind());
        binding.fragTvTempRange.setText(todayDataBean.getTemperature());
        binding.fragTvWeather.setText(todayDataBean.getWeather());
        // 处理字符串  周四 04月23日 (实时：15℃)
        String[] str = todayDataBean.getDate().split("：");
        String temp = str[1].replace(")", "");
        binding.fragTvCurrentTemp.setText(temp);
        // 设置未来三天的天气信息
        binding.centerTvDate1.setText(resultsBean.getWeather_data().get(1).getDate());
        binding.centerTvDate2.setText(resultsBean.getWeather_data().get(2).getDate());
        binding.centerTvDate3.setText(resultsBean.getWeather_data().get(3).getDate());
        binding.centerTvTempRange1.setText(resultsBean.getWeather_data().get(1).getTemperature());
        binding.centerTvTempRange2.setText(resultsBean.getWeather_data().get(2).getTemperature());
        binding.centerTvTempRange3.setText(resultsBean.getWeather_data().get(3).getTemperature());
        binding.centerTvWeather1.setText(resultsBean.getWeather_data().get(1).getWeather());
        binding.centerTvWeather2.setText(resultsBean.getWeather_data().get(2).getWeather());
        binding.centerTvWeather3.setText(resultsBean.getWeather_data().get(3).getWeather());
        Picasso.with(getActivity()).load(resultsBean.getWeather_data().get(1).getDayPictureUrl()).into(binding.centerIv1);
        Picasso.with(getActivity()).load(resultsBean.getWeather_data().get(2).getDayPictureUrl()).into(binding.centerIv2);
        Picasso.with(getActivity()).load(resultsBean.getWeather_data().get(3).getDayPictureUrl()).into(binding.centerIv3);
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        switch (v.getId()) {
            case R.id.frag_tv_dress_index:
                builder.setTitle("穿衣指数");
                WeatherBean.ResultsBean.IndexBean indexBean = indexList.get(0);
                String msg = indexBean.getZs() + "\n" + indexBean.getDes();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_tv_car_index:
                builder.setTitle("洗车指数");
                indexBean = indexList.get(1);
                msg = indexBean.getZs() + "\n" + indexBean.getDes();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_tv_cold_index:
                builder.setTitle("感冒指数");
                indexBean = indexList.get(2);
                msg = indexBean.getZs() + "\n" + indexBean.getDes();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_tv_sport_index:
                builder.setTitle("运动指数");
                indexBean = indexList.get(3);
                msg = indexBean.getZs() + "\n" + indexBean.getDes();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_tv_rays_index:
                builder.setTitle("紫外线指数");
                indexBean = indexList.get(4);
                msg = indexBean.getZs() + "\n" + indexBean.getDes();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
        }
        builder.create().show();
    }
}
