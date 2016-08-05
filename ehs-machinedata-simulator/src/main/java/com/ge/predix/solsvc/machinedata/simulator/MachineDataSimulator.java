package com.ge.predix.solsvc.machinedata.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ge.predix.solsvc.machinedata.simulator.config.Constants;

import com.ge.predix.solsvc.machinedata.simulator.vo.AssetBody;
import com.ge.predix.solsvc.machinedata.simulator.vo.EHSObjectVO;
import com.ge.predix.solsvc.restclient.impl.RestClient;



/**
 * 
 * @author predix -
 */
@Component
public class MachineDataSimulator
{
    /**
     * 
     */
    static final Logger         log = LoggerFactory.getLogger(MachineDataSimulator.class);
    private ObjectMapper mapper = new ObjectMapper(); 

    public MachineDataSimulator() {
    	mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
		mapper.setSerializationInclusion(Include.NON_NULL);   
		
	}


	/**
	 * 
	 */
    @Autowired
    ApplicationProperties applicationProperties;
    
    @Autowired
	private MapperService mapperService;

    @Autowired
    private RestClient restClient;
    /**
     *  -
     */
    @Scheduled(fixedDelay=3000)
    public void run()
    {
        
        try
        {
           //runTest();
        	//Sipra
        	String areaJson = "EHS_Area_Json.json";
        	readEHSDataAndGenerateRandomJSON(areaJson);
        	
        	//String machineJson = "EHS_Machine_JSON.json";
        	//readEHSDataAndGenerateRandomJSON(machineJson);
        	
        	
        }
        catch (Throwable e)
        {
            log.error("unable to run Machine DataSimulator Thread", e); //$NON-NLS-1$
        }
    }

    /**
     * @return String  Response string
     * @throws Exception -
     */
    public String runTest() throws Exception
    {
        List<JSONData> list = generateMockDataMap_RT();
    	
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        
            mapper.writeValue(writer, list);
            return postData(writer.toString());
        
    }
    
    
   /* private MessageObject getMessageObject(EHSObjectVO message) throws JsonProcessingException {
		MessageObject messageObject = new MessageObject();
		Long currentTimeMillis = System.currentTimeMillis();
		ArrayList<Long> datapoint = new ArrayList<>();
		datapoint.add(currentTimeMillis);
		datapoint.add(10l);
		datapoint.add(1l);
		messageObject.setMessageId(currentTimeMillis);
		for (AssetBody ehsArea : message.getBody()) {
			Body body = new Body();

			body.setO3(getValues(Constants.Parameter.O3));
			body.setNH3(getValues(Constants.Parameter.NH3));
			body.setNO2(getValues(Constants.Parameter.NO2));
			body.setPB(getValues(Constants.Parameter.Pb));
			body.setCO2(getValues(Constants.Parameter.CO));
			body.setSO2(getValues(Constants.Parameter.SO2));
			body.setPM2_5(getValues(Constants.Parameter.PM2_5));
			body.setPM10(getValues(Constants.Parameter.PM10));

			body.setName(ehsArea.getName());
			body.getDatapoints().add(datapoint);
			messageObject.getBody().add(body);
		}
		return messageObject;
	}*/
    
    private EHSObjectVO getRandomEHSObject(EHSObjectVO message, Long currentTimeMillis) throws JsonProcessingException {
    	EHSObjectVO messageObject = new EHSObjectVO();
		ArrayList<Long> datapoint = new ArrayList<>();
		datapoint.add(currentTimeMillis);
		datapoint.add(10l);
		datapoint.add(1l);
		messageObject.setMessageId(currentTimeMillis);
		for (AssetBody ehsArea : message.getBody()) {
			AssetBody body = new AssetBody();

			body.setO3(getValues(Constants.Parameter.O3));
			body.setNH3(getValues(Constants.Parameter.NH3));
			body.setNO2(getValues(Constants.Parameter.NO2));
			body.setPB(getValues(Constants.Parameter.Pb));
			body.setCO2(getValues(Constants.Parameter.CO));
			body.setSO2(getValues(Constants.Parameter.SO2));
			body.setPM2_5(getValues(Constants.Parameter.PM2_5));
			body.setPM10(getValues(Constants.Parameter.PM10));

			body.setName(ehsArea.getName());
			body.getDatapoints().add(datapoint);
			
			messageObject.getBody().add(body);
			
		}
		return messageObject;
	}
    
    
	private Double getValues(Constants.Parameter range) {
		Double values ;
		// Calculating the limits of random value to be generated
		Random r = new Random();
		int minLimit = 0;
		int maxLimit = 50;
		switch (range) {
		case O3:
			minLimit = 10;
			maxLimit = 50;
			break;
		case NH3:
			minLimit = 10;
			maxLimit = 50;
			break;
		case NO2:
			minLimit = 10;
			maxLimit = 50;
			break;
		case Pb:
			minLimit = 20;
			maxLimit = 50;
			break;
		case CO:
			minLimit = 700;
			maxLimit = 850;
			break;
		case SO2:
			minLimit = 10;
			maxLimit = 50;
			break;
		case PM2_5:
			minLimit = 10;
			maxLimit = 30;
			break;
		case PM10:
			minLimit = 10;
			maxLimit = 20;
			break;

		default:
			break;
		}
		// Adding 5 random value to the array list
		/*for (int i = 0; i < 5; i++) {
			// generating the random value from the limit
			int result = r.nextInt(maxLimit - minLimit) + minLimit;
			values.add((double) result);
		}*/
		int result = r.nextInt(maxLimit - minLimit) + minLimit;
		values= (double) result;
		return values;
	}
    
    // Added By Sipra
	/*public String pushEHSData(String jsonFileName) throws Exception {
		// List<JSONData> list = generateMockDataMap_RT();
		// read from json
		EHSObjectVO ehsObj = mapperService.parse(jsonFileName);

		List<AssetBody> bodyList = ehsObj.getBody();
		MessageObject messageObject = getMessageObject(ehsObj); 
		System.out.println();
		System.out.println("--------------------------------------------------");
		System.out.println("Generated JSON: \n" + mapper.writeValueAsString(messageObject));

		ArrayList<HashMap<String, Object>> selectedValues = pickValueFromGeneratedJson(messageObject);
		System.out.println("Selected values from Generated JSON: \n" + mapper.writeValueAsString(selectedValues) + "\n");

		System.out.println("--------------------------------------------------");
		System.out.println(); 
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();

	//	mapper.writeValue(writer, bodyList);
		mapper.writeValue(writer, selectedValues);
		return postData(writer.toString());

	}*/
	
	public String readEHSDataAndGenerateRandomJSON(String jsonFileName) throws Exception {
		// TODO: Read data from Sensor by passing current timestamp
		
		//
		//Generate JSON from EHS Data
		EHSObjectVO ehsObj = mapperService.parse(jsonFileName);
		Long currentTimeMillis = System.currentTimeMillis();
		EHSObjectVO updatedEHSObject = getRandomEHSObject(ehsObj, currentTimeMillis);
		List<AssetBody> bodyList = updatedEHSObject.getBody();
		
		System.out.println();
		System.out.println("--------------------------------------------------");
		System.out.println("Random EHSObject Data: \n" + updatedEHSObject);

		System.out.println("--------------------------------------------------");
		System.out.println(); 
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();

		mapper.writeValue(writer, bodyList);
		return postData(writer.toString());

	}
	
	
	
    /**
     * @return -
     */
	
	
    List<JSONData> generateMockDataMap_RT()
    {
        String machineControllerId = this.applicationProperties.getMachineControllerId();
        List<JSONData> list = new ArrayList<JSONData>();
        JSONData data = new JSONData();
        data.setName("Compressor-2015:CompressionRatio"); //$NON-NLS-1$
        data.setTimestamp(getCurrentTimestamp());
        data.setValue((generateRandomUsageValue(2.5, 3.0) - 1) * 65535.0 / 9.0);
        data.setDatatype("DOUBLE"); //$NON-NLS-1$
        data.setRegister(""); //$NON-NLS-1$
        data.setUnit(machineControllerId);
        list.add(data);

        data = new JSONData();
        data.setName("Compressor-2015:DischargePressure"); //$NON-NLS-1$
        data.setTimestamp(getCurrentTimestamp());
        data.setValue((generateRandomUsageValue(0.0, 23.0) * 65535.0) / 100.0);
        data.setDatatype("DOUBLE"); //$NON-NLS-1$
        data.setRegister(""); //$NON-NLS-1$
        data.setUnit(machineControllerId);
        list.add(data);

        data = new JSONData();
        data.setName("Compressor-2015:SuctionPressure"); //$NON-NLS-1$
        data.setTimestamp(getCurrentTimestamp());
        data.setValue((generateRandomUsageValue(0.0, 0.21) * 65535.0) / 100.0);
        data.setDatatype("DOUBLE"); //$NON-NLS-1$
        data.setRegister(""); //$NON-NLS-1$
        data.setUnit(machineControllerId);
        list.add(data);

        data = new JSONData();
        data.setName("Compressor-2015:MaximumPressure"); //$NON-NLS-1$
        data.setTimestamp(getCurrentTimestamp());
        data.setValue((generateRandomUsageValue(22.0, 26.0) * 65535.0) / 100.0);
        data.setDatatype("DOUBLE"); //$NON-NLS-1$
        data.setRegister(""); //$NON-NLS-1$
        data.setUnit(machineControllerId);
        list.add(data);

        data = new JSONData();
        data.setName("Compressor-2015:MinimumPressure"); //$NON-NLS-1$
        data.setTimestamp(getCurrentTimestamp());
        data.setValue(0.0);
        data.setDatatype("DOUBLE"); //$NON-NLS-1$
        data.setRegister(""); //$NON-NLS-1$
        data.setUnit(machineControllerId);
        list.add(data);

        data = new JSONData();
        data.setName("Compressor-2015:Temperature"); //$NON-NLS-1$
        data.setTimestamp(getCurrentTimestamp());
        data.setValue((generateRandomUsageValue(65.0, 80.0) * 65535.0) / 200.0);
        data.setDatatype("DOUBLE"); //$NON-NLS-1$
        data.setRegister(""); //$NON-NLS-1$
        data.setUnit(machineControllerId);
        list.add(data);

        data = new JSONData();
        data.setName("Compressor-2015:Velocity"); //$NON-NLS-1$
        data.setTimestamp(getCurrentTimestamp());
        data.setValue((generateRandomUsageValue(0.0, 0.1) * 65535.0) / 0.5);
        data.setDatatype("DOUBLE"); //$NON-NLS-1$
        data.setRegister(""); //$NON-NLS-1$
        data.setUnit(machineControllerId);
        list.add(data);

        return list;
    }

    private Timestamp getCurrentTimestamp()
    {
        java.util.Date date = new java.util.Date();
        Timestamp ts = new Timestamp(date.getTime());
        return ts;
    }

    private static double generateRandomUsageValue(double low, double high)
    {
        return low + Math.random() * (high - low);
    }
    
    
    private String postData(String content)
    {   	
        HttpClient client = null;
        try
        {
            HttpClientBuilder builder = HttpClientBuilder.create();
            if ( this.applicationProperties.getDiServiceProxyHost() != null
                    && !"".equals(this.applicationProperties.getDiServiceProxyHost()) //$NON-NLS-1$
                    && this.applicationProperties.getDiServiceProxyPort() != null
                    && !"".equals(this.applicationProperties.getDiServiceProxyPort()) ) //$NON-NLS-1$
            {
                HttpHost proxy = new HttpHost(this.applicationProperties.getDiServiceProxyHost(),
                        Integer.parseInt(this.applicationProperties.getDiServiceProxyPort()));
                builder.setProxy(proxy);
            }
            client = builder.build(); 
            String serviceURL = null;
            if ( this.applicationProperties.getPredixDataIngestionURL() == null )
            {
                serviceURL = "http://"+this.applicationProperties.getDiServiceHost()+":"+this.applicationProperties.getDiServicePort()+"/saveTimeSeriesData"; //$NON-NLS-1$
                URLEncoder.encode(serviceURL,"UTF-8"); 
            }
            else
            {
                serviceURL = this.applicationProperties.getPredixDataIngestionURL()+"/saveTimeSeriesData"; //$NON-NLS-1$
                URLEncoder.encode(serviceURL,"UTF-8"); 

            }
            log.info("Service URL : " + serviceURL); //$NON-NLS-1$
            log.info("Data : "+content);
            HttpPost request = new HttpPost(serviceURL);
            HttpEntity reqEntity = MultipartEntityBuilder.create().addTextBody("content", content) //$NON-NLS-1$
                    .addTextBody("destinationId", "TimeSeries").addTextBody("clientId", "TimeSeries") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    .addTextBody("tenantId", this.applicationProperties.getTenantId()).build(); //$NON-NLS-1$
            request.setEntity(reqEntity);
            HttpResponse response = client.execute(request);
            log.debug("Send Data to Ingestion Service : Response Code : " + response.getStatusLine().getStatusCode()); //$NON-NLS-1$
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = ""; //$NON-NLS-1$
            while ((line = rd.readLine()) != null)
            {
                result.append(line);
            }
            log.debug("Response : "+result.toString());
            if (result.toString().startsWith("You successfully posted")) { //$NON-NLS-1$
            	return "SUCCESS : "+result.toString(); //$NON-NLS-1$
            }
			return "FAILED : "+result.toString(); //$NON-NLS-1$

        }
        catch (Throwable e)
        {
            log.error("unable to post data ", e); //$NON-NLS-1$
            return "FAILED : "+e.getLocalizedMessage(); //$NON-NLS-1$
        }
    } 
    
/*    private String postData(String content)
    {   	
    	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
    	parameters.add(new BasicNameValuePair("content", content));
    	parameters.add(new BasicNameValuePair("destinationId", "TimeSeries"));
    	parameters.add(new BasicNameValuePair("clientId", "TimeSeries"));   	
    	parameters.add(new BasicNameValuePair("tenantId", this.applicationProperties.getTenantId()));
    	
    	
    	EntityBuilder builder = EntityBuilder.create();
    	builder.setParameters(parameters);
    	HttpEntity reqEntity = builder.build();
    	
    	String serviceURL = this.applicationProperties.getPredixDataIngestionURL(); //$NON-NLS-1$
    	if (serviceURL == null) {
    		serviceURL = this.applicationProperties.getDiServiceURL();
    	}
        if (serviceURL != null) {
	        try(CloseableHttpResponse response = restClient.post(serviceURL, reqEntity, null, 100, 1000);)
	        {        	
	            
	            log.info("Service URL : " + serviceURL); //$NON-NLS-1$
	            log.info("Data : "+content);
	            
	            log.debug("Send Data to Ingestion Service : Response Code : " + response.getStatusLine().getStatusCode()); //$NON-NLS-1$
	            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	            StringBuffer result = new StringBuffer();
	            String line = ""; //$NON-NLS-1$
	            while ((line = rd.readLine()) != null)
	            {
	                result.append(line);
	            }
	            log.info("Response : "+result.toString());
	            if (result.toString().startsWith("You successfully posted")) { //$NON-NLS-1$
	            	return "SUCCESS : "+result.toString(); //$NON-NLS-1$
	            }
				return "FAILED : "+result.toString(); //$NON-NLS-1$
	            
	        } catch (IOException e) {
	        	 log.error("unable to post data ", e); //$NON-NLS-1$
	             return "FAILED : "+e.getLocalizedMessage(); //$NON-NLS-1$
			}
        }else{
        	return "Dataingestion Service URL is empty.";
        }
    }*/
}
