package com.fool.log4j.Layout;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import com.fool.log4j.layout.MessageParsingMDCLayout;

import junit.framework.TestCase;

public class MessageParsingMDCLayoutTests extends TestCase {
	private MessageParsingMDCLayout layout;
	private Map<String, Object> capturedProperties;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		layout = new MessageParsingMDCLayout();
		layout.setLayout(new LayoutStub());
	}
	
	public void testAddsProperties() throws Exception {
		layout.setRegex("\\[([^\\]]+)\\] path=([\\S]+)");
		layout.setFieldNames("log_id, path");
		layout.activateOptions();
		String message = "[ID1] path=/foo/bar";

		layout.format(new LoggingEvent("fqn", null, System.currentTimeMillis(), Level.INFO, message, "thread", null, "ndc", null, null));
		
		assertNotNull("capturedProperties", capturedProperties);
		assertEquals("log_id", "ID1", capturedProperties.get("log_id"));
		assertEquals("path", "/foo/bar", capturedProperties.get("path"));
	}
	
	public void testOptionalCaptureGroupWithoutProperty() throws Exception {
		layout.setRegex("(?:hits=([\\d]+) )?status=([\\d]+)");
		layout.setFieldNames("hits, status");
		layout.activateOptions();
		String message = "status=0";

		layout.format(new LoggingEvent("fqn", null, System.currentTimeMillis(), Level.INFO, message, "thread", null, "ndc", null, null));
		
		assertNotNull("capturedProperties", capturedProperties);
		assertFalse("hits is not present", capturedProperties.containsKey("hits"));
		assertEquals("status", 0L, capturedProperties.get("status"));
	}
	
	public void testOptionalCaptureGroupWithProperty() throws Exception {
		layout.setRegex("(?:hits=([\\d]+) )?status=([\\d]+)");
		layout.setFieldNames("hits, status");
		layout.activateOptions();
		String message = "hits=987 status=0";

		layout.format(new LoggingEvent("fqn", null, System.currentTimeMillis(), Level.INFO, message, "thread", null, "ndc", null, null));
		
		assertNotNull("capturedProperties", capturedProperties);
		assertEquals("hits", 987L, capturedProperties.get("hits"));
		assertEquals("status", 0L, capturedProperties.get("status"));
	}

	public void testRemovesProperties() throws Exception {
		layout.setRegex("\\[([^\\]]+)\\] path=([^s]+)");
		layout.setFieldNames("log_id, path");
		layout.activateOptions();
		String message = "[ID1] path=/foo/bar";
		LoggingEvent event = new LoggingEvent("fqn", null, System.currentTimeMillis(), Level.INFO, message, "thread", null, "ndc", null, null);

		layout.format(event);
		
		assertFalse("Should remove property upon completion", event.getProperties().containsKey("log_id"));
	}

	public void testNotEnoughFieldNames() throws Exception {
		layout.setRegex("\\[([^\\]]+)\\] path=([^s]+)");
		layout.setFieldNames("log_id");
		layout.activateOptions();
		String message = "[ID1] path=/foo/bar";
		LoggingEvent event = new LoggingEvent("fqn", null, System.currentTimeMillis(), Level.INFO, message, "thread", null, "ndc", null, null);

		layout.format(event);
		
		assertFalse("Should remove property upon completion", event.getProperties().containsKey("log_id"));
	}
	
	public void testCoerceToLong() throws Exception {
		layout.setRegex("^.* hits=([\\d]+) .*$");
		layout.setFieldNames("hits");
		layout.activateOptions();
		String message = "[ID1] hits=42342342353432454 path=/foo/bar";

		layout.format(new LoggingEvent("fqn", null, System.currentTimeMillis(), Level.INFO, message, "thread", null, "ndc", null, null));
		
		assertNotNull("capturedProperties", capturedProperties);
		assertEquals("hits", 42342342353432454L, capturedProperties.get("hits"));
	}
	
	public void testCoerceToDouble() throws Exception {
		layout.setRegex("^.* time=([\\d.]+) .*$");
		layout.setFieldNames("time");
		layout.activateOptions();
		String message = "[ID1] time=42.454 path=/foo/bar";

		layout.format(new LoggingEvent("fqn", null, System.currentTimeMillis(), Level.INFO, message, "thread", null, "ndc", null, null));
		
		assertNotNull("capturedProperties", capturedProperties);
		assertEquals("time", 42.454D, capturedProperties.get("time"));
	}
	
	class LayoutStub extends Layout {
		@SuppressWarnings("unchecked")
		@Override
		public String format(LoggingEvent event) {
			capturedProperties = new HashMap<String, Object>(event.getProperties());
			return "a message";
		}
		
		@Override
		public void activateOptions() {
		}
		
		@Override
		public boolean ignoresThrowable() {
			return false;
		}
	}
}
