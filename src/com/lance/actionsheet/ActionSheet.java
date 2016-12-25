package com.lance.actionsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 模仿微信的底部弹出菜单,只能用在FragmentActivity界面,可以back键取消,但是如果在使用了Fragment的界面需要使用
 * 引用手动调用dismiss取消
 * 不需要作为依赖库,所以定制的话需要修改源码
 * 
 * @author ganchengkai
 * 
 */
@SuppressLint("NewApi")
public class ActionSheet extends Fragment implements OnClickListener {
	private static final String ACTION_SHEET_TITLE = "action_sheet_title";
	private static final String CANCEL_BUTTON_TITLE = "cancel_button_title";
	private static final String OTHER_BUTTON_TITLES = "other_button_titles";
	private static final String MARK_BUTTON_INDEX = "mark_button_index";// 标记为红色按钮
	private static final String CANCELABLE_ONTOUCHOUTSIDE = "cancelable_ontouchoutside";

	private static final int SHADE_VIEW_ID = 100;// 用于检测是否需要dismiss
	private static final int TRANSLATE_DURATION = 200;
	private static final int ALPHA_DURATION = 300;

	private boolean mDismissed = true;
	private ActionSheetListener mListener;
	private ViewGroup mDecordView;// 所有视图之上的容器视图
	private View mRootView;// 内容视图的容器
	private View mShadeView;// 弹出框上半透明阴影
	private LinearLayout mPanel;// 具体内容视图

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			View focusView = getActivity().getCurrentFocus();
			if (focusView != null) {
				imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
			}
		}

		mRootView = createView();
		mDecordView = (ViewGroup) getActivity().getWindow().getDecorView();

		createItems();

		mDecordView.addView(mRootView);
		mShadeView.startAnimation(createAlphaInAnimation());
		mPanel.startAnimation(createTranslationInAnimation());
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	private Animation createTranslationInAnimation() {
		int type = TranslateAnimation.RELATIVE_TO_SELF;
		TranslateAnimation anim = new TranslateAnimation(type, 0, type, 0,
				type, 1, type, 0);
		anim.setInterpolator(new LinearInterpolator());
		anim.setDuration(TRANSLATE_DURATION);
		return anim;
	}

	private Animation createAlphaInAnimation() {
		AlphaAnimation anim = new AlphaAnimation(0, 1);
		anim.setDuration(ALPHA_DURATION);
		return anim;
	}

	private Animation createTranslationOutAnimation() {
		int type = TranslateAnimation.RELATIVE_TO_SELF;
		TranslateAnimation anim = new TranslateAnimation(type, 0, type, 0,
				type, 0, type, 1);
		anim.setInterpolator(new LinearInterpolator());
		anim.setDuration(TRANSLATE_DURATION);
		anim.setFillAfter(true);
		return anim;
	}

	private Animation createAlphaOutAnimation() {
		AlphaAnimation anim = new AlphaAnimation(1, 0);
		anim.setDuration(ALPHA_DURATION);
		anim.setFillAfter(true);
		return anim;
	}

	private View createView() {
		FrameLayout parent = new FrameLayout(getActivity());
		parent.setLayoutParams(new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));
		mShadeView = new View(getActivity());
		mShadeView.setLayoutParams(new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));
		mShadeView.setBackgroundColor(Color.argb(136, 0, 0, 0));
		mShadeView.setId(ActionSheet.SHADE_VIEW_ID);
		mShadeView.setOnClickListener(this);

		mPanel = new LinearLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		mPanel.setLayoutParams(params);
		mPanel.setOrientation(LinearLayout.VERTICAL);
		mPanel.setBackgroundColor(Color.argb(255, 233, 233, 238));
		//mPanel.setPadding(0, dp2px(15), 0, dp2px(15));
		mPanel.setOnClickListener(null);// 防止点击空白组件后消失

		parent.addView(mShadeView);
		parent.addView(mPanel);
		return parent;
	}
	
	private int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getActivity().getResources().getDisplayMetrics());
    }

	/**
	 * 创建其他按钮及title组件
	 */
	private void createItems() {
		String title = getActionSheetTitle();
		if(title != null && !"".equals(title)){
			TextView titleView = new TextView(getActivity());
			titleView.setTextColor(Color.argb(255, 100, 100, 100));
			titleView.setTextSize(14);
			titleView.setGravity(Gravity.CENTER);
			LinearLayout.LayoutParams params = createButtonLayoutParams();
			params.bottomMargin = dp2px(1);
			titleView.setLayoutParams(params);
			titleView.setPadding(dp2px(10), dp2px(15), dp2px(10), dp2px(15));
			
			titleView.setText(title);
			mPanel.addView(titleView);
		}
		int markIndex = getMarkButtonIndex();
		String[] titles = getOtherButtonTitles();
		if (titles != null) {
			for (int i = 0; i < titles.length; i++) {
				Button bt = new Button(getActivity());
				bt.setId(i);
				bt.setOnClickListener(this);
				bt.setBackground(makeStateDrawable());
				bt.setText(titles[i]);
				bt.setTextColor(Color.BLACK);
				if(markIndex == i){
					bt.setTextColor(Color.argb(255, 255, 10, 10));
				}
				bt.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2px(14));
				if (i > 0) {
					LinearLayout.LayoutParams params = createButtonLayoutParams();
					params.topMargin = dp2px(1);
					mPanel.addView(bt, params);
				} else {
					mPanel.addView(bt);
				}
			}
		}
		// 创建取消按钮
		Button bt = new Button(getActivity());
		bt.getPaint().setFakeBoldText(true);
		bt.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2px(14));
		bt.setId(titles == null? 0:titles.length);
		bt.setBackground(makeStateDrawable());
		bt.setText(getCancelButtonTitle());
		bt.setTextColor(Color.BLACK);
		if(titles != null && titles.length == markIndex){
			bt.setTextColor(Color.argb(255, 255, 10, 10));
		}
		bt.setOnClickListener(this);
		LinearLayout.LayoutParams params = createButtonLayoutParams();
		params.topMargin = dp2px(5);
		mPanel.addView(bt, params);

	}
	
	// 创建按钮状态
	private Drawable makeStateDrawable(){
		StateListDrawable drawable = new StateListDrawable();
		ColorDrawable pressedDrawable = new ColorDrawable(Color.argb(255, 200, 200, 200));
		ColorDrawable defaultDrawable = new ColorDrawable(Color.argb(255, 229, 229, 229));
		drawable.addState(new int[] {android.R.attr.state_pressed}, pressedDrawable);
		drawable.addState(new int[] {}, defaultDrawable);// 放到最后才有效果
		return drawable;
	}

	public LinearLayout.LayoutParams createButtonLayoutParams() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		return params;
	}

	@Override
	public void onDestroyView() {
		mPanel.startAnimation(createTranslationOutAnimation());
		mShadeView.startAnimation(createAlphaOutAnimation());
		mRootView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mDecordView.removeView(mRootView);
			}
		}, ALPHA_DURATION);
		if (mListener != null) {
			mListener.onDismiss(this);
		}
		super.onDestroyView();
	}

	public void show(FragmentManager manager, String tag) {
		if (!mDismissed) {
			return;
		}
		mDismissed = false;
		FragmentTransaction ft = manager.beginTransaction();
		ft.add(this, tag);
		ft.addToBackStack(null);
		ft.commit();
	}

	public void dismiss() {
		if (mDismissed) {
			return;
		}
		mDismissed = true;
		getFragmentManager().popBackStack();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.remove(this);
		ft.commit();
	}

	private String getActionSheetTitle() {
		return getArguments().getString(ACTION_SHEET_TITLE);
	}

	private String getCancelButtonTitle() {
		return getArguments().getString(CANCEL_BUTTON_TITLE);
	}

	private String[] getOtherButtonTitles() {
		return getArguments().getStringArray(OTHER_BUTTON_TITLES);
	}
	
	private int getMarkButtonIndex(){
		return getArguments().getInt(MARK_BUTTON_INDEX);
	}

	private boolean getCancelableOnTouchOutside() {
		return getArguments().getBoolean(CANCELABLE_ONTOUCHOUTSIDE);
	}
	
	public void setActionSheetListener(ActionSheetListener listener) {
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == ActionSheet.SHADE_VIEW_ID
				&& !getCancelableOnTouchOutside()) {
			return;
		}
		dismiss();
		if (mListener != null) {
			mListener.onClickButtonAtIndex(this, v.getId());
		}
	}

	public static Builder createBuilder(Context context,
			FragmentManager fragmentManager) {
		return new Builder(context, fragmentManager);
	}

	public static class Builder {

		private Context mContext;
		private FragmentManager mFragmentManager;
		private String mActionSheetTitle;
		private String mCancelButtonTitle;
		private int mMarkButtonIndex = -1;// 默认未标记
		private String[] mOtherButtonTitles;
		private String mTag = "actionSheet";
		private boolean mCancelableOnTouchOutside;
		private ActionSheetListener mListener;

		public Builder(Context context, FragmentManager fragmentManager) {
			mContext = context;
			mFragmentManager = fragmentManager;
		}

		public Builder setTitle(String title) {
			mActionSheetTitle = title;
			return this;
		}

		public Builder setTitle(int strId) {
			return setTitle(mContext.getString(strId));
		}

		public Builder setCancelButtonTitle(String title) {
			mCancelButtonTitle = title;
			return this;
		}
		
		public Builder setMarkButtonIndex(int index){
			mMarkButtonIndex = index;
			return this;
		}

		public Builder setCancelButtonTitle(int strId) {
			return setCancelButtonTitle(mContext.getString(strId));
		}

		public Builder setOtherButtonTitles(String... titles) {
			mOtherButtonTitles = titles;
			return this;
		}

		public Builder setTag(String tag) {
			mTag = tag;
			return this;
		}

		public Builder setListener(ActionSheetListener listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setCancelableOnTouchOutside(boolean cancelable) {
			mCancelableOnTouchOutside = cancelable;
			return this;
		}

		public Bundle prepareArguments() {
			Bundle bundle = new Bundle();
			bundle.putString(ACTION_SHEET_TITLE, mActionSheetTitle);
			bundle.putString(CANCEL_BUTTON_TITLE, mCancelButtonTitle);
			bundle.putInt(MARK_BUTTON_INDEX, mMarkButtonIndex);
			bundle.putStringArray(OTHER_BUTTON_TITLES, mOtherButtonTitles);
			bundle.putBoolean(CANCELABLE_ONTOUCHOUTSIDE,
					mCancelableOnTouchOutside);
			return bundle;
		}

		public ActionSheet show() {
			ActionSheet actionSheet = (ActionSheet) Fragment.instantiate(
					mContext, ActionSheet.class.getName(), prepareArguments());
			actionSheet.setActionSheetListener(mListener);
			actionSheet.show(mFragmentManager, mTag);
			return actionSheet;
		}

	}

	public static interface ActionSheetListener {
		
		/**
		 * 销毁时回调
		 * 
		 * @param actionSheet
		 */
		public void onDismiss(ActionSheet actionSheet);

		/**
		 * 点击按钮的监听
		 * 
		 * @param actionSheet
		 * @param index
		 *            按钮的位置,所有按钮的数量值就是取消按钮
		 */
		public void onClickButtonAtIndex(ActionSheet actionSheet, int index);
	}
}
