package com.txtled.avs.wwa;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.TouchNetUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseFragment;
import com.txtled.avs.bean.WWADeviceInfo;
import com.txtled.avs.main.MainActivity;
import com.txtled.avs.utils.Utils;
import com.txtled.avs.wwa.mvp.WWAContract;
import com.txtled.avs.wwa.mvp.WWAPresenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.txtled.avs.utils.Constants.REQUEST_CODE_WIFI_SETTINGS;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class WWAFragment extends MvpBaseFragment<WWAPresenter> implements WWAContract.View
        , View.OnClickListener, DialogInterface.OnClickListener, SwipeRefreshLayout.OnRefreshListener
        , WWAAdapter.OnWWAItemClickListener {
    private static final String TAG = WWAFragment.class.getSimpleName();

    @BindView(R.id.ap_ssid_text)
    TextView mApSsidTV;
    @BindView(R.id.ap_bssid_text)
    TextView mApBssidTV;
    @BindView(R.id.ap_password_edit)
    TextInputEditText mApPasswordET;
    @BindView(R.id.device_count_edit)
    TextInputEditText mDeviceCountET;
    @BindView(R.id.package_broadcast)
    RadioButton packageBroadcast;
    @BindView(R.id.package_multicast)
    RadioButton packageMulticast;
    @BindView(R.id.package_mode_group)
    RadioGroup mPackageModeGroup;
    @BindView(R.id.message)
    TextView mMessageTV;
    @BindView(R.id.confirm_btn)
    MaterialButton mConfirmBtn;
    @BindView(R.id.rl_setting_page)
    RelativeLayout rlSettingPage;
    @BindView(R.id.tv_register)
    TextView tvRegister;
    @BindView(R.id.rlv_wwa_device)
    RecyclerView rlvWwaDevice;
    @BindView(R.id.srl_wwa_devices)
    SwipeRefreshLayout srlWwaDevices;
    @BindView(R.id.rl_wwa_device)
    RelativeLayout rlWwaDevice;
    private boolean isFinishedConfigure;
    private WWAAdapter adapter;
    private ArrayList<WWADeviceInfo> data;


    @Override
    protected void initInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wwa, null);
    }

    @Override
    public void init() {
        isFinishedConfigure = presenter.getIsConfigured();
        if (isFinishedConfigure) {
            tvRegister.setVisibility(View.GONE);
            rlSettingPage.setVisibility(View.GONE);
            rlWwaDevice.setVisibility(View.VISIBLE);
        }

        rlvWwaDevice.setHasFixedSize(true);
        rlvWwaDevice.setLayoutManager(new LinearLayoutManager(getContext()));
        srlWwaDevices.setOnRefreshListener(this);
        data = new ArrayList<>();
        adapter = new WWAAdapter(data, getContext());
        adapter.setListener(this);

        rlvWwaDevice.setAdapter(adapter);

        mDeviceCountET.setText("1");
        mConfirmBtn.setEnabled(false);
        mConfirmBtn.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        presenter.init(getContext());

        //连接iot
        presenter.getAmazonIotService();
        //presenter.createThing();

    }

    public void changeResetView(boolean type) {
        if (type) {
            tvRegister.setVisibility(View.VISIBLE);
            rlSettingPage.setVisibility(View.GONE);
            rlWwaDevice.setVisibility(View.GONE);
        } else {
            tvRegister.setVisibility(View.GONE);
            rlSettingPage.setVisibility(View.GONE);
            rlWwaDevice.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.destroy();
    }

    @Override
    public void registerBroadcastReceiver() {
        presenter.registerBroadcast();
        tvRegister.setVisibility(View.GONE);
        rlWwaDevice.setVisibility(View.GONE);
        rlSettingPage.setVisibility(View.VISIBLE);
    }

    @Override
    public void confirm() {
        presenter.setConfigured(false);
        ((MainActivity) getActivity()).removeNavigationIcon();
        byte[] ssid = mApSsidTV.getTag() == null ? ByteUtil.getBytesByString(mApSsidTV.getText().toString())
                : (byte[]) mApSsidTV.getTag();
        byte[] password = ByteUtil.getBytesByString(mApPasswordET.getText().toString());
        byte[] bssid = TouchNetUtil.parseBssid2bytes(mApBssidTV.getText().toString());
        byte[] deviceCount = mDeviceCountET.getText().toString().getBytes();
        byte[] broadcast = {(byte) (mPackageModeGroup.getCheckedRadioButtonId() == R.id.package_broadcast
                ? 1 : 0)};

        presenter.executeTask(this, ssid, bssid, password, deviceCount, broadcast);
    }

    @Override
    public void register() {
        if (presenter.isSDKAtLeastP()) {
            presenter.checkPermission(getActivity());
        } else {
            registerBroadcastReceiver();
        }
    }

    @Override
    public void setNoWifiView() {
        mApSsidTV.setText("");
        mApSsidTV.setTag(null);
        mApBssidTV.setText("");
        mMessageTV.setText(R.string.no_wifi_connection);
        mConfirmBtn.setEnabled(false);
        ((MainActivity) getActivity()).showSnackBar(rlSettingPage, R.string.no_wifi_conn, R.string.go, this);
    }

    @Override
    public void showAlertDialog(int configure_wifi_change_message, int cancel, @Nullable DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(getContext())
                .setMessage(configure_wifi_change_message)
                .setNegativeButton(cancel, listener)
                .show();
    }

    @Override
    public void setInfo(String ssid, WifiInfo info) {
        mApSsidTV.setText(ssid);
        mApSsidTV.setTag(ByteUtil.getBytesByString(ssid));
        byte[] ssidOriginalData = TouchNetUtil.getOriginalSsidBytes(info);
        mApSsidTV.setTag(ssidOriginalData);

        String bssid = info.getBSSID();
        mApBssidTV.setText(bssid);

        mConfirmBtn.setEnabled(true);
        mMessageTV.setText("");
        if (((MainActivity) getActivity()).snackbar != null &&
                ((MainActivity) getActivity()).snackbar.isShown()) {
            ((MainActivity) getActivity()).hideSnackBar();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int frequency = info.getFrequency();
            if (frequency > 4900 && frequency < 5900) {
                // Connected 5G wifi. Device does not support 5G
                mMessageTV.setText(R.string.wifi_5g_message);
            }
        }
    }

    @Override
    public void checkLocation() {
        boolean enable;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        enable = locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager);
        if (!enable) {
            mMessageTV.setText(R.string.location_disable_message);
        }
    }

    @Override
    public void hidSnackBar() {
        Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
        ((MainActivity) getActivity()).hideSnackBar();
    }

    @Override
    public void setData(ArrayList<WWADeviceInfo> strReceive) {

        if (strReceive != null) {
            try {
                JSONObject data = new JSONObject().getJSONObject("{\"ip\":\"设备IP地址\"," +
                        "\"netmask\":\"设备子网掩码\",\"gw\":\"设备网关地址\"," +
                        "\"host\":\"AWS Endpoint\",\"port\":AWS Endpoint端口," +
                        "\"cid\":\"ClientID\",\"thing\":\"事物名称\"}");
                WWADeviceInfo info = new WWADeviceInfo(data.getString("ip"),
                        data.getString("netmask"),
                        data.getString("gw"),
                        data.getString("host"),
                        data.getString("port"),
                        data.getString("cid"),
                        data.getString("thing"));
                info.setConfigure(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.setData(strReceive);
            adapter.notifyDataSetChanged();
        } else {
            ((MainActivity) getActivity()).showSnackBar(srlWwaDevices, R.string.no_wifi_connection, R.string.go, v -> {
                Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
                ((MainActivity) getActivity()).hideSnackBar();
            });
        }
        srlWwaDevices.setRefreshing(false);
    }

    @Override
    public void closeRefresh() {
        srlWwaDevices.setRefreshing(false);
        Toast.makeText(getContext(), R.string.nothing, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        presenter.onViewClick(v.getId());
    }

    //完成时ok按钮
    @Override
    public void onClick(DialogInterface dialog, int which) {
        rlSettingPage.setVisibility(View.GONE);
        rlWwaDevice.setVisibility(View.VISIBLE);
        presenter.setConfigured(true);
        ((MainActivity) getActivity()).setNavigationIcon(false);
        srlWwaDevices.setRefreshing(true);
        presenter.onRefresh();
    }

    //刷新
    @Override
    public void onRefresh() {
        presenter.onRefresh();
        presenter.hasData();
    }

    //配置iot core
    @Override
    public void onWWAClick(int position) {

    }

    public void insertWWAInfo(List<IEsptouchResult> data) {
        presenter.insertInfo(data);
    }

    public static class EsptouchAsyncTask4 extends AsyncTask<byte[], IEsptouchResult, List<IEsptouchResult>> {
        private WeakReference<WWAFragment> mActivity;

        private final Object mLock = new Object();
        private ProgressDialog mProgressDialog;
        private AlertDialog mResultDialog;
        private IEsptouchTask mEsptouchTask;

        public EsptouchAsyncTask4(WWAFragment activity) {
            mActivity = new WeakReference<>(activity);
        }

        public void cancelEsptouch() {
            cancel(true);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (mResultDialog != null) {
                mResultDialog.dismiss();
            }
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }

        @Override
        protected void onPreExecute() {
            Fragment activity = mActivity.get();
            mProgressDialog = new ProgressDialog(activity.getContext());
            mProgressDialog.setMessage(activity.getString(R.string.configuring_message));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(dialog -> {
                synchronized (mLock) {
                    if (mEsptouchTask != null) {
                        mEsptouchTask.interrupt();
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getText(android.R.string.cancel),
                    (dialog, which) -> {
                        synchronized (mLock) {
                            if (mEsptouchTask != null) {
                                mEsptouchTask.interrupt();
                            }
                        }
                    });
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(IEsptouchResult... values) {
            Context context = mActivity.get().getContext();
            if (context != null) {
                IEsptouchResult result = values[0];
                Utils.Logger(TAG, "EspTouchResult: ", String.valueOf(result));
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(byte[]... params) {
            WWAFragment activity = mActivity.get();
            int taskResultCount;
            synchronized (mLock) {
                byte[] apSsid = params[0];
                byte[] apBssid = params[1];
                byte[] apPassword = params[2];
                byte[] deviceCountData = params[3];
                byte[] broadcastData = params[4];
                taskResultCount = deviceCountData.length == 0 ? -1 : Integer.parseInt(new String(deviceCountData));
                Context context = activity.getActivity().getApplicationContext();
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, context);
                mEsptouchTask.setPackageBroadcast(broadcastData[0] == 1);
                mEsptouchTask.setEsptouchListener(this::publishProgress);
            }
            return mEsptouchTask.executeForResults(taskResultCount);
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            WWAFragment activity = mActivity.get();
            mProgressDialog.dismiss();
            if (result == null) {
                mResultDialog = new AlertDialog.Builder(activity.getContext())
                        .setMessage(R.string.configure_result_failed_port)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                mResultDialog.setCanceledOnTouchOutside(false);
                return;
            }

            // check whether the task is cancelled and no results received
            IEsptouchResult firstResult = result.get(0);
            if (firstResult.isCancelled()) {
                return;
            }
            // the task received some results including cancelled while
            // executing before receiving enough results

            if (!firstResult.isSuc()) {
                mResultDialog = new AlertDialog.Builder(activity.getContext())
                        .setMessage(R.string.configure_result_failed)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                mResultDialog.setCanceledOnTouchOutside(false);
                return;
            }

            ArrayList<CharSequence> resultMsgList = new ArrayList<>(result.size());
            for (IEsptouchResult touchResult : result) {
                String message = activity.getString(R.string.configure_result_success_item,
                        touchResult.getBssid(), touchResult.getInetAddress().getHostAddress());
                resultMsgList.add(message);
            }

            //activity.insertWWAInfo(result);

            CharSequence[] items = new CharSequence[resultMsgList.size()];
            mResultDialog = new AlertDialog.Builder(activity.getContext())
                    .setTitle(R.string.configure_result_success)
                    .setItems(resultMsgList.toArray(items), null)
                    .setPositiveButton(android.R.string.ok, activity)
                    .show();
            mResultDialog.setCanceledOnTouchOutside(false);
        }
    }
}
