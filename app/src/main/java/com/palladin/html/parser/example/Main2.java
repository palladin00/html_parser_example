package com.palladin.html.parser.example;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main2 extends ActionBarActivity {

	


    private static String URL_PRIMARY = "http://wabu.ms.kr"; //홈페이지 원본 주소이다.
    private static String GETNOTICE;
	private String url;
    private URL URL;

    private Source source;
    private ProgressDialog progressDialog;
    private BBSListAdapter BBSAdapter = null;
    private ListView BBSList;
    private int BBSlocate;

    private ConnectivityManager cManager;
    private NetworkInfo mobile;
    private NetworkInfo wifi;

    ArrayList<ListData> mListData = new ArrayList<>();


    @Override
    protected void onStop() { //멈추었을때 다이어로그를 제거해주는 메서드
        super.onStop();
        if ( progressDialog != null)
            progressDialog.dismiss(); //다이어로그가 켜져있을경우 (!null) 종료시켜준다
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BBSList = (ListView)findViewById(R.id.listView); //리스트선언
        BBSAdapter = new BBSListAdapter(this);
        BBSList.setAdapter(BBSAdapter); //리스트에 어댑터를 먹여준다.
        BBSList.setOnItemClickListener( //리스트 클릭시 실행될 로직 선언
			new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

					ListData mData = mListData.get(position); // 클릭한 포지션의 데이터를 가져온다.
					String URL_BCS = mData.mUrl; //가져온 데이터 중 url 부분만 적출해낸다.

					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRIMARY + URL_BCS))); //적출해낸 url 을 이용해 URL_PRIMARY 와 붙이고

				}
			});


        url = URL_PRIMARY + GETNOTICE; //파싱하기전 PRIMARY URL 과 공지사항 URL 을 합쳐 완전한 URL 을만든다.

        if(isInternetCon()) { //false 반환시 if 문안의 로직 실행
            Toast.makeText(this, "인터넷에 연결되지않아 불러오기를 중단합니다.", Toast.LENGTH_SHORT).show();
            finish();
        }else{ //인터넷 체크 통과시 실행할 로직
            try {
                process(); //네트워크 관련은 따로 쓰레드를 생성해야 UI 쓰레드와 겹치지 않는다. 그러므로 Thread 가 선언된 process 메서드를 호출한다.
                BBSAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.d("ERROR", e + "");

            }
        }





    }


    private void process() throws IOException {

        new Thread() {

            @Override
            public void run() {

                Handler Progress = new Handler(Looper.getMainLooper()); //네트워크 쓰레드와 별개로 따로 핸들러를 이용하여 쓰레드를 생성한다.
                Progress.postDelayed(new Runnable() {
						@Override
						public void run() {
							progressDialog = ProgressDialog.show(Main2.this, "", "게시판 정보를 가져오는중 입니다.");
						}
					}, 0);

                try {
                    URL = new URL(url);
                    InputStream html = URL.openStream();
                    source = new Source(new InputStreamReader(html, "euc-kr")); //소스를 UTF-8 인코딩으로 불러온다.
                    source.fullSequentialParse(); //순차적으로 구문분석
                } catch (Exception e) {
                    Log.d("ERROR", e + "");
                }

                List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.DIV); // DIV 타입의 모든 태그들을 불러온다.

                for(int arrnum = 0;arrnum < tabletags.size(); arrnum++){ //DIV 모든 태그중 bbsContent 태그가 몇번째임을 구한다.


					if(tabletags.get(arrnum).toString().equals("<div class=\"boardRead\">")) {
						BBSlocate = arrnum; //DIV 클래스가 bbsContent 면 arrnum 값을 BBSlocate 로 몇번째인지 저장한다.
						Log.d("BBSLOCATES", arrnum+""); //arrnum 로깅
						break;
					}
                }



                Element BBS_DIV = (Element) source.getAllElements(HTMLElementName.DIV).get(BBSlocate); //BBSlocate 번째 의 DIV 를 모두 가져온다.
                Element BCS_title = (Element) BBS_DIV.getAllElements(HTMLElementName.DD).get(0); //DD 접속
Element BCS_writer1= (Element) BBS_DIV.getAllElements(HTMLElementName.DD).get(1); //DD 접속
Element BCS_date = (Element) BBS_DIV.getAllElements(HTMLElementName.DD).get(2); //DD 접속
				Element BBS_TBODY = (Element) BBS_DIV.getAllElements(HTMLElementName.DIV).get(4);
                Element BBS_TBODY2 = (Element) BBS_DIV.getAllElements(HTMLElementName.DIV).get(5); 
				Element BBS_Down = (Element) BBS_DIV.getAllElements(HTMLElementName.TD).get(1); 
				Element BC_a = (Element) BBS_DIV.getAllElements(HTMLElementName.A).get(0); 
				String BCS_down = BC_a.getAttributeValue("href");
				String BCS_abc = BBS_TBODY.toString()+BBS_TBODY2.toString();
				String BCS_title1 = BCS_title.toString();
				String BCS_writer = BCS_writer1.toString();
				String BCS_date1 = BCS_date.toString();
				String BCS_url1 = null;
                          final String BCS_type = null;

				final String BCS_url= null;
                        mListData.add(new ListData(BCS_title1, BCS_writer, BCS_date1, BCS_abc, BCS_down)); //데이터가 모이면 데이터 리스트 클래스에 데이터들을 등록한다.
                        Log.d("BCSARR","타입:"+BCS_type+"\n제목:" +BCS_title +"\n주소:"+BCS_url +"\n글쓴이:" + BCS_writer + "\n날짜:" + BCS_date+"\n내용:"+BBS_TBODY+BBS_TBODY2);



                    
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							BBSAdapter.notifyDataSetChanged(); //모든 작업이 끝나면 리스트 갱신
							progressDialog.dismiss(); //모든 작업이 끝나면 다이어로그 종료
						}
					}, 0);



			}

        }.start();


    }




    private boolean isInternetCon() {
        cManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        mobile = cManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); //모바일 데이터 여부
        wifi = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); //와이파이 여부
        return !mobile.isConnected() && !wifi.isConnected(); //결과값을 리턴
    }




    // <리스트 적용부분
    class ViewHolder {

        public TextView mType;
        public TextView mTitle;
        public TextView mUrl;
        public TextView mWriter;
        public TextView mDate;
	public TextView mDown;

        public TextView mabc;
    }



    public class BBSListAdapter extends BaseAdapter {
        private Context mContext = null;

        public BBSListAdapter(Context mContext) {
            this.mContext = mContext;
        }


        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.itemstyle2, null);

                holder.mTitle = (TextView) convertView.findViewById(R.id.item_title1);
                holder.mWriter = (TextView) convertView.findViewById(R.id.item_writer1);
                holder.mDate = (TextView) convertView.findViewById(R.id.item_date1);
holder.mabc = (TextView) convertView.findViewById(R.id.item_abc);
holder.mDown = (TextView) convertView.findViewById(R.id.item_down);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData mData = mListData.get(position);
mData.mType=null;

            if(mData.mType.equals("공지")){
                holder.mTitle.setText(Html.fromHtml("<font color=#616161>[공지] </font>" +mData.mTitle)); //"공지" 의 색깔을 부분적으로 약간 진하게 수정.
            }else{
                holder.mTitle.setText(mData.mTitle);
            }

            holder.mWriter.setText(mData.mWriter+" 선생님"); //선생님을 붙힘
            holder.mDate.setText(mData.mDate);
            return convertView;

        }


    }

    public class ListData { // 데이터를 받는 클래스

        public String mType;
        public String mTitle;
        public String mUrl;
        public String mWriter;
        public String mDate;
        public String mabc;
        public String mDown;


        public ListData()  {


        }

        public ListData(String mTitle,String mWriter,String mDate, String mabc,String mDown)  { //데이터를 받는 클래스 메서드
     
            this.mTitle = mTitle;
            
            this.mWriter = mWriter;
            this.mDate = mDate;
            this.mabc = mabc;
            this.mDown = mDown;
        }

    }
    // 리스트 적용부분 >
}

