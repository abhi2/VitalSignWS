package vital.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.utility.TestMail;

public class VitalSignWS {

	Connection con;
	Statement st;
	ResultSet rs;
	
	String msg="";
	public VitalSignWS()
	{
		try{
			Class.forName("com.mysql.jdbc.Driver");
			con=(Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/ndorange","root","root");
			System.out.println("Connected to database");
			
			
		}
		catch(Exception e)
		{
		e.printStackTrace();
		
		}
	}
	public String userLogin(String uname,String pwd)
	{
		
		System.out.println("Username "+uname+" Password "+pwd);
		try{
		st=con.createStatement();
		rs=st.executeQuery("select * from userdetails where username='"+uname+"'and password='"+pwd+"'");
		if(rs.next())
		{
			msg="login succesfull";
			System.out.println("login succesfull");
			//msg+="@"+rs.getString(1);
			
			
		}
		
		else
		{
			msg="Enter the correct username and password";
			System.out.println("Enter the correct username and password");
		}
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
		}
		return msg;
	}

	public String viewProfile(String uname,String pwd)
	{
		try{
			st=con.createStatement();
			rs=st.executeQuery("select * from userdetails where username='"+uname+"' and password='"+pwd+"'");
			if(rs.next())
			{
				msg=rs.getString(1)+"@";
				msg+=rs.getString(2)+"@";
				msg+=rs.getString(9)+"@";
				
				msg+=rs.getString(7)+"@";
				msg+=rs.getString(8)+"@";
				msg+=rs.getString(5)+"@";
				msg+=rs.getString(4)+"@";
				msg+=rs.getString(6)+"@";
				
			}
			else
			{
				msg="No Data";
				
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			msg="Server Exception";
			e.printStackTrace();
		}
		return msg;
	}
	
	public String addVitals(String uname,String vitalType,String vitalValue,String unitOfMeasure,String partyID,String encounterID,String authToken)
	{
		try
		{
			st=con.createStatement();
			Date d=new Date();
			DateFormat sd= new SimpleDateFormat("yyyy-MM-d hh:mm:ss");
			String dateandtime=sd.format(d);
			String[] s=dateandtime.split(" ");
			String date=s[0];
			String time=s[1];
			int q=st.executeUpdate("insert into addvitals values(null,'"+partyID+"','"+encounterID+"','"+vitalType+"','"+vitalValue+"','"+unitOfMeasure+"','"+uname+"','"+date+"','"+time+"','"+authToken+"')");
			if(q>0)
			{
				String vitalType1="";
				msg="added successfully";
				if(vitalType.equals("BP-S"))
				{
					vitalType1="Blood Pressure-Systolic";
				}
				else if(vitalType.equals("BP-D"))
				{
					vitalType1="Blood Pressure-Diastolic";
				}
				else
				{
					vitalType1=vitalType;
				}
				ResultSet rs=st.executeQuery("select threshold from setthresholds where pid='"+partyID+"' and vitaltype='"+vitalType1+"'");
				
				if(rs.next())
				{
					if(Integer.parseInt(vitalValue)>=Integer.parseInt(rs.getString(1)))
					{
						TestMail mail=new TestMail();
						mail.sendMail("abhisheksachdeva@gmail.com", "ExceedThresholdValues", "threshold value exceeds for patient"+"\n"+"VitalSignType:"+vitalType+"\n"+"PatientID:"+partyID);
					}
				}
			}
			else
			{
				msg="failed to insert";
			}

		}
		catch (Exception e) {
			// TODO: handle exception
			msg="Server Exception";
			e.printStackTrace();
		}
		return msg;
	}
	
	public String getVitals(String uname)
	{
		String result="";
		String authTokens="";
		try
		{
	st=con.createStatement();
//		rs=st.executeQuery("select distinct(vitalType),vitalValue,unitOfMeasure.encounterID from addvitals where username='"+uname+"' and enteredDate=CURDATE() order by eneterdTime DESC");
//		while(rs.next())
//		{
//			msg+=rs.getString(1)+"@";
//			msg+=rs.getString(2)+"@";
//			msg+=rs.getString(3)+"@";
//			msg+=rs.getString(4)+"#";
	
//		}
	String pid="";
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	String date1=sdf.format(new Date());
	ResultSet rs1=st.executeQuery("select pid from userdetails where username='"+uname+"' ");
	if(rs1.next())
	{
		pid=rs1.getString(1);
	}
		rs=st.executeQuery("select distinct(encounterId),authtoken from addvitals where partyId='"+pid+"' and entereddate='"+date1+"'");
		while(rs.next())
		{
			result+=rs.getString(1)+"@";
			authTokens+=rs.getString(2)+"@";
		}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result+"#"+authTokens;
	}
	public String updateVitals(String uname,String vitalType,String vitalValue)
	{
		String partyID="";
		try
		{
			
			st=con.createStatement();
			ResultSet rs=st.executeQuery("select pid from userdetails where username='"+uname+"'");
			if(rs.next())
			{
				partyID=rs.getString(1);
			}
			int q=st.executeUpdate("update addvitals set vitalValue='"+vitalValue+"', enteredDate=CURDATE(), enteredTime=CURTIME() where username='"+uname+"' and vitalType='"+vitalType+"'");
			if(q>0)
			{
				msg="Updated Successfully";
                ResultSet rs1=st.executeQuery("select threshold from setthresholds where pid='"+partyID+"' and vitaltype='"+vitalType+"'");
				
				if(rs1.next())
				{
					if(Integer.parseInt(vitalValue)>=Integer.parseInt(rs1.getString(1)))
					{
						TestMail mail=new TestMail();
						mail.sendMail("abhisheksachdeva@gmail.com", "ExceedThresholValues", "threshold value exceeds for patient"+uname+"\n"+"VitalSignType"+vitalType+"\n"+"PatientID:"+partyID);
					}
				}
			}
			else
				msg="Failed to update";
			
			
		
		}	
		catch (Exception e) {
			// TODO: handle exception
			msg="Server Exception";
			e.printStackTrace();
		}
		return msg;
	}
}
