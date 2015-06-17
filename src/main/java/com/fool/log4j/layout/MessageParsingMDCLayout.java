package com.fool.log4j.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

public class MessageParsingMDCLayout extends Layout {
	private Layout layout;
	private String regex;
	private String fieldNames;
	
	private List<String> fieldList = new ArrayList<String>();
	private Pattern pattern;
	
	@Override
	public void activateOptions() {
		if (layout == null) {
			throw new RuntimeException(this.getClass().getSimpleName() + " requires layout to delegate to.");
		}
		if (regex == null) {
			throw new RuntimeException(this.getClass().getSimpleName() + " requires regex.");
		}
		if (fieldNames == null) {
			throw new RuntimeException(this.getClass().getSimpleName() + " requires fieldNames.");
		}
		
		this.pattern = Pattern.compile(regex);
		for (String s : fieldNames.split(",")) {
			fieldList.add(s.trim());
		}
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}
	
	@Override
	public String format(LoggingEvent event) {
		String message = event.getRenderedMessage();
		
		Matcher matcher = pattern.matcher(message);
		if (!matcher.matches()) {
			return layout.format(event);
		}
		
		if (matcher.groupCount() > fieldList.size()) {
			LogLog.warn("Captured " + (matcher.groupCount()) + " values but only have " + fieldList.size() + " fields.");
			return layout.format(event);
		}
		
		Map<String, Object> properties = new HashMap<String, Object>(event.getProperties());
		
		for(int i=1; i<=matcher.groupCount(); i++) {
			Object value = coerce(matcher.group(i));
			if (value == null) continue;
			properties.put(fieldList.get(i-1), value);
		}

		event = new LoggingEvent(
				event.fqnOfCategoryClass,
				event.getLogger(),
				event.timeStamp,
				event.getLevel(), 
				event.getMessage(),
				event.getThreadName(),
				event.getThrowableInformation(),
				event.getNDC(),
				null,
				properties);
		
		return layout.format(event);	
	}

	public Object coerce(String value) {
		if (value == null || value.equals("")) {
			return null;
		}
		
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ex) {
		}
		
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException ex) {
		}
		
		return value;
	}

	@Override
	public boolean ignoresThrowable() {
		return layout.ignoresThrowable();
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public void setFieldNames(String fieldNames) {
		this.fieldNames = fieldNames;
	}

}

