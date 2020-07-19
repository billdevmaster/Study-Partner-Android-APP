package com.studypartner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;
import com.studypartner.utils.Connection;

import java.io.File;
import java.util.ArrayList;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotesFragment extends Fragment {
	private static final String TAG = "NotesFragment";
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private File noteFolder;

	public NotesFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		Connection.checkConnection(this);
		
		View rootView = inflater.inflate(R.layout.fragment_notes, container, false);
		noteFolder = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders");
		
		final MainActivity activity = (MainActivity) requireActivity();
		activity.mBottomAppBar.bringToFront();
		activity.fab.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				fab.setOnClickListener(null);
				
				activity.mNavController.navigate(R.id.action_nav_notes_to_nav_home);
			}
		});
		
		fab = activity.fab;
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "onClick: fab onclick called");
				Toast.makeText(activity, "Adding folder", Toast.LENGTH_SHORT).show();
				addFolder();
				displayFolder();
			}
		});
		
		recyclerView = rootView.findViewById(R.id.recyclerView);
		
		displayFolder();
		
		return rootView;
	}
	
	@Override
	public void onPause() {
		fab.setOnClickListener(null);
		super.onPause();
	}
	
	void addFolder() {
		String newFolder = "New Folder";
		File file = new File(noteFolder, newFolder);
		file.mkdirs();
	}
	
	void displayFolder() {
		
		File file = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders");
		File test = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders/test");
		File test2 = new File(String.valueOf(getContext().getExternalFilesDir(null)), "Folders/test2");
		File test3 = new File(String.valueOf(getContext().getFilesDir()), "test3");
		File test4 = new File(String.valueOf(getContext().getExternalMediaDirs()[0]), "test4");
		test.mkdirs();
		
		test2.mkdirs();
		
		test3.mkdirs();
		
		test4.mkdirs();
		
		File[] f = file.listFiles();
		ArrayList<FileItem> subjects = new ArrayList<>();
		
		if (f != null && f.length > 0) {
			
			for (File value : f) subjects.add(new FileItem(value.getAbsolutePath(), value.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
			subjects.add(new FileItem(test3.getAbsolutePath(), test3.getName(), FileItem.FileType.FILE_TYPE_FOLDER));
			subjects.add(new FileItem(test4.getAbsolutePath(), test4.getName(), FileItem.FileType.FILE_TYPE_FOLDER));

			final MainActivity activity = (MainActivity) requireActivity();

			NotesAdapter adapter = new NotesAdapter(getContext(),activity, subjects);
			recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
			recyclerView.setAdapter(adapter);
		}
	}
}
