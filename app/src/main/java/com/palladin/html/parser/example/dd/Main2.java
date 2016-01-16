package com.palladin.html.parser.example.dd;

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
import com.palladin.html.parser.example.*;

public class Main2 extends ActionBarActivity {

	


    private static String URL_PRIMARY1 = "http://wabu.ms.kr"; //홈페이지 원본 주소이다.
    private static String GETNOTICE1 = null;
	private String url1;
    private URL URL1;
   
    private Source source1;
    private ProgressDialog progressDialog1;
    private BBSListAdapter BBSAdapter1;
    private ListView BBSList1;
    private int BBSlocate1;

    private ConnectivityManager cManager1;
    private NetworkInfo mobile;
    private NetworkInfo wifi;
	
	
	
    ArrayList<ListData> mListData1 = new ArrayList<>();


    @Override
    protected void onStop() { //멈추었을때 다이어로그를 제거해주는 메서드
        super.onStop();
        if ( progressDialog1 != null)
            progressDialog1.dismiss(); //다이어로그가 켜져있을경우 (!null) 종료시켜준다
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		Intent intent = getIntent();
		GETNOTICE1 =intent.getExtras().getString("url").toString();
		if(BBSAdapter1==null){
			Toast.makeText(this, "단합니다.", Toast.LENGTH_SHORT);
			finish();
		}
		
        BBSList1 = (ListView)findViewById(R.id.listView1); //리스트선언
        BBSAdapter1 = new BBSListAdapter(this);
        BBSList1.setAdapter(BBSAdapter1); //리스트에 어댑터를 먹여준다.
        BBSList1.setOnItemClickListener( //리스트 클릭시 실행될 로직 선언
			new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

					ListData mData1 = mListData1.get(position); // 클릭한 포지션의 데이터를 가져온다.
					String URL_BCS = mData1.mDown; //가져온 데이터 중 url 부분만 적출해낸다.

					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRIMARY1 + URL_BCS))); //적출해낸 url 을 이용해 URL_PRIMARY 와 붙이고

				}
			});


        url1 = URL_PRIMARY1 + GETNOTICE1; //파싱하기전 PRIMARY URL 과 공지사항 URL 을 합쳐 완전한 URL 을만든다.

        if(isInternetCon()) { //false 반환시 if 문안의 로직 실행
            Toast.makeText(this, "인터넷에 연결되지않아 불러오기를 중단합니다.", Toast.LENGTH_SHORT).show();
            finish();
        }else{ //인터넷 체크 통과시 실행할 로직
            try {
                process(); //네트워크 관련은 따로 쓰레드를 생성해야 UI 쓰레드와 겹치지 않는다. 그러므로 Thread 가 선언된 process 메서드를 호출한다.
                BBSAdapter1.notifyDataSetChanged();
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
							progressDialog1 = ProgressDialog.show(Main2.this, "", "게시판 정보를 가져오는중 입니다.");
						}
					}, 0);

                try {
                    URL1 = new URL(url1);
                    InputStream html = URL1.openStream();
                    source1 = new Source(new InputStreamReader(html, "euc-kr")); //소스를 UTF-8 인코딩으로 불러온다.
                    source1.fullSequentialParse(); //순차적으로 구문분석
                } catch (Exception e) {
                    Log.d("ERROR", e + "");
                }

                List<StartTag> tabletags = source1.getAllStartTags(HTMLElementName.DIV); // DIV 타입의 모든 태그들을 불러온다.

                for(int arrnum = 0;arrnum < tabletags.size(); arrnum++){ //DIV 모든 태그중 bbsContent 태그가 몇번째임을 구한다.


					if(tabletags.get(arrnum).toString().equals("<div class=\"boardRead\">")) {
						BBSlocate1 = arrnum; //DIV 클래스가 bbsContent 면 arrnum 값을 BBSlocate 로 몇번째인지 저장한다.
						Log.d("BBSLOCATES", arrnum+""); //arrnum 로깅
						break;
					}
                }



                Element BBS_DIV = (Element) source1.getAllElements(HTMLElementName.DIV).get(BBSlocate1); //BBSlocate 번째 의 DIV 를 모두 가져온다.
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
                        mListData1.add(new ListData(BCS_title1, BCS_writer, BCS_date1, BCS_abc, BCS_down)); //데이터가 모이면 데이터 리스트 클래스에 데이터들을 등록한다.
                        Log.d("BCSARR","타입:"+BCS_type+"\n제목:" +BCS_title +"\n주소:"+BCS_url +"\n글쓴이:" + BCS_writer + "\n날짜:" + BCS_date+"\n내용:"+BBS_TBODY+BBS_TBODY2);

                    
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							BBSAdapter1.notifyDataSetChanged(); //모든 작업이 끝나면 리스트 갱신
							progressDialog1.dismiss(); //모든 작업이 끝나면 다이어로그 종료
						}
					}, 0);



			}

        }.start();


    }




    private boolean isInternetCon() {
        cManager1=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        mobile = cManager1.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); //모바일 데이터 여부
        wifi = cManager1.getNetworkInfo(ConnectivityManager.TYPE_WIFI); //와이파이 여부
        return !mobile.isConnected() && !wifi.isConnected(); //결과값을 리턴
    }




    // <리스트 적용부분
    class ViewHolder {

        public TextView mTitle1;
        public TextView mWriter1;
        public TextView mDate1;
	public TextView mDown;

        public TextView mabc;
    }



    public class BBSListAdapter extends BaseAdapter {
        private Context mContext;

        public BBSListAdapter(Context mContext) {
            this.mContext = mContext;
        }


        @Override
        public int getCount() {
            return mListData1.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData1.get(position);
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

                holder.mTitle1 = (TextView) convertView.findViewById(R.id.item_title1);
                holder.mWriter1 = (TextView) convertView.findViewById(R.id.item_writer1);
                holder.mDate1 = (TextView) convertView.findViewById(R.id.item_date1);
holder.mabc = (TextView) convertView.findViewById(R.id.item_abc);
holder.mDown = (TextView) convertView.findViewById(R.id.item_down);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData mData = mListData1.get(position);

            
                holder.mTitle1.setText(mData.mTitle1);
            

            holder.mWriter1.setText(mData.mWriter1+" 선생님"); //선생님을 붙힘
            holder.mDate1.setText(mData.mDate1);
            return convertView;

        }


    }

    public class ListData { // 데이터를 받는 클래스

        public String mTitle1;
        public String mWriter1;
        public String mDate1;
        public String mabc;
        public String mDown;


        public ListData()  {


        }

        public ListData(String mTitle1,String mWriter1,String mDate1, String mabc,String mDown)  { //데이터를 받는 클래스 메서드
     
            this.mTitle1 = mTitle1;
            
            this.mWriter1 = mWriter1;
            this.mDate1 = mDate1;
            this.mabc = mabc;
            this.mDown = mDown;
        }

    }
    // 리스트 적용부분 >
}

