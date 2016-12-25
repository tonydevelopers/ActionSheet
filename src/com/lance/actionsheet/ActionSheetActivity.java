package com.lance.actionsheet;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.lance.actionsheet.ActionSheet.ActionSheetListener;

/**
 * 仿微信的actionSheet并参考了
 * @author ganchengkai
 *
 */
public class ActionSheetActivity extends FragmentActivity implements OnClickListener, ActionSheetListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.bt_exit).setOnClickListener(this);
		findViewById(R.id.bt_upload).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.bt_exit:
			ActionSheet.createBuilder(this, getSupportFragmentManager())
				.setTitle("您确定要退出登录吗?")
				.setCancelableOnTouchOutside(true)
				.setListener(this)
				.setMarkButtonIndex(0)
				.setCancelButtonTitle("取消")
				.setOtherButtonTitles(new String[]{"退出登录"})
				.show();
			break;
		case R.id.bt_upload:
			ActionSheet.createBuilder(this, getSupportFragmentManager())
			.setCancelableOnTouchOutside(true)
			.setListener(this)
			.setCancelButtonTitle("取消")
			.setOtherButtonTitles(new String[]{"从相册获取", "拍照"})
			.show();
			break;
		}
	}

	@Override
	public void onDismiss(ActionSheet actionSheet) {
		System.out.println("销毁了--->");
	}

	@Override
	public void onClickButtonAtIndex(ActionSheet actionSheet, int index) {
		System.out.println("点击位置--->"+index);
	}
	
}
