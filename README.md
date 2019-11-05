# coolweather
Android 第一行代码（第二版）的实战项目
<br>
酷欧天气 coolweather
<br>

- #### 使用的开源库

1. okHttp
2. Glide
3. gson

- #### 在写的过程遇见过一些问题

1. okHttp3 开源框架下，实现的sendOkHttpRequest ，收到接口地址访问结果，status != ok ，总是onFailure情况。

2. 模拟器上可以运行，手机上加载失败（加载城市）。 在queryCounties中调用 queryFromServer（）目的是获得服务器返回的天气数据。通过sendOkHttpRequesst()方法。返回callback 进入了onFailure（）方法。

   模拟器能够跑起来，URL这些是没有问题的。

   手机使用的Android 9.0 应该需要在某处设置一些参数。(未解决)

   ```java
   private void queryCounties(){
       //访问服务器 获取数据
          String address = "http://guolin.tech/api/china/"+provinceCode + "/" + cityCode;
          queryFromServer(address,"county");
       }
   
   
   /**
        * 根据传入的地址 和 类型 从服务器上查询省市,,查询后存入数据库 ，再调用上面三个方法
        */
   
       private void queryFromServer(String address , final String type){
           showProgressDialog();
           HttpUtil.sendOkHttpRequest(address, new Callback() {
   
   
               @Override
               public void onFailure(Call call, IOException e) {
                   //通过runOnUiThread() 方法回到主线程
                   getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           closeProgressDialog();
                           Toast.makeText(getContext()
                                          ,"加载失败"
                                          ,Toast.LENGTH_SHORT).show();
                       }
                   });
               }
   
               @Override
               public void onResponse(Call call, Response response) throws IOException {
                   String responseText = response.body().string();
                   boolean result = false;
                   if ("province".equals(type)){
                       result = Utility.handleProvinceResponse(responseText);
                   }else if ("city".equals(type)){
                       result = Utility
                           .handleCityResponse(responseText , selectedProvince.getId());
                   }else if( "county".equals(type)){
                       result = Utility
                           .handleCountyResponse(responseText,selectedCity.getId());
                   }
                   
                   if(result){
                       getActivity().runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               closeProgressDialog();
                               if ("province".equals(type)){
                                   queryProvinces();
                               }else if("city".equals(type)){
                                   queryCities();
                               }else if ("county".equals(type)){
                                   queryCounties();
                               }
                           }
                       });
                   }
               }
           });
       }
   
   ```

   

3. 每次下拉刷新，数据变动（理应不变）。(未解决)