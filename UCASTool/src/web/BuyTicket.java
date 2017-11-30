package web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

class BuyTicket extends WebMethod {
	private int buyState = 0;
	
    private String loginPage = "http://sep.ucas.ac.cn";
    private String buyTicketSystem = loginPage+"/portal/site/311/1800";
    private String buyTicketHomePage = "http://payment.ucas.ac.cn";
    private String buyTicketIdentity = buyTicketHomePage+"/NetWorkUI/sepLogin.htm?Identity=";
    private String buyTicketSystemLogin = buyTicketHomePage+"/NetWorkUI/sepLoginAction!findbyIdserial.do?idserial=";
    private String queryRemainingSeats = buyTicketHomePage+"/NetWorkUI/appointmentBus!queryRemainingSeats.action";
    private String queryBusByDate = buyTicketHomePage+"/NetWorkUI/appointmentBus!queryBusByDate.action";
    private String getPayProjectId = buyTicketHomePage+"/NetWorkUI/reservedBus514R001";
    private String goReserved = buyTicketHomePage + "/NetWorkUI/appointmentBus!goReserved.do?";
    private String goPay = buyTicketHomePage+"/NetWorkUI/appointmentBus!goPay.action";
    private String showUserSelectPayType = buyTicketHomePage+"/NetWorkUI/showUserSelectPayType25";
    private String onlinePay = buyTicketHomePage+"/NetWorkUI/onlinePay";

    private String buyTicketIdentityReal = "";
    private String buyTicketSystemLoginReal = "";
    private String bookingdate = "";
    private String routecode = "";
    private Integer freeseat = null;
    private String payProjectId = "";
    private String telNum = "";
    private String paymentUrl = "";
    
    private List<String> takeBusDay = new ArrayList<String>();
    private Date time = new Date();

    private BusRouteData busRouteData;
    private BusRouteData.Route routeContent;
    
    private HttpURLConnection conn;
    private String response;
    private Pattern pattern;
    private Matcher match;
	private Gson gson = new Gson();
    
    
	/** 登陆购票系统
	 * @return 成果返回true，否则返回false（重试）
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public boolean ticketLogin() throws MalformedURLException, IOException {
		
        SimpleDateFormat strTime = new SimpleDateFormat("yyyy-MM-dd E");
        takeBusDay.add(strTime.format(time));
        takeBusDay.add(strTime.format(new Date(time.getTime() + 24 * 60 * 60 * 1000)));
        takeBusDay.add(strTime.format(new Date(time.getTime() + 24 * 60 * 60 * 1000 * 2)));
        takeBusDay.add(strTime.format(new Date(time.getTime() + 24 * 60 * 60 * 1000 * 3)));
        
        conn = GetPageContent(buyTicketSystem);
        response = GetBuffer(conn);
        pattern = Pattern.compile("http://payment\\.ucas\\.ac\\.cn/NetWorkUI/sepLogin\\.htm\\?Identity=([\\d\\w=&-]*)");
        match = pattern.matcher(response);
        if(match.find())
        	buyTicketIdentityReal = buyTicketIdentity + match.group(1);
        else {
        	System.out.println("Match not found in payment page");
        	buyState = 0;
        	WebMethod.authState = false;
        	return false;
        }
        
        conn = GetPageContent(buyTicketIdentityReal);
        response = GetBuffer(conn);
        pattern = Pattern.compile("<option value=\"([\\w]*)\">\\1</option>");
        match = pattern.matcher(response);
        if(match.find()) {
        	System.out.println("Student id is " + match.group(1));
        	buyTicketSystemLoginReal = buyTicketSystemLogin + match.group(1);
        } else {
        	System.out.println("Match not found in payment login page");
        	buyState = 0;
        	WebMethod.authState = false;
        	return false;
        }
        
        conn = GetPageContent(buyTicketSystemLoginReal);
        response = GetBuffer(conn);
		
        conn = GetPageContent(getPayProjectId);
        if(networkUnstable(conn)) {
        	buyState = 0;
        	WebMethod.authState = false;
        	return false;
        }
        response = GetBuffer(conn);
        pattern = Pattern.compile("<input type=\"hidden\" value=\\'([\\d]*)\\' name=\"payProjectId\" id=\"payProjectId\" />");
        match = pattern.matcher(response);
        if(match.find()) {
        	payProjectId = match.group(1);
        	System.out.println("Pay Project id " + payProjectId);
        } else {
        	System.out.println("Match not found in pay project page");
        	buyState = 0;
        	WebMethod.authState = false;
        	return false;
        }
        
        buyState = 1;
        return true;
	}
	
	/** 乘车日期
	 * @return 乘车日期的列表，从当前日期开始四日内
	 */
	public List<String> ticketDate() {
        return takeBusDay;
	}
	
	public class BusRouteData {
		public class Route {
			String routedatetype = "";
			String routetime;
			String routecode;
			String routeid;
			String bespeaktype;
			int maxseatnum;
			String calculatecode;
			String moneyperseat;
			String routename;
			String routedetail;
			String forbiddencode;
			public String toString() {
				return routecode + ": " + routename;
			}
		}
		String returncode;
		List<Route> routelist;
		public String toString() {
			StringBuilder str = new StringBuilder();
			for(Route route : routelist) {
				str.append(route.toString());
				str.append("\n");
			}
			return str.toString();
		}
	}
	
	/** 获取线路信息
	 * @param num 选择第几日的线路（0-3）
	 * @return 是否成功查询
	 */
	public boolean fetchBusRouteData(int num) {
		if(buyState < 1) {
			return false;
		}
		try {
			bookingdate = takeBusDay.get(num).split(" ")[0];
			conn = SendPost(queryBusByDate, "bookingdate=" + bookingdate, "factorycode=R001");
			response = GetBuffer(conn);
			busRouteData = gson.fromJson(response, BusRouteData.class);
			if(!busRouteData.returncode.equals("SUCCESS")) {
				buyState = 0;
				return false;
			}
			buyState = 2;
			return true;
		} catch (Exception e) {
			System.out.println("Error, retry");
			buyState = 0;
			return false;
		}
	}
	
	public List<String> getRouteList() {
		if(buyState < 2)
			return null;
		List<String> result = new ArrayList<String>();
		for(BusRouteData.Route route : busRouteData.routelist) {
			result.add(route.toString());
		}
		return result;
	}
	
	public class SeatData {
		public class ReturnData {
			int freeseat;
			int bookingnum;
			int passengernum;
		}
		String returncode;
		ReturnData returndata;
	}
	
	/**
	 * @param num
	 * @return 是否成功查询（查询到没有剩余座位不算成功查询）
	 * @throws IOException
	 */
	public boolean CheckRemainSeat(int num) {
		if(buyState < 2)
			return false;
		try {
			routecode = busRouteData.routelist.get(num).routecode;
			conn = SendPost(queryRemainingSeats, "routecode=" + routecode, "bookingdate" + bookingdate, "factorycode=R001");
			if(networkUnstable(conn)) {
				buyState = 1;
				return false;
			}
			response = GetBuffer(conn);
			SeatData returnData = gson.fromJson(response, SeatData.class);
			if(returnData.returndata.freeseat > 0) {
				freeseat = returnData.returndata.freeseat;
				routeContent = busRouteData.routelist.get(num);
				buyState = 3;
				return true;
			}
			buyState = 1;
			return false;
		} catch (Exception e) {
			System.out.println("Error, retry");
			buyState = 1;
			return false;
		}
	}
	
	public Integer getFreeSeat() {
		if(buyState < 3)
			return null;
		return freeseat;
	}
	
	public class PaymentData {
		class PayOrderTrade {
			String id;
		}
		String returncode;
		String returnmsg;
		PayOrderTrade payOrderTrade;
	}
	
	public boolean ConfigPayment() throws IOException {
		if(buyState < 3)
			return false;
		conn = SendPost(
				goReserved, 
				"routecode=" + routecode, 
				"bookingdate=" + bookingdate, 
				"routeName=" + routeContent.routename,
				"payAmt=" + "6.00",
				"payProjectId=" + payProjectId,
				"freeseat=" + freeseat
				);
		if(networkUnstable(conn)) {
			buyState = 2;
			return false;
		}
		response = GetBuffer(conn);
		pattern = Pattern.compile("<p >([\\d]{11})</p>");
		match = pattern.matcher(response);
		if(match.find()) {
			telNum = match.group(1);
			System.out.println(telNum);
		} else {
			System.out.println("Match not found in config payment page");
			buyState = 2;
			return false;
		}
		try {
			conn = SendPost(
					goPay, 
					"routecode=" + routecode, 
					"bookingdate=" + bookingdate, 
					"payAmt=" + "6.00",
					"payProjectId=" + payProjectId,
					"tel=" + telNum,
					"factorycode=R001"
					);
			if(networkUnstable(conn)) {
				buyState = 2;
				return false;
			}
			response = GetBuffer(conn);
			PaymentData returnData = gson.fromJson(response, PaymentData.class);
			if(!returnData.returncode.equals("SUCCESS")) {
				System.out.println(returnData.returnmsg);
				buyState = 2;
				return false;
			}
			showUserSelectPayType += returnData.payOrderTrade.id;
			System.out.println(showUserSelectPayType);
		} catch(Exception e) {
			System.out.println(e);
			buyState = 2;
			return false;
		}
		
		conn = GetPageContent(showUserSelectPayType);
		if(networkUnstable(conn)) {
			buyState = 2;
			return false;
		}
		response = GetBuffer(conn);
		System.out.println(response);
		
		List<String> postdata = new ArrayList<String>();
		//postdata.add("start_limittxtime=");
		//postdata.add("end_limittxtime=");
		postdata.add("payType=03");
		
		String[] reList = {
                "<input type=\"hidden\" value=\\'(?<value>[\\w]*)\\'   name=\"(?<name>[\\w]*)\" id=\"orderno\" />",
                "<input type=\"hidden\" value='(?<value>[\\S]*)' name=\"(?<name>[\\w]*)\" id=\"orderamt\" />",
                "<input type=\"hidden\" id=\"mess\" value=\"(?<value>[\\S]*)\" name=\"(?<name>[\\w]*)\"/>",
                "<input type=\"hidden\" name=\"(?<name>[\\S]*)\" value=\"(?<value>[\\w]*)\" />",
                "<input type=\"hidden\" name=\"(?<name>[\\w]*)\" value=\"(?<value>[\\S]*)\" />"
		};
		
		boolean flag = false;
		for(String re : reList) {
			pattern = Pattern.compile(re);
			match = pattern.matcher(response);
			if(!match.find()) {
				flag = true;
				break;
			}
			postdata.add(match.group("name") + "=" + match.group("value"));
		}
		if(flag) {
			System.out.println("Match not found in select payment page");
			buyState = 2;
			return false;
		}
		conn = SendPost(onlinePay, postdata.toArray(new String[postdata.size()]));
		
		int responseCode;
		if((responseCode = conn.getResponseCode()) != 302) {
			System.out.println("Error in payment page, response " + responseCode);
			System.out.println(GetBuffer(conn));
			buyState = 2;
			return false;
		}
		paymentUrl = unquote(conn.getHeaderField("Location").split("&")[2].split("=")[1]);
		
		buyState = 4;
		return true;
	}
	
	public String getPaymentUrl() {
		if(buyState < 4)
			return null;
		return paymentUrl;
	}
	
	public int getBuyState() {
		switch(buyState) {
		case 0: System.out.println("Nothing done");break;
		case 1: System.out.println("After ticket login");break;
		case 2: System.out.println("After getting bus route data");break;
		case 3: System.out.println("After checking remain seat");break;
		case 4: System.out.println("After getting url successfully");break;
		}
		return buyState;
	}

	public BuyTicket() {
		super();
	}
	
}
