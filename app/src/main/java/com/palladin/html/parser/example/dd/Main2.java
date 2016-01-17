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
import android.widget.*;
import com.palladin.html.parser.example.*;

public class Main2 extends ActionBarActivity {



    private static String URL_PRIMARY = "http://gaon.caems.kr"; //홈페이지 원본 주소이다.
    private static String GETNOTICE = "/boardCnts/list.do?boardID=211141&m=1001"; //홈페이지 의 게시판을 나타내는 뒤 주소, 비슷한 게시판들은 거의 파싱이 가능하므로 응용하여 사용하자.
    private String url;
    private URL URL;

    private Source source;
    private ProgressDialog progressDialog;
    ;
    
    private int BBSlocate;

    private ConnectivityManager cManager;
    private NetworkInfo mobile;
    private NetworkInfo wifi;

	private String BCS_down;

    


    @Override
    protected void onStop() { //멈추었을때 다이어로그를 제거해주는 메서드
        super.onStop();
        if ( progressDialog != null)
            progressDialog.dismiss(); //다이어로그가 켜져있을경우 (!null) 종료시켜준다
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        

        url = URL_PRIMARY + GETNOTICE; //파싱하기전 PRIMARY URL 과 공지사항 URL 을 합쳐 완전한 URL 을만든다.

        if(isInternetCon()) { //false 반환시 if 문안의 로직 실행
            Toast.makeText(Main2.this, "인터넷에 연결되지않아 불러오기를 중단합니다.", Toast.LENGTH_SHORT).show();
            finish();
        }else{ //인터넷 체크 통과시 실행할 로직
            try {
                process(); //네트워크 관련은 따로 쓰레드를 생성해야 UI 쓰레드와 겹치지 않는다. 그러므로 Thread 가 선언된 process 메서드를 호출한다.
                
            } catch (Exception e) {
                Log.d("ERROR", e + "");

            }
        }





    }
	
	public void onClick(View v){

		if(v.getId() == R.id.card_view1){

		

		}
		else if(v.getId()== R.id.card_view2){

		
		}
		else if(v.getId()== R.id.card_view3){

	
		 // 클릭한 포지션의 데이터를 가져온다.
		String URL_BCS = null; //가져온 데이터 중 url 부분만 적출해낸다.

		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRIMARY + BCS_down))); //적출해낸 url 을 이용해 URL_PRIMARY 와 붙이고

	}}
	
	

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
                    source = new Source(new InputStreamReader(html, "utf-8")); //소스를 UTF-8 인코딩으로 불러온다.
                    source.fullSequentialParse(); //순차적으로 구문분석
                } catch (Exception e) {
                    Log.d("ERROR", e + "");
                }

                List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.TABLE); // DIV 타입의 모든 태그들을 불러온다.

                for(int arrnum = 0;arrnum < tabletags.size(); arrnum++){ //DIV 모든 태그중 bbsContent 태그가 몇번째임을 구한다.


					if(tabletags.get(arrnum).toString().equals("<div class=\"infoArea\">")) {
                     BBSlocate = arrnum; //DIV 클래스가 bbsContent 면 arrnum 값을 BBSlocate 로 몇번째인지 저장한다.
                     Log.d("BBSLOCATES", arrnum+""); //arrnum 로깅
                     break;
                 }
                }



                Element BBS_DIV = (Element) source.getAllElements(HTMLElementName.DIV).get(BBSlocate); //BBSlocate 번째 의 DIV 를 모두 가져온다.
                Element BBS_TABLE = (Element) BBS_DIV.getAllElements(HTMLElementName.DIV).get(0); //테이블 접속
                Element BBS_TBODY = (Element) BBS_TABLE.getAllElements(HTMLElementName.DIV).get(0); //데이터가 있는 TBODY 에 접속


                for(int C_TR = 0; C_TR < BBS_TBODY.getAllElements(HTMLElementName.TR).size();C_TR++){ //여기서는 이제부터 게시된 게시물 데이터를 불러와 게시판 인터페이스를 구성할 것이다.


                    // 소스의 효율성을 위해서는 for 문을 사용하는것이 좋지만 , 이해를 돕기위해 소스를 일부로 늘려 두었다.

                    try {
                        Element BBS_TR = (Element) BBS_TBODY.getAllElements(HTMLElementName.DIV).get(C_TR); //TR 접속
						Element BC_title = (Element) BBS_TR.getAllElements(HTMLElementName.DD).get(0);
                        Element BC_TYPE = (Element) BBS_TR.getAllElements(HTMLElementName.DD).get(0); //타입 을 불러온다.

                        Element BC_info = (Element) BBS_TR.getAllElements(HTMLElementName.TD).get(1); //URL(herf) TITLE(title) 을 담은 정보를 불러온다.
                        Element BC_a = (Element) BC_info.getAllElements(HTMLElementName.A).get(0); //BC_info 안의 a 태그를 가져온다.
                        String BCS_down = BC_a.getAttributeValue("href"); //a 태그의 herf 는 BCS_url 로 선언
                        String BCS_title = BC_title.toString(); //a 태그의 title 은 BCS_title 로 선언

                        Element BC_writer = (Element) BBS_TR.getAllElements(HTMLElementName.DD).get(1); //글쓴이를 불러온다.
                        Element BC_date = (Element) BBS_TR.getAllElements(HTMLElementName.TD).get(2); // 날짜를 불러온다.

                        String BCS_type = BC_TYPE.getContent().toString(); // 타입값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.
                        String BCS_writer = BC_writer.getContent().toString(); // 작성자값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.
                        String BCS_date = BC_date.getContent().toString(); // 작성일자값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.


                        Log.d("BCSARR","타입:"+BCS_type+"\n제목:" +BCS_title +"\n주소:"+BCS_down +"\n글쓴이:" + BCS_writer + "\n날짜:" + BCS_date);



                    }catch(Exception e){
                       Log.d("BCSERROR",e+"");
                    }
                    }
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       //모든 작업이 끝나면 리스트 갱신
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
    }}




   
