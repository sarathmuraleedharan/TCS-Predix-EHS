package com.ge.predix.solsvc.dataingestion.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ge.predix.entity.asset.Asset;
import com.ge.predix.entity.asset.AssetTag;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.Body;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
import com.ge.predix.solsvc.dataingestion.api.Constants;
import com.ge.predix.solsvc.dataingestion.service.type.JSONData;
import com.ge.predix.solsvc.dataingestion.vo.AssetBody;
import com.ge.predix.solsvc.dataingestion.vo.EHSObjectVO;
import com.ge.predix.solsvc.dataingestion.websocket.WebSocketClient;
import com.ge.predix.solsvc.dataingestion.websocket.WebSocketConfig;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesRestConfig;
import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesWSConfig;
import com.ge.predix.solsvc.timeseries.bootstrap.factories.TimeseriesFactory;

/**
 * 
 * @author predix -
 */
@Component
public class TimeSeriesDataIngestionHandler extends BaseFactory {
	private static Logger log = Logger.getLogger(TimeSeriesDataIngestionHandler.class);
	@Autowired
	private TimeseriesFactory timeSeriesFactory;

	@Autowired
	private AssetDataHandler assetDataHandler;

	@Autowired
	private TimeseriesWSConfig tsInjectionWSConfig;

	@Autowired
	private WebSocketConfig wsConfig;

	@Autowired
	private WebSocketClient wsClient;

	@Autowired
	private JsonMapper jsonMapper;
	
	private Map<String,Asset> assetMap = new HashMap<String,Asset>();

	@Autowired
	private TimeseriesRestConfig timeseriesRestConfig;

	/**
	 * -
	 */
	@SuppressWarnings("nls")
	@PostConstruct
	public void intilizeDataIngestionHandler() {
		log.info("*******************TimeSeriesDataIngestionHandler Initialization complete*********************");
	}

	/**
	 * @param data -
	 * @param authorization -
	 */
	@SuppressWarnings("nls")
	public void handleData(String data, String authorization) {
		log.info("Data from Simulator : " + data);
		List<Header> headers = this.restClient.getSecureTokenForClientId();
	this.restClient.addZoneToHeaders(headers, this.timeseriesRestConfig.getZoneId());
		headers.add(new BasicHeader("Origin", "http://localhost"));
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			//Changes Start Sipra
		//	List<JSONData> list = mapper.readValue(data, new TypeReference<List<JSONData>>() {
			List<AssetBody> list = mapper.readValue(data, new TypeReference<List<AssetBody>>() {
				//
			});
			log.debug("TimeSeries URL : " + this.tsInjectionWSConfig.getInjectionUri());
			log.debug("WebSocket URL : " + this.wsConfig.getPredixWebSocketURI());
			//Sipra
		//	DatapointsIngestion dpIngestion = createTimeseriesDataBody(list, authorization);
			String assetURI = "/Geography/001";
			
			DatapointsIngestion dpIngestion = createTimeseriesDataBody(list, assetURI, authorization);
			log.info("TimeSeries JSON : " + this.jsonMapper.toJson(dpIngestion));
			if (dpIngestion.getBody() != null && dpIngestion.getBody().size() > 0) {
				// commented by sipra
				//this.wsClient.postToWebSocketServer(this.jsonMapper.toJson(dpIngestion));
				log.info("Posted Data to Predix Websocket Server");

				this.timeSeriesFactory.postDataToTimeseriesWebsocket(dpIngestion, headers);
				log.info("Posted Data to Timeseries");
			}

		} catch (JsonParseException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param json
	 * @param i
	 * @param asset
	 * @param assetTag
	 * @return -
	 */
	@SuppressWarnings({ "unchecked", "unused", "nls" })
	private DatapointsIngestion createTimeseriesDataBody(JSONData json, Long i, Asset asset, AssetTag assetTag) {
		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));

		Body body = new Body();
		body.setName(assetTag.getSourceTagId());
		//Sipra
		// attributes
		com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
		map.put("assetId", asset.getAssetId());
		if (!StringUtils.isEmpty(assetTag.getSourceTagId())) {
			String sourceTagAttribute = assetTag.getSourceTagId().split(":")[1];
			map.put("sourceTagId", sourceTagAttribute);
		}
		body.setAttributes(map);
		//Sipra
		// datapoints
		List<Object> datapoint1 = new ArrayList<Object>();
		datapoint1.add(converLocalTimeToUtcTime(json.getTimestamp().getTime()));
		Double convertedValue = getConvertedValue(assetTag.getTagDatasource().getNodeName(),
				Double.parseDouble(json.getValue().toString()));
		datapoint1.add(convertedValue);

		List<Object> datapoints = new ArrayList<Object>();
		datapoints.add(datapoint1);
		body.setDatapoints(datapoints);

		List<Body> bodies = new ArrayList<Body>();
		bodies.add(body);

		dpIngestion.setBody(bodies);

		return dpIngestion;
	}

	@SuppressWarnings({ "unchecked", "nls" })
	//Sipra
	//private DatapointsIngestion createTimeseriesDataBody(List<JSONData> jsonData, String authorization) {
		private DatapointsIngestion createTimeseriesDataBody(List<AssetBody> jsonData, String machineValue, String authorization) {
		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));
		List<Body> bodies = new ArrayList<Body>();

		for (AssetBody data : jsonData) {
			
			String filter = "attributes.machineControllerId.value";
			//String value = "/asset/Bently.Nevada.3500.Rack" + data.getUnit();
			//Sipra
			String value = machineValue;//"/Geography/1";
			String nodeName = data.getName();
			Asset asset = this.assetMap.get(value);
			if (asset == null) {
				asset = this.assetDataHandler.retrieveAsset(null, filter, value, authorization);
				if (asset == null) {
					throw new RuntimeException("Error retriving asset for filter=" + filter + "=" + value); //$NON-NLS-1$
				}
				this.assetMap.put(value, asset);
			}
			if (asset != null) {
				LinkedHashMap<String, AssetTag> tags = asset.getAssetTag();
				if (tags != null) {
					AssetTag assetTag = getAssetTag(asset.getAssetTag(), nodeName);
					Body body = new Body();
					body.setName(assetTag.getSourceTagId());
					// attributes
					com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
					map.put("assetId", asset.getAssetId());
					if (!StringUtils.isEmpty(assetTag.getSourceTagId())) {
						String sourceTagAttribute = assetTag.getSourceTagId().split(":")[1];
						map.put("sourceTagId", sourceTagAttribute);
					}
					body.setAttributes(map);

					// datapoints
				//	List<Object> datapoint1 = new ArrayList<Object>();
					/*datapoint1.add(converLocalTimeToUtcTime(data.getTimestamp().getTime()));
					Double convertedValue = getConvertedValue(assetTag.getTagDatasource().getNodeName(),
							Double.parseDouble(data.getValue().toString()));*/
					//datapoint1.add(convertedValue);
					
						
					// Sipra

					List<Object> datapoints = new ArrayList<Object>();
					
					Long currentUTCTimeFromObject = converLocalTimeToUtcTime(data.getDatapoints().get(0).get(0));
					
					List<Object> assetDatapoint = new ArrayList<Object>();					
					assetDatapoint.add(currentUTCTimeFromObject);
					assetDatapoint.add(data.getDatapoints().get(0).get(1));
					assetDatapoint.add(data.getDatapoints().get(0).get(2));
					
					List<Object> CO2Datapoint = new ArrayList<Object>();					
					CO2Datapoint.add(currentUTCTimeFromObject);
					CO2Datapoint.add(data.getCO2());
					
					List<Object> nameDatapoint = new ArrayList<Object>();					
					nameDatapoint.add(currentUTCTimeFromObject);
					nameDatapoint.add(data.getName());
					
					List<Object> NH3Datapoint = new ArrayList<Object>();					
					NH3Datapoint.add(currentUTCTimeFromObject);
					NH3Datapoint.add(data.getNH3());
					
					List<Object> NO2Datapoint = new ArrayList<Object>();					
					NO2Datapoint.add(currentUTCTimeFromObject);
					NO2Datapoint.add(data.getNO2());
					
					List<Object> O3Datapoint = new ArrayList<Object>();					
					O3Datapoint.add(currentUTCTimeFromObject);
					O3Datapoint.add(data.getO3());
					
					List<Object> PBDatapoint = new ArrayList<Object>();					
					PBDatapoint.add(currentUTCTimeFromObject);
					PBDatapoint.add(data.getPB());
					
					List<Object> PM10Datapoint = new ArrayList<Object>();					
					PM10Datapoint.add(currentUTCTimeFromObject);
					PM10Datapoint.add(data.getPM10());
					
					List<Object> PM25Datapoint = new ArrayList<Object>();					
					PM25Datapoint.add(currentUTCTimeFromObject);
					PM25Datapoint.add(data.getPM2_5());
					
					List<Object> SO2Datapoint = new ArrayList<Object>();					
					SO2Datapoint.add(currentUTCTimeFromObject);
					SO2Datapoint.add(data.getSO2());
					
					
					datapoints.add(assetDatapoint);
					datapoints.add(CO2Datapoint);
					datapoints.add(nameDatapoint);
					datapoints.add(NH3Datapoint);
					datapoints.add(NO2Datapoint);
					datapoints.add(O3Datapoint);
					datapoints.add(PBDatapoint);
					datapoints.add(PM10Datapoint);
					datapoints.add(PM25Datapoint);					
					datapoints.add(SO2Datapoint);
					
					body.setDatapoints(datapoints);

					bodies.add(body);
				}
			}
		}

		dpIngestion.setBody(bodies);

		return dpIngestion;
	}

	/**
	 * @param nodeName
	 *            -
	 * @param value
	 *            -
	 * @return -
	 */
	@SuppressWarnings("nls")
	public Double getConvertedValue(String nodeName, Double value) {
		Double convValue = null;
		switch (nodeName.toLowerCase()) {
		case Constants.COMPRESSION_RATIO:
			convValue = value * 9.0 / 65535.0 + 1;
			break;
		case Constants.DISCHG_PRESSURE:
			convValue = value * 100.0 / 65535.0;
			break;
		case Constants.SUCT_PRESSURE:
			convValue = value * 100.0 / 65535.0;
			break;
		case Constants.MAX_PRESSURE:
			convValue = value * 100.0 / 65535.0;
			break;
		case Constants.MIN_PRESSURE:
			convValue = value * 100.0 / 65535.0;
			break;
		case Constants.VELOCITY:
			convValue = value * 0.5 / 65535.0;
			break;
		case Constants.TEMPERATURE:
			convValue = value * 200.0 / 65535.0;
			break;
		default:
			throw new UnsupportedOperationException("nameName=" + nodeName + " not found");
		}
		return convValue;
	}

	private long converLocalTimeToUtcTime(long timeSinceLocalEpoch) {
		return timeSinceLocalEpoch + getLocalToUtcDelta();
	}

	private long getLocalToUtcDelta() {
		Calendar local = Calendar.getInstance();
		local.clear();
		local.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
		return local.getTimeInMillis();
	}

	@SuppressWarnings("nls")
	private AssetTag getAssetTag(LinkedHashMap<String, AssetTag> tags, String nodeName) {
		AssetTag ret = null;
		if (tags != null) {
			for (Entry<String, AssetTag> entry : tags.entrySet()) {
				AssetTag assetTag = entry.getValue();
				// TagDatasource dataSource = assetTag.getTagDatasource();
				if (assetTag != null && !assetTag.getSourceTagId().isEmpty() && nodeName != null
						&& nodeName.toLowerCase().contains(assetTag.getSourceTagId().toLowerCase())) {
					ret = assetTag;
					return ret;
				}
			}
		} else {
			log.warn("2. asset has no assetTags with matching nodeName" + nodeName);
		}
		return ret;
	}
}
