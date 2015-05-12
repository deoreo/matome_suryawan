package com.jds.matomemobile;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.Toast;

import com.jds.matomemobile.net.BrowseLocal;
import com.jds.matomemobile.plugin.CustomWebView;
import com.jds.matomemobile.plugin.DBHelper;
import com.jds.matomemobile.plugin.WebData;
import com.jds.matomemobile.util.ArticleList;
import com.jds.matomemobile.util.ListViewAdapter;
import com.jds.matomemobile.util.ResizeAnimation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Date;

public class BrowserFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    DBHelper dbHelper = new DBHelper(getActivity());

    Handler handler;
    Runnable saveArticle;
    Thread saveArticleThread;

    CustomWebView webView;
    RelativeLayout layout_content;
    RelativeLayout savedBtn, readBtn, notifBtn, searchBtn, frame_content, frame_logo;
    LinearLayout action_top_left, search_btn_frame, search_in_frame, frame_refresh, savedListDialogContainer, retryContainer, frameTopBar;
    FrameLayout notification_count_frame, retryButton;
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    Dialog savedListDialog;
    Window savedListWindow;
    int savedListDialog_maxHeight = 0;
    TextView sizeArt, frame_refresh_text, notification_count;
    ListView savedListView;
    ImageView capImage;
    ImageButton deleteAllListButton;
    Button savedListCloseButton;
    EditText searchIn;
    ImageButton searchGo, searchBack;
    View v;
    Thread saveArtCheckThread;
    float move_start_Y;
    int max_last_height = 7;
    float[] last_height = new float[max_last_height];
    String refresh_dir = "";
    boolean pulling = false, canRefresh = false;

    private BrowseLocal savedList, cache;
    private ArticleList articleList;
    private boolean startSaveArticle = false;
    private boolean warnDisplayed = false;
    private boolean bgSaving = false;
    private int top_left_width, top_right_width;
    private InputMethodManager inputMethodManager;
    public boolean is_resume = false;
    private boolean search_in_shown = false;

    View rootView;

    public BrowserFragment() {
        // Required empty public constructor
    }

    public void setInputMethodManager(InputMethodManager inputMethodManager) {
        this.inputMethodManager = inputMethodManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        SetHandler();

        rootView = inflater.inflate(R.layout.design_frame, container, false);

        //set notifications count and data
        notification_count = (TextView) rootView.findViewById(R.id.notification_text);
        notification_count_frame = (FrameLayout) rootView.findViewById(R.id.notification_holder);
        try {
            dbHelper.openDatabase();
            int data = dbHelper.countUnreadData();
            SetNotificationCount(data);
        } catch (SQLException e) {
//            e.printStackTrace();
        }

        notifBtn = (RelativeLayout) rootView.findViewById(R.id.frame_top_left);
        savedBtn = (RelativeLayout) rootView.findViewById(R.id.frame_top_left_saved);
        readBtn = (RelativeLayout) rootView.findViewById(R.id.frame_top_left_read);
        searchBtn = (RelativeLayout) rootView.findViewById(R.id.frame_top_right);
        frameTopBar = (LinearLayout) rootView.findViewById(R.id.frame_top_bar);
        frame_refresh = (LinearLayout) rootView.findViewById(R.id.frame_refresh);
        frame_refresh_text = (TextView) rootView.findViewById(R.id.frame_refresh_text);
        retryContainer = (LinearLayout) rootView.findViewById(R.id.retry_container);
        retryButton = (FrameLayout) rootView.findViewById(R.id.retry_button);
        frame_content = (RelativeLayout) rootView.findViewById((R.id.frame_content));
        frame_logo = (RelativeLayout) rootView.findViewById(R.id.frame_logo);
        search_btn_frame = (LinearLayout) rootView.findViewById(R.id.search_btn_frame);
        search_in_frame = (LinearLayout) rootView.findViewById(R.id.search_in_frame);
        progressBar = (ProgressBar) rootView.findViewById(R.id.frame_browser_progress);
        capImage = (ImageView) rootView.findViewById(R.id.capImage);
        searchIn = (EditText) rootView.findViewById(R.id.search_in);
        searchGo = (ImageButton) rootView.findViewById(R.id.search_go);
        searchBack = (ImageButton) rootView.findViewById(R.id.search_back);
        searchIn.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);

        initWebView(savedInstanceState);

        is_resume = true;

        articleList = new ArticleList(getActivity(), getActivity().getFilesDir().getPath() + WebData.DATA_DIR);
        articleList.loadArticles();

        savedListDialog = new Dialog(webView.getContext());
        savedListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        savedListDialog.setContentView(R.layout.saved_list_dialog);
        savedListDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager wm = (WindowManager) parent().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);

        savedListDialog_maxHeight = (size.y * 80) / 100; //size.y - ((notifBtn.getHeight() + 110) * 2); //

        savedListWindow = savedListDialog.getWindow();
        savedListWindow.setLayout(webView.getLayoutParams().width, ActionBar.LayoutParams.WRAP_CONTENT);
        deleteAllListButton = (ImageButton) savedListDialog.findViewById(R.id.deleteAll);
        savedListCloseButton = (Button) savedListDialog.findViewById(R.id.savedListCloseButton);
        savedListView = (ListView) savedListDialog.findViewById(R.id.savedListView);
        sizeArt = (TextView) savedListDialog.findViewById(R.id.sizeArt);
        savedListDialogContainer = (LinearLayout) savedListDialog.findViewById(R.id.savedListDialogContainer);

        if (articleList.getArticleSize() > 0) {
            updateListView();
        }

        if (savedInstanceState == null) {
            Runnable saveArtDialogCheck = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (savedListDialog.isShowing()) {
                                Message msg1 = new Message();
                                msg1.arg1 = WebData.SET_SAVED_ARTICLE_HEIGHT;
                                handler.sendMessage(msg1);
                            }
                        } catch (IllegalStateException e) {
//                            e.printStackTrace();
                        }
                    }
                }
            };

            Thread saveArtCheckThread = new Thread(saveArtDialogCheck);
            saveArtCheckThread.start();
        }

        savedList = new BrowseLocal(getActivity().getFilesDir().getPath() + WebData.SAVED_DIR);

        v = rootView.findViewById(R.id.frame_top_menu);
        top_right_width = searchBtn.getWidth();

        SetClickListener();

        setRetainInstance(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reinit();
        is_resume = false;
        if (savedInstanceState != null) {
            is_resume = true;
        }
        recreateWebView(is_resume);
    }

    private void reinit() {
        Runnable saveArtDialogCheck = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (savedListDialog.isShowing()) {
                            Message msg1 = new Message();
                            msg1.arg1 = WebData.SET_SAVED_ARTICLE_HEIGHT;
                            handler.sendMessage(msg1);
                        }
                    } catch (IllegalStateException e) {
//                        e.printStackTrace();
                    }
                }
            }
        };

        saveArtCheckThread = new Thread(saveArtDialogCheck);
        saveArtCheckThread.start();
    }

    private void initWebView(Bundle savedState) {
        webView = new CustomWebView(getActivity(), rootView, R.id.frame_browser, R.id.frame_browser_container, is_resume);
        if (savedState == null) {
            recreateWebView(false);
            loadLastUrl();
        } else {
            webView.loadBrowserHistories();
            loadLastUrl();
        }
    }

    private void
    recreateWebView(boolean is_resume) {
        webView.setCustomChromeClient(progressBar, R.anim.fade_out);
        webView.useCustomClient(true);
        webView.setResume(is_resume);
    }

    private void loadLastUrl() {
        try {
            cache = new BrowseLocal(getActivity().getFilesDir().getPath() + WebData.CACHE_DIR);
            if (cache.fileAvailable(WebData.CACHE_FILENAME)) {
                webView.setVisibility(View.GONE);
                webView.loadUrl(WebData.NO_URL);
                cache.loadLocal(webView.getWebView(), getActivity().getFilesDir().getPath() + WebData.CACHE_DIR + WebData.CACHE_FILENAME);
            } else {
                webView.loadUrl(WebData.NO_URL);
                webView.loadUrl(WebData.MAIN_URL);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        is_resume = true;
        super.onResume();
        if (inputMethodManager == null) {
            inputMethodManager = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
//        search_in_frame = (LinearLayout) rootView.findViewById(R.id.search_in_frame);
//        initWebView();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveArtCheckThread.interrupt();
        webView.removeAllViews();
        webView.stopLoading();
        webView.onPause();
        webView.pauseTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
//        webView.kill();
//        webView = null;
    }

    private void SetHandler() {
        //Dialog and Thread communication handler
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    switch (msg.arg1) {
                        case WebData.HIDE_PROGRESS_DIALOG:
                            boolean success = false;
                            boolean causeFull = false;
                            String message = "";
                            switch (msg.arg2) {
                                case WebData.ARTICLE_SAVE_SUCCESS:
                                    saveArticleThread.interrupt();
                                    success = true;
                                    //Toast.makeText(parent().getApplicationContext(), "Article has been saved successfully", Toast.LENGTH_SHORT).show();
                                    break;
                                case WebData.ARTICLE_CAPACITY_IS_FULL:
                                    saveArticleThread.interrupt();
                                    causeFull = true;
                                    //Toast.makeText(parent().getApplicationContext(), "Maximum article can be saved already full", Toast.LENGTH_SHORT).show();
                                    break;
                                case WebData.ARTICLE_FAIL_MHT:
                                    saveArticleThread.interrupt();
                                    causeFull = true;
                                    if (Build.VERSION.RELEASE.equals("4.2.2")) {
                                        //Toast.makeText(parent().getApplicationContext(), "Article failed to be saved or updated. You can try again by pressing \"Read Later\" Button", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Toast.makeText(parent().getApplicationContext(), "Article failed to be saved. Make sure your device storage capacity is sufficient", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case WebData.ARTICLE_FAIL_OFFLINE:
                                    saveArticleThread.interrupt();
                                    message = "no internet connection";
                                    break;
                                case WebData.ARTICLE_FAIL_JSON:
                                    saveArticleThread.interrupt();
                                    message = "download failed";
                                    break;
                                case WebData.ARTICLE_FAIL_IO:
                                    saveArticleThread.interrupt();
                                    message = "connection failed";
                                    break;
                                case WebData.ARTICLE_FAIL_RTO:
                                    saveArticleThread.interrupt();
                                    message = "request has been timed out";
                                    break;
                                default:
                                    saveArticleThread.interrupt();
                                    break;
                            }
                            if (bgSaving == false) {
                                hideProgressDialog(success);
                            }
                            if (!success && !causeFull) {
                                if (bgSaving == false) {
                                    showWarning(message);
                                }
                                //Toast.makeText(parent().getApplicationContext(), "Article failed to be saved. Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case WebData.ARTICLE_WARN_RETRY:
                            showWarning("saving article took so much time");
                            break;
                        case WebData.CANCEL_SAVE_ARTICLE:
                            if (bgSaving == false) {
                                hideProgressDialog(false);
                            }
                            //Toast.makeText(parent().getApplicationContext(), "Article download has been canceled", Toast.LENGTH_SHORT).show();
                            break;
                        case WebData.START_SAVE_ARTICLE:
//                        if (webView.isFileLoadedFromUrl()) {
                            if (!startSaveArticle) {
                                showProgressDialog(WebData.SAVE_PROGRESS_MESSAGE);
                                bgSaving = false;
                                startSaveArticle = true;
                                //Toast.makeText(parent().getApplicationContext(), "Downloading article...", Toast.LENGTH_SHORT).show();
                            }
                            if (webView.getProgress() != 100) {
                                if (!bgSaving) {
                                    if (!webView.getLastUrlFail()) {
                                        Message msg1 = new Message();
                                        msg1.arg1 = WebData.START_SAVE_ARTICLE;
                                        handler.sendMessage(msg1);
                                    } else {
                                        Message msg1 = new Message();
                                        msg1.arg1 = WebData.HIDE_PROGRESS_DIALOG;
                                        msg1.arg2 = WebData.ARTICLE_FAIL_OFFLINE;
                                        handler.sendMessage(msg1);
                                    }
                                }
                            } else {
                                if (!bgSaving) {
                                    doSaveArticle(false);
                                }
                            }
//                        } else {
//                            Toast.makeText(parent().getApplicationContext(), "Unable to check article update. Make sure your device have internet access.", Toast.LENGTH_SHORT).show();
//                        }
                            break;
                        case WebData.START_SAVE_BG_ARTICLE:
//                        if (webView.isFileLoadedFromUrl()) {
                            if (!startSaveArticle) {
                                bgSaving = true;
                                startSaveArticle = true;
                                //Toast.makeText(parent().getApplicationContext(), "Dowloading article...", Toast.LENGTH_SHORT).show();
                            }
                            if (webView.getProgress() != 100) {
                                if (bgSaving) {
                                    if (!webView.getLastUrlFail()) {
                                        Message msg1 = new Message();
                                        msg1.arg1 = WebData.START_SAVE_BG_ARTICLE;
                                        handler.sendMessage(msg1);
                                    } else {
                                        Message msg1 = new Message();
                                        msg1.arg1 = WebData.HIDE_PROGRESS_DIALOG;
                                        msg1.arg2 = WebData.ARTICLE_FAIL_OFFLINE;
                                        handler.sendMessage(msg1);
                                    }
                                }
                            } else {
                                if (bgSaving) {
                                    doSaveArticle(true);
                                }
                            }
//                        } else {
////                            if (Build.VERSION.RELEASE.equals("4.2.2")) {
////                                Toast.makeText(parent().getApplicationContext(), "Unable to load or update your article. Make sure your device have internet access.", Toast.LENGTH_SHORT).show();
////                            } else {
////                                Toast.makeText(parent().getApplicationContext(), "Unable to check for article update. Make sure your device have internet access.", Toast.LENGTH_SHORT).show();
////                            }
////                            bgSaving = false;
////                            startSaveArticle = false;
//                        }
                            break;
                        case WebData.SET_SAVED_ARTICLE_HEIGHT:
                            int curHeight = savedListDialogContainer.getMeasuredHeight();
                            if (curHeight > savedListDialog_maxHeight) {
                                savedListDialogContainer.getLayoutParams().height = savedListDialog_maxHeight;
                                savedListDialogContainer.requestLayout();
                            }
                            break;
                        case WebData.SAVED_ARTICLE_UPDATE:
                            updateListView();
                            break;
                        case WebData.SHOW_HIDE_SAVED_ARTICLE:
                            showOrHideSavedList();
                            break;
                        default:
                            break;
                    }
                } catch (NullPointerException e) {
//                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowManager wm = (WindowManager) parent().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        int width = size.x;

        frame_refresh.getLayoutParams().width = width;
        frame_refresh.requestLayout();

        webView.getLayoutParams().width = width;
        webView.requestLayout();

        notifBtn.getLayoutParams().width = width;
        notifBtn.requestLayout();
        // Checks the orientation of the screen
        //if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(parent(), "landscape", Toast.LENGTH_SHORT).show();

      //  } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(parent(), "portrait", Toast.LENGTH_SHORT).show();
        //}
    }

    private void SetClickListener() {
        //Notifications Button
        notifBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Rect bound = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (bound.contains(view.getLeft() + (int) event.getX(), view.getTop() + (int) event.getY())) {
                        ShowNotifications();
                    }
                    notifBtn.setBackgroundResource(R.drawable.button);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    notifBtn.setBackgroundResource(R.drawable.button_pressed);
                }

                return true;
            }
        });

        //Home Button
//        action_top_center.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                webView.loadUrl("http://m.matome.id");
//                return false;
//            }
//        });

        //Search Articles Button
        search_btn_frame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    search_btn_frame.setBackgroundResource(R.drawable.button);
                    Rect bound = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (bound.contains(view.getLeft() + (int) event.getX(), view.getTop() + (int) event.getY())) {
//                if (inputMethodManager != null) {
                        top_left_width = v.getMeasuredWidth();
                        searchIn.requestFocus();
                        searchIn.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
                        inputMethodManager.showSoftInput(searchIn, 0);
                        showSearchIn();
//                }
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    search_btn_frame.setBackgroundResource(R.drawable.button_pressed);
                }
                return true;
            }
        });

        //Back from search action
        searchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchIn();
                inputMethodManager.hideSoftInputFromWindow(searchIn.getWindowToken(), 0);
                webView.requestFocus();
            }
        });

        //Start searching and exit from search action
        searchGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchIn();
                startSearching();
                inputMethodManager.hideSoftInputFromWindow(searchIn.getWindowToken(), 0);
                webView.requestFocus();
            }
        });

        searchIn.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                hideSearchIn();
                startSearching();
                inputMethodManager.hideSoftInputFromWindow(searchIn.getWindowToken(), 0);
                webView.requestFocus();
                return false;
            }
        });

        searchIn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (i == KeyEvent.KEYCODE_BACK) {
                        hideSearchIn();
                    }
                }
                return false;
            }
        });

        //Pull to refresh feature
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                pullToRefresh(view, motionEvent);
                if (search_btn_frame.getVisibility() != View.VISIBLE) {
                    hideSearchIn();
                    inputMethodManager.hideSoftInputFromWindow(searchIn.getWindowToken(), 0);
                }
                return false;
            }
        });

        retryContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                pullToRefresh(view, motionEvent);
                if (search_btn_frame.getVisibility() != View.VISIBLE) {
                    hideSearchIn();
                    inputMethodManager.hideSoftInputFromWindow(searchIn.getWindowToken(), 0);
                }
                return false;
            }
        });

        retryButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    retryButton.setBackgroundResource(R.drawable.button);
                    Rect bound = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if(bound.contains(view.getLeft() + (int) event.getX(), view.getTop() + (int) event.getY())) {
                        webView.reload();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    retryButton.setBackgroundResource(R.drawable.button_pressed);
                }
                return true;
            }
        });

        //Saved Article Button
        savedBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    savedBtn.setBackgroundResource(R.drawable.button);
                    Rect bound = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (bound.contains(view.getLeft() + (int) event.getX(), view.getTop() + (int) event.getY())) {
                        Message msg = new Message();
                        msg.arg1 = WebData.SHOW_HIDE_SAVED_ARTICLE;
                        handler.sendMessage(msg);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    savedBtn.setBackgroundResource(R.drawable.button_pressed);
                }
                return true;
            }
        });

        //Read Later Button
        readBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    readBtn.setBackgroundResource(R.drawable.button);
                    Rect bound = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (bound.contains(view.getLeft() + (int) event.getX(), view.getTop() + (int) event.getY())) {
                        Message msg = new Message();
                        msg.arg1 = WebData.START_SAVE_ARTICLE;
                        handler.sendMessage(msg);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    readBtn.setBackgroundResource(R.drawable.button_pressed);
                }

                return true;
            }
        });

        //Delete all articles from Saved List Dialog
        deleteAllListButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    deleteAllListButton.setBackgroundResource(R.drawable.list_button);
                    Rect bound = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (bound.contains(view.getLeft() + (int) event.getX(), view.getTop() + (int) event.getY())) {
                        articleList.deleteAllArticles();
                        updateListView();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    deleteAllListButton.setBackgroundResource(R.drawable.list_button_pressed);
                }
                return true;
            }
        });

        //Click an Article from Saved List Dialog
        savedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                savedList.loadLocal(webView.getWebView(), articleList.getArticleSavedLoc(position));
                showOrHideSavedList();
                Message msg = new Message();
                msg.arg1 = WebData.START_SAVE_BG_ARTICLE;
                handler.sendMessage(msg);
            }
        });

        //Close Saved List Dialog
        savedListCloseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    savedListCloseButton.setBackgroundResource(R.drawable.button);
                    Rect bound = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (bound.contains(view.getLeft() + (int) event.getX(), view.getTop() + (int) event.getY())) {
                        showOrHideSavedList();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    savedListCloseButton.setBackgroundResource(R.drawable.button_pressed);
                }

                return true;
            }
        });

        frame_logo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    webView.loadUrl(WebData.MAIN_URL);
                }

                return true;
            }
        });
    }

    private void pullToRefresh(View view, MotionEvent motionEvent) {
        float alphaVal = (float) (frame_refresh.getHeight() - 100) / 200;
        AlphaAnimation alpha = new AlphaAnimation(0, alphaVal);
        alpha.setDuration(0);
        alpha.setFillAfter(true);
        frame_refresh.setAnimation(alpha);

        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            actionPull(motionEvent);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            actionRelease();
        }

        if (pulling == true) {
            webView.scrollToZero();
        } else {
            webView.setScrollYPosition(webView.getScrollYPosition());
        }
    }

    private void actionRelease() {
        if (pulling) {
            addLastHeight(frame_refresh.getHeight());
            for (int i = 0; i < 4 * max_last_height; i++) {
                addLastHeight(getAverageHeight());
            }
            frame_refresh.getLayoutParams().height = (int) getAverageHeight();
            frame_refresh.requestLayout();

            ResizeAnimation resize = new ResizeAnimation(frame_refresh, frame_refresh.getWidth(), 0);
            resize.setInterpolator(new AccelerateDecelerateInterpolator());
            AlphaAnimation alpha = new AlphaAnimation(Animation.RELATIVE_TO_SELF, 0.0f);
            AnimationSet shrink = new AnimationSet(true);
            shrink.addAnimation(resize);
            shrink.addAnimation(alpha);
            shrink.setDuration(500);
            frame_refresh.startAnimation(shrink);

            shrink.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (canRefresh) {
                        frame_refresh_text.setText(R.string.pull_to_refresh);
                        webView.reload();
                        //Toast.makeText(parent().getApplicationContext(), "Refreshing...", Toast.LENGTH_SHORT).show();
                        canRefresh = false;
                    }
                    frame_refresh.getLayoutParams().height = 0;
                    frame_refresh.requestLayout();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            pulling = false;
        }
    }

    private void actionPull(MotionEvent motionEvent) {
        int scrollPos = webView.getScrollYPosition();
        float use_height = calculateHeight(motionEvent);

        if (scrollPos == 0) {
            use_height = startPulling(motionEvent, use_height);
        } else {
            pulling = false;
        }

        if (pulling) {
            if (use_height > 0.0f) {
                pullingFX((int) use_height);
            } else {
                cancelRefresh();
            }
        }
    }

    private float startPulling(MotionEvent motionEvent, float use_height) {
        float uh = use_height;
        if (!pulling) {
            uh = 0.01f;
            move_start_Y = motionEvent.getY();
            pulling = true;
            canRefresh = false;
            for (int i = 0; i < max_last_height; i++) {
                last_height[i] = 0;
            }
        }
        return uh;
    }

    private void cancelRefresh() {
        if (pulling) {
            frame_refresh.getLayoutParams().height = 0;
            frame_refresh.requestLayout();
        }
        canRefresh = false;
        pulling = false;
    }

    private void pullingFX(int use_height) {
        if (frame_refresh.getHeight() > 220) {
            frame_refresh_text.setText(R.string.pull_to_refresh_2);
            canRefresh = true;
        } else {
            frame_refresh_text.setText(R.string.pull_to_refresh);
            canRefresh = false;
        }
        frame_refresh.getLayoutParams().height = use_height;
        frame_refresh.requestLayout();
    }

    private float calculateHeight(MotionEvent motionEvent) {
        float height = (float) -(getDistanceY(motionEvent) / 1.025);
        addLastHeight(height);
        return getAverageHeight();
    }

    private void addLastHeight(float height) {
        for (int i = 1; i < max_last_height; i++) {
            last_height[i - 1] = last_height[i];
        }
        last_height[max_last_height - 1] = height;
    }

    private float getAverageHeight() {
        float sum = 0.0f;
        for (float h : last_height) {
            sum += h;
        }

        return sum / max_last_height;
    }

    private void startSearching() {
        String query = null;
        String search = searchIn.getText().toString();
        try {
            query = URLEncoder.encode(search, "UTF-8");
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
        }

        //Toast.makeText(parent().getApplicationContext(), "Search: \"" + search + "\"", Toast.LENGTH_SHORT).show();
        webView.loadUrl(WebData.MAIN_SEARCH + query);
    }

    private void showSearchIn() {
        ResizeAnimation resize = new ResizeAnimation(v, 0, v.getHeight());
        AlphaAnimation fade_out = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(resize);
        animationSet.addAnimation(fade_out);
        animationSet.setDuration(500);
        Animation hideSearchBtn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        Animation showSearchIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);

        v.startAnimation(animationSet);
        search_btn_frame.startAnimation(hideSearchBtn);
        search_in_frame.startAnimation(showSearchIn);
        search_in_frame.setVisibility(View.VISIBLE);

        showSearchIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                search_in_shown = true;
                search_btn_frame.setVisibility(View.GONE);
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void hideSearchIn() {
        ResizeAnimation showTopLeft = new ResizeAnimation(v, frameTopBar.getWidth(), frameTopBar.getHeight());
        showTopLeft.setDuration(500);
        Animation showSearchBtn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        Animation hideSearchIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);

        search_btn_frame.setVisibility(View.VISIBLE);
        v.setVisibility(View.VISIBLE);

        v.startAnimation(showTopLeft);
        search_btn_frame.startAnimation(showSearchBtn);
        search_in_frame.startAnimation(hideSearchIn);

        hideSearchIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                search_in_frame.setVisibility(View.GONE);
                search_in_shown = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);

        if (progress >= 100) {
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressDialog(String text) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(text);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                sendCancelSaveArticle();
            }
        });
    }

    public void hideProgressDialog(boolean success) {
        progressDialog.dismiss();
        if (success) {
            saveSuccessAnim();
        } else {
            saveFailAnim();
        }
    }

    private void saveSuccessAnim() {
        webView.setDrawingCacheEnabled(false);
        webView.setDrawingCacheEnabled(true);
        Bitmap cap = webView.getDrawingCache();
        Log.v("[sv]", "success " + cap.toString());
        capImage.setImageBitmap(cap);
        capImage.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.read_later);
        capImage.setAnimation(anim);
        anim.start();
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                capImage.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void saveFailAnim() {
        webView.setDrawingCacheEnabled(false);
        webView.setDrawingCacheEnabled(true);
        Bitmap cap = invertColor(webView.getDrawingCache());
        capImage.setImageBitmap(cap);
        capImage.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.read_later_fail);
        capImage.setAnimation(anim);
        anim.start();
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                capImage.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private Bitmap invertColor(Bitmap src) {
        ColorMatrix colorMatrix_Inverted =
                new ColorMatrix(new float[]{
                        0.7f, 0.6f, 0.5f, 0, 0,
                        0.6f, 0.5f, 0, 0, 0,
                        0.6f, 0.5f, 0.4f, 0, 0,
                        0, 0, 0, 1, 0});

        ColorFilter ColorFilter_Sepia = new ColorMatrixColorFilter(
                colorMatrix_Inverted);

        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setColorFilter(ColorFilter_Sepia);
        canvas.drawBitmap(src, 0, 0, paint);

        return bitmap;
    }

    private void showWarning(String message) {
        progressDialog.dismiss();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(WebData.FAILED_PROGRESS_MESSAGE + "\n" +
                "\nArticle cannot be saved because " + message + "." +
                "\nDo you want to cancel or retry?");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendCancelSaveArticle();
                progressDialog.dismiss();
            }
        });

        progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendRetrySaveArticle();
            }
        });
        progressDialog.show();
    }

    private void sendCancelSaveArticle() {
        if (startSaveArticle) {
            while (saveArticleThread == null) {

            }
            saveArticleThread.interrupt();
            handler.removeCallbacksAndMessages(saveArticle);
            Message msg = new Message();
            msg.arg1 = WebData.CANCEL_SAVE_ARTICLE;
            handler.sendMessage(msg);
            startSaveArticle = false;
        }
    }

    private void sendRetrySaveArticle() {
        saveArticleThread.interrupt();
        showProgressDialog(WebData.SAVE_PROGRESS_MESSAGE);
        doSaveArticle(false);
    }

    private void updateListView() {
        try {
            sizeArt.setText(articleList.getArticleSize() + "/" + WebData.MAX_ARTICLE);
            savedListView.setAdapter(new ListViewAdapter(getActivity().getApplicationContext(), articleList));
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private void showOrHideSavedList() {
        updateListView();

        try {
            if (savedListDialog.isShowing()) {
                savedListDialog.dismiss();
            } else {
                savedListDialog.show();
            }
        } catch (IllegalStateException e) {
//            e.printStackTrace();
        }
    }

    private void doSaveArticle(boolean bgSaving) {
        warnDisplayed = false;
        final String webViewUrl = webView.getUrl();

        String[] url = webViewUrl.split("/");
        String saveDraft = url[url.length - 1];
        if (saveDraft.equals("m.matome.id")) {
            saveDraft = "home";
        } else {
            if (webView.getUrl().contains("search")) {
                saveDraft = "search_" + saveDraft;
            } else if (webViewUrl.contains("topic")) {
                saveDraft = "topic_" + saveDraft;
            } else if (webViewUrl.contains("category")) {
                saveDraft = "category_" + saveDraft;
            }
        }

        articleList.setBgSaving(bgSaving);

        final String save = saveDraft;
        if (savedList.saveLocal(webView.getWebView(), save + WebData.WEB_ARCHIVE_EXT)) {
            final String saveLoc = savedList.getLatestSaveLoc();

            saveArticle = new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.arg2 = articleList.addArticle(save, webViewUrl, saveLoc, save);
                    Message msg2 = new Message();
                    msg2.arg1 = WebData.SAVED_ARTICLE_UPDATE;
                    handler.handleMessage(msg2);

                    if (msg.arg2 == WebData.ARTICLE_CAPACITY_NOT_FULL) {
                        Date start = new Date();
                        while (!articleList.getCurrentArticle().finishedParsing()) {
                            Date current = new Date();
                            if (!warnDisplayed) {
                                if (articleList.getCurrentArticle().getConnectionRetry() > WebData.WARN_RETRY_CONNECTION ||
                                        current.getTime() - start.getTime() >= 2000) {
                                    Message msg3 = new Message();
                                    msg3.arg1 = WebData.ARTICLE_WARN_RETRY;
                                    handler.sendMessage(msg3);
                                    warnDisplayed = true;
                                }
                            }
                        }
                        msg.arg2 = articleList.getCurrentArticle().getArticleCreateState();
                    }

                    msg.arg1 = WebData.HIDE_PROGRESS_DIALOG;
                    handler.sendMessage(msg);
                    startSaveArticle = false;
                }
            };

            saveArticleThread = new Thread(saveArticle);
            saveArticleThread.start();
        } else {
            Message msg = new Message();
            msg.arg1 = WebData.HIDE_PROGRESS_DIALOG;
            msg.arg2 = WebData.ARTICLE_FAIL_MHT;
            handler.sendMessage(msg);
            startSaveArticle = false;
        }
    }

    float getDistanceY(MotionEvent ev) {
        final int historySize = ev.getHistorySize();
        float startY = move_start_Y;
        for (int h = 0; h < historySize; h++) {
            // historical point
            float hy = ev.getHistoricalY(0, h);
            // distance between startX,startY and historical point
            float dy = (hy - startY);
            // make historical point the start point for next loop iteration
            startY = hy;
        }
        // add distance from last historical point to event's point
        return move_start_Y - startY;
    }

    //this function will be used to handle notification count and view
    public void SetNotificationCount(int count) {
        String value = String.valueOf(count);
        Log.d("notification count", value);
        notification_count.setText(value);

        if (count > 0) {
            notification_count_frame.setVisibility(View.VISIBLE);
        } else {
            notification_count_frame.setVisibility(View.GONE);
        }
    }

    private void ShowNotifications() {

        SetNotificationCount(0);
        if (parent().getSupportFragmentManager().findFragmentByTag("notification") == null) {
            parent().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                    .add(R.id.fragment_container, new NotificationFragment(), "notification")
                    .commit();
        }

/*
        LayoutInflater factory = LayoutInflater.from(getActivity().getApplicationContext());
        final View notificationDialog = factory.inflate(R.layout.notification_dialog, null);

        final AlertDialog notifications = new AlertDialog.Builder(getActivity()).create();
        notifications.setView(notificationDialog);
        notifications.setCancelable(false);

        Button btn_close = (Button) notificationDialog.findViewById(R.id.notification_close);
        btn_close.setBackgroundColor(Color.TRANSPARENT);
        btn_close.setTextColor(Color.BLACK);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifications.dismiss();
            }
        });

        notifications.show();
*/
    }

    private ManagerActivity parent() {
        return ((ManagerActivity) getActivity());
    }

    public boolean canGoBack() {
        if (webView != null) {
            return webView.canGoBack();
        } else {
            return false;
        }
    }

    public void goBack() {
        webView.goBack();
    }

    public boolean searchShown() {
        return search_in_shown;
    }

    public CustomWebView getWebView() {
        return webView;
    }
}
