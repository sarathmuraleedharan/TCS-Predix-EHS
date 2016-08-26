package com.ge.predix.solsvc.experience.datasource.handlers.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.DatapointsResponse;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.Results;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.Tag;
import com.ge.predix.entity.util.map.Map;

@Component
public class TimeSeriesAqiParser {
	

	private Float getValue(Map attributes, String key) {
		Float value = null;
		try {
			value = Float.parseFloat(((ArrayList<String>) attributes.get(key)).get(0));
		} catch (Exception e) {
		}
		return value;
	}
	private Long getValue(List<Object> values) {
		Long value = null;
		try {
			value = ((List<Long>) values.get(0)).get(0);
		} catch (Exception e) {
		}
		return value;
	}

	public List<ResponseObject> parse(DatapointsResponse datapointsResponse) {
		List<ResponseObject> list = new ArrayList<ResponseObject>();
		try {
			Tag tag = datapointsResponse.getTags().get(0);
			List<Results> results = tag.getResults();
			for (int i = 0; i < results.size(); i++) {
				
				ResponseObject responseObject = new ResponseObject();

				Map attributes = results.get(i).getAttributes();
				List<Object> values = results.get(i).getValues();

				responseObject.setNO2(getValue(attributes, "NO2"));
				responseObject.setPM2_5(getValue(attributes, "PM2_5"));
				responseObject.setPB(getValue(attributes, "PB"));
				responseObject.setO3(getValue(attributes, "O3"));
				responseObject.setCO2(getValue(attributes, "CO2"));
				responseObject.setSO2(getValue(attributes, "SO2"));
				responseObject.setNH3(getValue(attributes, "NH3"));
				responseObject.setPM10(getValue(attributes, "PM10"));

				responseObject.setTimestamp(getValue(values));
				list.add(responseObject);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Collections.sort(list, new Comparator<ResponseObject>() {
			public int compare(ResponseObject r1, ResponseObject r2) {
				return (int) (r1.getTimestamp() - r2.getTimestamp());
			}
		});
		return list;

	}
	
	
	@JsonInclude(Include.NON_NULL)
	public class ResponseObject {
		private Long timestamp;
		private Float NO2;
		private Float PM2_5;
		private Float PB;
		private Float O3;
		private Float CO2;
		private Float SO2;
		private Float NH3;
		private Float PM10;

		public Long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Long timestamp) {
			this.timestamp = timestamp;
		}

		public Float getNO2() {
			return NO2;
		}

		public void setNO2(Float nO2) {
			NO2 = nO2;
		}

		public Float getPM2_5() {
			return PM2_5;
		}

		public void setPM2_5(Float pM2_5) {
			PM2_5 = pM2_5;
		}

		public Float getSO2() {
			return SO2;
		}

		public void setSO2(Float sO2) {
			SO2 = sO2;
		}

		public Float getPB() {
			return PB;
		}

		public void setPB(Float pB) {
			PB = pB;
		}

		public Float getO3() {
			return O3;
		}

		public void setO3(Float o3) {
			O3 = o3;
		}

		public Float getCO2() {
			return CO2;
		}

		public void setCO2(Float cO2) {
			CO2 = cO2;
		}

		public Float getNH3() {
			return NH3;
		}

		public void setNH3(Float nH3) {
			NH3 = nH3;
		}

		public Float getPM10() {
			return PM10;
		}

		public void setPM10(Float pM10) {
			PM10 = pM10;
		}

	}
}
