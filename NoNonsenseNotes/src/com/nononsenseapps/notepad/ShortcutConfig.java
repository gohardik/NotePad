package com.nononsenseapps.notepad;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nononsenseapps.notepad.database.Task;
import com.nononsenseapps.notepad.database.TaskList;

import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.Loader;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Switch;

@EActivity(R.layout.activity_shortcut_config)
public class ShortcutConfig extends Activity {

	@ViewById
	Switch noteSwitch;
	@ViewById
	Spinner listSpinner;

	@AfterViews
	protected void setup() {
		// Default result is fail
		setResult(RESULT_CANCELED);
		setListEntries(listSpinner);
	}

	@Click(R.id.ok)
	void onOK() {
		final Intent shortcutIntent = new Intent();
		// Set icon
		final ShortcutIconResource iconResource = Intent.ShortcutIconResource
				.fromContext(ShortcutConfig.this, R.drawable.app_icon);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				iconResource);
		String shortcutTitle = "";
		final Intent intent = new Intent();
		if (noteSwitch.isChecked()) {
			shortcutTitle = ShortcutConfig.this
					.getString(R.string.title_create);

			intent.setClass(ShortcutConfig.this, ActivityMain_.class)
					.setData(Task.URI)
					.setAction(Intent.ACTION_INSERT)
					.putExtra(Task.Columns.DBLIST,
							listSpinner.getSelectedItemId());
		}
		else {
			final Cursor c = (Cursor) listSpinner.getSelectedItem();

			if (c != null && !c.isClosed() && !c.isAfterLast()) {
				shortcutTitle = c.getString(1);
			}
			shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, ""
					+ listSpinner.getSelectedItem());

			intent.setClass(ShortcutConfig.this, ActivityMain_.class)
					.setAction(Intent.ACTION_VIEW)
					.setData(TaskList.getUri(listSpinner.getSelectedItemId()));
		}
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutTitle);

		setResult(RESULT_OK, shortcutIntent);

		// Destroy activity
		finish();
	}

	private void setListEntries(final Spinner listSpinner) {
		final SimpleCursorAdapter mSpinnerAdapter = new SimpleCursorAdapter(
				this, android.R.layout.simple_spinner_dropdown_item, null,
				new String[] { TaskList.Columns.TITLE },
				new int[] { android.R.id.text1 }, 0);

		// mSpinnerAdapter
		// .setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		listSpinner.setAdapter(mSpinnerAdapter);

		getLoaderManager().restartLoader(0, null,
				new LoaderCallbacks<Cursor>() {

					@Override
					public Loader<Cursor> onCreateLoader(int id, Bundle args) {
						return new CursorLoader(ShortcutConfig.this,
								TaskList.URI, new String[] {
										TaskList.Columns._ID,
										TaskList.Columns.TITLE }, null, null,
								TaskList.Columns.TITLE);
					}

					@Override
					public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
						mSpinnerAdapter.swapCursor(c);
					}

					@Override
					public void onLoaderReset(Loader<Cursor> arg0) {
						mSpinnerAdapter.swapCursor(null);
					}
				});
	}
}
