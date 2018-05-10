package ucas.android.ucas_toolkit.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * @author sanvenir
 *
 */
public class WebMethod {
	
	protected static CookieManager cookieManager = new CookieManager();
	protected static boolean authState = false;
	
	public static void init() {
		HttpURLConnection.setFollowRedirects(true);
		CookieHandler.setDefault(cookieManager);
	}

	private static HttpURLConnection OpenUrl(String url) throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		createHeader(conn);
		return conn;
	}
	
	private static void createHeader(HttpURLConnection conn) {
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Pragma", "no-cache");
		conn.setRequestProperty("Cache-Control", "no-cache");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,"
				+ "application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:45.0) Gecko/20100101 Firefox/45.0");
		conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
		conn.setRequestProperty("Accept-Language", "zh-CH,zh;q=0.8,en;q=0.6");
	}
	
	protected static String GetBuffer(HttpURLConnection conn) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}
	
	public static boolean networkUnstable(HttpURLConnection conn) throws IOException {
		String responseCode;
		if(!(responseCode = conn.getResponseMessage()).equals("OK")) {
			System.out.println("Network  response: " + responseCode);
			return true;
		} else
			return false;
	}   
	public static String unquote(String a) {
      Properties prop = new Properties();
      try {
         prop.load(new ByteArrayInputStream(("x=" + a).getBytes()));
      }
      catch (IOException ignore) {}
      return prop.getProperty("x");
   }
	
	/** 发送POST请求
	 * @param url url地址
	 * @param postParams POST参数
	 * @return 页面html
	 * @throws IOException
	 */
	public static HttpURLConnection SendPost(String url, String...postParams) throws IOException {
		HttpURLConnection conn = OpenUrl(url);
		conn.setRequestMethod("POST");
		
		StringBuilder params = new StringBuilder();
		for(String param : postParams) {
			if(params.length() == 0)
				params.append(param);
			else
				params.append("&" + param);
		}
		System.out.println(params.toString());
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(params.toString());
		wr.flush();
		wr.close();
		
		return conn;
	}
	
	/** 获取页面内容（GET请求）
	 * @param url url地址
	 * @return 页面html
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static HttpURLConnection GetPageContent(String url) throws MalformedURLException, IOException { 
		HttpURLConnection conn = OpenUrl(url);
		conn.setRequestMethod("GET");
		
		return conn;
	}
	
	/** 登陆Sep系统
	 * @param username 用户名
	 * @param password 密码
	 * @return 成功登陆，返回true；否则返回false
	 * @throws IOException
	 */
	public static boolean login(String username, String password) throws IOException {
		String loginPage = "http://sep.ucas.ac.cn";
		String loginUrl = loginPage + "/slogin";

		System.out.println("url---->" + loginUrl);

		String responseCode;

//        HttpURLConnection conn = SendPost(loginUrl, "userName=" + username, "pwd=" + password, "sb=sb");
//        String response = GetBuffer(conn);
//        System.out.println("resp=====>" + response);


		if(!(responseCode = SendPost(loginUrl, "userName=" + username, "pwd=" + password, "sb=sb").getResponseMessage()).equals("OK")) {
			System.out.println("Login Page Response----------->" + responseCode);
			authState = false;
			return false;
		}
		for(HttpCookie cookie : cookieManager.getCookieStore().getCookies()){
            System.out.println("cookie---->" + cookie.getName());
            if(cookie.getName().equals("sepuser")) {
                authState = true;
                return true;
            }
        }

		authState = false;
		return false;
	}
	
	public static boolean getAuthState() {
		return authState;
	}

	public static void main(String[] args) throws Exception{
		BuyTicket web = new BuyTicket();
		label: 
			while(true) {
				if(!WebMethod.authState)
					WebMethod.login("", "");
				switch(web.getBuyState()) {
				case 0:web.ticketLogin();
				case 1:web.fetchBusRouteData(0);System.out.println(web.getRouteList());
				case 2:web.CheckRemainSeat(0);
				case 3:web.ConfigPayment();
				case 4:break label;
				}
			}
		System.out.println(web.getPaymentUrl());
	}

}
