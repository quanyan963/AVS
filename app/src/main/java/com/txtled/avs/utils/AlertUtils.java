package com.txtled.avs.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.txtled.avs.R;
import com.txtled.avs.wwa.listener.OnCreateThingListener;


public class AlertUtils {
    private static OnCreateThingListener thingListener;

    public static void showErrorMessage(Context context, int titleRes,
                                        String errorCode, DialogInterface.OnClickListener listener) {
//        if (!((Activity) context).isFinishing()) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(context)
//                    .setMessage(context.getResources().getIdentifier("ERROR_CODE_" + errorCode,
//                            "string", context.getPackageName()));
//            if (titleRes != 0) {
//                builder.setTitle(titleRes);
//            }
//            if (listener == null) {
//                builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//            } else {
//                builder.setNegativeButton(R.string.ok, listener);
//            }
//            Dialog dialog = builder.create();
//            dialog.setCancelable(true);
//            dialog.show();
//        }
    }

    public static void showErrorMessage(Context context, String errorCode) {
        showErrorMessage(context, 0, errorCode, null);
    }

    public static void showErrorMessage(Context context,
                                        String errorCode, DialogInterface.OnClickListener listener) {
        showErrorMessage(context, 0, errorCode, listener);
    }

    public static void showAlertDialog(Context context, String message,
                                       DialogInterface.OnClickListener listener0,
                                       DialogInterface.OnClickListener listener1) {
//        if (!((Activity) context).isFinishing()) {
//            AlertDialog dialog = new AlertDialog.Builder(context)
//                    .setMessage(message)
//                    .setNegativeButton(R.string.cancel, listener0)
//                    .setPositiveButton(R.string.confirm, listener1)
//                    .create();
//            dialog.setCancelable(true);
//            dialog.show();
//        }
    }

//    public static void showScheduleDialog(Context context, List<List<ScheduleDetail>> mData) {
//        if (!((Activity) context).isFinishing()) {
//            LayoutInflater layoutInflater = LayoutInflater.from(context);
//            View view = layoutInflater.inflate(R.layout.dialog_schedule, null);
//            AlertDialog dialog = new AlertDialog.Builder(context)
//                    .setView(view)
//                    .create();
//            dialog.setCanceledOnTouchOutside(true);
//            RecyclerView list = (RecyclerView) view.findViewById(R.id.rlv_schedule);
//            LinearLayoutManager manager = new LinearLayoutManager(context){
//                @Override
//                public boolean canScrollVertically() {
//                    return false;
//                }
//            };
//            list.setHasFixedSize(true);
//            list.setLayoutManager(manager);
//            list.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL_LIST,
//                    context.getResources().getDimensionPixelSize(R.dimen.dp_8_y),
//                    context.getResources().getColor(R.color.light_grey)));
//            String[] mTitle = context.getResources().getStringArray(R.array.day);
//            ScheduleAdapter adapter = new ScheduleAdapter(mData,mTitle,context);
//            list.setAdapter(adapter);
//            dialog.setCancelable(true);
//            dialog.show();
//        }
//    }

//    public static void showCaptChaDialog(final Context context, final CaptchaListener listener){
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//        View view = layoutInflater.inflate(R.layout.dialog_captcha, null);
//        final AlertDialog dialog = new AlertDialog.Builder(context)
//                .setView(view)
//                .create();
//        dialog.setCanceledOnTouchOutside(true);
//        ImageView ivCaptcha = (ImageView) view.findViewById(R.id.iv_captcha);
//        final EditText etCaptcha = (EditText) view.findViewById(R.id.et_captcha);
//        MyApplication.getImageLoader(context).displayImage(Url.COMMENT+Url.CAPTCHA+ Math.random(),ivCaptcha);
//        //ivCaptcha.setImageURI(Uri.parse());
//        etCaptcha.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                if (i == EditorInfo.IME_ACTION_SEND){
//                    Utils.hideSoftKeyboard(context,etCaptcha);
//                    listener.submit(etCaptcha.getText().toString());
//                    dialog.dismiss();
//                    return true;
//                }
//                return false;
//            }
//        });
//        dialog.setCancelable(true);
//        dialog.show();
//    }
    public interface OnConfirmClickListener{
        void onConfirmClick(String friendlyName);
    }

    public static OnCreateThingListener getThingListener(){
        return thingListener;
    }

    public static void showAlertDialog(Context context, int viewId,
                                       OnConfirmClickListener listener) {
        if (!((Activity) context).isFinishing()) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(viewId, null);
            TextInputEditText editText = (TextInputEditText) view.findViewById(R.id.alert_wwa_item);
            TextView textView = (TextView) view.findViewById(R.id.tv_alert_create);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(view)
                    .setNegativeButton(R.string.cancel, (dialog12, which) -> dialog12.dismiss())
                    .setPositiveButton(R.string.confirm,null)
                    .create();
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().trim().isEmpty()){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(true);
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                listener.onConfirmClick(editText.getText().toString());
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            });
            thingListener = new OnCreateThingListener() {
                @Override
                public void onStatueChange(int str) {
                    ((Activity) context).runOnUiThread(() -> {
                        textView.setText(str);
                        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                        animation.setDuration(500);
                        textView.setAnimation(animation);
                        animation.start();
                    });
                }

                @Override
                public void dismiss() {
                    dialog.dismiss();
                }
            };
        }
    }

    public static void showAlertDialog(Context context, int title, int message) {
        if (!((Activity) context).isFinishing()) {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    public static void showAlertDialog(Context context, String message) {
        if (!((Activity) context).isFinishing()) {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    public static void showAlertDialog(Context context, int messageRes) {
        if (!((Activity) context).isFinishing()) {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage(messageRes)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.setCancelable(true);
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    public static void showAlertDialog(Context context, int messageRes,
                                       DialogInterface.OnClickListener listener) {
        if (!((Activity) context).isFinishing()) {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage(messageRes)
                    .setPositiveButton(R.string.ok, listener)
                    .create();
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    public static void showProgressDialog(Context context, int id) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(id));
        progressDialog.show();
    }

//    public static void showEditDialog(final Context context, final OnEditDialogClickListener listener) {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        View dialogView = LayoutInflater.from(context)
//                .inflate(R.layout.alert_dialog, null);
//        final CustomEditText etName = (CustomEditText) dialogView.
//                findViewById(R.id.et_name);
//
//        etName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                if (etName.getText().toString().length() > 20) {
//                    etName.setError(context.getString(R.string.input_limit));
//                }
//            }
//        });
//
//        builder.setView(dialogView);
//        builder.setTitle(R.string.collect);
//        builder.setPositiveButton(R.string.ok, null);
//        builder.setNegativeButton(R.string.cancel, null);
//
//        final AlertDialog dialog = builder.create();
//
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface mDialog) {
//                final Button positionButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                final Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
////                positionButton.setTextColor(context.getResources().getColor(R.color.yellow_seek_bar));
////                negativeButton.setTextColor(context.getResources().getColor(R.color.yellow_seek_bar));
//
//                positionButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (etName.getText().toString().length() < 21 &&
//                                etName.getText().toString().length() > 0) {
//                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
////                            if (imm.isActive()){
////                                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
////                            }
//                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                            dialog.dismiss();
//                            listener.onEditDialogClick(etName.getText().toString());
//                        }
//                    }
//                });
//                negativeButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
////                        if (imm.isActive()){
////                            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
////                        }
//                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                        dialog.dismiss();
//                    }
//                });
//            }
//        });
//        //dialog.getWindow().setBackgroundDrawableResource(R.color.pop_window_bg);
//        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        dialog.show();
//    }

    public interface OnEditDialogClickListener {
        void onEditDialogClick(String str);
    }

    public static void showSnackbar(Context context, View view, int text){
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
        snackbar.show();
    }
}
