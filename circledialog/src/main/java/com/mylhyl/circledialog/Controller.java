package com.mylhyl.circledialog;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;

import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.view.BuildViewAdImpl;
import com.mylhyl.circledialog.view.BuildViewConfirmImpl;
import com.mylhyl.circledialog.view.BuildViewCustomBodyImpl;
import com.mylhyl.circledialog.view.BuildViewInputImpl;
import com.mylhyl.circledialog.view.BuildViewItemsListViewImpl;
import com.mylhyl.circledialog.view.BuildViewItemsRecyclerViewImpl;
import com.mylhyl.circledialog.view.BuildViewLottieImpl;
import com.mylhyl.circledialog.view.BuildViewPopupImpl;
import com.mylhyl.circledialog.view.BuildViewProgressImpl;
import com.mylhyl.circledialog.view.listener.AdView;
import com.mylhyl.circledialog.view.listener.ButtonView;
import com.mylhyl.circledialog.view.listener.CloseView;
import com.mylhyl.circledialog.view.listener.InputView;
import com.mylhyl.circledialog.view.listener.ItemsView;
import com.mylhyl.circledialog.view.listener.OnAdItemClickListener;
import com.mylhyl.circledialog.view.listener.OnRvItemClickListener;

/**
 * Created by hupei on 2017/3/29.
 */

public final class Controller {

    public static final boolean SDK_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final boolean SDK_JELLY_BEAN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

    private Context mContext;
    private CircleParams mParams;
    private BuildView mCreateView;
    private OnDialogInternalListener mOnDialogInternalListener;

    public Controller(Context context, CircleParams params, OnDialogInternalListener dialogInternalListener) {
        this.mContext = context;
        this.mParams = params;
        this.mOnDialogInternalListener = dialogInternalListener;
        BackgroundHelper.INSTANCE.init(context, params);
    }

    public static int dp2px(Context context, float value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value
                , context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public void createView() {

        // lottie动画框
        if (mParams.lottieParams != null) {
            mCreateView = new BuildViewLottieImpl(mContext, mParams);
            mCreateView.buildBodyView();
        }
        // 自定义内容视图
        else if (mParams.bodyViewId != 0 || mParams.bodyView != null) {
            mCreateView = new BuildViewCustomBodyImpl(mContext, mParams);
            mCreateView.buildBodyView();
            View bodyView = mCreateView.getBodyView();
            if (mParams.createBodyViewListener != null)
                mParams.createBodyViewListener.onCreateBodyView(bodyView);
        }
        // 广告
        else if (mParams.adParams != null) {
            mCreateView = new BuildViewAdImpl(mContext, mParams);
            mCreateView.buildBodyView();
            AdView bodyView = mCreateView.getBodyView();
            bodyView.regOnImageClickListener(new OnAdItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position) {
                    if (mParams.adItemClickListener != null) {
                        boolean b = mParams.adItemClickListener.onItemClick(view, position);
                        if (b) {
                            mOnDialogInternalListener.dialogDismiss();
                        }
                    }
                    return false;
                }
            });
        }
        // popup
        else if (mParams.popupParams != null) {
            int[] screenSize = mOnDialogInternalListener.getScreenSize();
            int statusBarHeight = mOnDialogInternalListener.getStatusBarHeight();
            mCreateView = new BuildViewPopupImpl(mContext, mOnDialogInternalListener, mParams
                    , screenSize, statusBarHeight);
            mCreateView.buildBodyView();
            final ItemsView itemsView = mCreateView.getBodyView();
            itemsView.regOnItemClickListener(new OnRvItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position) {
                    if (mParams.rvItemListener != null) {
                        boolean b = mParams.rvItemListener.onItemClick(view, position);
                        if (b) {
                            mOnDialogInternalListener.dialogDismiss();
                        }
                    }
                    return false;
                }
            });
        }

        // 列表
        else if (mParams.itemsParams != null) {
            //设置列表特殊的参数
            DialogParams dialogParams = mParams.dialogParams;
            // FIXME: hupei 2019/5/30 since 4.0.2修复 设置 dialogParams.gravity 无效的bug
            if (dialogParams.gravity == Gravity.NO_GRAVITY) {
                dialogParams.gravity = Gravity.BOTTOM;//默认底部显示
            }
            //判断是否已经设置过
            if (dialogParams.gravity == Gravity.BOTTOM && dialogParams.yOff == -1) {
                dialogParams.yOff = 20;//底部与屏幕的距离
            }
            if (mParams.itemListViewType) {
                mCreateView = new BuildViewItemsListViewImpl(mContext, mParams);
                mCreateView.buildBodyView();
                final ItemsView itemsView = mCreateView.getBodyView();
                itemsView.regOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (mParams.itemListener != null) {
                            boolean b = mParams.itemListener.onItemClick(parent, view, position, id);
                            if (b) {
                                mOnDialogInternalListener.dialogDismiss();
                            }
                        }
                    }
                });
            } else {
                mCreateView = new BuildViewItemsRecyclerViewImpl(mContext, mParams);
                mCreateView.buildBodyView();
                final ItemsView itemsView = mCreateView.getBodyView();
                itemsView.regOnItemClickListener(new OnRvItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position) {
                        if (mParams.rvItemListener != null) {
                            boolean b = mParams.rvItemListener.onItemClick(view, position);
                            if (b) {
                                mOnDialogInternalListener.dialogDismiss();
                            }
                        }
                        return false;
                    }
                });
            }
        }
        // 进度条
        else if (mParams.progressParams != null) {
            mCreateView = new BuildViewProgressImpl(mContext, mParams);
            mCreateView.buildBodyView();
        }
        // 输入框
        else if (mParams.inputParams != null) {
            mCreateView = new BuildViewInputImpl(mContext, mParams);
            mCreateView.buildBodyView();
        }
        // 文本
        else {
            mCreateView = new BuildViewConfirmImpl(mContext, mParams);
            mCreateView.buildBodyView();
        }

        // 图标x关闭按钮
        if (mParams.closeParams != null) {
            CloseView closeView = mCreateView.buildCloseImgView();
            closeView.regOnCloseClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnDialogInternalListener.dialogDismiss();
                }
            });
        }

        // 底部按钮
        ButtonView buttonView = mCreateView.buildButton();
        regNegativeListener(buttonView);
        regNeutralListener(buttonView);
        if (mParams.inputParams != null) {
            InputView inputView = mCreateView.getBodyView();
            //输入框确定按钮事件特殊性
            regPositiveInputListener(buttonView, inputView);
        } else {
            regPositiveListener(buttonView);
        }
    }

    public void refreshView() {
        getView().post(new Runnable() {
            @Override
            public void run() {
                mCreateView.refreshTitle();
                mCreateView.refreshContent();
                mCreateView.refreshButton();
                //刷新时带动画
                if (mParams.dialogParams.refreshAnimation != 0 && getView() != null) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, mParams.dialogParams.refreshAnimation);
                    if (animation != null) {
                        getView().startAnimation(animation);
                    }
                }
            }
        });
    }

    View getView() {
        return mCreateView.getRootView();
    }

    private void regNegativeListener(final ButtonView viewButton) {
        viewButton.regNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mParams.clickNegativeListener != null) {
                    mParams.clickNegativeListener.onClick(v);
                }
                mOnDialogInternalListener.dialogDismiss();
            }
        });
    }

    private void regNeutralListener(final ButtonView viewButton) {
        viewButton.regNeutralListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mParams.clickNeutralListener != null) {
                    mParams.clickNeutralListener.onClick(v);
                }
                mOnDialogInternalListener.dialogDismiss();
            }
        });
    }

    private void regPositiveInputListener(final ButtonView viewButton, final InputView inputView) {
        viewButton.regPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = inputView.getInput();
                String text = editText.getText().toString();
                if (mParams.inputListener != null) {
                    boolean b = mParams.inputListener.onClick(text, editText);
                    if (b) {
                        mOnDialogInternalListener.dialogDismiss();
                    }
                }
            }
        });
    }

    private void regPositiveListener(final ButtonView viewButton) {
        viewButton.regPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mParams.clickPositiveListener != null) {
                    mParams.clickPositiveListener.onClick(v);
                }
                mOnDialogInternalListener.dialogDismiss();
            }
        });
    }

    public interface OnDialogInternalListener {

        void dialogAtLocation(int x, int y);

        void dialogDismiss();

        int[] getScreenSize();

        int getStatusBarHeight();
    }
}
