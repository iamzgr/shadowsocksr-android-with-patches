package com.sugarsvalley.svpn.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by TJbaobao on 2017/11/2.
 * CSDN:http://blog.csdn.net/u013640004/article/details/78257536
 *
 * 当前版本:V1.1.4
 * 更新日志:
 *
 * V1.1.4 2018/01/03
 * 修改-实现单例模式，避免多实例导致的谷歌接口回调错乱问题。
 *
 * V1.1.3 2017/12/19
 * 修复-服务启动失败时导致的空指针错误。
 *
 * V1.1.2    2017/12/18
 * 修复-修复内购未被消耗的BUG。
 * 增加-每次启动都获取一次历史内购订单，并且全部消耗。
 * 增加-可以通过设置isAutoConsumeAsync来确定内购是否每次自动消耗。
 * 增加-将consumeAsync改为public，你可以手动调用消耗。
 *
 * V1.1.1  2017/11/2
 * 升级-内购API版本为google最新版本。compile 'com.android.billingclient:billing:1.0'
 * 特性-不需要key了，不需要IInAppBillingService.aidl了，不需要那一大堆Utils了，创建新实例的时候必须要传入购买回调接口。
 *
 * V1.0.3 2017/10/27
 * 增加-支持内购
 *
 * V1.0.2  2017/09/11
 * 修复-修复BUG
 *
 * v1.0.1 2017/07/29
 * 初始版本
 */


public class GoogleBillingUtil {
    private String[] inAppSKUS = new String[]{};//内购ID
    private String[] subsSKUS = new String[]{};//订阅ID

    public static final String BILLING_TYPE_INAPP = BillingClient.SkuType.INAPP;//内购
    public static final String BILLING_TYPE_SUBS = BillingClient.SkuType.SUBS;//订阅

    private static Context appContext = null;
    private static BillingClient mBillingClient;
    private static OnPurchaseFinishedListener mOnPurchaseFinishedListener;
    private static OnStartSetupFinishedListener mOnStartSetupFinishedListener ;
    private static OnQueryFinishedListener mOnQueryFinishedListener;

    private static boolean mIsServiceConnected = false;
    private boolean isAutoConsumeAsync = true;

    private static final GoogleBillingUtil mGoogleBillingUtil = new GoogleBillingUtil() ;

    private GoogleBillingUtil()
    {

    }

    public static GoogleBillingUtil getInstance(Context context)
    {
        appContext = context;
        cleanListener();
        return mGoogleBillingUtil;
    }

    public GoogleBillingUtil build()
    {
        if(mBillingClient==null)
        {
            synchronized (mGoogleBillingUtil)
            {
                if(mBillingClient==null)
                {
                    mBillingClient = BillingClient.newBuilder(appContext).setListener(mGoogleBillingUtil.new MyPurchasesUpdatedListener()).build();
                }
            }
        }
        synchronized (mGoogleBillingUtil)
        {
            if(!mIsServiceConnected)
            {
                mGoogleBillingUtil.startConnection();
            }
            else
            {
                mGoogleBillingUtil.queryInventoryInApp();
                mGoogleBillingUtil.queryInventorySubs();
                mGoogleBillingUtil.queryPurchasesInApp();
            }
        }
        return mGoogleBillingUtil;
    }

    public void startConnection()
    {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    mIsServiceConnected = true;
                    queryInventoryInApp();
                    queryInventorySubs();
                    queryPurchasesInApp();
                    if(mOnStartSetupFinishedListener!=null)
                    {
                        mOnStartSetupFinishedListener.onSetupSuccess();
                    }
                }
                else
                {
                    mIsServiceConnected = false;
                    if(mOnStartSetupFinishedListener!=null)
                    {
                        mOnStartSetupFinishedListener.onSetupFail(billingResponseCode);
                    }
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                mIsServiceConnected = false;
                if(mOnStartSetupFinishedListener!=null)
                {
                    mOnStartSetupFinishedListener.onSetupError();
                }
            }
        });
    }

    /**
     * Google购买商品回调接口(订阅和内购都走这个接口)
     */
    private class MyPurchasesUpdatedListener implements PurchasesUpdatedListener
    {

        @Override
        public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> list) {
            if(mOnPurchaseFinishedListener==null)
            {
                return ;
            }
            if(responseCode== BillingClient.BillingResponse.OK&&list!=null)
            {
                if(isAutoConsumeAsync)
                {
                    //消耗商品
                    for(Purchase purchase:list)
                    {
                        if(getSkuType(purchase.getSku()).equals(BillingClient.SkuType.INAPP))
                        {
                            consumeAsync(purchase.getPurchaseToken());
                        }
                    }
                }
                mOnPurchaseFinishedListener.onPurchaseSuccess(list);
            }
            else
            {
                mOnPurchaseFinishedListener.onPurchaseFail(responseCode);
            }
        }
    }

    /**
     * 查询内购商品信息
     */
    public void queryInventoryInApp()
    {
        queryInventory(BillingClient.SkuType.INAPP);
    }

    /**
     * 查询订阅商品信息
     */
    public void queryInventorySubs()
    {
        queryInventory(BillingClient.SkuType.SUBS);
    }

    private void queryInventory(final String skuType) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBillingClient == null)
                {
                    if(mOnQueryFinishedListener!=null)
                    {
                        mOnQueryFinishedListener.onQueryError();
                    }
                    return ;
                }
                ArrayList<String> skuList = new ArrayList<>();
                if(skuType.equals(BillingClient.SkuType.INAPP))
                {
                    for(String sku:inAppSKUS)
                    {
                        skuList.add(sku);
                    }
                }
                else if(skuType.equals(BillingClient.SkuType.SUBS))
                {
                    for(String sku:subsSKUS)
                    {
                        skuList.add(sku);
                    }
                }
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(skuType);
                mBillingClient.querySkuDetailsAsync(params.build(),new MySkuDetailsResponseListener(mOnQueryFinishedListener));
            }
        };
        executeServiceRequest(runnable);
    }

    /**
     * Google查询商品信息回调接口
     */
    private class MySkuDetailsResponseListener implements SkuDetailsResponseListener
    {
        private OnQueryFinishedListener mOnQueryFinishedListener ;
        public MySkuDetailsResponseListener(OnQueryFinishedListener onQueryFinishedListener) {
            mOnQueryFinishedListener = onQueryFinishedListener;
        }

        @Override
        public void onSkuDetailsResponse(int responseCode , List<SkuDetails> list) {

            if(mOnQueryFinishedListener==null)
            {
                return ;
            }
            if(responseCode== BillingClient.BillingResponse.OK&&list!=null)
            {
                mOnQueryFinishedListener.onQuerySuccess(list);
            }
            else
            {
                mOnQueryFinishedListener.onQueryFail(responseCode);
            }
        }

    }

    /**
     * 发起内购
     * @param skuId
     * @return
     */
    public void purchaseInApp(Activity activity,String skuId)
    {
        purchase(activity,skuId,BillingClient.SkuType.INAPP);
    }

    /**
     * 发起订阅
     * @param skuId
     * @return
     */
    public void purchaseSubs(Activity activity,String skuId)
    {
        purchase(activity,skuId,BillingClient.SkuType.SUBS);
    }

    private void purchase(Activity activity,final String skuId,final String skuType)
    {
        if(mIsServiceConnected)
        {
            if(mBillingClient==null)
            {
                if(mOnPurchaseFinishedListener!=null)
                {
                    mOnPurchaseFinishedListener.onPurchError();
                }
            }
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(skuType)
                    .build();
            mBillingClient.launchBillingFlow(activity,flowParams);
        }
        else
        {
            if(mOnPurchaseFinishedListener!=null)
            {
                mOnPurchaseFinishedListener.onPurchError();
            }
            startConnection();
        }
    }

    /**
     * 消耗商品
     * @param purchaseToken
     */
    public void consumeAsync(String purchaseToken)
    {
        if(mBillingClient==null)
        {
            return ;
        }
        mBillingClient.consumeAsync(purchaseToken, new MyConsumeResponseListener());
    }

    /**
     * Googlg消耗商品回调
     */
    private class MyConsumeResponseListener implements ConsumeResponseListener
    {
        @Override
        public void onConsumeResponse(int responseCode, String s) {
            if (responseCode == BillingClient.BillingResponse.OK) {

            }
        }
    }


    /**
     * 获取已经内购的商品
     * @return
     */
    public List<Purchase> queryPurchasesInApp()
    {
        return queryPurchases(BillingClient.SkuType.INAPP);
    }

    /**
     * 获取已经订阅的商品
     * @return
     */
    public List<Purchase> queryPurchasesSubs()
    {
        return queryPurchases(BillingClient.SkuType.SUBS);
    }

    private List<Purchase> queryPurchases(String skuType)
    {
        if(mBillingClient==null)
        {
            return null;
        }
        if(!mIsServiceConnected)
        {
            startConnection();
        }
        else
        {
            Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(skuType);
            if(purchasesResult!=null)
            {
                if(purchasesResult.getResponseCode()== BillingClient.BillingResponse.OK)
                {
                    List<Purchase> purchaseList =  purchasesResult.getPurchasesList();
                    if(isAutoConsumeAsync)
                    {
                        if(purchaseList!=null)
                        {
                            for(Purchase purchase:purchaseList)
                            {
                                if(skuType.equals(BillingClient.SkuType.INAPP))
                                {
                                    consumeAsync(purchase.getPurchaseToken());
                                }
                            }
                        }
                    }
                    return purchaseList;
                }
            }

        }
        return null;
    }

    /**
     * 获取有效订阅的数量
     * @return -1查询失败，0没有有效订阅，>0具有有效的订阅
     */
    public int getPurchasesSizeSubs()
    {
        List<Purchase > list = queryPurchasesSubs();
        if(list!=null)
        {
            return list.size();
        }
        return -1;
    }

    /**
     * 通过sku获取订阅商品序号
     * @param sku
     * @return
     */
    public int getSubsPositionBySku(String sku)
    {
        return getPositionBySku(sku, BillingClient.SkuType.SUBS);
    }

    /**
     * 通过sku获取内购商品序号
     * @param sku
     * @return
     */
    public int getInAppPositionBySku(String sku)
    {
        return getPositionBySku(sku, BillingClient.SkuType.INAPP);
    }

    private int getPositionBySku(String sku,String skuType)
    {

        if(skuType.equals(BillingClient.SkuType.INAPP))
        {
            int i = 0;
            for(String s:inAppSKUS)
            {
                if(s.equals(sku))
                {
                    return i;
                }
                i++;
            }
        }
        else if(skuType.equals(BillingClient.SkuType.SUBS))
        {
            int i = 0;
            for(String s:subsSKUS)
            {
                if(s.equals(sku))
                {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    private void executeServiceRequest(final Runnable runnable)
    {
        if(mIsServiceConnected)
        {
            runnable.run();
        }
        else
        {
            startConnection();
        }
    }

    /**
     * 通过序号获取订阅sku
     * @param position
     * @return
     */
    public String getSubsSkuByPosition(int position)
    {
        if(position>=0&&position<subsSKUS.length)
        {
            return subsSKUS[position];
        }
        else {
            return null;
        }
    }

    /**
     * 通过序号获取内购sku
     * @param position
     * @return
     */
    public String getInAppSkuByPosition(int position)
    {
        if(position>=0&&position<inAppSKUS.length)
        {
            return inAppSKUS[position];
        }
        else
        {
            return null;
        }
    }

    /**
     * 通过sku获取商品类型(订阅获取内购)
     * @param sku
     * @return inapp内购，subs订阅
     */
    private String getSkuType(String sku)
    {
        if(Arrays.asList(inAppSKUS).contains(sku))
        {
            return BillingClient.SkuType.INAPP;
        }
        else if(Arrays.asList(subsSKUS).contains(sku))
        {
            return BillingClient.SkuType.SUBS;
        }
        return null;
    }

    public GoogleBillingUtil setOnQueryFinishedListener(OnQueryFinishedListener onQueryFinishedListener) {
        mOnQueryFinishedListener = onQueryFinishedListener;
        return mGoogleBillingUtil;
    }

    public GoogleBillingUtil setOnPurchaseFinishedListener(OnPurchaseFinishedListener onPurchaseFinishedListener) {
        mOnPurchaseFinishedListener = onPurchaseFinishedListener;
        return mGoogleBillingUtil;
    }

    public OnStartSetupFinishedListener getOnStartSetupFinishedListener() {
        return mOnStartSetupFinishedListener;
    }

    public GoogleBillingUtil setOnStartSetupFinishedListener(OnStartSetupFinishedListener onStartSetupFinishedListener) {
        mOnStartSetupFinishedListener = onStartSetupFinishedListener;
        return mGoogleBillingUtil;
    }

    /**
     *  本工具查询回调接口
     */
    public interface OnQueryFinishedListener{
        public void onQuerySuccess(List<SkuDetails> list);
        public void onQueryFail(int responseCode);
        public void onQueryError();
    }

    /**
     * 本工具购买回调接口(内购与订阅都走这接口)
     */
    public interface OnPurchaseFinishedListener{

        public void onPurchaseSuccess(List<Purchase> list);

        public void onPurchaseFail(int responseCode);

        public void onPurchError();

    }

    /**
     * oogle服务启动接口
     */
    public interface OnStartSetupFinishedListener{
        public void onSetupSuccess();

        public void onSetupFail(int responseCode);

        public void onSetupError();
    }

    public boolean isServiceConnected() {
        return mIsServiceConnected;
    }

    public boolean isAutoConsumeAsync()
    {
        return isAutoConsumeAsync;
    }

    public void setIsAutoConsumeAsync(boolean isAutoConsumeAsync)
    {
        this.isAutoConsumeAsync= isAutoConsumeAsync;
    }

    public static void cleanListener()
    {
        mOnPurchaseFinishedListener = null;
        mOnQueryFinishedListener = null;
        mOnStartSetupFinishedListener = null;
    }

}
