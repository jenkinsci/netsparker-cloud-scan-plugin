package NCJenkinsPlugin.Utility;

import Utility.AppCommon;
import org.junit.Assert;
import org.junit.Test;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppCommonTests{
	@Test
	public void LocalURLValidation() {
		AppCommon appCommon = new AppCommon();
		boolean isValid = appCommon.IsUrlValid("http://localhost:2097");
		Assert.assertTrue(isValid);
	}
	
	@Test
	public void OnpremisesServerURLValidation() {
		AppCommon appCommon = new AppCommon();
		boolean isValid = appCommon.IsUrlValid("http://TFSServer:2097");
		Assert.assertTrue(isValid);
	}
	
	@Test
	public void IsGuIDValid() {
		String guidString = "eb3eecdc-266d-4b80-1230-a7ee03737f48";
		AppCommon appCommon = new AppCommon();
		boolean isValid = appCommon.IsGUIDValid(guidString);
		
		Assert.assertTrue(isValid);
	}
	
	@Test
	public void IsDateValid() throws ParseException {
		long timestamp = 1513090394000L;
		Date date = new Date(timestamp);
		SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		String dateString = originalFormat.format(date);
		Assert.assertEquals("2017-12-12T17:53+0300", dateString);
	}
}